package text2graph.postagParser;

import java.util.ArrayList;
import java.util.List;

import text2graph.dependencyParser.PlagFile;


public class POSFile {
	private String relPath;
	private List<String[]> sentences;
	
	public POSFile(PlagFile file) {
		this.relPath = file.getRelPath();
		this.sentences = new ArrayList<>();
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
}
