package nlpgraphs.algorithm;

import static org.junit.Assert.*;


import no.roek.nlpgraphs.algorithm.GraphEditDistance;
import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

import org.junit.Test;


public class GraphEditDistanceTest {

	private GraphEditDistance ged;
	private Graph g1;
	private Graph g2;
	
	@Test
	public void testDistance() {
		g1 = new Graph();
		g2 = new Graph();
		
		Node n1 = new Node("1", new String[] {"NN", "one"});
		Node n2 = new Node("2", new String[] {"GG", "two"});
		Node n3 = new Node("3", new String[] {"TT", "three"});

		g1.addNode(n1);
		g1.addNode(n2);
		g1.addNode(n3);
		g1.addEdge(new Edge("1-2", n1, n2));
		g1.addEdge(new Edge("2-3", n2, n3));
		
		Node n4 = new Node("4", new String[] {"NN", "one"});
		Node n5 = new Node("5", new String[] {"GG", "two"});
		g2.addNode(n4);
		g2.addNode(n5);
		g2.addEdge(new Edge("4-5", n4, n5));
		
		ged = new GraphEditDistance(g1, g2, 1, 1, 1);
		
		double dist = ged.getDistance();
		assertEquals(2, (int)dist);
	}
	
	@Test
	public void testCostMatrix() {
		
	}
	
	@Test
	public void testEdgeDiff() {
		
	}
	
//TODO: uncomment to test execution time for graph edit distance algorithm.
//	@Test
//	public void testExecutionTime() {
//		g1 = new Graph();
//		g2 = new Graph();
//		
//		for (int i = 0; i < 1000; i++) {
//			g1.addNode(new Node(String.valueOf(i), new String[] {String.valueOf(i)}));
//			g2.addNode(new Node(String.valueOf(i), new String[] {String.valueOf(i)}));
//			if(i>0) {
//				g1.addEdge(new Edge(String.valueOf(i-1)+"_"+String.valueOf(i), g1.getNode(i-1), g1.getNode(i)));
//				g2.addEdge(new Edge(String.valueOf(i-1)+"_"+String.valueOf(i), g1.getNode(i-1), g1.getNode(i)));
//			}
//		}
//		
//		ged = new GraphEditDistance(g1, g2, 1, 1, 1);
//		double dist = ged.getDistance();
//		assertEquals(0, (int)dist);
//	}
}
