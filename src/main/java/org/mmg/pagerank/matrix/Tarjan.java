package org.mmg.pagerank.matrix;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;

import org.mmg.pagerank.matrix.PRMatrix;

/**
 * Simple implementation of the Tarjan algorithm to find Strong
 * Connected Components (SCC) in the PRMatrix. Those components
 * are used to find whether the PRMatrix has rank sinks.
 * 
 * <br>
 * <br>
 * 
 * For more information, see the Wikipedia entry:
 * http://en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm
 * 
 * @author Manuel Medina Gonzálex
 *
 */
public class Tarjan {

	private int index = 0;
	private Stack<PRMatrixEntry> theStack = new Stack<PRMatrixEntry>();
	//private Map<String, PRMatrixEntry> theMap;
	private PRMatrix theMatrix;
	
	public Tarjan(PRMatrix theMatrix) {
		this.theMatrix = theMatrix;
	}
	
	public List<HashSet<String>> getSCComponents() {		
		List<HashSet<String>> components = new ArrayList<HashSet<String>>();
		for (PRMatrixEntry entry : theMatrix.getAllEntries()) {
			if (!entry.hasBeenVisited()) {
				components.addAll(strongConnect(entry));
			}
		}
		
		return components;
	}
	
	public List<HashSet<String>> strongConnect(PRMatrixEntry node) {
		List<HashSet<String>> components = new ArrayList<HashSet<String>>();
		HashSet<String> scc = new HashSet<String>();
		
		node.setIndex(index);
		node.setLowindex(index);
		node.setVisited(true);
		index += 1;
		theStack.push(node);
		
		for (LinkInfo li : node.getLinksOut()) {			
			PRMatrixEntry outNode = theMatrix.getEntry(li.getUrl());
			
			if (!outNode.hasBeenVisited()) {
				components.addAll(strongConnect(outNode));
				node.setLowindex(Math.min(node.getLowindex(), outNode.getLowindex()));
			}
			else if (theStack.contains(outNode)) {
				node.setLowindex(Math.min(node.getLowindex(), outNode.getIndex()));
			}
		}
		
		if (node.getLowindex() == node.getIndex()) {			
			PRMatrixEntry inStackEntry = null;
			
			
			do {
				inStackEntry = theStack.pop();
				scc.add(inStackEntry.getURL());
			} while(!inStackEntry.equals(node));
			
			components.add(scc);
		}
		
		return components;
	}
	
}
