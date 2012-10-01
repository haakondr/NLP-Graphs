package no.roek.nlpgraphs.concurrency;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.GraphPair;
import no.roek.nlpgraphs.document.TextPair;

public class PlagiarismJob extends Job {

	private String[] similarDocs;
	private List<TextPair> textPairs;
	private List<GraphPair> graphPairs;

	
	public PlagiarismJob(Path file) {
		super(file);
		this.textPairs = new ArrayList<>();
		this.graphPairs = new ArrayList<>();
	}
	
	public PlagiarismJob(String filename) {
		this(Paths.get(filename));
	}
	
	
	public PlagiarismJob(String filename, String[] similarDocs) {
		this(filename);
		this.similarDocs = similarDocs;
	}
	
	public String[] getSimilarDocuments() {
		return similarDocs;
	}

	public void setSimilarDocuments(String[] similarDocs) {
		this.similarDocs = similarDocs;
	}


	public List<TextPair> getTextPairs() {
		return textPairs;
	}


	public void setTextPairs(List<TextPair> textPairs) {
		this.textPairs = textPairs;
	}
	
	public void addAllTextPairs(List<TextPair> textPairs) {
		this.textPairs.addAll(textPairs);
	}
	
	public void addTextPair(TextPair textPair) {
		this.textPairs.add(textPair);
	}

	public List<GraphPair> getGraphPairs() {
		return graphPairs;
	}

	public void setGraphPairs(List<GraphPair> graphPairs) {
		this.graphPairs = graphPairs;
	}
	
	public void addGraphPair(GraphPair graphPair) {
		this.graphPairs.add(graphPair);
	}
}
