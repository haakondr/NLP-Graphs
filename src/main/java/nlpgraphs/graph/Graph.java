package nlpgraphs.graph;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Graph {

	private Path file;
	private List<Node> nodes;
	private List<Edge> edges;
	
	public Graph(Path file) {
		this();
		this.file = file;
	}
	
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

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

}
