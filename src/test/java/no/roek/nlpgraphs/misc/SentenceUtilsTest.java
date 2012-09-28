package no.roek.nlpgraphs.misc;

import static org.junit.Assert.assertEquals;

import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;

import org.junit.Test;

import edu.stanford.nlp.ling.Word;

public class SentenceUtilsTest {

	
	@Test
	public void shouldRetrieveSentences() {
		
		List<NLPSentence> sentences = SentenceUtils.getSentences("src/test/resources/documents/sentences2.txt");
		NLPSentence s1 = sentences.get(0);

		assertEquals("My name is Håkon, and I live in Trondheim!".length(), s1.getLength());
		assertEquals("My name is Håkon, and I live in Trondheim!",s1.getText());
		assertEquals("It sure is a fine day for school.", sentences.get(1).getText());
		
		List<Word> words = s1.getWords();
		
		assertEquals("My", words.get(0).word());
		assertEquals(0, words.get(0).beginPosition());
		assertEquals(2, words.get(0).endPosition());
		
		assertEquals("name", words.get(1).word());
		assertEquals(2, words.get(1).beginPosition());
		assertEquals(7, words.get(1).endPosition());
		
		int sum=0, charactersInText = 0, textLength=0;
		for (NLPSentence s : sentences) {
			textLength += s.getLength();
			for (Word word : s.getWords()) {
				sum++;
				charactersInText += word.word().length();
			}
		}
		
		// each sentence has a whitespace after, except the last one.
		textLength += sentences.size() - 1;
		
		assertEquals(25, sum);
		assertEquals(82, charactersInText);
		assertEquals(103, textLength);
	}
	
	@Test
	public void testOffset() {
		List<NLPSentence> sentences = SentenceUtils.getSentences("src/test/resources/documents/source-document08364.txt");
		NLPSentence s1 = sentences.get(0);
		assertEquals(0, s1.getStart());
		assertEquals(92, sentences.get(1).getStart());
		
		assertEquals(91, s1.getStart() + s1.getLength());
		assertEquals(1803, sentences.get(14).getStart());
	}
	
	@Test
	public void testOffset2() {
		List<NLPSentence> sentences = SentenceUtils.getSentences("src/test/resources/documents/suspicious-document00014.txt");
		NLPSentence sentence = sentences.get(7);
		assertEquals(8, sentence.getNumber());
//		assertEquals(1385, sentences.get(6).getStart() + sentences.get(6).getLength());
		assertEquals(1386, sentence.getStart());
	}
}
