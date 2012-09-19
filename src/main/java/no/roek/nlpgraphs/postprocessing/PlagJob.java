package no.roek.nlpgraphs.postprocessing;

public class PlagJob {

	private String testFile;
	private String[] similarDocuments;
	
	public PlagJob(String testFile, String[] similarDocuments) {
		this.testFile = testFile;
		this.similarDocuments = similarDocuments;
	}

	public String getTestFile() {
		return testFile;
	}

	public String[] getSimilarDocuments() {
		return similarDocuments;
	}
}
