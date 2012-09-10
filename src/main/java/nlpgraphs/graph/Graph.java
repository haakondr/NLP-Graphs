package nlpgraphs.graph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Graph {

	private Path file;
	private List<Node> nodes;
	private HashMap<String, List<Edge>> adjacent;


	public Graph(Path file) {
		this();
		this.file = file;
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

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

}
