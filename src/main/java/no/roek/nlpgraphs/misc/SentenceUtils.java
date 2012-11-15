package no.roek.nlpgraphs.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.WordToken;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

public class SentenceUtils {

	public static List<NLPSentence> getSentencesFromParsedFile(String filename) {
		List<NLPSentence> sentences = new ArrayList<>();
		JsonReader jsonReader = null;
		JsonParser jsonParser = new JsonParser();
		
		for(File sentenceFile : Fileutils.getFiles(filename+"/")) {
			try {
				jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(sentenceFile)));
				
				JsonObject jsonSentence = jsonParser.parse(jsonReader).getAsJsonObject();
				sentences.add(getSentence(jsonSentence));
			} catch (IOException  e) {
				e.printStackTrace();
				return null;
			} finally {
				try {
					jsonReader.close();
				} catch(IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return sentences;
	}
	
	public static NLPSentence getSentence(JsonObject jsonSentence) {
		int number = jsonSentence.get("sentenceNumber").getAsInt();
		int offset = jsonSentence.get("offset").getAsInt();
		int length = jsonSentence.get("length").getAsInt();
		String filename = jsonSentence.get("filename").getAsString();
//		String text = jsonSentence.get("originalText").getAsString();
		
		List<WordToken> tokens = new ArrayList<>();
		for(JsonElement jsonToken : jsonSentence.get("tokens").getAsJsonArray()) {
			tokens.add(getToken(jsonToken.getAsJsonObject()));
		}

		return new NLPSentence(filename, number, offset, length, tokens);
	}
	
	public static WordToken getToken(JsonObject jsonToken) {
		String word = jsonToken.get("word").getAsString();
		String lemma = jsonToken.get("lemma").getAsString();
		String pos = jsonToken.get("pos").getAsString();
		return new WordToken(word, lemma, pos);
	}

	public static boolean isAlmostEqual(int a, int b) {
		return Math.abs(a-b)< 10;
	}
}
