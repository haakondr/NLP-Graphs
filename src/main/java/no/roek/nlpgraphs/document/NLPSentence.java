package no.roek.nlpgraphs.document;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;


import edu.stanford.nlp.ling.Word;

public class NLPSentence {
	
	private int number, start;
	private String text, filename;
	private String[] postags;
	private List<Word> words;
	
	public NLPSentence(String filename, int number, int start, String text, String[] postags) {
		this(filename, number, start, text);
		this.postags = postags;
	}
	
	public NLPSentence(String filename, int number, int start, String text, List<Word> words) {
		this(filename, number, start, text);
		this.words = words;
	}
	
	public NLPSentence(String filename, int number, int start, String text) {
		this.filename = filename;
		this.number = number;
		this.start = start;
		this.text = text;
	}
	
	public int getLength() {
		return text.length();
	}

	public int getNumber() {
		return number;
	}

	public int getStart() {
		return start;
	}

	public String getText() {
		return text;
	}

	public String toString() {
		return text;
	}

	public String[] getPostags() {
		return postags;
	}
	
	public void setPostags(String[] postags) {
		this.postags = postags;
	}

	public List<Word> getWords() {
		return words;
	}
	
	public void addWord(Word word) {
		words.add(word);
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}
	
	public JSONObject toJson() throws JSONException {
		JSONObject jsonSentence = new JSONObject();
		jsonSentence.put("sentenceNumber", number);
		jsonSentence.put("originalText", text);
		jsonSentence.put("offset", start);
		jsonSentence.put("length", getLength());

		return jsonSentence;
	}
}
