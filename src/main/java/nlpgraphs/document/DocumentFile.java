package nlpgraphs.document;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import nlpgraphs.misc.Fileutils;


public class DocumentFile {
	private Path relPath;
//	private List<String[]> sentences;
	private List<NLPSentence> sentences;
	private boolean isLastInQueue;
	private Path path;
	private String originalText;

	public DocumentFile(Path path, Path basePath) {
		this.path = path;
		this.sentences = new ArrayList<>();
		this.relPath = basePath.relativize(path);
		this.originalText = Fileutils.getText(path);
	}

//	public void addSentence(String[] tokens) {
//		sentences.add(tokens);
//	}
//
//	public List<String[]> getSentences() {
//		return sentences;
//	}
	
	public void setSentences(List<NLPSentence> sentences) {
		this.sentences = sentences;
	}
	
	public void addSentence(NLPSentence sentence) {
		sentences.add(sentence);
	}
	
	public List<NLPSentence> getSentences() {
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

	public String getOriginalText() {
		return originalText;
	}
}