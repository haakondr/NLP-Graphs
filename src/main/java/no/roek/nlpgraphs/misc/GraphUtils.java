package no.roek.nlpgraphs.misc;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.preprocessing.ParseUtils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class GraphUtils {

	public static Graph getGraph(String[] parsedTokens, NLPSentence sentence) {
		Graph graph = new Graph();
		graph.setFilename(sentence.getFilename());
		graph.setLength(sentence.getLength());
		graph.setOffset(sentence.getStart());
		graph.setOriginalText(sentence.getText());
		graph.setSentenceNumber(sentence.getNumber());

		HashMap<String, List<String[]>> adjacent = new HashMap<>();

		for(String wordString : parsedTokens) {
			Node node = getNode(wordString, adjacent);
			graph.addNode(node);
		}

		addEdges(graph, adjacent);

		return graph;
	}

	public static Node getNode(String wordString, HashMap<String, List<String[]>> adjacent) {
		String[] token = wordString.split("\t");
		String id = token[0];
		String word = token[1];
		String pos = token[4];
		String rel = token[6];
		String deprel = token[7];

		if(!adjacent.containsKey(id)) {
			adjacent.put(id, new ArrayList<String[]>());
		}

		if(!isRelationToIdNull(rel)) {
			adjacent.get(id).add(new String[] {rel, deprel});
		}

		return new Node(id, new String[] {word, pos});
	}

	private static boolean isRelationToIdNull(String rel) {
		return rel.equals("0");
	}


	public static List<Graph> getGraphsFromFile(String filename) {
		List<Graph> graphs = new ArrayList<>();
		JsonReader jsonReader = null;
		try {
			jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(filename)));

			JsonParser jsonParser = new JsonParser();
			JsonObject fileObject = jsonParser.parse(jsonReader).getAsJsonObject();
			for (JsonElement sentence : fileObject.get("sentences").getAsJsonArray()) {
				graphs.add(parseGraph(sentence.getAsJsonObject(), filename));
			}
		} catch (IOException  e) {
			e.printStackTrace();
			return null;
		} finally {
			try {
				jsonReader.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}

		return graphs;
	}
	
	
	
	public static Graph getGraphFromFile(String filename, int sentenceNumber) {
		JsonReader jsonReader = null;
		try {
			jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(filename)));

			JsonParser jsonParser = new JsonParser();
			JsonObject fileObject = jsonParser.parse(jsonReader).getAsJsonObject();
			JsonElement sentence = fileObject.get("sentences").getAsJsonObject().get(Integer.toString(sentenceNumber));
			
			return parseGraph(sentence.getAsJsonObject(), filename);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				jsonReader.close();
			} catch(IOException e) {
				e.printStackTrace();
			}
		}
		System.out.println("Could not find sentence "+sentenceNumber+" in file "+filename);
		return null;
	}

	public static Graph parseGraph(JsonObject jsonGraph, String filename) {
		Graph graph = new Graph(filename);
		graph.setLength(jsonGraph.get("length").getAsInt());
		graph.setOffset(jsonGraph.get("offset").getAsInt());
		graph.setOriginalText(jsonGraph.get("originalText").getAsString());
		graph.setSentenceNumber(jsonGraph.get("sentenceNumber").getAsInt());

		HashMap<String, List<String[]>> adj = new HashMap<>();

		for (JsonElement jsonNode : jsonGraph.get("tokens").getAsJsonArray()) {
			graph.addNode(createNodeFromJson(jsonNode.getAsJsonObject(), adj));
		}

		addEdges(graph, adj);
		return graph;
	}

	public static Node createNodeFromJson(JsonObject jsonNode, HashMap<String, List<String[]>> adj) {
		String id = jsonNode.get("id").getAsString();
		String word = jsonNode.get("word").getAsString();
		String lemma = jsonNode.get("lemma").getAsString();
		String pos = jsonNode.get("pos").getAsString();
		String rel = jsonNode.get("rel").getAsString();
		String deprel = jsonNode.get("deprel").getAsString();

		if(!adj.containsKey(id)) {
			adj.put(id, new ArrayList<String[]>());
		}

		if(!rel.equals("0")) {
			adj.get(id).add(new String[] {rel, deprel});
		}

		return new Node(id, new String[] {word, lemma, pos}); 
	}

	public static void addEdges(Graph graph, HashMap<String, List<String[]>> adj) {
		for (Node node: graph.getNodes()) {
			for (String[] edge : adj.get(node.getId())){
				Node to = graph.getNode(edge[0]);
				graph.addEdge(new Edge(node.getId()+"_"+to.getId(), node, to, new String[] {edge[1]}));
				graph.addEdge(new Edge(node.getId()+"_"+to.getId(), node, to, new String[] {edge[1]}));
			}
		}
	}

	public static <T> int listDiff(List<T> list1, List<T> list2) {
		//TODO: move to some other utils class?
		Set<T> intersect = new HashSet<>(list1);
		intersect.retainAll(list2);

		Set<T> temp = new HashSet<>();
		temp.addAll(list1);
		temp.addAll(list2);

		temp.removeAll(intersect);

		return temp.size();
	}
}
