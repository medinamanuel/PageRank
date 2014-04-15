package org.mmg.pagerank.matrix;

/**
 * Exception that raises when a line in the file containing the URLs 
 * is not in the correct format.
 * <br>
 * <br>
 * <b> Correct format: </b>
 * <br>
 * <br>
 * URL URL (2 urls separated by a space)
 * 
 * @author Manuel Medina González
 *
 */
public class MalformedEntryException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public MalformedEntryException(String cause) {
		super(cause);
	}
}
