package no.roek.nlpgraphs.search;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import net.sourceforge.semantics.Compare;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.TextPair;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.misc.GraphUtils;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.ling.WordLemmaTag;

public class SentenceUtils {

	public static List<TextPair> getSimilarSentences(String dataDir, String parsedDir, String testDir, String trainDir, String testFile, String trainFile) {
		List<TextPair> textPairs = new LinkedList<>();
		int n = textPairs.size();
		for (Graph testGraph : GraphUtils.getGraphsFromFile(parsedDir+testDir+testFile)) {
			for (Graph trainGraph : GraphUtils.getGraphsFromFile(parsedDir+trainDir+trainFile)) {
				TextPair tp = getTextPair(testGraph, trainGraph);

				int index = getIndexToInsertTextPair(tp, textPairs, n);
				if(index != -1) {
					textPairs.add(index, tp);
					n = textPairs.size();
					if(n> 30) {
						textPairs.remove(n-1);
					}
					n = textPairs.size();
				}
			}
		}

		return textPairs;
	}


	private static TextPair getTextPair( Graph testGraph, Graph trainGraph) {
		double similar = 0;
		for(Node g1 : testGraph.getNodes()) {
			for (Node g2: trainGraph.getNodes()) {
				similar += getSim(g1.getAttributes(), g2.getAttributes());
			}
		}

		TextPair tp = new TextPair(testGraph.getFilename(), trainGraph.getFilename(), testGraph.toSentence(), trainGraph.toSentence());
		tp.setSimilarity(similar / (testGraph.getSize() + trainGraph.getSize()));
		return tp;
	}

	private static int getIndexToInsertTextPair(TextPair tp, List<TextPair> textPairs, int n) {
		if(n == 0) {
			return 0;
		}
		if(tp.getSimilarity() > textPairs.get(n-1).getSimilarity() && n > 30) {
			return -1;
		}

		for (int i = n-1; i >= 0; i--) {
			if(tp.getSimilarity() > textPairs.get(i).getSimilarity()) {
				return i+1;
			}
		}

		return 0;
	}

	private static double getSim(List<String> attr1, List<String> attr2) {
		double sim = 0;
		for (int i = 0; i <attr1.size(); i++) {
			if(attr1.get(i).equalsIgnoreCase(attr2.get(i))) {
				sim++;
			}
		}

		return sim / attr1.size(); 
	}
	//	private static boolean isSimilar(NLPSentence querySentence, NLPSentence sentence) {
	//		Compare cmp = new Compare(querySentence.getText(), sentence.getText());
	//		return cmp.getResult() > 0.06;
	//	}

	public static List<NLPSentence> getSentences(String file) {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader reader = null;
		try {
			String filename = Paths.get(file).getFileName().toString();
			fstream = new FileInputStream(file);
			in = new DataInputStream(fstream);
			reader = new BufferedReader(new InputStreamReader(in));

			StringBuilder wordBuilder = new StringBuilder();
			StringBuilder sentenceBuilder = new StringBuilder();
			List<NLPSentence> sentences = new ArrayList<NLPSentence>();
			List<WordLemmaTag> words = new ArrayList<>();

			int character = 1, offset = 0, sentenceNumber = 1, sentenceStart = 0;
			while((character = reader.read()) != -1) {
				char c = (char) character;

				sentenceBuilder = stripWhitespaceBeforeText(sentenceBuilder, offset);

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


				offset++;
				if(isSentenceDelimiter(c)) {
					String previousWord;
					if(words.size()== 0) {
						previousWord = "placeholder";
					}else {
						previousWord = words.get(words.size()-1).word();
					}
					if((sentenceBuilder.toString().trim().length() > 1) && !isWordWithPunctation(previousWord)) {
						int sentenceLength = offset - sentenceStart;
						sentences.add(new NLPSentence(filename, sentenceNumber, sentenceStart, sentenceLength, sentenceBuilder.toString(), words));
					}
					createWord(wordBuilder, words, offset);
					words = new ArrayList<WordLemmaTag>();
					sentenceBuilder = new StringBuilder();
					wordBuilder = new StringBuilder();
					sentenceNumber++;
					sentenceStart = offset +1;
				}

			}
			if(wordBuilder.toString().trim().length()>0) {
				words.add(new WordLemmaTag(wordBuilder.toString()));
				int sentenceLength = offset - sentenceStart;
				sentences.add(new NLPSentence(filename, sentenceNumber, offset, sentenceLength, sentenceBuilder.toString(), words));
				sentenceNumber++;
			}

			return sentences;
		}catch (IOException e) {
			e.printStackTrace();
		}finally {
			IOUtils.closeQuietly(reader);
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(fstream);
		}
		return null;
	}

	private static StringBuilder stripWhitespaceBeforeText(StringBuilder sentenceBuilder, int currentOffset) {
		if(sentenceBuilder.toString().length() > 0) {
			if(sentenceBuilder.toString().trim().length() == 0) {
				return new StringBuilder();
			}
		}
		return sentenceBuilder;
	}

	private static StringBuilder createWord(StringBuilder wordBuilder, List<WordLemmaTag> words, int offset) {
		if(wordBuilder.toString().trim().length() > 0) {
			//TODO: is offset/length really needed for each word? In such case, wordlemmatag is not the right class
			//			words.add(new Word(wordBuilder.toString().trim(), offset-wordBuilder.length(), offset));
			words.add(new WordLemmaTag(wordBuilder.toString().trim()));
			return new StringBuilder();
		}

		return wordBuilder;
	}



	private static boolean isNewLine(char c) {
		return (c == '\n' || c == '\r');
	}

	private static boolean isSentenceDelimiter(char c) {
		return c == '.' || c == '!' || c == '?';
	}

	private static boolean isWordDelimiter(char c ) {
		return !Character.isLetter(c) && !Character.isDigit(c) || (c == 10) || (c == 13);
	}

	//	private static boolean isPartOfWord(char c) {
	//		return Character.isLetter(c) || Character.isDigit(c);
	//	}

	private static boolean isWordWithPunctation(String s) {
		return s.equalsIgnoreCase("Mr") || s.equalsIgnoreCase("Mrs") || s.equalsIgnoreCase("ca");
	}


	public static boolean isAlmostEqual(int a, int b) {
		return Math.abs(a-b)< 10;
	}
}
