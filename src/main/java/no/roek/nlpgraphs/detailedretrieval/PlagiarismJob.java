package no.roek.nlpgraphs.detailedretrieval;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonObject;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;

import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.misc.Job;

public class PlagiarismJob extends Job {

	private List<PlagiarismPassage> textPairs;


	public PlagiarismJob(Path file) {
		super(file);
		this.textPairs = new ArrayList<>();
	}

	public PlagiarismJob(String filename) {
		this(Paths.get(filename));
	}

	public List<PlagiarismPassage> getTextPairs() {
		return textPairs;
	}

	public void setTextPairs(List<PlagiarismPassage> textPairs) {
		this.textPairs = textPairs;
	}

	public void addTextPair(PlagiarismPassage textPair) {
		this.textPairs.add(textPair);
	}

	public String toJson() {
		JSONObject json = new JSONObject();
		JSONArray passages = new JSONArray();
		try {
			for(PlagiarismPassage passage : textPairs) {
				JSONObject jsonPassage = new JSONObject();
				jsonPassage.put("testFile", passage.getTestFile());
				jsonPassage.put("trainFile", passage.getTrainFile());
				jsonPassage.put("testSentence", passage.getTestSentence());
				jsonPassage.put("trainSentence", passage.getTrainSentence());
				jsonPassage.put("candretScore", passage.getSimilarity());
				passages.put(jsonPassage);
			}

			json.put("passages", passages);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		return json.toString();
	}

	public BasicDBList toDBObject() {
		BasicDBList passages = new BasicDBList();
		for(PlagiarismPassage passage : textPairs) {
			BasicDBObject p = new BasicDBObject();
			p.put("testFile", passage.getTestFile());
			p.put("trainFile", passage.getTrainFile());
			p.put("testSentence", passage.getTestSentence());
			p.put("trainSentence", passage.getTrainSentence());
			p.put("candretScore", passage.getSimilarity());
			passages.add(p);
		}

		return passages;
	}
}
