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
//				findAdjacentPlagiarism(ref, passage.getTrainSentence(), passage.getTestSentence(), false);
//				findAdjacentPlagiarism(ref, passage.getTrainSentence(), passage.getTestSentence(), true);
				plagReferences.add(ref);
			}
		}

		return plagReferences;
	}

	public PlagiarismReference getPlagiarism(String trainFile, int trainSentence, String testFile, int testSentence) {
		/**
		 * Checks the given sentence pair for plagiarism with the graph edit distance algorithm
		 */
		try {
			Graph train = GraphUtils.getGraph(db.getSentence(trainFile, trainSentence));
			Graph test = GraphUtils.getGraph(db.getSentence(testFile, testSentence));
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
		}catch(NullPointerException e) {
			return null;
		}
	}
	
	public List<PlagiarismReference> mergePassages(List<PlagiarismReference> references) {
		List<PlagiarismReference> merged = new ArrayList<>();
		
		for (PlagiarismReference ref : references) {
			addRef(ref, merged);
		}
		
		return merged;
	}
	
	public void addRef(PlagiarismReference ref, List<PlagiarismReference> merged) {
		boolean added = false;
		for (PlagiarismReference ref2 : merged) {
			if(shouldMergePassages(ref, ref2)) {
				merged.remove(ref2);
				merged.add(mergePassage(ref, ref2));
				added = true;
			}
		}
		
		if(!added) {
			merged.add(ref);
		}
	}
	
	public PlagiarismReference mergePassage(PlagiarismReference ref, PlagiarismReference other) {
		if(ref.getOffsetInt() == other.getOffsetInt()) {
			int len = Math.max(ref.getLengthInt(), other.getLengthInt());
			ref.setLength(len);
		}
		if(ref.getSourceOffsetInt() == other.getSourceOffsetInt()) {
			int len = Math.max(ref.getSourceLengthInt(), other.getSourceLengthInt());
			ref.setSourceLength(len);
		}
		
		if(ref.getOffsetInt() < other.getOffsetInt()) {
			ref.setLength(other.getOffsetInt() + other.getLengthInt() - ref.getOffsetInt());
		}else {
			ref.setLength(ref.getOffsetInt() + ref.getLengthInt() - other.getOffsetInt());
			ref.setOffset(other.getOffsetInt());
		}

		if(ref.getSourceOffsetInt() < other.getSourceOffsetInt()) {
			ref.setSourceLength(other.getSourceOffsetInt() + other.getSourceLengthInt() - ref.getSourceOffsetInt());
		}else {
			ref.setSourceLength(ref.getSourceOffsetInt() + ref.getSourceLengthInt() - other.getSourceOffsetInt());
			ref.setSourceOffset(other.getSourceOffsetInt());
		}
		
		return ref;
	}
	
	
	public  boolean shouldMergePassages(PlagiarismReference ref1, PlagiarismReference ref2) {
		if(!equalFilenames(ref1, ref2)) {
			return false;
		}
		int suspiciousDiff = getPassageDiff(ref1.getOffsetInt(), ref1.getEndInt(), ref2.getOffsetInt(), ref2.getEndInt());
		int sourceDiff = getPassageDiff(ref1.getSourceOffsetInt(), ref1.getSourceEndInt(), ref2.getSourceOffsetInt(), ref2.getSourceEndInt());
		return (suspiciousDiff < 100) && (sourceDiff < 100);
	}
	
	private boolean equalFilenames(PlagiarismReference ref1, PlagiarismReference ref2) {
		return ref1.getFilename().equals(ref2.getFilename()) && ref1.getSourceReference().equals(ref2.getSourceReference());
	}
	
	private int getPassageDiff(int offset1, int end1, int offset2, int end2) {
		if(isOverlap(offset1, end1, offset2, end2)) {
			return 0;
		}
		int dist1 = Math.abs(end1 - offset2);
		int dist2 = Math.abs(end2 - offset1);
		
		return Math.min(dist1, dist2);
	}

	private boolean isOverlap(int offset1, int end1, int offset2, int end2) {
		if(offset1 <= offset2 && offset2 <= end1) {
			return true;
		}else if(offset2 <= offset1 && offset1 <= end2) {
			return true;
		}else {
			return false;
		}
	}

//	public void findAdjacentPlagiarism(PlagiarismReference ref, int sourceSentence, int suspiciousSentence, boolean ascending) {
//		int i = ascending ? 1 : -1;
//		PlagiarismReference adjRef = getPlagiarism(ref.getSourceReference(), sourceSentence+i, ref.getFilename(), suspiciousSentence+i);
//		if(adjRef != null) {
//			ref.setOffset(adjRef.getOffset());
//			ref.setLength(getNewLength(ref.getOffset(), ref.getLength(), adjRef.getOffset(), i));
//			ref.setSourceOffset(adjRef.getSourceOffset());
//			ref.setSourceLength(getNewLength(ref.getSourceOffset(), ref.getSourceLength(), adjRef.getSourceOffset(), i));
//			findAdjacentPlagiarism(ref, sourceSentence+i*2, suspiciousSentence+i*2, ascending);
//		}
//	}

//	public String getNewLength(String offsetString, String lengthString, String newOffsetString, int ascending) {
//		int offset = Integer.parseInt(offsetString);
//		int len = Integer.parseInt(lengthString);
//		int newOffset = Integer.parseInt(newOffsetString);
//
//		int newLen =  len + ((offset - newOffset) * ascending);
//		return Integer.toString(newLen);
//	}


//	public List<PlagiarismReference> listCandidateReferences(PlagiarismJob job) {
//		/**
//		 * Only returns the plagiarism references from candidate retrieval.
//		 * Use this for measuring the candidate retrieval phase.
//		 */
//		List<PlagiarismReference> plagReferences = new ArrayList<>();
//		for (PlagiarismPassage pair : job.getTextPairs()) {
//			Graph suspicious = GraphUtils.getGraph(db.getSentence(pair.getTestFile(), pair.getTestSentence()));
//			Graph source = GraphUtils.getGraph(db.getSentence(pair.getTrainFile(), pair.getTrainSentence()));
//
//			plagReferences.add(XMLUtils.getPlagiarismReference(source, suspicious, false));
//		}
//
//		return plagReferences;
//	}
}
