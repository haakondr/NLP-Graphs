package no.roek.nlpgraphs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBObject;


import no.roek.nlpgraphs.detailedretrieval.GraphEditDistance;
import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.misc.DatabaseService;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.POSTagParser;

public class GED {

	//TODO: move most of these classes to utility classes?
	public static void main(String[] args) {
		
		String[] texts = getInputTexts(args);
		POSTagParser postagger = new POSTagParser();
		DependencyParser depParser = new DependencyParser();
		
		Graph g1 = getGraph(texts[0], postagger, depParser);
		Graph g2 = getGraph(texts[1], postagger, depParser);
		printNodes(g1);
		printNodes(g2);
		
		GraphEditDistance ged = new GraphEditDistance(g1, g2);
		
		//		ged.printMatrix();
		System.out.println("GED for the two graphs: "+ged.getDistance()+". Normalised: "+ged.getNormalizedDistance());
		System.out.println("Edit path:");
		for(String editPath : ged.getEditPath(true)) {
			System.out.println(editPath);
		}
	}
	
	public static void printNodes(Graph g) {
		for(Node n : g.getNodes()) {
			System.out.print(n.getAttributes().get(0)+"\t");
		}
		System.out.println();
	}
	
	public static void printEdges(Graph g) {
		for(Node n : g.getNodes()) {
			System.out.print(g.getEdges(n));
		}
		System.out.println();
	}
	
	public static Graph getGraph(String text, POSTagParser postagger, DependencyParser depParser) {
		BasicDBObject dbObj = depParser.parseSentence(postagger.postagSentence(text), "test", 0,0,0);
		return GraphUtils.getGraph(dbObj);
	}

	public static String[] getInputTexts(String[] args)  {
		String text1="", text2="";
		if(args.length!=2) {
			InputStreamReader converter = new InputStreamReader(System.in);
			BufferedReader in = new BufferedReader(converter);


			try {
				System.out.println("Please enter the first sentence: ");
				text1 = in.readLine();
				
				System.out.println("Please enter the second sentence: ");
				text2 = in.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}else {
			return args;
		}
		
		return new String[] {text1, text2};
	}
}
