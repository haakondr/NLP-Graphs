package no.roek.nlpgraphs.document;

import edu.stanford.nlp.ling.Word;

public class NLPToken extends Word{

	private String pos;
	
	public NLPToken(String word) {
		super(word);
	}
	
	public NLPToken(String word, String pos) {
		this(word);
		this.pos = pos;
	}
	
	public String getWord() {
		return super.word();
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}
}
