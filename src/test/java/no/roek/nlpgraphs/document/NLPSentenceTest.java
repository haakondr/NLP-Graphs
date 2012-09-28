package no.roek.nlpgraphs.document;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class NLPSentenceTest {

	@Test
	public void shouldGetRelativePath() {
		NLPSentence sentence = new NLPSentence("data/source-document/source-document0293.txt", 5, 0, 11, "omglol text");
		
		assertEquals("source-document/source-document0293/source-document0293_5", sentence.getRelativePath());
		

	}
}
