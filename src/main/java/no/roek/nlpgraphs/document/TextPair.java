package no.roek.nlpgraphs.document;

public class TextPair {
	
	private String testFile, trainFile;
	private NLPSentence testSentence, trainSentence;
	
	public TextPair(String testFile, String trainFile) {
		this.testFile = testFile;
		this.trainFile = trainFile;
	}
	
	public TextPair(String testFile, String trainFile, NLPSentence testSentence, NLPSentence trainSentence) {
		this(testFile, trainFile);
		this.testSentence = testSentence;
		this.trainSentence = trainSentence;
	}

	public String getTestFile() {
		return testFile;
	}

	public void setTestFile(String testFile) {
		this.testFile = testFile;
	}

	public String getTrainFile() {
		return trainFile;
	}

	public void setTrainFile(String trainFile) {
		this.trainFile = trainFile;
	}

	public NLPSentence getTestSentence() {
		return testSentence;
	}

	public void setTestSentence(NLPSentence testSentence) {
		this.testSentence = testSentence;
	}

	public NLPSentence getTrainSentence() {
		return trainSentence;
	}

	public void setTrainSentence(NLPSentence trainSentence) {
		this.trainSentence = trainSentence;
	}

}
