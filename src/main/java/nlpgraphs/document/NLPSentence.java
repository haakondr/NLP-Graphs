package nlpgraphs.document;

import java.util.List;

import edu.stanford.nlp.ling.Word;

public class NLPSentence {
	
	private int number, offset;
	private String text;
	private String[] postags;
	private List<Word> words;
	
	public NLPSentence(int number, int offset, String text, String[] postags) {
		this(number, offset, text);
		this.postags = postags;
	}
	
	public NLPSentence(int number, int offset, String text, List<Word> words) {
		this(number, offset, text);
		this.words = words;
	}
	
	public NLPSentence(int number, int offset, String text) {
		this.number = number;
		this.offset = offset;
		this.text = text;
	}
	
	public int getLength() {
		return text.length();
	}

	public int getNumber() {
		return number;
	}

	public int getOffset() {
		return offset;
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