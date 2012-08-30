package text2graph.candidateRetrieval;


import static org.junit.Assert.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;


import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;
import org.junit.Before;
import org.junit.Test;

public class CandidateRetrievalTest {

	private DocumentRetrievalService drs;
	private String query;

	@Before
	public void setup() throws CorruptIndexException, IOException {
		drs = new DocumentRetrievalService(Paths.get("src/test/resources/documents"));
		query = "Hello my name is Håkon, and I live in Trondheim.";
	}
	
	@Test
	public void shouldReturnSimilarDocuments() throws IOException, ParseException {
		List<String> similarDocuments = drs.getSimilarDocuments(query, 1);
		assertEquals("omg.txt", similarDocuments.get(0));
		assertEquals(1, similarDocuments.size());
	}

	@Test
	public void shouldRetrieveCorrectAmountOfDocuments() throws ParseException, IOException{
		List<String> similarDocuments = drs.getSimilarDocuments(query, 3);
		assertEquals(3, similarDocuments.size());
	}
}
