package no.roek.nlpgraphs.graph;

import java.util.Arrays;
import java.util.List;

public class Node {

	private String id;
//	private List<Edge> edges;
	private List<String> attributes;
	//	private String[] attributes;

	public Node(String id, String index, List<String> attributes) {
		this.id = id;
//		this.edges = new ArrayList<Edge>();
		this.attributes = attributes;
	}

	public Node(String id, String[] attributes) {
		this.id = id;
//		this.edges = new ArrayList<Edge>();
		this.attributes  = Arrays.asList(attributes);
	}

	public String getId() {
		return id;
	}
	
	

//	public List<Edge> getEdges() {
//		return edges;
//	}
//
//	public void addEdge(Edge edge) {
//		this.edges.add(edge);
//	}

	//	public String[] getAttributes() {
	//		return attributes;
	//	}
	//
	//	public void setAttributes(String[] attributes) {
	//		this.attributes = attributes;
	//	}
	public List<String> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return this.id+"-"+attributes.get(0);
	}

	public void addAttribute(String attr) {
		attributes.add(attr);
	}

	@Override
	public  boolean equals(Object obj) {
		if(getClass() == obj.getClass()) {
			Node other = (Node) obj;
			return equalsAttributes(other.attributes);
		}
		return false;
	}
	
	private boolean equalsAttributes(List<String> other) {
		for (int i = 0; i < attributes.size(); i++) {
			if(!attributes.get(i).equals(other.get(i))) {
				return false;
			}
		}
		
		return true;
	}

	@Override
	public int hashCode() {
		return attributes.hashCode();
	}
}
