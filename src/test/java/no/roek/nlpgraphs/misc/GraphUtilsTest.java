package no.roek.nlpgraphs.misc;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.List;

import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

import org.junit.Before;
import org.junit.Test;

public class GraphUtilsTest {

	private Graph graph;
	
	@Before
	public void setup() {
		graph = GraphUtils.parseGraph(Paths.get("src/test/resources/parsed_documents/test.txt"));
	}
	
	@Test
	public void shouldBeCorrectSize() {
		assertEquals(12, graph.getSize());
	}
	
	@Test
	public void shouldRetrieveSameNode() {
		Node n1 = graph.getNode(0);
		Node n1_ = graph.getNode("1_1");
		assertEquals(n1.getId(), n1_.getId());
	}
	
	@Test
	public void shouldRetrieveEdges() {
		Node node_the = graph.getNode("1_10");
		Node node_sun = graph.getNode("1_11");
		
		List<Edge> edges_the = graph.getEdges(node_the);
		List<Edge> edges_sun = graph.getEdges(node_sun);
		assertEquals(1, edges_the.size());
		assertEquals(2, edges_sun.size());
		
		assertEquals("1_11", edges_the.get(0).getTo().getId());
		assertEquals("1_10", edges_the.get(0).getFrom().getId());
	}
}
