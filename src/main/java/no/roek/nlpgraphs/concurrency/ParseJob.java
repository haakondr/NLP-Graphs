package no.roek.nlpgraphs.concurrency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;

public class ParseJob extends Job {

	private List<NLPSentence> sentences;
	
	
	public ParseJob(Path file) {
		super(file);
		this.sentences = new ArrayList<>();
	}
	
	public ParseJob(String file) {
		this(Paths.get(file));
	}

	public List<NLPSentence> getSentences() {
		return sentences;
	}
	
	public void addSentence(NLPSentence sentence) {
		sentences.add(sentence);
	}
}
