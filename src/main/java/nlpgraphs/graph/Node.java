package nlpgraphs.graph;

import java.util.ArrayList;
import java.util.List;

public class Node {
	
	private String id;
	private List<Edge> edges;
	private List<String> attributes;

	public Node(String id, List<String> attributes) {
		this.id = id;
		this.edges = new ArrayList<Edge>();
		this.attributes = attributes;
	}
	
	public String getId() {
		return id;
	}
	
	public List<Edge> getEdges() {
		return edges;
	}
	
	public void addEdge(Edge edge) {
		this.edges.add(edge);
	}
	
	public List<String> getAttributes() {
		return attributes;
	}
	
	@Override
	public String toString() {
		return this.id;
	}
	
	public void addAttribute(String attr) {
		attributes.add(attr);
	}
	
	@Override
	public  boolean equals(Object obj) {
		if(getClass() == obj.getClass()) {
			Node other = (Node) obj;
			return attributes.equals(other.attributes);
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return attributes.hashCode();
	}
}
