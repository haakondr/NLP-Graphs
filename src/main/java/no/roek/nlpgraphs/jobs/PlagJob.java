package no.roek.nlpgraphs.jobs;

import java.nio.file.Path;

public class PlagJob {

	private String file;
	private String[] similarDocuments;
	
	public PlagJob(String file, String[] similarDocuments) {
		this.file = file;
		this.similarDocuments = similarDocuments;
	}
	
	public PlagJob(Path testFile, String[] similarDocuments) {
		this(testFile.toString(), similarDocuments);
	}

	public String getFile() {
		return file;
	}

	public String[] getSimilarDocuments() {
		return similarDocuments;
	}
}
