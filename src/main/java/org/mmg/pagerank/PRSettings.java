package org.mmg.pagerank;

import org.mmg.pagerank.matrix.*;

/**
 * Class that contains the settings for creating the PRMatrix
 * and calculating the Pagerank.
 * 
 * @author Manuel Medina González
 *
 */
public class PRSettings {

	private PRMatrixPolicy dNodesPolicy;
	private PRMatrixPolicy selfLinksPolicy;
	private double epsilon;
	
	public PRMatrixPolicy getdNodesPolicy() {
		return dNodesPolicy;
	}
	public void setdNodesPolicy(PRMatrixPolicy dNodesPolicy) {
		this.dNodesPolicy = dNodesPolicy;
	}
	public PRMatrixPolicy getSelfLinksPolicy() {
		return selfLinksPolicy;
	}
	public void setSelfLinksPolicy(PRMatrixPolicy selfLinksPolicy) {
		this.selfLinksPolicy = selfLinksPolicy;
	}
	public double getEpsilon() {
		return epsilon;
	}
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	
	
	
}
