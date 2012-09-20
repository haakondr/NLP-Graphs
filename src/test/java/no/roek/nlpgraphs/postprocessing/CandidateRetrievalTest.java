package no.roek.nlpgraphs.postprocessing;


import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import no.roek.nlpgraphs.document.PlagiarismReference;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.XMLUtils;
import no.roek.nlpgraphs.postprocessing.DocumentRetrievalService;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;



public class CandidateRetrievalTest {

	private DocumentRetrievalService drs;
	private String queryDoc;

	@Before
	public void setup() throws CorruptIndexException, IOException {
		drs = new DocumentRetrievalService(Paths.get("src/test/resources/documents"));
		queryDoc = "src/test/resources/documents/source-document01193.txt";
	}

	@Test
	public void shouldReturnSimilarDocuments() throws IOException, ParseException {
		List<String> similarDocuments = drs.getSimilarDocuments(queryDoc, 1);
		assertEquals("source-document01193.txt", similarDocuments.get(0));
		assertEquals(1, similarDocuments.size());
	}

	@Test
	public void shouldRetrieveCorrectAmountOfDocuments() throws ParseException, IOException{
		List<String> similarDocuments = drs.getSimilarDocuments(queryDoc, 3);
		assertEquals(3, similarDocuments.size());
	}
}
