package no.roek.nlpgraphs.application;

import java.awt.Label;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import no.roek.nlpgraphs.detailedretrieval.GraphEditDistance;
import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

public class AssignmentAlgorithmBenchmark {
	public static void main(String[] args) {

		List<Integer> xValues = new ArrayList<>();
		List<Double> munkresSpeeds = new ArrayList<>();
		List<Double> vjSpeeds = new ArrayList<>();
		for (int i = 1; i < 200; i+=10) {
			munkresSpeeds.add(getAvgSpeed(i, true));
			vjSpeeds.add(getAvgSpeed(i, false));
			xValues.add(i);
			System.out.println("Done calculating avg speed for nodecount: "+i);
		}
		List<List<Double>> yValues = new ArrayList<>();
		yValues.add(munkresSpeeds);
		yValues.add(vjSpeeds);

		for (List<Double> list : yValues) {
			for (Double double1 : list) {
				System.out.print(double1+" ");
			}
			System.out.println();
		}
		
		writeToCSV(new String[]{"Nodecount", "Runtime(ms)", "Munkres", "VolgenantJonker"}, xValues, yValues);
	}

	public static void writeToCSV(String[] labels, List<Integer> xValues, List<List<Double>> yValues) {
		FileWriter output = null;
		BufferedWriter writer = null;
		try {
			output = new FileWriter("assignment_algorithm_benchmark.txt");
			writer = new BufferedWriter(output);

			for (int i = 0; i < labels.length; i++) {
				if(i==labels.length) {
					writer.write(labels[i]+"\n");
				}else {
					writer.write(labels[i]+" ");
				}
			}
			writer.newLine();
			
			for (int i = 0; i < xValues.size(); i++) {
				writer.write(xValues.get(i)+" ");
				for (int j = 0; j < yValues.size(); j++) {
					if(j==yValues.size()-1) {
						writer.write(yValues.get(j).get(i)+"\n");
					}else {
						writer.write(yValues.get(j).get(i)+" ");
					}
				}
			}
			
			writer.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static double getAvgSpeed(int nodecount, boolean munkres) {
		GraphEditDistance ged = init(nodecount);

		List<Double> speeds = new ArrayList<>();
		for (int i = 0; i < 1000; i++) {
			if(munkres) {
				speeds.add(getMunkresExecutionTime(ged));
			}else {
				speeds.add(getVJExecutionTime(ged));
			}
		}

		return avg(speeds);
	}


	public static double avg(List<Double> list) {
		double sum = 0;
		for (Double speed : list) {
			sum += speed;
		}
		return sum / list.size();
	}

	public static double getMunkresExecutionTime(GraphEditDistance ged) {
		long start = System.currentTimeMillis();
		double dist = ged.getDistance();
		long end = System.currentTimeMillis();

		return end-start;
	}

	public static double getVJExecutionTime(GraphEditDistance ged) {
		long start = System.currentTimeMillis();
		double dist = ged.getJVDistance();
		long end = System.currentTimeMillis();

		return end-start;
	}



	public static GraphEditDistance init(int nodecount) {
		Graph g1 = new Graph();
		Graph g2 = new Graph();

		for (int i = 0; i < nodecount; i++) {
			g1.addNode(new Node(String.valueOf(i), String.valueOf(i), new String[] {String.valueOf(i)}));
			g2.addNode(new Node(String.valueOf(i), String.valueOf(i), new String[] {String.valueOf(i)}));
			if(i>0) {
				g1.addEdge(new Edge(String.valueOf(i-1)+"_"+String.valueOf(i), g1.getNode(i-1), g1.getNode(i), String.valueOf(i)));
				g2.addEdge(new Edge(String.valueOf(i-1)+"_"+String.valueOf(i), g1.getNode(i-1), g1.getNode(i), String.valueOf(i)));
			}
		}

		return new GraphEditDistance(g1, g2, new HashMap<String, Double>(), new HashMap<String, Double>());
	}

}
