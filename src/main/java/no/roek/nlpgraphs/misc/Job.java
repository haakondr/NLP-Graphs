package no.roek.nlpgraphs.misc;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Job {

	private Path file;
	private boolean isLastInQueue;

	
	public Job(Path file) {
		this.file = file;
	}
	
	public Job(String filename) {
		this(Paths.get(filename));
	}
	
	
	public String getFilename() {
		return file.getFileName().toString();
	}
	
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
	
	public String getParentDir() {
		return file.getParent().getFileName().toString()+"/";
	}
}
