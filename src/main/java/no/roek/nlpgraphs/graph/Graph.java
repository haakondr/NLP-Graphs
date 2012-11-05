package no.roek.nlpgraphs.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;

public class Graph {

	private String filename, originalText;
	private int offset, length, sentenceNumber;
	private List<Node> nodes;
	private HashMap<String, List<Edge>> edges;


	public Graph(String filename) {
		this();
		this.filename = filename;
	}

	public Graph() {
		nodes = new ArrayList<Node>();
		this.edges = new HashMap<>();
	}

	public void addNode(Node node) {
		if(!edges.containsKey(node.getId())) {
			edges.put(node.getId(), new ArrayList<Edge>());
		}
		nodes.add(node);
	}

	public int getSize() {
		return nodes.size();
	}


	public HashMap<String, List<Edge>> getEdges() {
		return edges;
	}

	public void addEdge(Edge edge) {
		edges.get(edge.getTo().getId()).add(edge);
		edges.get(edge.getFrom().getId()).add(edge);
	}

	public List<Edge> getEdges(Node node) {
		return getEdges(node.getId());
	}

	public List<Edge> getEdges(String nodeId) {
		return edges.get(nodeId);
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

	public String getOriginalText() {
		return originalText;
	}

	public void setOriginalText(String originalText) {
		this.originalText = originalText;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getSentenceNumber() {
		return sentenceNumber;
	}

	public void setSentenceNumber(int sentenceNumber) {
		this.sentenceNumber = sentenceNumber;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public NLPSentence toSentence() {
//		return new NLPSentence(filename, sentenceNumber, offset, length, originalText);
		return new NLPSentence(filename, sentenceNumber, offset, length);
	}
}
