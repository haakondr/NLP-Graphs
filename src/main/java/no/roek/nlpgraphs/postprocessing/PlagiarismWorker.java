package no.roek.nlpgraphs.postprocessing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import no.roek.nlpgraphs.algorithm.GraphEditDistance;
import no.roek.nlpgraphs.document.GraphPair;
import no.roek.nlpgraphs.document.PlagiarismReference;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;

public class PlagiarismWorker extends Thread {

	private File[] testFiles;
	private String parsedData, trainDir, testDir, originalDir, resultsDir;
	private int documentRecall, plagiarismThreshold;

	public PlagiarismWorker(File[] testFiles) {
		this.testFiles = testFiles;
		this.originalDir = ConfigService.getDataDir();
		this.parsedData = ConfigService.getParsedFilesDir();
		this.trainDir = ConfigService.getTrainDir();
		this.testDir = ConfigService.getTestDir();
		this.resultsDir = ConfigService.getResultsDir();
		this.documentRecall = ConfigService.getDocumentRecall();
		this.plagiarismThreshold = ConfigService.getPlagiarismThreshold();
	}

	@Override
	public void run() {
		for (File testFile : testFiles) {
			List<PlagiarismReference> plagiarisms = findPlagiarism(testFile);
			writeResults(testFile, plagiarisms);
		}

		System.out.println(Thread.currentThread().getName() + " done finding plagiarism in "+ testFiles.length+" files.");
	}


	public List<PlagiarismReference> findPlagiarism(File file) {
		List<PlagiarismReference> references = new ArrayList<>();

		List<GraphPair> similarGraphPairs = findSimilarSentencePairs(parsedData+trainDir, parsedData+testDir);
		
		for (GraphPair graphPair : similarGraphPairs) {
			references.add(getPlagiarismReference(graphPair.getSuspiciousGraph(), graphPair.getSourceGraph()));
		}

		return references;
	}

	public List<GraphPair> findSimilarSentencePairs(String trainDir, String testDir) {
		List<GraphPair> graphPairs = new ArrayList<>();
		File[] trainFiles  = Fileutils.getFiles(Paths.get(trainDir));

		for (File testFile : Fileutils.getFiles(Paths.get(testDir))) {
			for (Graph sentence : GraphUtils.getGraphs(testFile.toString())) {
				for (Graph similarSentence : getSimilarSentences(sentence, trainFiles)) {
					graphPairs.add(new GraphPair(sentence, similarSentence));
				}
			}
		}
		
		return graphPairs;
	}

	public List<Graph> getSimilarSentences(Graph testSentence, File[] trainFiles) {
		List<Graph> simGraphs = new ArrayList<>();
		
		for (File file : trainFiles) {
			for(Graph otherGraph : GraphUtils.getGraphs(file.toString())) {
				GraphEditDistance ged = new GraphEditDistance(testSentence, otherGraph);
				if(ged.getDistance() < plagiarismThreshold) {
					simGraphs.add(otherGraph);
				}
			}
		}
		
		return simGraphs;
	}

	public PlagiarismReference getPlagiarismReference(Graph test, Graph train) {
		String offset = Integer.toString(test.getOffset());
		String length = Integer.toString(test.getLength());
		String sourceReference = train.getFilename();
		String sourceOffset = Integer.toString(train.getOffset());
		String sourceLength = Integer.toString(train.getLength());
		return new PlagiarismReference(offset, length, sourceReference, sourceOffset, sourceLength);
	}

	public void writeResults(File file, List<PlagiarismReference> plagiarisms) {
		Element root = new Element("document");
		root.setAttribute("reference", file.getName());
		for (PlagiarismReference plagiarismReference : plagiarisms) {
			Element reference = new Element("feature");
			reference.setAttribute("name", "detected-plagiarism");
			reference.setAttribute("this_offset", plagiarismReference.getOffset());
			reference.setAttribute("this_length", plagiarismReference.getLength());
			reference.setAttribute("source_reference", plagiarismReference.getSourceReference());
			reference.setAttribute("source_offset", plagiarismReference.getSourceOffset());
			reference.setAttribute("source_length", plagiarismReference.getSourceLength());
			root.addContent(reference);
		}

		Document doc = new Document();
		doc.setContent(root);

		XMLOutputter outputter = new XMLOutputter();
		try {
			FileWriter writer = new FileWriter(resultsDir+file.getName());
			outputter.output(doc, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
