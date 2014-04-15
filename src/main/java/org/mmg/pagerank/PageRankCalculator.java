package org.mmg.pagerank;

import org.mmg.pagerank.matrix.PRMatrix;

/**
 * Interface that should be implemented by classes that calculate
 * pagerank values.
 * 
 * @author Manuel Medina González
 *
 */
public interface PageRankCalculator {

	public void calculatePageRank(PRMatrix prMatrix, PRSettings settings);
}
