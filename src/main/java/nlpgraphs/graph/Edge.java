package nlpgraphs.graph;

import java.util.ArrayList;
import java.util.List;

public class Edge {

	private String id;
	private Node from;
	private Node to;
	private List<String> attributes;
	
	public Edge(String id, Node from, Node to, List<String> attributes) {
		this.id = id;
		this.from = from;
		this.to = to;
		this.attributes = attributes;
	}
	
	public Edge(String id, Node from, Node to) {
		this(id, from, to, new ArrayList<String>());
	}
	
	public String getId() {
		return id;
	}

	public Node getFrom() {
		return from;
	}

	public Node getTo() {
		return to;
	}

	public List<String> getAttributes() {
		return attributes;
	}
	
	public void addAttribute(String attr) {
		attributes.add(attr);
	}
	
	@Override
	public String toString() {
		return from.getId()+"-"+to.getId();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(getClass() == obj.getClass()) {
			Edge other = (Edge) obj;
			return (from.equals(other.getFrom())) && (to.equals(other.getTo()) && (attributes.equals(other.attributes)));
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return from.hashCode() * to.hashCode() * attributes.hashCode();
	}
}
