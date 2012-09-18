package no.roek.nlpgraphs.postprocessing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryParser.ParseException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import no.roek.nlpgraphs.algorithm.GraphEditDistance;
import no.roek.nlpgraphs.document.PlagiarismReference;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;

public class PlagiarismWorker extends Thread {

	private File[] testFiles;
	private String parsedData, trainDir, testDir, originalDir;
	public PlagiarismWorker(File[] testFiles) {
		this.testFiles = testFiles;
		this.originalDir = ConfigService.getDataDir();
		this.parsedData = ConfigService.getParsedFilesDir();
		this.trainDir = ConfigService.getTrainDir();
		this.testDir = ConfigService.getTestDir();
	}

	@Override
	public void run() {
		for (File testFile : testFiles) {
			List<PlagiarismReference> plagiarisms = findPlagiarism(testFile, 5);
			writeResults(testFile, plagiarisms);
		}
		
		System.out.println(Thread.currentThread().getName() + " done finding plagiarism in "+ testFiles.length+" files.");
	}


	public List<PlagiarismReference> findPlagiarism(File file, double threshold) {
		List<PlagiarismReference> references = new ArrayList<>();
		
		List<String> simDocs = findSimilarDocuments(file, 10);
		List<Graph> mySentences = GraphUtils.getGraphs(parsedData+testDir+file.toString());

		for (String trainFile : simDocs) {
			for (Graph otherGraph : GraphUtils.getGraphs(parsedData+trainDir+trainFile)) {
				for (Graph graph : mySentences) {
					GraphEditDistance ged = new GraphEditDistance(graph, otherGraph);
					if (ged.getDistance() < threshold) {
						references.add(getPlagiarismReference(graph, otherGraph));
					}
				}
			}
		}
		
		return references;
	}

	public PlagiarismReference getPlagiarismReference(Graph test, Graph train) {
		String offset = Integer.toString(test.getOffset());
		String length = Integer.toString(test.getLength());
		String sourceReference = train.getFilename();
		String sourceOffset = Integer.toString(train.getOffset());
		String sourceLength = Integer.toString(train.getLength());
		return new PlagiarismReference(offset, length, sourceReference, sourceOffset, sourceLength);
	}

	public List<String> findSimilarDocuments(File file, int recall) {
		try {
			DocumentRetrievalService drs = new DocumentRetrievalService(Paths.get(originalDir+trainDir));
			String filename = originalDir+testDir+file.getName();
			return drs.getSimilarDocuments(Fileutils.getText(Paths.get(filename)), recall);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}
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
			FileWriter writer = new FileWriter("userinfo.xml");
			outputter.output(doc, writer);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
