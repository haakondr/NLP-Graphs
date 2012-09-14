package no.roek.nlpgraphs.misc;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

public class GraphUtils {

	public static Graph parseGraphs(String filename) {
		return parseGraph(Paths.get(filename));
	}
	
	public static Graph parseGraph(Path file) {
		Graph graph = new Graph(file.getFileName().toString());
		HashMap<String, List<String[]>> adj = new HashMap<>();

		try {
			List<String> lines =  Files.readAllLines(file, StandardCharsets.UTF_8);
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
		if(!tokens[6].matches("[\\d]+_0")){
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
	
	public static <T> int listDiff(List<T> list1, List<T> list2) {
		//TODO: rewrite to something that doesn't require overriding hashCode in edge/node
		Set<T> intersect = new HashSet<>(list1);
		intersect.retainAll(list2);
		
		Set<T> temp = new HashSet<>();
		temp.addAll(list1);
		temp.addAll(list2);
		
		temp.removeAll(intersect);
		
		return temp.size();
	}
}
