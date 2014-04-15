package org.mmg.pagerank.matrix;

/**
 * Defines the policies to follow when dealing with concepts like
 * dangling nodes and self links in the PRMatrix.
 * <br>
 * <br>
 * The meaning of each policy depends on the concept it is regulating.
 * 
 * @author Manuel Medina González
 *
 */
public enum PRMatrixPolicy {

	KEEP,
	IGNORE;
}
