package text2graph.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


public class POSFile {
	private String relPath;
	private List<String[]> sentences;
	private boolean isLastInQueue;
	private File file;
	
	public POSFile(File file, String baseDir) {
		this.file = file;
		this.relPath = new File(baseDir).toURI().relativize(file.toURI()).getPath();
		this.sentences = new ArrayList<>();
	}
	
	public String getFilename() {
		return file.getName();
	}
	
	public String getPath() {
		return file.getPath();
	}
	
	public void addSentence(String[] tokens) {
		sentences.add(tokens);
	}

	public List<String[]> getSentences() {
		return sentences;
	}

	public String getRelPath() {
		return relPath;
	}

	public boolean isLastInQueue() {
		return isLastInQueue;
	}
	
	public void setLastInQueue(boolean isLast) {
		this.isLastInQueue = isLast;
	}
}
