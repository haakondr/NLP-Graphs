package nlpgraphs.graph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Graph {

	private String filename;
	private List<Node> nodes;
	private HashMap<String, List<Edge>> adjacent;


	public Graph(String filename) {
		this();
		this.filename = filename;
	}

	public Graph() {
		nodes = new ArrayList<Node>();
		this.adjacent = new HashMap<>();
	}

	public void addNode(Node node) {
		if(!adjacent.containsKey(node.getId())) {
			adjacent.put(node.getId(), new ArrayList<Edge>());
		}
		nodes.add(node);
	}

	public int getSize() {
		return nodes.size();
	}


	public HashMap<String, List<Edge>> getAdjacent() {
		return adjacent;
	}

	public void addEdge(Edge edge) {
		adjacent.get(edge.getTo().getId()).add(edge);
		adjacent.get(edge.getFrom().getId()).add(edge);
	}

	public List<Edge> getEdges(Node node) {
		return getEdges(node.getId());
	}

	public List<Edge> getEdges(String nodeId) {
		return adjacent.get(nodeId);
	}

	public void removeNode(int i) {
		nodes.remove(i);
	}

	public void removeNode(String id) {
		for (Node node : nodes) {
			if(node.getId().equals(id)) {
				nodes.remove(node);
			}
		}
	}

	public Node getNode(int i) {
		return nodes.get(i);
	}

	public Node getNode(String id) {
		//TODO: rewrite to linkedhashmap or something, so iteration isnt needed?
		for (Node node : nodes) {
			if(node.getId().equals(id)) {
				return node;
			}
		}
		return null;
	}

	public List<Node> getNodes() {
		return nodes;
	}

	public String getFilename() {
		return filename;
	}
}
