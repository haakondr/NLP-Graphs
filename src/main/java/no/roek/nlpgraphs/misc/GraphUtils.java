package no.roek.nlpgraphs.misc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;

public class GraphUtils {

	public static Graph getGraph(BasicDBObject dbObject) {
		Graph g = new Graph(dbObject.getString("filename"), dbObject.getInt("sentenceNumber"), dbObject.getInt("offset"), dbObject.getInt("length"));

		HashMap<String, List<String[]>> adj = new HashMap<>();
		for(Object temp : (BasicDBList)dbObject.get("tokens")) {
			BasicDBObject token = (BasicDBObject)temp;
			g.addNode(getNode(token, adj));
		}
		addEdges(g, adj);
		
		return g;
	}

	public static Node getNode(BasicDBObject token, HashMap<String, List<String[]>> adj) {
		//TODO: lemma is the only attribute added to node. Pos should probably be added + synonyms?
		String id = token.getString("id");
		String lemma = token.getString("lemma");
		String pos = token.getString("pos");
		if(pos.equals(",")) {
			pos = "punct";
		}
		String rel = token.getString("rel");
		String deprel = token.getString("deprel");

		if(!adj.containsKey(id)) {
			adj.put(id, new ArrayList<String[]>());
		}

		if(!rel.equals("0")) {
			adj.get(id).add(new String[] {rel, deprel});
		}

		return new Node(id, lemma, new String[] {pos});
	}

	public static void addEdges(Graph graph, HashMap<String, List<String[]>> adj) {
		for (Node node: graph.getNodes()) {
			for (String[] edge : adj.get(node.getId())){
				Node to = graph.getNode(edge[0]);
				graph.addEdge(new Edge(node.getId()+"_"+to.getId(), node, to, edge[1]));
			}
		}
	}
}
