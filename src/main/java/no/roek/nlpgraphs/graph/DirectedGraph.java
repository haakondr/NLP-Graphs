package no.roek.nlpgraphs.graph;

public class DirectedGraph extends Graph{

	public DirectedGraph(String filename) {
		super(filename);
	}
	
	public DirectedGraph(String filename, int sentenceNumber, int offset, int length) {
		super(filename, sentenceNumber, offset, length);
	}
	
	
	public void addEdge(DirectedEdge edge) {
		edges.get(edge.getFrom().getId()).add(edge);
	}
}
