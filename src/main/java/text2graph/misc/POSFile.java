package text2graph.misc;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class POSFile {
	private Path relPath;
	private List<String[]> sentences;
	private boolean isLastInQueue;
//	private File file;
	private Path path;
	
//	public POSFile(File file, String baseDir) {
//		this.file = file;
//		this.setPath(FileSystems.getDefault().getPath(baseDir, file.getName()));
//		this.relPath = new File(baseDir).toURI().relativize(file.toURI()).getPath();
//		this.sentences = new ArrayList<>();
//	}
	
	public POSFile(Path path, Path basePath) {
		this.path = path;
		this.sentences = new ArrayList<>();
		this.relPath = basePath.relativize(path);
	}
	
//	public String getFilename() {
//		return file.getName();
//	}
	
//	public String getPath() {
//		return file.getPath();
//	}
	
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
