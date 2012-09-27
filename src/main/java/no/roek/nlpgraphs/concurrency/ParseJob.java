package no.roek.nlpgraphs.concurrency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;

public class ParseJob {

	private Path file;
	private boolean isLastInQueue;
	private NLPSentence sentence;
	
	
	public ParseJob(Path file) {
		this.file = file;
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

	public NLPSentence getSentence() {
		return sentence;
	}

	public void setSentence(NLPSentence sentence) {
		this.sentence = sentence;
	}
	
	public String getParsedFilename() {
		String outfilename = file.getFileName().toString().replace(".txt", "");
		return outfilename+"/"+outfilename+"_"+sentence.getNumber();
	}
	
	public String getParentDir() {
		return file.getParent().getFileName().toString()+"/";
	}
}
