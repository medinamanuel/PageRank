package org.mmg.pagerank.matrix;

import java.util.ArrayList;
import java.util.Collection ;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a series of URL and their information needed to
 * calculate pagerank values.
 * 
 * This class implements Clonable just to be able to deep copy it
 * when checking error rate.
 * 
 * The only way to create an instance of this class is by using the
 * PRMatrixFactory.
 * 
 * @author Manuel Medina González
 * @see PRMatrixFactory
 *
 */
public class PRMatrix implements Cloneable {

	private ConcurrentHashMap<String, PRMatrixEntry> theMap;
	private List<HashSet<String>> sinks;
	private HashSet<String> danglingNodes;
	private Logger logger = LoggerFactory.getLogger(PRMatrix.class);
	
	// Protected constructor to prevent unsafe creation
	protected PRMatrix() {
		theMap = new ConcurrentHashMap<String,PRMatrixEntry>(500,0.75f, Runtime.getRuntime().availableProcessors());
		sinks = new ArrayList<HashSet<String>>();
		danglingNodes = new HashSet<String>();
	}
	
	
	public int getNumberOfLinks() {
		return theMap.size();
	}
	
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		for (Entry<String, PRMatrixEntry> entry : theMap.entrySet()) {
			sb.append(entry.getKey() + " => " + entry.getValue().toString() + System.getProperty("line.separator"));
		}
		
		return sb.toString();
	}
	
	public void printToScreen() {
		System.out.println(toString());
	}
	
	public List<HashSet<String>> getSinks() {
		return sinks;
	}
	
	public void setSinks(List<HashSet<String>> scc) {
		this.sinks = scc;
	}
	
	public boolean hasRankSinks() {
		return !sinks.isEmpty();
	}
	
	public void printRankSinks() {
		if (hasRankSinks()) {
			logger.info("== List of rank sinks ==");
			for (HashSet<String> b : sinks) {
				logger.info(b.toString());												
			}
		}
		else {
			logger.info("No sinks were found in the file");
		}
	}
	
	public void printPageRanks() {
		logger.info(" ==> Current pageranks: ");
		for (Entry<String, PRMatrixEntry>entry : theMap.entrySet()) {
			logger.info(entry.getKey() + " => " + entry.getValue().getPageRank());
		}
	}
	
	protected void addDanglingNode(String url) {
		danglingNodes.add(url);
	}
	
	protected void removeDanglingNode(String url) {
		danglingNodes.remove(url);
	}
	
	public HashSet<String> getDanglingNodes() {
		return danglingNodes;
	}
	
	public boolean hasDanglingNodes() {
		return !danglingNodes.isEmpty();
	}
	
	public void printDanglingNodes() {
		if (hasDanglingNodes()) {
			logger.info("== List of rank leaks ==");
			for (String url : danglingNodes) {
				logger.info("- " + url);
			}
		}
		else {
			logger.info("No rank leaks were found in the file");
		}
	}
	
	// Adding code to avoid exposing theMap outside
	
	public PRMatrixEntry getEntry(String url) {
		return theMap.get(url);
	}
	
	public void setEntry(String url, PRMatrixEntry entry) {
		theMap.put(url, entry);
	}
	
	public Set<String> getAllURLs() {
		return theMap.keySet();
	}
	
	public Collection<PRMatrixEntry> getAllEntries() {
		return theMap.values();
	}
	
	public boolean containsURL(String url) {
		return theMap.containsKey(url);
	}
	
	public Set<Entry<String,PRMatrixEntry>> getURLEntrySet() {
		return theMap.entrySet();
	}
	
	
}
