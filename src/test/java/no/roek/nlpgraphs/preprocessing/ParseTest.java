package no.roek.nlpgraphs.preprocessing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.search.SentenceUtils;

import org.junit.Test;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class ParseTest {

	@Test
	public void shouldDependencyParse() throws ClassNotFoundException, IOException, NullPointerException, MaltChainedException {
		String filename = "parse_test.txt";
		String dir = "src/test/resources/documents/";
		NLPSentence sentence = SentenceUtils.getSentences(dir+filename).get(0);
		
		ConfigService cs = new ConfigService();
		MaxentTagger tagger = new MaxentTagger(cs.getPOSTaggerParams());
		String[] postags = ParseUtils.getPosTagString(sentence, tagger);
		sentence.setPostags(postags);
		
		ParseJob job = new ParseJob(dir+filename);
		job.addSentence(sentence);
		
		MaltParserService maltService = new MaltParserService();
		maltService.initializeParserModel(cs.getMaltParams());
		String outDir = "src/test/resources/parsetest/";
		ParseUtils.dependencyParse(job, outDir, maltService);
		
		Graph graph = GraphUtils.getGraphFromFile(outDir+job.getParentDir()+job.getFilename(), 1);
		
		Node node = graph.getNode(1);
		assertEquals("is", node.getAttributes().get(0));
		assertEquals("be", node.getAttributes().get(1));
		assertEquals("VBZ", node.getAttributes().get(2));
	}
}
