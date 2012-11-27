package no.roek.nlpgraphs.graph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

public class Node {

	private String id;
	private String label;
	private List<String> attributes;

	public Node(String id, String label) {
		this.id = id;
		this.label = label;
		this.attributes = new ArrayList<>();
	}
	
	public Node(String id, String index, String label, List<String> attributes) {
		this(id, label);
		this.attributes = attributes;
	}

	public Node(String id, String label, String[] attributes) {
		this.id = id;
		this.label = label;
		this.attributes  = Arrays.asList(attributes);
	}

	public String getId() {
		return id;
	}


	public String  getLabel() {
		return label;
	}
	
	public List<String> getAttributes() {
		return attributes;
	}

	@Override
	public String toString() {
		return label;
	}

	public void addAttribute(String attr) {
		attributes.add(attr);
	}

		@Override
		public  boolean equals(Object obj) {
			if(getClass() == obj.getClass()) {
				Node other = (Node) obj;
				return label.equals(other.getLabel());
			}
			return false;
		}


	public boolean equalsAttributes(Node other) {
		for (int i = 0; i < attributes.size(); i++) {
			if(!attributes.get(i).equals(other.getAttributes().get(i))) {
				return false;
			}
		}

		return true;
	}

	@Override
	public int hashCode() {
		return attributes.get(0).hashCode();
	}
}
