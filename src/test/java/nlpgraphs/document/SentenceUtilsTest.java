package nlpgraphs.document;

import static org.junit.Assert.assertEquals;

import java.nio.file.Paths;
import java.util.List;

import nlpgraphs.misc.Fileutils;
import nlpgraphs.misc.SentenceUtils;

import org.junit.Before;
import org.junit.Test;

import edu.stanford.nlp.ling.Word;

public class SentenceUtilsTest {

	
	@Test
	public void shouldRetrieveSentences() {
		
		List<NLPSentence> sentences = SentenceUtils.getSentences("src/test/resources/documents/sentences.txt");
		NLPSentence s1 = sentences.get(0);

		assertEquals("My name is Håkon, and I live in Trondheim!".length(), s1.getLength());
		assertEquals("My name is Håkon, and I live in Trondheim!",s1.getText());
		assertEquals("It sure is a fine day for school.", sentences.get(1).getText());
		
		List<Word> words = s1.getWords();
		
		assertEquals("My", words.get(0).word());
		assertEquals(1, words.get(0).beginPosition());
		assertEquals(3, words.get(0).endPosition());
		
		assertEquals("name", words.get(1).word());
		assertEquals(3, words.get(1).beginPosition());
		assertEquals(8, words.get(1).endPosition());
		
		int sum=0, charactersInText = 0, textLength=0;
		for (NLPSentence s : sentences) {
			textLength += s.getLength();
			for (Word word : s.getWords()) {
				sum++;
				charactersInText += word.word().length();
			}
		}
		
		assertEquals(22, sum);
		assertEquals(79, charactersInText);
		assertEquals(100, textLength);
		
		
		//42 + 59
	}
}
