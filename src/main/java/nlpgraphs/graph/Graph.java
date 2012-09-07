package nlpgraphs.graph;

import java.util.ArrayList;
import java.util.List;

public class Graph {

	private List<Node> nodes;
	private List<Edge> edges;
	
	public Graph() {
		nodes = new ArrayList<Node>();
		edges = new ArrayList<Edge>();
	}

	public void addNode(Node node) {
		nodes.add(node);
	}
	
	public void addEdge(Edge edge) {
		edge.getTo().addEdge(edge);
		edge.getFrom().addEdge(edge);
		edges.add(edge);
	}
	
	public Node getNode(int i) {
		return nodes.get(i);
	}
	
	public List<Edge> getEdges() {
		return edges;
	}

	public List<Node> getNodes() {
		return nodes;
	}

}
