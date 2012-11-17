package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;

import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;


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

		} catch (JSONException e) {
			e.printStackTrace();
		}

		Fileutils.writeToFile(outDir+job.getParentDir()+job.getFilename(), out.toString());
	}
}
