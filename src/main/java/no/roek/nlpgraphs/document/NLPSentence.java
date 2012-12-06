package no.roek.nlpgraphs.document;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import no.roek.nlpgraphs.detailed.analysis.PlagiarismReference;
import no.roek.nlpgraphs.misc.SentenceUtils;

import org.json.JSONException;
import org.json.JSONObject;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

import edu.stanford.nlp.ling.WordLemmaTag;

public class NLPSentence {

	protected int number, start, length;
	protected String filename, text;
	private String[] postags;
	protected List<WordToken> words;

	public NLPSentence(String filename, int number, int start, int length, List<WordToken> words, String[] postags) {
		this(filename, number, start, length);
		this.postags = postags;
	}

	public NLPSentence(String filename, int number, int start, int length, List<WordToken> words) {
		this(filename, number, start, length);
		this.words = words;
	}

	public NLPSentence(String filename, int number, int start, int length) {
		this.filename = filename;
		this.number = number;
		this.start = start;
		this.length = length;
	}

	public int getLength() {
		return length;
	}

	public int getNumber() {
		return number;
	}

	public int getStart() {
		return start;
	}

	//	public String getText() {
	//		return text;
	//	}


	public String[] getPostags() {
		return postags;
	}

	public void setPostags(String[] postags) {
		this.postags = postags;
	}

	public List<WordToken> getWords() {
		return words;
	}

	public void addWord(WordToken word) {
		words.add(word);
	}

	public String getLemmas() {
		//TODO: this string should probably be created when object is created. 
		StringBuilder sb = new StringBuilder();
		for (WordToken word : words) {
			sb.append(word.getLemma());
			sb.append(" ");
		}

		return sb.toString();
	}

//	public String getLemmasAndSynonyms() {
//		//TODO: this string should probably be created when object is created.
//		StringBuilder sb = new StringBuilder();
//		for (WordToken word : words) {
//			sb.append(word.getLemma()+" ");
//			for (String synonym: word.getSynonyms()) {
//				sb.append(synonym+" ");
//			}
//		}
//		return sb.toString();
//	}
//
	public String getFilename() {
		return filename;
	}

	public String getRelativePath() {
		Path file = Paths.get(filename);
		String outfilename = file.getFileName().toString().replace(".txt", "");
		String parentDir = file.getParent().getFileName().toString();
		return parentDir+"/"+outfilename+"/"+outfilename+"_"+number;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public JSONObject toJson() throws JSONException {
		JSONObject jsonSentence = new JSONObject();
		jsonSentence.put("sentenceNumber", number);
		//		jsonSentence.put("originalText", text);
		jsonSentence.put("offset", start);
		jsonSentence.put("length", getLength());

		return jsonSentence;
	}

	public boolean matchesPlagSourceRef(PlagiarismReference ref) {
		int refLen = Integer.parseInt(ref.getSourceLength());
		int refOffset = Integer.parseInt(ref.getSourceOffset());

		return SentenceUtils.isAlmostEqual(refOffset, start) && SentenceUtils.isAlmostEqual(refLen, length) && ref.getSourceReference().equals(getFilename());
	}

	public boolean matchesPlagSuspiciousRef(PlagiarismReference ref) {
		int refLen = Integer.parseInt(ref.getLength());
		int refOffset = Integer.parseInt(ref.getOffset());

		return SentenceUtils.isAlmostEqual(refOffset, start) && SentenceUtils.isAlmostEqual(refLen, length) && ref.getFilename().equals(getFilename());
	}
}
