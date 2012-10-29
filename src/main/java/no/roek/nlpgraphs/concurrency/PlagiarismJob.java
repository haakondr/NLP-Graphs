package no.roek.nlpgraphs.concurrency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.GraphPair;
import no.roek.nlpgraphs.document.SentencePair;
import no.roek.nlpgraphs.document.TextPair;

public class PlagiarismJob extends Job {

	private List<SentencePair> textPairs;

	
	public PlagiarismJob(Path file) {
		super(file);
		this.textPairs = new ArrayList<>();
	}
	
	public PlagiarismJob(String filename) {
		this(Paths.get(filename));
	}
	
	public List<SentencePair> getTextPairs() {
		return textPairs;
	}

	public void setTextPairs(List<SentencePair> textPairs) {
		this.textPairs = textPairs;
	}
	
	public void addTextPair(SentencePair textPair) {
		this.textPairs.add(textPair);
	}
}
