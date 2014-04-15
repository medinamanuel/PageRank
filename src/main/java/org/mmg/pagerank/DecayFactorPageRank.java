package org.mmg.pagerank;

import java.util.HashSet;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.mmg.pagerank.matrix.PRMatrix;
import org.mmg.pagerank.matrix.PRMatrixEntry;
import org.mmg.pagerank.matrix.PRMatrixPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rits.cloning.Cloner;

/**
 * Implements the Random Surfer pagerank calculation: Uses a decay factor to
 * avoid rank sinks and rank leaks
 * 
 * @author Manuel Medina González
 *
 */
public class DecayFactorPageRank implements PageRankCalculator {

	// To define a thread pool
	private ExecutorService executor;
	private static final int NTHREADS = Runtime.getRuntime().availableProcessors();
	private int maxIterations;
	private double damping_factor;
	// Error rate
	private double epsilon;
	private Logger logger = LoggerFactory.getLogger(DecayFactorPageRank.class);
	
	public DecayFactorPageRank(int maxIterations, double damping_factor, double epsilon) {
		executor = Executors.newFixedThreadPool(NTHREADS); 
		
		this.maxIterations = maxIterations;
		this.damping_factor = damping_factor;
		this.epsilon = epsilon;
	}
	
	
	public void calculatePageRank(PRMatrix prMatrix, PRSettings settings) {
		ErrorComputer ec = new ErrorComputer(prMatrix);
		
		// To control threads calculating pageranks
		CountDownLatch cdLatch = null;
		
		logger.trace("Number of processors: " + NTHREADS);
		
		int cntIterations = 0;
		double currentError = 1.0;
		
		// Main loop
		while (cntIterations < maxIterations && currentError > epsilon) {
			// in case there are less links than processors
			cdLatch = new CountDownLatch(Math.min(NTHREADS, prMatrix.getNumberOfLinks()));
			ec.setPreviousMatrix(prMatrix);
											
			for (String url: prMatrix.getAllURLs()) {
				executor.execute(new TemporaryPageRankCalculator(url, prMatrix, settings, damping_factor, cdLatch));
			}
						
			
			try {
				cdLatch.await();
			} catch (InterruptedException e) {
				logger.error("Error: " + e.getMessage());
			}
			
			Future<Double> fError = executor.submit(ec);
			
			try {
				currentError = fError.get();
			} catch (InterruptedException | ExecutionException e) {
				logger.error("Error: " + e.getMessage());
			}
			
			
			logger.info(" ========= Iteration " + (cntIterations + 1) + " ========= ");
			prMatrix.printPageRanks();
			logger.info("Current error rate: " + currentError + System.getProperty("line.separator"));
			cntIterations += 1;
		}
		
		executor.shutdown();
		
		logger.info(" ===== Execution finished. Total number of iterations performed: " + cntIterations + " ====");
		
	}
}

/**
 * Calculates the current pagerank of a URL
 * @author Manuel Medina González
 *
 */
class TemporaryPageRankCalculator implements Runnable {
	private PRMatrix prMatrix;
	private PRSettings settings;
	private String url;
	private CountDownLatch cdLatch;
	private double damping_factor;
	
	private Logger logger = LoggerFactory.getLogger("rootLogger");
	
	public TemporaryPageRankCalculator(String url, 
			                               PRMatrix prMatrix,
			                               PRSettings settings,
			                               double damping_factor, 
			                               CountDownLatch cdLatch) {
		this.url = url;
		this.prMatrix = prMatrix;
		this.damping_factor = damping_factor;		
		this.cdLatch = cdLatch;
		this.settings = settings;
	}
	
	public void run() {
		PRMatrixEntry currentEntry = prMatrix.getEntry(url);
		HashSet<String> linksIn = currentEntry.getLinksIn();
		double tempCalc = 0.0;
		int m = prMatrix.getNumberOfLinks();
		
		logger.trace("== Thread working with " + url);
		
		// Sequential for the time being
		for (String incomingLink: linksIn) {
			PRMatrixEntry incomingEntry = prMatrix.getEntry(incomingLink);
			
			tempCalc += incomingEntry.getPageRank() / incomingEntry.getLinksOut().size();			
		}
		
		// Handling dangling nodes = KEEP
		if (settings.getdNodesPolicy() == PRMatrixPolicy.KEEP) {
			for (String incomingLink: prMatrix.getDanglingNodes()) {
				logger.trace("Now adding dangling node " + incomingLink);
				// Self links
				if ((incomingLink.equals(url) && settings.getSelfLinksPolicy() == PRMatrixPolicy.KEEP) ||
					 !incomingLink.equals(url)) {	
						
					PRMatrixEntry incomingEntry = prMatrix.getEntry(incomingLink);
					
					tempCalc += incomingEntry.getPageRank() / incomingEntry.getLinksOut().size();
				}
				else 
					logger.trace("Skipping " + incomingLink + " as it's the same as " + url);
			}
		}
		
		
		// Here it is. The magic formula.
		double currentPageRank = damping_factor * tempCalc + (1 - damping_factor) / m;
		currentEntry.setPageRank(currentPageRank);
		
		logger.trace("== Finished working with " + url + " . Waiting...");
		
		
		cdLatch.countDown();
	}
}


/**
 * Calculates the error rate by performing the L1 norm between the previous values
 * in the matrix and the current ones (after a calculation).
 * 
 * @author Manuel Medina González
 *
 */

class ErrorComputer implements Callable<Double> {
		
	private PRMatrix thePreviousMatrix;
	private PRMatrix currentMatrix;
	// To create deep copies of the matrix
	Cloner cloner = new Cloner();
	
	public ErrorComputer(PRMatrix currentMatrix) {
		this.currentMatrix = currentMatrix;
	}
	
	private double L1Norm() {
		double error = 0.0;
		// Current and previous have the same number of elementsvalue
		for (Entry<String, PRMatrixEntry> entry : currentMatrix.getURLEntrySet()) {
			error += Math.abs(entry.getValue().getPageRank() - thePreviousMatrix.getEntry(entry.getKey()).getPageRank());
		}
		
		return error;
		
	}	
	
	public void setPreviousMatrix(PRMatrix previousMatrix) {
		thePreviousMatrix = cloner.deepClone(previousMatrix);
	}
	
	
	public Double call() {
		return L1Norm();
	}
	
	
}
