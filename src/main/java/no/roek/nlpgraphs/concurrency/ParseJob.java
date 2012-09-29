package no.roek.nlpgraphs.concurrency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;

public class ParseJob {

	private Path file;
	private boolean isLastInQueue;
	private List<NLPSentence> sentences;
	
	
	public ParseJob(Path file) {
		this.file = file;
		this.sentences = new ArrayList<>();
	}
	
	public ParseJob(String file) {
		this(Paths.get(file));
	}

	public boolean isLastInQueue() {
		return isLastInQueue;
	}

	public void setLastInQueue(boolean isLastInQueue) {
		this.isLastInQueue = isLastInQueue;
	}

	public Path getFile() {
		return file;
	}
	
	public String getFilename() {
		return file.getFileName().toString();
	}

	public List<NLPSentence> getSentences() {
		return sentences;
	}
	
	public void addSentence(NLPSentence sentence) {
		sentences.add(sentence);
	}

	public String getParentDir() {
		return file.getParent().getFileName().toString()+"/";
	}
}
