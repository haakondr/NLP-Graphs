package no.roek.nlpgraphs.jobs;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Job {

	private Path file;
	private boolean isLastInQueue;
//	private String[] similarDocs;
	
	public Job(String filename) {
		this.file = Paths.get(filename);
	}
	
	public Job(Path file) {
		this.file = file;
	}
	
//	public Job(String filename, String[] similarDocs) {
//		this(filename);
//		this.similarDocs = similarDocs;
//	}
	
//	public Job(Path file, String[] similarDocs) {
//		this(file);
//		this.similarDocs = similarDocs;
//	}

	public Path getFile() {
		return file;
	}

	public void setFile(Path file) {
		this.file = file;
	}

	public boolean isLastInQueue() {
		return isLastInQueue;
	}

	public void setLastInQueue(boolean isLast) {
		this.isLastInQueue = isLast;
	}

//	public String[] getSimilarDocs() {
//		return similarDocs;
//	}
//
//	public void setSimilarDocs(String[] similarDocs) {
//		this.similarDocs = similarDocs;
//	}
	
}
