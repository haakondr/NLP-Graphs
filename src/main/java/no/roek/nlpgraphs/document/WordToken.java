package no.roek.nlpgraphs.document;

public class WordToken {

	private String word, lemma, pos;
	private String[] synonyms;
	public WordToken(String word, String lemma, String pos) {
		this.word = word;
		this.lemma = lemma;
		this.pos = pos;
	}
	
	public WordToken(String word, String lemma, String pos, String[] synonyms) {
		this(word, lemma, pos);
		this.synonyms = synonyms;
	}

	public String getWord() {
		return word;
	}

	public void setWord(String word) {
		this.word = word;
	}

	public String getLemma() {
		return lemma;
	}

	public void setLemma(String lemma) {
		this.lemma = lemma;
	}

	public String getPos() {
		return pos;
	}

	public void setPos(String pos) {
		this.pos = pos;
	}

	public String[] getSynonyms() {
		return synonyms;
	}

	public void setSynonyms(String[] synonyms) {
		this.synonyms = synonyms;
	}
}
