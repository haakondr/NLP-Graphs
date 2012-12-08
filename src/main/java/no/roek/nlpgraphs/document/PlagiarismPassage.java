package no.roek.nlpgraphs.document;

import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.GraphUtils;

public class PlagiarismPassage {

	private String sourceFile, suspiciousFile;
	private int sourceSentence, suspiciousSentence;
	private double similarity;
	
	public PlagiarismPassage(String sourceFile, int sourceSentence, String suspiciousFile, int suspiciousSentence, double similarity) {
		this(sourceFile, sourceSentence, suspiciousFile, suspiciousSentence);
		this.similarity = similarity;
	}
	
	public PlagiarismPassage(String sourceFile, int sourceSentence, String suspiciousFile, int suspiciousSentence) {
		this.sourceFile = sourceFile;
		this.sourceSentence = sourceSentence;
		this.suspiciousFile = suspiciousFile;
		this.suspiciousSentence = suspiciousSentence;
	}

	public String getTrainFile() {
		return sourceFile;
	}

	public void setTrainFile(String trainFile) {
		this.sourceFile = trainFile;
	}

	public String getTestFile() {
		return suspiciousFile;
	}

	public void setTestFile(String testFile) {
		this.suspiciousFile = testFile;
	}

	public int getTrainSentence() {
		return sourceSentence;
	}

	public void setTrainSentence(int trainSentence) {
		this.sourceSentence = trainSentence;
	}

	public int getTestSentence() {
		return suspiciousSentence;
	}

	public void setTestSentence(int testSentence) {
		this.suspiciousSentence = testSentence;
	}

	public double getSimilarity() {
		return similarity;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}
}
