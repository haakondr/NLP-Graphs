package no.roek.nlpgraphs.document;

import java.util.List;

import edu.stanford.nlp.ling.Word;

public class NLPSentence {
	
	private int number, start;
	private String text;
	private String[] postags;
	private List<Word> words;
	
	public NLPSentence(int number, int start, String text, String[] postags) {
		this(number, start, text);
		this.postags = postags;
	}
	
	public NLPSentence(int number, int start, String text, List<Word> words) {
		this(number, start, text);
		this.words = words;
	}
	
	public NLPSentence(int number, int start, String text) {
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
}
