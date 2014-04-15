package org.mmg.pagerank.matrix;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;

import org.mmg.pagerank.PRSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

/**
 * Factory that creates instances of PRMatrix from a file containing links.
 * <br>
 * 
 * @author Manuel Medina González
 * @see PRMatrix
 *
 */
public class PRMatrixFactory {

	private static Logger logger = LoggerFactory.getLogger(PRMatrixFactory.class);
	
	/**
	 * Creates an instance of PRMatrix representing the links contained in the given file.
	 * It also follows the policies specified in the settings.
	 * 
	 * @param filename The name of the file that contains the links information
	 * @param settings Settings used when creating the PRMatrix.
	 * @return A new instance of PRMatrix
	 * @throws MalformedEntryException In case one line in the file is not in the correct format (String or 2 urls separated by a space)
	 * @throws IOException If the file does not exist of if there were problems while reading it.
	 * 
	 * @see PRSettings
	 */
	public static PRMatrix buildPRMatrix(String filename, PRSettings settings) throws MalformedEntryException, IOException {
		PRMatrix prMatrix = new PRMatrix();
					
		
		try(BufferedReader br = new BufferedReader(new FileReader(filename))) {
			String line = null;
			int counter = 1;
			while ( (line = br.readLine()) != null) {
				try {
					insertIntoMatrix(parseLine(line),prMatrix,settings.getSelfLinksPolicy());
				} catch (MalformedEntryException e) {
					logger.error("==> Line " + counter + " does not have the correct format. Please check: " + line);
				} finally {
					counter++;
				}
			}
			
			
			// Find sinks
			findSinks(prMatrix);
			
			// Once all links have been inserted, set the initial pagerank for each of them
			setInitialPageRank(prMatrix, settings);
		} catch (IOException e) {
			logger.error("Something went wrong while reading the file " + filename + " Aborting");
			System.exit(1);
		}
		
		return prMatrix;
	}
	
	/*
	 * Parses the given line and checks whether it's well formed (one line, two links)
	 */
	private static Pair<String,String> parseLine(String line) throws MalformedEntryException {
		String[] entries = line.replaceAll(" +", " ").split(" ");
		
		if (entries.length == 2)		
			return new Pair<String,String>(entries[0],entries[1]);
		else
			throw new MalformedEntryException("An entry in the file should have only 2 URLS.");
	}
	
	
	/*
	 * Given a pair of URLs (origin and target), create a matrix entry and insert it into the main matrix
	 */
	private static void insertIntoMatrix(Pair<String,String> entry,PRMatrix prMatrix, PRMatrixPolicy selfLinksPolicy) {
		PRMatrixEntry prMatrixEntry = null;
		String currentURL = entry.getFirst();
		String outLinkURL = entry.getSecond();
		
		
		if (!prMatrix.containsURL(currentURL)) {
			prMatrixEntry = new PRMatrixEntry(currentURL);
			
		}
		else {
			// URL already in the map. Add the second entry as outgoing link
			prMatrixEntry = prMatrix.getEntry(currentURL);
			
		}
		
		// We're sure that currentURL is not a dangling node
		/*prMatrixEntry.setDangling(false);
		prMatrix.removeDanglingNode(currentURL);*/
		
				
		/* New entry if currentURL is not in the map,
		 * entry updated with the outLinkURL otherwise; 
		 * In case of self links, follow the policy
		 */
		
		if ((currentURL.equals(outLinkURL) && selfLinksPolicy == PRMatrixPolicy.KEEP) || 
			!currentURL.equals(outLinkURL)) {
			
			prMatrixEntry.setDangling(false);
			prMatrix.removeDanglingNode(currentURL);
			
				// Add target as out link of the current one
				prMatrixEntry.addLinkOut(outLinkURL);
				
				//theMap.put(currentURL, prMatrixEntry);
				prMatrix.setEntry(currentURL,prMatrixEntry);
		
				PRMatrixEntry outURLEntry = null;
				// Add the outLinkURL in case is not already in the map
				if (!prMatrix.containsURL(outLinkURL)) { 
					outURLEntry = new PRMatrixEntry(outLinkURL);
					/* Here we stil are not sure  if outLinkURL is a dangling node
					 * Set to true. When outURLEntry comes as currentURL the value
					 * will be set as false.
					 */
					outURLEntry.setDangling(true);
					prMatrix.addDanglingNode(outLinkURL);
				}				
				else {					
					outURLEntry = prMatrix.getEntry(outLinkURL);										
				}
				
				//Add currentURL as linkIn of outLinkURL
				outURLEntry.addLinkIn(currentURL);

				prMatrix.setEntry(outLinkURL,outURLEntry);
		}
		
		
	}
	
