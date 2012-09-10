package nlpgraphs.misc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import nlpgraphs.graph.Edge;
import nlpgraphs.graph.Graph;
import nlpgraphs.graph.Node;

public class GraphUtils {


	public static Graph parseGraph(String filename) {
		Graph graph = new Graph(Paths.get(filename));
		HashMap<String, List<String[]>> adj = new HashMap<>();

		try {
			List<String> lines =  Files.readAllLines(Paths.get(filename), StandardCharsets.UTF_8);
			for (String line : lines) {
				graph.addNode(createNode(line, adj));
			}

			addEdges(graph, adj);
			return graph;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Node createNode(String line, HashMap<String, List<String[]>> adj) {
		String[] tokens = line.split("\t");
		if(!adj.containsKey(tokens[0])) {
			adj.put(tokens[0], new ArrayList<String[]>());
		}
		if(!tokens[6].equals("0")) {
			adj.get(tokens[0]).add(new String[] {tokens[6], tokens[7]});
		}

		return new Node(tokens[0], new String[] {tokens[1], tokens[4]});
	}

	public static void addEdges(Graph graph, HashMap<String, List<String[]>> adj) {
		for (Node node: graph.getNodes()) {
			if(!graph.getAdjacent().containsKey(node.getId())) {
				graph.getAdjacent().put(node.getId(), new ArrayList<Edge>());
			}
			for (String[] edge : adj.get(node.getId())){
				Node to = graph.getNode(edge[0]);
				graph.getAdjacent().get(node.getId()).add(new Edge(node.getId()+"_"+to.getId(), node, to, new String[] {edge[1]}));
				graph.getAdjacent().get(to.getId()).add(new Edge(node.getId()+"_"+to.getId(), node, to, new String[] {edge[1]}));
			}
		}
	}
}
