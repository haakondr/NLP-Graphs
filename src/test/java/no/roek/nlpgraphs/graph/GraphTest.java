package no.roek.nlpgraphs.graph;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import no.roek.nlpgraphs.misc.GraphUtils;

import org.junit.Test;


public class GraphTest {

	@Test
	public void testListDiff() {
		List<String> list1 = Arrays.asList(new String[] {"apple", "orange"});
		List<String> list2 = Arrays.asList(new String[] {"apple", "orange", "banana", "strawberry"});
		List<String> list3 = Arrays.asList(new String[] {"apple", "apple", "apple", "orange"});
		
		assertEquals(2, GraphUtils.listDiff(list1, list2));
		assertEquals(GraphUtils.listDiff(list1, list2), GraphUtils.listDiff(list2, list1));
		assertEquals(0, GraphUtils.listDiff(list1, list3));
		assertEquals(2, GraphUtils.listDiff(list3, list2));

		Node n1 = new Node("1", new String[] {"NN", "one"});
		Node n2 = new Node("2", new String[] {"GG", "two"});
		Node n3 = new Node("3", new String[] {"NN", "one"});
		Node n4 = new Node("4", new String[] {"GG", "two"});
		
		Graph g1 = new Graph();
		g1.addNode(n1);
		g1.addNode(n2);
		g1.addNode(n3);
		g1.addNode(n4);
		
		g1.addEdge(new Edge("1-2", n1, n2));
		g1.addEdge(new Edge("3-4", n3, n4));
		g1.addEdge(new Edge("2-4", n2, n4));
		
		assertEquals(0, GraphUtils.listDiff(g1.getEdges(n1), g1.getEdges(n3)));
		assertEquals(GraphUtils.listDiff(g1.getEdges(n1), g1.getEdges(n3)), GraphUtils.listDiff(g1.getEdges(n3), g1.getEdges(n1)));
		assertEquals(1, GraphUtils.listDiff(g1.getEdges(n1), g1.getEdges(n2)));
		assertEquals(1, GraphUtils.listDiff(g1.getEdges(n1), g1.getEdges(n2)));
		
		assertEquals(1, GraphUtils.listDiff(g1.getEdges(n4), g1.getEdges(n1)));
	}

	@Test
	public void testNodeEquals() {
		Node n1 = new Node("1", new String[] {"NN", "one"});
		Node n2 = new Node("2", new String[] {"NN", "one"});
		Node n3 = new Node("3", new String[] {"NN", "two"});
		
		assertEquals(true, n1.equals(n2));
		assertEquals(false, n1.equals(n3));
	}
	
	@Test
	public void testEdgeEquals() {
		Node n1 = new Node("1", new String[] {"NN", "one"});
		Node n2 = new Node("2", new String[] {"GG", "two"});
		Node n3 = new Node("3", new String[] {"NN", "one"});
		Node n4 = new Node("4", new String[] {"GG", "two"});
		
//		Edge e1 = new Edge("1-2", n1, n2);
//		Edge e2 = new Edge("3-4", n3, n4);
//		Edge e3 = new Edge("1-3", n1, n3);
//		Edge e4 = new Edge("4-1", n4, n1);
//		Edge e5 = new Edge("1-3", n1, n3);
				
		Edge e1 = new Edge("1-2", n1, n2);
		Edge e2 = new Edge("3-4", n3, n4);
		Edge e3 = new Edge("1-3", n1, n3);
		Edge e4 = new Edge("4-1", n4, n1);
		Edge e5 = new Edge("1-3", n1, n3);
		
		assertEquals(true, n1.equals(n3));
		assertEquals(true, n2.equals(n4));
		assertEquals(true, e1.equals(e2));
		assertEquals(false, e1.equals(e3));
		assertEquals(false, e3.equals(e4));
		assertEquals(true, e3.equals(e5));
		

	}
	
}
