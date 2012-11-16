package no.roek.nlpgraphs.detailedretrieval;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

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
}
