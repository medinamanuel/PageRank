package org.mmg.pagerank;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.mmg.pagerank.matrix.MalformedEntryException;
import org.mmg.pagerank.matrix.PRMatrix;
import org.mmg.pagerank.matrix.PRMatrixFactory;
import org.mmg.pagerank.matrix.PRMatrixPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that handles command line arguments and makes the necessary calls
 * to perform the pagerank calculation for a series of links
 * 
 * @author Manuel Medina González
 *
 */
public class MMPageRank {

	private static final String DANGLING_NODES_POLICY = "dangling.nodes.policy";
	private static final String SELF_LINKS_POLICY = "self.links.policy";
	private static final String EPSILON = "error.rate";
	private static final String CHECK_MODE = "check";
	private static final String RUN_MODE = "run";
	private static final double DEFAULT_EPSILON = 0.0001;
	
	private static Logger logger = LoggerFactory.getLogger(MMPageRank.class);
	
	/*
	 * Retrieves policies and error rate from a file
	 */
	private static Properties readPropertiesFile(String filename) {
		
		Properties prop = new Properties();
		// In order to get the file from resources directory
		InputStream in = PRMatrixFactory.class.getResourceAsStream("/" + filename);
		
		if (in == null) {
			System.exit(1);
		}
		try {			
			prop.load(in);
		} catch (IOException e) {
			//logger.error(e.getMessage());
			System.exit(1);
		} finally {
			try {
				if (in != null)
					in.close();
			} catch (IOException e) {
				//logger.error(e.getMessage());
			}
		}
		
		return prop;
		
	}
	
	/*
	 * Sets the values for policies and errors
	 */
	private static PRSettings readSettingsFromFile(String filename) {
		Properties prop = readPropertiesFile(filename);
		PRSettings settings = new PRSettings();
		
		if (!prop.containsKey(DANGLING_NODES_POLICY)) {
			settings.setdNodesPolicy(PRMatrixPolicy.IGNORE);
		}
		else {
			String dnp = (String)prop.get(DANGLING_NODES_POLICY);
			switch (dnp) {
			case "keep":
				settings.setdNodesPolicy(PRMatrixPolicy.KEEP);
				break;
			
			default:
				settings.setdNodesPolicy(PRMatrixPolicy.IGNORE);
			}
		}
		
		if (!prop.containsKey(SELF_LINKS_POLICY)) {
			settings.setSelfLinksPolicy(PRMatrixPolicy.IGNORE);
		}
		else {
			String slp = (String)prop.get(SELF_LINKS_POLICY);
			switch (slp) {
			case "keep":
				settings.setSelfLinksPolicy(PRMatrixPolicy.KEEP);
				break;
				
			default:
				settings.setSelfLinksPolicy(PRMatrixPolicy.IGNORE);
			}
		}
		
		if (!prop.containsKey(EPSILON)) {
			settings.setEpsilon(DEFAULT_EPSILON);			
		}
		else {
			settings.setEpsilon(Double.parseDouble((String)prop.get(EPSILON)));
		}
		
		return settings;
	}
	
	
	private static String usage() {
		return "Usage: MMPageRank check <filename> | run <filename> <number of iterations> <decay factor>";
	}
	
	/*
	 * Called in case of abnormal exit.
	 */
	private static void errorExit(String msg) {
		logger.error(msg);
		System.exit(1);
	}
	
	
	public static void main (String a[]) {
		// First check mode
		if (a.length < 2) {
			errorExit(usage());
		}
		else {
			String inputFilename = a[1];
			String runMode = a[0];
			int nIterations = 0;
			double decay_factor = 0.0;
			
			if (!(runMode.equals(CHECK_MODE) || runMode.equals(RUN_MODE))) {
				errorExit("Valid run modes are \"check\" and \"run\" only.");
			}
			
			PRSettings settings = readSettingsFromFile("prMatrix.props");
			try {
				PRMatrix prMatrix = PRMatrixFactory.buildPRMatrix(inputFilename, settings);
				
				logger.trace(prMatrix.toString());
				
				if (runMode.equals(CHECK_MODE)) {
					if (a.length > 2) {
						errorExit("Usage: MMPageRank check <filename>");
					}
					
					logger.info(" -----------  Pagerank running with the following parameters -----------");
					logger.info("Dangling Nodes (Rank leaks): " + settings.getdNodesPolicy());
					logger.info("Self Links: " + settings.getSelfLinksPolicy());
					logger.info("-------------------------------------------------------");
					
					prMatrix.printRankSinks();
					prMatrix.printDanglingNodes();					
				}
				else {
										
					if (a.length < 4) {
						errorExit("Usage MMPageRank run <filename> <number of iterations> <decay factor>");
					}
					
					// Get restant parameters
					try {
						nIterations = Integer.parseInt(a[2]);
						decay_factor = Double.parseDouble(a[3]);
					} catch (NumberFormatException e) {
						errorExit("Error while parsing number parameters: " + e.getMessage());
					}
					
					logger.info(" -----------  Pagerank running with the following settings -----------");
					logger.info("Dangling Nodes (Rank leaks): " + settings.getdNodesPolicy());
					logger.info("Self Links: " + settings.getSelfLinksPolicy());
					logger.info("Max number of iterations: " + nIterations);
					logger.info("Decay (Damping) factor: " + decay_factor);
					logger.info("Error rate: " + settings.getEpsilon());
					logger.info("-------------------------------------------------------");
					
					// Calculate pagerank
					
					PageRankCalculator prCalculator = new DecayFactorPageRank(nIterations,decay_factor,settings.getEpsilon());
					
					prCalculator.calculatePageRank(prMatrix, settings);
				}
				
			} catch (MalformedEntryException | IOException e) {
				e.printStackTrace();
			}
		}
		
		
	}
}
