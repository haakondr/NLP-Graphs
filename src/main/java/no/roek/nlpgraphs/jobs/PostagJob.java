package no.roek.nlpgraphs.jobs;

import java.nio.file.Path;
import java.util.List;

import no.roek.nlpgraphs.document.TextPair;

public class PostagJob extends Job {

	private List<TextPair> textPairs;
	
	public PostagJob(Path file) {
		super(file);
	}
	
	public PostagJob(String filename) {
		super(filename);
	}
	
	public PostagJob(String filename, List<TextPair> textPairs) {
		super(filename);
		this.textPairs =  textPairs;
	}

	public List<TextPair> getTextPairs() {
		return textPairs;
	}

	public void setTextPairs(List<TextPair> textPairs) {
		this.textPairs = textPairs;
	}
	
	public void addTextPair(TextPair pair) {
		textPairs.add(pair);
	}
	
	public void addAllTextPairs(List<TextPair> pairs) {
		textPairs.addAll(pairs);
	}
}
