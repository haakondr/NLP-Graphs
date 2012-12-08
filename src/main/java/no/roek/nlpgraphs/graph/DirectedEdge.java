package no.roek.nlpgraphs.graph;

import java.util.List;

public class DirectedEdge extends Edge {

	public DirectedEdge(String id, Node from, Node to, String label,
			String[] attributes) {
		super(id, from, to, label, attributes);
	}

	public DirectedEdge(String id, Node from, Node to, String label) {
		super(id, from, to, label);
	}
	
	public DirectedEdge(String id, Node from, Node to, String label, List<String> attributes) {
		super(id, from, to, label, attributes);
	}
	
	@Override
	public boolean equals(Object obj) {
		if(getClass() == obj.getClass()) {
			Edge other = (Edge) obj;
			return label.equals(other.getLabel()) && from.equals(other.getFrom());
		}
		return false;
	}
}
