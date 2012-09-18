package no.roek.nlpgraphs.misc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

public class GraphUtils {

	public static Graph parseGraph(String filename) {
		JsonReader jsonReader;
		try {
			jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(filename)));
		
		JsonParser jsonParser = new JsonParser();
		JsonObject fileObject = jsonParser.parse(jsonReader).getAsJsonObject();
//		for (JsonElement sentence : fileObject.get("sentences").getAsJsonArray()) {
//			
//		}
		JsonElement jsonSentence = fileObject.get("sentences").getAsJsonArray().get(0);
		return parseGraph(jsonSentence.getAsJsonObject(), filename);
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static Graph parseGraph(JsonObject jsonGraph, String filename) {
		Graph graph = new Graph(filename);
		graph.setLength(jsonGraph.get("length").getAsInt());
		graph.setOffset(jsonGraph.get("offset").getAsInt());
		graph.setOriginalText(jsonGraph.get("originalText").getAsString());
		graph.setSentenceNumber(jsonGraph.get("sentenceNumber").getAsInt());

		HashMap<String, List<String[]>> adj = new HashMap<>();

		for (JsonElement jsonNode : jsonGraph.get("tokens").getAsJsonArray()) {
			graph.addNode(createNode(jsonNode.getAsJsonObject(), adj));
		}

		addEdges(graph, adj);
		return graph;
	}

	public static Node createNode(JsonObject jsonNode, HashMap<String, List<String[]>> adj) {
		String id = jsonNode.get("id").getAsString();
		String word = jsonNode.get("word").getAsString();
		String pos = jsonNode.get("pos").getAsString();
		String rel = jsonNode.get("rel").getAsString();
		String deprel = jsonNode.get("deprel").getAsString();

		if(!adj.containsKey(id)) {
			adj.put(id, new ArrayList<String[]>());
		}

		if(!rel.matches("[\\d]+_0")) {
			adj.get(id).add(new String[] {rel, deprel});
		}

		return new Node(id, new String[] {word, pos}); 
	}

	//public static Node createNode(String line, HashMap<String, List<String[]>> adj) {
	//	String[] tokens = line.split("\t");
	//	if(!adj.containsKey(tokens[0])) {
	//		adj.put(tokens[0], new ArrayList<String[]>());
	//	}
	//	if(!tokens[6].matches("[\\d]+_0")){
	//		adj.get(tokens[0]).add(new String[] {tokens[6], tokens[7]});
	//	}
	//
	//	return new Node(tokens[0], new String[] {tokens[1], tokens[4]});
	//}

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
