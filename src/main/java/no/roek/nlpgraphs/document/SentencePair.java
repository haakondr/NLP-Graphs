package no.roek.nlpgraphs.document;

import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.GraphUtils;

public class SentencePair {

	private String trainFile, testFile;
	private int trainSentence, testSentence;
	private float similarity;
	private Graph testGraph, trainGraph;
	private String dir, testDir, trainDir;
	
	public SentencePair(ConfigService cs, String trainFile, int trainSentence, String testFile, int testSentence, float similarity) {
		this.dir = cs.getParsedFilesDir();
		this.testDir = cs.getTestDir();
		this.trainDir = cs.getTrainDir();
		this.trainFile = trainFile;
		this.trainSentence = trainSentence;
		this.testFile = testFile;
		this.testSentence = testSentence;
		this.similarity = similarity;
	}

	public Graph getTrainGraph() {
		if(trainGraph == null) {
			trainGraph = GraphUtils.getGraphFromFile(dir+trainDir+trainFile, trainSentence);
		}
		return trainGraph;
	}
	
	public Graph getTestGraph() {
		if(testGraph == null) {
			testGraph = GraphUtils.getGraphFromFile(dir+testDir+testFile, testSentence);
		}
		
		return testGraph;
	}
	
	public String getTrainFile() {
		return trainFile;
	}

	public void setTrainFile(String trainFile) {
		this.trainFile = trainFile;
	}

	public String getTestFile() {
		return testFile;
	}

	public void setTestFile(String testFile) {
		this.testFile = testFile;
	}

	public int getTrainSentence() {
		return trainSentence;
	}

	public void setTrainSentence(int trainSentence) {
		this.trainSentence = trainSentence;
	}

	public int getTestSentence() {
		return testSentence;
	}

	public void setTestSentence(int testSentence) {
		this.testSentence = testSentence;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}
}
