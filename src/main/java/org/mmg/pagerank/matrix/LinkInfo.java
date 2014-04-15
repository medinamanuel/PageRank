package org.mmg.pagerank.matrix;

/**
 * Holds a url and the transition probability from a certain URL to it.
 * <br>
 * Currently, the transition probability is not used.
 * 
 * @author Manuel Medina González
 *
 */
public class LinkInfo implements Cloneable {

	private String url;
	private double transProb;
	
	public LinkInfo(String url, double transProb) {
		this.url = url;
		this.transProb = transProb;
	}
	
	public LinkInfo(String url) {
		this(url, 0.0);
	}
	
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public double getTransProb() {
		return transProb;
	}
	public void setTransProb(double transProb) {
		this.transProb = transProb;
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
		LinkInfo other = (LinkInfo) obj;
		if (url == null) {
			if (other.url != null)
				return false;
		} else if (!url.equals(other.url))
			return false;
		return true;
	}
	
	@Override
	public String toString() {
		return "[" + url + " -> " + transProb + "]";
	}
	
	
}
