package no.roek.nlpgraphs.document;

import no.roek.nlpgraphs.graph.Graph;

public class GraphPair {
	
	public Graph sourceGraph, suspiciousGraph;
	public double similarity;

	public GraphPair(Graph g1, Graph g2) {
		this.sourceGraph = g1;
		this.suspiciousGraph = g2;
	}
	
	public GraphPair(Graph g1, Graph g2, double similarity) {
		this(g1, g2);
		this.similarity = similarity;
	}

	public Graph getSourceGraph() {
		return sourceGraph;
	}

	public Graph getSuspiciousGraph() {
		return suspiciousGraph;
	}

	public double getSimilarity() {
		return similarity;
	}
}
