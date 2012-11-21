package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.WordToken;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;


public class DependencyParser {

	private MaltParserService maltService;

	public DependencyParser() {
		ConfigService cs = new ConfigService();
		try {
			this.maltService = new MaltParserService();
			maltService.initializeParserModel(cs.getMaltParams());
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}

	public void dependencyParse(ParseJob job, String outDir) throws MaltChainedException, NullPointerException {
		JSONObject out = dependencyParse(job);
		Fileutils.writeToFile(outDir+job.getParentDir()+job.getFilename(), out.toString());
	}
	
	public JSONObject dependencyParse(ParseJob job) throws MaltChainedException, NullPointerException {
		JSONObject out = new JSONObject();

		try {
			out.put("filename", job.getFilename());

			JSONObject jsonSentences = new JSONObject();
			for (NLPSentence sentence : job.getSentences()) {
				String[] parsedSentences = maltService.parseTokens(sentence.getPostags());

				JSONObject jsonSentence = sentence.toJson();
				JSONArray jsonTokens = new JSONArray();
				for (String parsedToken : parsedSentences) {
					String[] token = parsedToken.split("\t");

					JSONObject jsonToken = new JSONObject();
					jsonToken.put("id", token[0]);
					jsonToken.put("word", token[1]);
					jsonToken.put("lemma", token[2]);
					jsonToken.put("pos", token[4]);
					jsonToken.put("rel", token[6]);
					jsonToken.put("deprel", token[7]);
					jsonTokens.put(jsonToken);
				}
				jsonSentence.put("tokens", jsonTokens);
				jsonSentences.put(Integer.toString(sentence.getNumber()), jsonSentence);
			}

			out.put("sentences", jsonSentences);

		} catch (JSONException e) {
			e.printStackTrace();
		}

		return out;
	}
	
//	jsonSentence.put("sentenceNumber", number);
//	//		jsonSentence.put("originalText", text);
//	jsonSentence.put("offset", start);
//	jsonSentence.put("length", getLength());
	
	public JsonObject parseSentence(String[] postagString) {
		JsonObject out = new JsonObject();
		try {
			JsonObject jsonSentences = new JsonObject();
			JsonObject jsonSentence = new JsonObject();
			jsonSentence.addProperty("sentenceNumber", 1);
			jsonSentence.addProperty("offset", 0);
			jsonSentence.addProperty("length", postagString.length);
			
			String[] parsedSentence = maltService.parseTokens(postagString);
			
			JsonArray jsonTokens = new JsonArray();
			for (String parsedToken : parsedSentence) {
				String[] token = parsedToken.split("\t");

				JsonObject jsonToken = new JsonObject();
				jsonToken.addProperty("id", token[0]);
				jsonToken.addProperty("word", token[1]);
				jsonToken.addProperty("lemma", token[2]);
				jsonToken.addProperty("pos", token[4]);
				jsonToken.addProperty("rel", token[6]);
				jsonToken.addProperty("deprel", token[7]);
				jsonTokens.add(jsonToken);
			}
			jsonSentence.add("tokens", jsonTokens);
			jsonSentences.add("1", jsonSentence);
			out.add("sentences", jsonSentences);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
		
		return out;
	}
}
