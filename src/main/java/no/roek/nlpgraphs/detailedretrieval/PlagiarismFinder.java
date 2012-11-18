package no.roek.nlpgraphs.detailedretrieval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;

public class PlagiarismFinder {

	private String parsedDir, testDir, trainDir, resultsDir;
	private double plagiarismThreshold;

	public PlagiarismFinder() {
		ConfigService cs = new ConfigService();
		parsedDir = cs.getParsedFilesDir();
		testDir =cs.getTestDir();
		trainDir = cs.getTrainDir();
		this.resultsDir = cs.getResultsDir();
		this.plagiarismThreshold = cs.getPlagiarismThreshold();
	}

	public List<PlagiarismReference> findPlagiarism(PlagiarismJob job) {
		List<PlagiarismReference> plagReferences = new ArrayList<>();

		for(PlagiarismPassage passage : job.getTextPairs()) {
			plagReferences.add(findPlagiarism(passage.getTrainFile(), passage.getTrainSentence(), passage.getTestFile(), passage.getTestSentence()));
		}

		return plagReferences;
	}

	public List<PlagiarismReference> findAdjacentPlagiarism(String trainFile, int trainSentence, String testFile, int testSentence) {
		List<PlagiarismReference> plagReferences = new ArrayList<>();
		plagReferences.add(findPlagiarism(trainFile, trainSentence, testFile, testSentence));
		plagReferences.addAll(findAdjacentPlagiarism(trainFile, trainSentence, testFile, testSentence, true));
		plagReferences.addAll(findAdjacentPlagiarism(trainFile, trainSentence, testFile, testSentence, false));
		
		return plagReferences;
	}

	public List<PlagiarismReference> findAdjacentPlagiarism(String trainFile, int trainSentence, String testFile, int testSentence, boolean ascending) {
		List<PlagiarismReference> plagRefs = new ArrayList<>();
		
		int i = ascending ? 1 : -1;
		PlagiarismReference plagRef = findPlagiarism(trainFile, trainSentence+i, testFile, testSentence+i);
		if(plagRef != null) {
			plagRefs.add(plagRef);
		}
		if(plagRef.getSimilarity() < plagiarismThreshold*0.8) {
			plagRefs.addAll(findAdjacentPlagiarism(trainFile, trainSentence+i, testFile, testSentence+i, ascending));
		}
		
		return plagRefs;
	}
	
	public PlagiarismReference findPlagiarism(String trainFile, int trainSentence, String testFile, int testSentence) {
		/**
		 * Checks sentence pair for plagiarism using Graph Edit Distance algorithm.
		 * Adjacent sentences are checked, if they exist, and added if they are above the plagiarism threshold
		 */
		//TODO: alle test graphs er fra samme fil. hent inn alle i en dict eller noe
		Graph train = GraphUtils.getGraphFromFile(parsedDir+trainDir+trainFile, trainSentence);
		Graph test = GraphUtils.getGraphFromFile(parsedDir+testDir+testFile, testSentence);
		
		if(train==null || test == null) {
			return null;
		}

		GraphEditDistance ged = new GraphEditDistance(test, train);
		double dist = ged.getDistance();
		return getPlagiarismReference(train, test, dist, (dist < plagiarismThreshold));
	}

	public PlagiarismReference getPlagiarismReference(Graph train, Graph test, double similarity, boolean detectedPlagiarism) {
		String filename = test.getFilename();
		String offset = Integer.toString(test.getOffset());
		String length = Integer.toString(test.getLength());
		String sourceReference = train.getFilename();
		String sourceOffset = Integer.toString(train.getOffset());
		String sourceLength = Integer.toString(train.getLength());
		String name = detectedPlagiarism ? "detected-plagiarism" : "candidate-passage";
		return new PlagiarismReference(filename, name, offset, length, sourceReference, sourceOffset, sourceLength, similarity);
	}

	public void writeResults(String file, List<PlagiarismReference> plagiarisms) {
		Element root = new Element("document");
		root.setAttribute("reference", file);
		for (PlagiarismReference plagiarismReference : plagiarisms) {
			Element reference = new Element("feature");
			reference.setAttribute("name", plagiarismReference.getName());
			reference.setAttribute("this_offset", plagiarismReference.getOffset());
			reference.setAttribute("this_length", plagiarismReference.getLength());
			reference.setAttribute("obfuscation", Double.toString(plagiarismReference.getSimilarity()));
			reference.setAttribute("source_reference", plagiarismReference.getSourceReference());
			reference.setAttribute("source_offset", plagiarismReference.getSourceOffset());
			reference.setAttribute("source_length", plagiarismReference.getSourceLength());
			root.addContent(reference);
		}

		Document doc = new Document();
		doc.setContent(root);

		XMLOutputter outputter = new XMLOutputter();

		FileWriter writer = null;
		try {
			writer = new FileWriter(resultsDir+Fileutils.replaceFileExtention(file, "xml"));
			outputter.output(doc, writer);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(writer);
		}
	}

	public List<PlagiarismReference> listCandidateReferences(PlagiarismJob job) {
		/**
		 * Only returns the plagiarism references from candidate retrieval.
		 * Use this for measuring the candidate retrieval phase.
		 */
		List<PlagiarismReference> plagReferences = new ArrayList<>();
		for (PlagiarismPassage pair : job.getTextPairs()) {
			plagReferences.add(getPlagiarismReference(pair.getTrainGraph(), pair.getTestGraph(), pair.getSimilarity(), false));
		}

		return plagReferences;
	}
}
