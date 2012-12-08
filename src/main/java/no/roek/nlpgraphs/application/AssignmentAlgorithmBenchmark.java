package no.roek.nlpgraphs.application;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import no.roek.nlpgraphs.detailedanalysis.GraphEditDistance;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

import com.google.code.javakbest.JVC;
import com.konstantinosnedas.HungarianAlgorithm;

public class AssignmentAlgorithmBenchmark {
	public static void main(String[] args) {

		List<Integer> xValues = new ArrayList<>();
		List<Double> munkresSpeeds = new ArrayList<>();
		List<Double> vjSpeeds = new ArrayList<>();
		for (int i = 1; i < 250; i+=5) {
			munkresSpeeds.add(getMedianSpeed(i, true));
			vjSpeeds.add(getMedianSpeed(i, false));
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

	public static double getMedianSpeed(int nodecount, boolean munkres) {
		List<Double> speeds = new ArrayList<>();
		GraphEditDistance ged = initGED(nodecount);
		for (int i = 0; i < 1000; i++) {
			if(munkres) {
				speeds.add(getMunkresExecutionTime(ged.getCostMatrix()));
			}else {
				speeds.add(getVJExecutionTime(ged.getCostMatrix()));
			}
		}

		return median(speeds);
	}
	
	public static double getMedianGEDSpeed(int nodecount, boolean munkres) {
		List<Double> speeds = new ArrayList<>();
		GraphEditDistance ged = initGED(nodecount);
		for (int i = 0; i < 1000; i++) {
			if(munkres) {
				speeds.add(getMunkresGEDTime(ged));
			}else {
				speeds.add(getVolgenantJonkerGEDTime(ged));
			}
		}

		return median(speeds);
	}

	public static double median(List<Double> list) {
		Collections.sort(list);
		int i = list.size() / 2;
		return list.get(i);
	}

	public static double getMunkresGEDTime(GraphEditDistance ged) {
		long start = System.currentTimeMillis();
		ged.createCostMatrix();
		ged.getDistance();
		long end = System.currentTimeMillis();

		return end-start;
	}
	
	public static double getVolgenantJonkerGEDTime(GraphEditDistance ged) {
		long start = System.currentTimeMillis();
		ged.createCostMatrix();
		ged.getVolgenantJonkerDistance();
		long end = System.currentTimeMillis();

		return end-start;
	}
	
	public static double getMunkresExecutionTime(double[][] costMatrix) {
		long start = System.currentTimeMillis();
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(costMatrix, "min");
		long end = System.currentTimeMillis();

		return end-start;
	}

	public static double getVJExecutionTime(double[][] costMatrix) {
		long start = System.currentTimeMillis();
		JVC jvc = JVC.solve(costMatrix);
		jvc.getCost();
		long end = System.currentTimeMillis();

		return end-start;
	}



	public static double[][] init(int nodecount) {
		double[][] costMatrix = new double[nodecount][nodecount];

		for (int i = 0; i < nodecount; i++) {
			for (int j = 0; j < nodecount; j++) {
				costMatrix[i][j] = Math.random();
			}
		}

		return costMatrix;
	}

	public static GraphEditDistance initGED(int nodecount) {
		Graph g1 = new Graph();
		Graph g2 = new Graph();
		
		for (int i = 0; i < nodecount; i++) {
			g1.addNode(new Node(""+i, ""+Math.random(), new String[] {"lol"}));
			g2.addNode(new Node(""+i, ""+Math.random(), new String[] {"lol"}));
		}
		
		return new GraphEditDistance(g1, g2, new HashMap<String,Double>(), new HashMap<String,Double>());
	}
	

}
