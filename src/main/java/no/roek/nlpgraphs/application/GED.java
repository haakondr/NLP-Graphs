package no.roek.nlpgraphs.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.konstantinosnedas.HungarianAlgorithm;
import com.mongodb.BasicDBObject;


import no.roek.nlpgraphs.detailed.analysis.GraphEditDistance;
import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.DatabaseService;
import no.roek.nlpgraphs.misc.EditWeightService;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.POSTagParser;

public class GED {

	//TODO: move most of these classes to utility classes?
	public static void main(String[] args) {
		ConfigService cs = new ConfigService();

		String[] texts = getInputTexts(args);
		POSTagParser postagger = new POSTagParser();
		DependencyParser depParser = new DependencyParser();

		Graph g1 = getGraph(texts[0], postagger, depParser);
		Graph g2 = getGraph(texts[1], postagger, depParser);
		printNodes(g1);
		printNodes(g2);

		Map<String, Double> posEditWeights = EditWeightService.getEditWeights(cs.getPosSubFile(), cs.getPosInsdelFile());
		Map<String, Double> deprelEditWeights = EditWeightService.getInsDelCosts(cs.getDeprelInsdelFile());
		GraphEditDistance ged = new GraphEditDistance(g1, g2, posEditWeights, deprelEditWeights);

//						ged.printMatrix();
		printLatexEditPath(g1, g2, ged.getCostMatrix());
		printLatexMatrix(g1, g2, ged.getCostMatrix());
		System.out.println("GED for the two graphs: "+ged.getDistance()+". Normalised: "+ged.getNormalizedDistance());
		System.out.println("Edit path:");
		for(String editPath : getEditPath(g1, g2, ged.getCostMatrix(), true)) {
			System.out.println(editPath);
		}
//		for(String freeEdit : getFreeEdits(g1, g2, ged.getCostMatrix())) {
//			System.out.print(freeEdit+", ");
//		}
	}

	public static void printNodes(Graph g) {
		for(Node n : g.getNodes()) {
			System.out.print(n.getLabel()+","+n.getAttributes().get(0)+"\t");
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

	public static List<String> getEditPath(Graph g1, Graph g2, double[][] costMatrix, boolean printCost) {
		return getAssignment(g1, g2, costMatrix, true, printCost);
	}
	
	public static List<String> getFreeEdits(Graph g1, Graph g2, double[][] costMatrix) {
		return getAssignment(g1, g2, costMatrix, false, false);
	}

	public static List<String> getAssignment(Graph g1, Graph g2, double[][] costMatrix, boolean editPath, boolean printCost) {
		List<String> editPaths = new ArrayList<>();
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");

		for (int i = 0; i < assignment.length; i++) {
			String from = getEditPathAttribute(assignment[i][0], g1);
			String to = getEditPathAttribute(assignment[i][1], g2);

			double cost = costMatrix[assignment[i][0]][assignment[i][1]];
			if(cost != 0 && editPath) {
				if(printCost) {
					editPaths.add("("+from+" -> "+to+") = "+cost);
				}
			}else if(cost == 0 && !editPath) {
				editPaths.add("("+from+" -> "+to+")");
			}
		}

		return editPaths;

	}

	private static void printLatexEditPath(Graph g1, Graph g2, double[][] costMatrix) {
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");


		System.out.println("\\textbf{Edit operation} & \\textbf{cost} \\\\");
		System.out.println("\\hline");

		for (int i = 0; i < assignment.length; i++) {
			String from = getEditPathAttribute(assignment[i][0], g1);
			from  = from.equals("ε") ? "\\epsilon" : from;
			String to = getEditPathAttribute(assignment[i][1], g2);
			to = to.equals("ε") ? "\\epsilon" : to;

			double cost = costMatrix[assignment[i][0]][assignment[i][1]];
			String costString = String.format("%.2f", cost);
			if(cost != 0) {
				System.out.println("($"+from+" \\rightarrow "+to+"$) & "+costString+" \\\\");
			}
		}
	}

	private static String getEditPathAttribute(int nodeNumber, Graph g) {
		if(nodeNumber < g.getNodes().size()) {
			Node n= g.getNode(nodeNumber);
			return n.getLabel();
		}else {
			return "ε";
		}
	}

	public static void printLatexMatrix(Graph g1, Graph g2, double[][] costMatrix) {
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");
		System.out.println("-------------");
		System.out.println("Cost matrix substitute matrix: ");
		System.out.println("\\hline");
		System.out.print(" & ");
		for (int i = 0; i < g2.getNodes().size(); i++) {
			System.out.print(g2.getNode(i));
			if(i < g2.getSize() -1) {
				System.out.print(" & ");
			}else {
				System.out.println(" \\\\");
			}
		}

		System.out.println("\\hline");

		for (int i = 0; i < g1.getSize(); i++) {
			for (int j = 0; j < g2.getSize(); j++) {
				if(j == 0) {
					System.out.print(g1.getNode(i)+" & ");
				}
				System.out.print(getCostString(i, j, assignment, costMatrix));
				if(j< g2.getSize()-1) {
					System.out.print(" & ");
				}else {
					System.out.println(" \\\\");
				}
			}
			System.out.println("\\hline");
		}
	}

	private static String getCostString(int i, int j, int[][] assignment, double[][] costMatrix) {
		String temp = String.format("%.2f", costMatrix[i][j]);
		for (int k = 0; k < assignment.length; k++) {
			if(assignment[k][0] == i && assignment[k][1] == j) {
				return "\\color{blue}{\\textbf{"+temp+"}}";
			}
		}

		return temp;

	}
}
