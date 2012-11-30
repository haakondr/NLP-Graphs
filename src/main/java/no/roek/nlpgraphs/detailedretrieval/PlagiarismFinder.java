package no.roek.nlpgraphs.detailedretrieval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.DatabaseService;
import no.roek.nlpgraphs.misc.EditWeightService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.misc.XMLUtils;

public class PlagiarismFinder {

	private String parsedDir, testDir, trainDir, resultsDir;
	private double plagiarismThreshold;
	private DatabaseService db;
	private Map<String, Double> posEditWeights, deprelEditWeights;

	public PlagiarismFinder(DatabaseService db) {
		this.db = db;
		ConfigService cs = new ConfigService();
		parsedDir = cs.getParsedFilesDir();
		testDir =cs.getTestDir();
		trainDir = cs.getTrainDir();
		resultsDir = cs.getResultsDir();
		plagiarismThreshold = cs.getPlagiarismThreshold();
		posEditWeights = EditWeightService.getEditWeights(cs.getPosSubFile(), cs.getPosInsdelFile());
		deprelEditWeights = EditWeightService.getInsDelCosts(cs.getDeprelInsdelFile());
	}

	public List<PlagiarismReference> findPlagiarism(PlagiarismJob job) {
		List<PlagiarismReference> plagReferences = new ArrayList<>();

		for(PlagiarismPassage passage : job.getTextPairs()) {
			PlagiarismReference ref = getPlagiarism(passage.getTrainFile(), passage.getTrainSentence(), passage.getTestFile(), passage.getTestSentence());
			if(ref != null) {
				findAdjacentPlagiarism(ref, passage.getTrainSentence(), passage.getTestSentence(), false);
				findAdjacentPlagiarism(ref, passage.getTrainSentence(), passage.getTestSentence(), true);
				plagReferences.add(ref);
			}
		}
		

		return plagReferences;
	}

	public PlagiarismReference getPlagiarism(String trainFile, int trainSentence, String testFile, int testSentence) {
		/**
		 * Checks the given sentence pair for plagiarism with the graph edit distance algorithm
		 */
		Graph train = GraphUtils.getGraph(db.getSentence(trainFile, trainSentence));
		Graph test = GraphUtils.getGraph(db.getSentence(testFile, testSentence));
		if(train==null || test == null) {
			return null;
		}
		
		if(train.getSize() > 100 || test.getSize() > 100) {
			return null;
		}
		

		GraphEditDistance ged = new GraphEditDistance(test, train, posEditWeights, deprelEditWeights);
		double dist = ged.getNormalizedDistance();
		if(dist < plagiarismThreshold) {
			return XMLUtils.getPlagiarismReference(train, test, true);
		}else {
			return null;
		}
	}

	public void findAdjacentPlagiarism(PlagiarismReference ref, int sourceSentence, int suspiciousSentence, boolean ascending) {
		int i = ascending ? 1 : -1;
		PlagiarismReference adjRef = getPlagiarism(ref.getSourceReference(), sourceSentence+i, ref.getFilename(), suspiciousSentence+i);
		ref.setOffset(adjRef.getOffset());
		ref.setLength(getNewLength(ref.getOffset(), ref.getLength(), adjRef.getOffset(), i));
		ref.setSourceOffset(adjRef.getSourceOffset());
		ref.setSourceLength(getNewLength(ref.getSourceOffset(), ref.getSourceLength(), adjRef.getSourceOffset(), i));
	}

	public String getNewLength(String offsetString, String lengthString, String newOffsetString, int ascending) {
		int offset = Integer.parseInt(offsetString);
		int len = Integer.parseInt(lengthString);
		int newOffset = Integer.parseInt(newOffsetString);

		int newLen =  len + ((offset - newOffset) * ascending);
		return Integer.toString(newLen);
	}


	public List<PlagiarismReference> listCandidateReferences(PlagiarismJob job) {
		/**
		 * Only returns the plagiarism references from candidate retrieval.
		 * Use this for measuring the candidate retrieval phase.
		 */
		List<PlagiarismReference> plagReferences = new ArrayList<>();
		for (PlagiarismPassage pair : job.getTextPairs()) {
			Graph suspicious = GraphUtils.getGraph(db.getSentence(pair.getTestFile(), pair.getTestSentence()));
			Graph source = GraphUtils.getGraph(db.getSentence(pair.getTrainFile(), pair.getTrainSentence()));

			plagReferences.add(XMLUtils.getPlagiarismReference(source, suspicious, false));
		}

		return plagReferences;
	}
}