	/*
	 *  O(n)
	 *  
	 *  Adds a link to each of all other pages to the outgoing links of the 
	 *  dangling nodes.
	 */
	private static void insertDanglingNodes(PRMatrix prMatrix, PRMatrixPolicy selfLinksPolicy) {
		
		HashSet<LinkInfo> theLinks = new HashSet<LinkInfo>();
		Cloner cloner = new Cloner();
		
		for (String url : prMatrix.getAllURLs()) {				
			theLinks.add(new LinkInfo(url));
		}

		
		for (Entry<String,PRMatrixEntry> entry : prMatrix.getURLEntrySet()) {
			PRMatrixEntry prMatrixEntry = entry.getValue();
			
			if (prMatrixEntry.isDangling()) {
				
				HashSet<LinkInfo> linksToAdd = 	cloner.deepClone(theLinks);
			
				// Delete own link from the links
				if (selfLinksPolicy == PRMatrixPolicy.IGNORE) {
					linksToAdd.remove(new LinkInfo(entry.getKey()));
				}
				
				prMatrixEntry.setLinksOut(linksToAdd);				
				entry.setValue(prMatrixEntry);
				
				// Restore own link to the links
				/*if (selfLinksPolicy == PRMatrixPolicy.IGNORE) {
					theLinks.add(new LinkInfo(entry.getKey()));
				}*/
				
				System.out.println("");
			}
		}
	}
	
	/* O(n)
	 * 
	 * Initial pagerank set to 1/N 
	 * Where N = number of links
	 */
	private static void setInitialPageRank(PRMatrix prMatrix, PRSettings settings) {
		
		/*
		 * Adding dangling nodes means that we will assume that they have a link to 
		 * all other pages. Create those entries and add them properly.
		 */
		if (settings.getdNodesPolicy() == PRMatrixPolicy.KEEP) {
			insertDanglingNodes(prMatrix, settings.getSelfLinksPolicy());
		}

		
		for (Entry<String, PRMatrixEntry> entry : prMatrix.getURLEntrySet()) {
			PRMatrixEntry prMatrixEntry = entry.getValue();
			// Be sure to allows this to be variable
			prMatrixEntry.setPageRank((double)1/prMatrix.getNumberOfLinks());
			entry.setValue(prMatrixEntry);
		}
	}
	
	/*
	 * Using Tarjan algorithm find strong connected components.
	 * If inside a SCC there's a link that is not part of the SCC
	 * then we have a sink.
	 */
	private static void findSinks(PRMatrix prMatrix) {
		Tarjan tarjan = new Tarjan(prMatrix);
		List<HashSet<String>> components = tarjan.getSCComponents();
		List<HashSet<String>> sinks = new ArrayList<HashSet<String>>();
		for (HashSet<String> b : components) {
			
			boolean sink = true;
			
			
			// A rank sink is a scc with no links outside it
			for (String url: b) {
				
				PRMatrixEntry entry = prMatrix.getEntry(url);
				
				HashSet<LinkInfo> linksOut = entry.getLinksOut();
				
				for (LinkInfo li : linksOut) {
					// If there's an URL that is not in the Set, this is not a sink
					if (!b.contains(li.getUrl())) {
						sink = false;
						break;
					}
				
				}
				
				if (sink) {
					sinks.add(b);
					break;
				}
				
			}
			
							
		}
		
		prMatrix.setSinks(sinks);
	}
	
}
