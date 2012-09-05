package text2graph.algorithm;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nlpgraphs.algorithm.GraphEditDistance;
import nlpgraphs.graph.Edge;
import nlpgraphs.graph.Graph;
import nlpgraphs.graph.Node;
import nlpgraphs.misc.Utils;

import org.junit.Before;
import org.junit.Test;


public class GraphEditDistanceTest {

	private GraphEditDistance ged;
	private Graph g1;
	private Graph g2;
	
	@Test
	public void testDistance() {
		g1 = new Graph();
		g2 = new Graph();
		
		Node n1 = new Node("1", Arrays.asList(new String[] {"NN", "one"}));
		Node n2 = new Node("2", Arrays.asList(new String[] {"GG", "two"}));
		Node n3 = new Node("3", Arrays.asList(new String[] {"TT", "three"}));

		g1.addNode(n1);
		g1.addNode(n2);
		g1.addNode(n3);
		g1.addEdge(new Edge("1-2", n1, n2));
		g1.addEdge(new Edge("2-3", n2, n3));
		
		Node n4 = new Node("4", Arrays.asList(new String[] {"NN", "one"}));
		Node n5 = new Node("5", Arrays.asList(new String[] {"GG", "two"}));
		g2.addNode(n4);
		g2.addNode(n5);
		g2.addEdge(new Edge("4-5", n4, n5));
		
		ged = new GraphEditDistance(g1, g2, 1, 1, 1);
		ged.printMatrix();
		
		double dist = ged.getDistance();
		assertEquals(2, (int)dist);
	}
	
	@Test
	public void testCostMatrix() {
		
	}
	


}
