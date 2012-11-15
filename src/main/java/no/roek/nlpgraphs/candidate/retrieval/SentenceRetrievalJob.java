package no.roek.nlpgraphs.candidate.retrieval;

import java.nio.file.Path;
import java.nio.file.Paths;

import no.roek.nlpgraphs.misc.Job;

public class SentenceRetrievalJob extends Job {

	private String[] similarDocs;
	
	public SentenceRetrievalJob(Path file) {
		super(file);
	}
	
	public SentenceRetrievalJob(String file) {
		this(Paths.get(file));
	}
	
	public SentenceRetrievalJob(String file, String[] simDocs) {
		this(file);
		this.similarDocs = simDocs;
	}

	public String[] getSimilarDocs() {
		return similarDocs;
	}

	public void setSimilarDocs(String[] similarDocs) {
		this.similarDocs = similarDocs;
	}
}
