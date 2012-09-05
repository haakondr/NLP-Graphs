package nlpgraphs.misc;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class POSFile {
	private Path relPath;
	private List<String[]> sentences;
	private boolean isLastInQueue;
	private Path path;
	
	public POSFile(Path path, Path basePath) {
		this.path = path;
		this.sentences = new ArrayList<>();
		this.relPath = basePath.relativize(path);
	}
	
	public void addSentence(String[] tokens) {
		sentences.add(tokens);
	}

	public List<String[]> getSentences() {
		return sentences;
	}

	public Path getRelPath() {
		return relPath;
	}

	public boolean isLastInQueue() {
		return isLastInQueue;
	}
	
	public void setLastInQueue(boolean isLast) {
		this.isLastInQueue = isLast;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}
}
