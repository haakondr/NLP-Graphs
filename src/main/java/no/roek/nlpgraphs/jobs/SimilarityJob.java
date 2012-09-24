package no.roek.nlpgraphs.jobs;

import java.util.List;

import no.roek.nlpgraphs.document.GraphPair;
import no.roek.nlpgraphs.graph.Graph;

public class SimilarityJob  extends Job {

	private List<GraphPair> graphPairs;
	
	public SimilarityJob(String filename, List<GraphPair> graphPairs) {
		super(filename);
		this.graphPairs = graphPairs;
	}

	public List<GraphPair> getGraphPairs() {
		return graphPairs;
	}
	
	public void addGraphPair(GraphPair pair) {
		graphPairs.add(pair);
	}
}
