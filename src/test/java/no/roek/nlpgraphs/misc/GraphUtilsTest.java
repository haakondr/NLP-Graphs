package no.roek.nlpgraphs.misc;

import static org.junit.Assert.assertEquals;

import java.util.List;

import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.search.SentenceUtils;

import org.junit.Before;
import org.junit.Test;

public class GraphUtilsTest {

	private Graph graph1, graph2;
	
	@Before
	public void setup() {
		graph1 = GraphUtils.getGraphFromFile("src/test/resources/parsed/suspicious-document03843.txt", 24);
	}
	
	
	@Test
	public void shouldBeCorrectSize() {
		assertEquals(50, graph1.getSize());
	}
	
	@Test
	public void testSentenceCount() {
		int size = GraphUtils.getGraphsFromFile("src/test/resources/parsed/suspicious-document03843.txt").size();
		assertEquals(67, size);
	}
	
	@Test
	public void shouldBeCorrectText() {
		String origText = "He must kill out the smaller centres of interest, in order that his whole will, love, and attention may pour itself out towards, seize upon, unite with, that special manifestation of the beauty and significance of the universe to which he is drawn.";
		assertEquals(origText, graph1.getOriginalText());
	}
	
//	
//	@Test
//	public void shouldRetrieveSameNode() {
//		Node n1 = graph.getNode(0);
//		Node n1_ = graph.getNode("1_1");
//		assertEquals(n1.getId(), n1_.getId());
//	}
//	
//	@Test
//	public void shouldRetrieveEdges() {
//		Node node_the = graph.getNode("1_10");
//		Node node_sun = graph.getNode("1_11");
//		
//		List<Edge> edges_the = graph.getEdges(node_the);
//		List<Edge> edges_sun = graph.getEdges(node_sun);
//		assertEquals(1, edges_the.size());
//		assertEquals(2, edges_sun.size());
//		
//		assertEquals("1_11", edges_the.get(0).getTo().getId());
//		assertEquals("1_10", edges_the.get(0).getFrom().getId());
//	}
}
