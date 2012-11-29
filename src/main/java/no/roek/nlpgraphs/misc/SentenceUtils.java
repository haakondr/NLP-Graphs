package no.roek.nlpgraphs.misc;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

		try {
			jsonReader = new JsonReader(new InputStreamReader(new FileInputStream(filename)));
			JsonParser jsonParser = new JsonParser();

			JsonObject fileObject = jsonParser.parse(jsonReader).getAsJsonObject();
			JsonObject jsonSentences = fileObject.get("sentences").getAsJsonObject();
			for(Map.Entry<String,JsonElement> entry : jsonSentences.entrySet()) {
				JsonObject sentence = entry.getValue().getAsJsonObject();
				sentences.add(getSentence(sentence, fileObject.get("filename").getAsString()));
			}
			
			
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

		return sentences;
	}

	public static NLPSentence getSentence(JsonObject jsonSentence, String filename) {
		int number = jsonSentence.get("sentenceNumber").getAsInt();
		int offset = jsonSentence.get("offset").getAsInt();
		int length = jsonSentence.get("length").getAsInt();

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
