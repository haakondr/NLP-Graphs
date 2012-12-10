package no.roek.nlpgraphs.graph;

public class DirectedGraph2 extends Graph{

	public DirectedGraph2(String filename) {
		super(filename);
	}
	
	public DirectedGraph2(String filename, int sentenceNumber, int offset, int length) {
		super(filename, sentenceNumber, offset, length);
	}
	
	
	public void addEdge(DirectedEdge edge) {
		edges.get(edge.getTo().getId()).add(edge);
		edges.get(edge.getFrom().getId()).add(edge);
	}
}
