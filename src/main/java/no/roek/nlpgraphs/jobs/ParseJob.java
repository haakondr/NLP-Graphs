package no.roek.nlpgraphs.jobs;

import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.TextPair;

public class ParseJob  extends Job{

	private String testFile, trainFile;
	private List<TextPair> textPairs;
	
	public ParseJob(String file) {
		super(file);
		this.textPairs = new ArrayList<>();
	}
	
	public String getTestFile() {
		return testFile;
	}

	public String getTrainFile() {
		return trainFile;
	}

	public List<TextPair> getTextPairs() {
		return textPairs;
	}

	public void addTextPair(TextPair textPair) {
		textPairs.add(textPair);
	}
	
}
