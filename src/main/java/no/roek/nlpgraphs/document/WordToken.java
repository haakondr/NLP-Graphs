package no.roek.nlpgraphs.document;

import com.mongodb.BasicDBObject;

public class WordToken {

	private String word, lemma, pos;
	private String rel, deprel;
	private String[] synonyms;
	public WordToken(String word, String lemma, String pos) {
		this.word = word;
		this.lemma = lemma;
		this.pos = pos;
	}
	
	public WordToken(String word, String lemma, String pos, String rel, String deprel) {
		this(word, lemma, pos);
		this.rel = rel;
		this.deprel = deprel;
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

	public String getRel() {
		return rel;
	}

	public void setRel(String rel) {
		this.rel = rel;
	}

	public String getDeprel() {
		return deprel;
	}

	public void setDeprel(String deprel) {
		this.deprel = deprel;
	}
	
	public BasicDBObject toDBObject() {
		BasicDBObject obj = new BasicDBObject();
		obj.put("word", word);
		obj.put("lemma", lemma);
		obj.put("pos", pos);
		obj.put("rel", rel);
		obj.put("deprel", deprel);
		
		return obj;
	}
}
