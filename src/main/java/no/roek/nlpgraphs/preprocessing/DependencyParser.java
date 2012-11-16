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
		try {
			File dir = new File(outDir+job.getParentDir()+job.getFilename());
			if(!dir.exists()) {
				dir.mkdir();
			}
			
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
				jsonSentence.put("filename", job.getFilename());
				Fileutils.writeToFile(outDir+job.getParentDir()+job.getFilename()+"/"+sentence.getNumber(), jsonSentence.toString());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
