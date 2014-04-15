package org.mmg.pagerank.matrix;

import java.util.HashSet;

/**
 * Class that contains all the necessary information of a URL needed to 
 * calculate the pagerank values
 * 
 * @author Manuel Medina González
 *
 */
public class PRMatrixEntry {

	// Reference to the url this instance holds information for/
	private String url;
	// Outgoing links
	private HashSet<LinkInfo> linksOut;
	// Incoming links
	private HashSet<String> linksIn;
	
	// Data used in Tarjan algorithm
	private int index;
	private int lowindex;
	private boolean visited;
	
	// The pagerank value
	private double pageRank;
	
	// Is this a dangling node?
	private boolean dangling;
	
	
	public PRMatrixEntry(String url) {
		linksOut = new HashSet<LinkInfo>();
		linksIn = new HashSet<String>();
		index = lowindex = -1;
		pageRank = 0.0;
		visited = false;
		this.url = url;
	}
	
	
	public HashSet<LinkInfo> getLinksOut() {
		return linksOut;
	}
	
	public void setLinksOut(HashSet<LinkInfo> linksOut) {
		this.linksOut = linksOut;
	}
	
	public void addLinkOut(String link) {
		linksOut.add(new LinkInfo(link, 0.0));
	}
	
	public void addLinkOut(LinkInfo li) {
		linksOut.add(li);
	}
		
	public HashSet<String> getLinksIn() {
		return linksIn;
	}
	public void setnLinksIn(HashSet<String> linksIn) {
		this.linksIn = linksIn;
	}
	
	public void addLinkIn(String link) {
		linksIn.add(link);
	}
	
	public int getNumberOfIncomingLinks() {
		return linksIn.size();
	}
	
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public int getLowindex() {
		return lowindex;
	}
	public void setLowindex(int lowindex) {
		this.lowindex = lowindex;
	}
	public double getPageRank() {
		return pageRank;
	}
	public void setPageRank(double pageRank) {
		this.pageRank = pageRank;
	}
	
	public boolean isDangling() {
		return dangling;
	}
	
	public void setDangling(boolean dangling) {
		this.dangling = dangling;
	}
	
	public boolean hasBeenVisited() {
		return visited;
	}
	
	public void setVisited(boolean visited) {
		this.visited = visited;
	}
	
	public void setURL(String url) {
		this.url = url;
	}
	
	public String getURL() {
		return url;
	}
		
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("(" + url + ")");
		sb.append("Out: [");
		
		int counter = 0;
		
		// I miss Scala mkstring
		for (LinkInfo li : linksOut) {
			sb.append(li.getUrl());
			if (counter + 1 != linksOut.size())
				sb.append(",");
			
			counter++;
		}
		
		sb.append("], In: [");
		
		counter = 0;
		for (String s: linksIn) {
			sb.append(s);
			if (counter + 1 != linksIn.size())
				sb.append(",");
			
			counter++;
		}
		
		sb.append("]");
		
		sb.append(" PageRank: " + pageRank);		
		
		if (dangling) 
			sb.append(" → Rank Leak");
		
		/*if (index != -1) 
			sb.append(" Index: " + index);				
		
		
		if (lowindex != -1) 
			sb.append(" Low index: " + lowindex);
		
		if (visited)
			sb.append(" Visited");*/
		
		
		return sb.toString();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PRMatrixEntry other = (PRMatrixEntry) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	
	
}
