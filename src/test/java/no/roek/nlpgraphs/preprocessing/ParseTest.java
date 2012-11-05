package no.roek.nlpgraphs.preprocessing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.misc.SentenceUtils;

import org.junit.Test;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class ParseTest {

//TODO: takes a couple seconds, uncommit to test dependency parsing
	@Test
	public void shouldDependencyParse() throws ClassNotFoundException, IOException, NullPointerException, MaltChainedException {
		String filename = "parse_test.txt";
		String dir = "src/test/resources/documents/";
//		List<NLPSentence> sentences = SentenceUtils.getSentences(dir+filename);
		POSTagParser parser = new POSTagParser();
		
		ParseJob job = parser.posTagFile(Paths.get(dir+filename));
		
		ConfigService cs = new ConfigService();
		MaltParserService maltService = new MaltParserService();
		maltService.initializeParserModel(cs.getMaltParams());
		String outDir = "src/test/resources/parsetest/";
		ParseUtils.dependencyParse(job, outDir, maltService);
		
		Graph graph = GraphUtils.getGraphFromFile(outDir+job.getParentDir()+job.getFilename(), 1);
		
		Node node = graph.getNode(1);
		assertEquals("be", node.getAttributes().get(0));
		assertEquals("VBZ", node.getAttributes().get(1));
	}
}
