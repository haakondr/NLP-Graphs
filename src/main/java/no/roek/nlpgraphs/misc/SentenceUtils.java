package no.roek.nlpgraphs.misc;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;
import edu.stanford.nlp.ling.Word;

public class SentenceUtils {

	public static List<NLPSentence> getSentences(String filename) {
		try {
			FileInputStream fstream = new FileInputStream(filename);
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));

			StringBuilder wordBuilder = new StringBuilder();
			StringBuilder sentenceBuilder = new StringBuilder();
			List<NLPSentence> sentences = new ArrayList<NLPSentence>();
			List<Word> words = new ArrayList<>();

			int character = 1, offset = 1, sentenceNumber = 1, sentenceStart = 0;
			while((character = reader.read()) != -1) {
				char c = (char) character;

				sentenceBuilder = stripWhitespaceBeforeText(sentenceBuilder);

				if(isWordDelimiter(c)) {
					wordBuilder = createWord(wordBuilder, words, offset);
				}

				if(!isNewLine(c)) {
					wordBuilder.append((char) c);
					sentenceBuilder.append((char) c);
				}else {
					if(!isSentenceDelimiter(c)) {
						wordBuilder = createWord(wordBuilder, words, offset);
						sentenceBuilder.append(" ");
					}
				}

				if(isSentenceDelimiter(c)) {
					String previousWord;
					if(words.size()== 0) {
						previousWord = "placeholder";
					}else {
						previousWord = words.get(words.size()-1).word();
					}
					if((sentenceBuilder.toString().trim().length() > 1) && !isWordWithPunctation(previousWord)) {
						sentences.add(new NLPSentence(sentenceNumber, sentenceStart, sentenceBuilder.toString(), words));
						createWord(wordBuilder, words, offset);
						words = new ArrayList<Word>();
						sentenceBuilder = new StringBuilder();
						wordBuilder = new StringBuilder();
						sentenceNumber++;
						sentenceStart = offset+1;
					}
				}
				//TODO: should offset be before or after creating sentence?
				offset++;
			}
			if(wordBuilder.toString().trim().length()>0) {
				words.add(new Word(wordBuilder.toString()));
				sentences.add(new NLPSentence(sentenceNumber, offset, sentenceBuilder.toString(), words));
				sentenceNumber++;
			}
			reader.close();

			return sentences;
		}catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}


	public static StringBuilder stripWhitespaceBeforeText(StringBuilder sentenceBuilder) {
		if(sentenceBuilder.toString().trim().length() == 0) {
			return new StringBuilder();
		}
		return sentenceBuilder;
	}
	public static StringBuilder createWord(StringBuilder wordBuilder, List<Word> words, int offset) {
		if(wordBuilder.toString().trim().length() > 0) {
			words.add(new Word(wordBuilder.toString().trim(), offset-wordBuilder.length(), offset));
			return new StringBuilder();
		}

		return wordBuilder;
	}

	public static boolean isNewLine(char c) {
		return (c == '\n' || c == '\r');
	}

	public static boolean isSentenceDelimiter(char c) {
		return c == '.' || c == '!' || c == '?';
	}

	public static boolean isWordDelimiter(char c ) {
		return !Character.isLetter(c) && !Character.isDigit(c) || (c == 10) || (c == 13);
	}

	public static boolean isPartOfWord(char c) {
		return Character.isLetter(c) || Character.isDigit(c);
	}

	public static boolean isWordWithPunctation(String s) {
		return s.equalsIgnoreCase("Mr") || s.equalsIgnoreCase("Mrs") || s.equalsIgnoreCase("ca");
	}
}
