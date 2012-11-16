package no.roek.nlpgraphs.search;


import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import no.roek.nlpgraphs.candretrieval.CandidateRetrievalService;
import no.roek.nlpgraphs.detailedretrieval.PlagiarismReference;
import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.SentenceUtils;
import no.roek.nlpgraphs.misc.XMLUtils;

import org.apache.lucene.index.CorruptIndexException;
import org.junit.Before;
import org.junit.Test;

public class CandidateRetrievalTest {

	private static ConfigService cs;
	private static String dir;

	public static void main(String[] args) throws CorruptIndexException, IOException {
		cs = new ConfigService();
		dir = cs.getParsedFilesDir();

		shouldRetrieveCorrectPassage();
	}

	public static void shouldRetrieveCorrectPassage() throws CorruptIndexException, IOException {
		String filename = "suspicious-document00228.txt";
		CandidateRetrievalService crs = new CandidateRetrievalService(Paths.get(cs.getTrainDir()));
		List<PlagiarismPassage> passages = crs.getSimilarSentences(dir+cs.getTestDir()+filename, 50);
		List<PlagiarismReference> references = XMLUtils.getPlagiarismReferences("src/test/resources/suspicious-document00228.xml");

		int correct = 0;
		for (PlagiarismPassage passage : passages) {
			if(containsPassage(passage, references)) {
				correct++;
			}
		}

		System.out.println("Correct: "+correct+"/"+references.size());
		shouldBeSorted(passages);
	}

	public static void shouldBeSorted(List<PlagiarismPassage> passages) {
		if(passages.size()>1) {
			for (int i = 1; i < passages.size()-1; i++) {
				assertEquals(true, passages.get(i).getSimilarity() <= passages.get(i-1).getSimilarity());
			}
		}
	}

	public static boolean containsPassage(PlagiarismPassage passage, List<PlagiarismReference> references) {
		for(PlagiarismReference ref : references) {
			if(isMatch(passage, ref)) {
				return true;
			}
		}

		return false;
	}

	public static boolean isMatch(PlagiarismPassage passage, PlagiarismReference ref) {

		
		if(!passage.getTestFile().equals(ref.getFilename())) {
			return false;
		}if(!passage.getTrainFile().equals(ref.getSourceReference())) {
			return false;
		}

		System.out.println("--------------------------");
		System.out.println("Retrieved files: "+passage.getTestFile()+" - "+ref.getSourceReference());
		System.out.println(passage.getTestGraph().getOffset()+": "+passage.getTestGraph().getTextString());
		System.out.println(passage.getTrainGraph().getOffset()+": "+passage.getTrainGraph().getTextString());

		if(!isWithinPassage(passage.getTestGraph().getOffset(), passage.getTestGraph().getLength(), Integer.parseInt(ref.getOffset()), Integer.parseInt(ref.getLength()))) {
			System.out.println("test is not within passage");
			return false;
		}

		if(!isWithinPassage(passage.getTrainGraph().getOffset(), passage.getTrainGraph().getLength(), Integer.parseInt(ref.getSourceOffset()), Integer.parseInt(ref.getSourceLength()))) {
			System.out.println("train is not within passage");
			return false;
		}
		//		if(!SentenceUtils.isAlmostEqual(passage.getTestGraph().getOffset(), Integer.parseInt(ref.getOffset()))) {
		//			return false;
		//		}if(!SentenceUtils.isAlmostEqual(passage.getTestGraph().getLength(), Integer.parseInt(ref.getLength()))) {
		//			return false;
		//		}if(!SentenceUtils.isAlmostEqual(passage.getTrainGraph().getOffset(), Integer.parseInt(ref.getSourceOffset()))) {
		//			return false;
		//		}if(!SentenceUtils.isAlmostEqual(passage.getTrainGraph().getLength(), Integer.parseInt(ref.getSourceLength()))) {
		//			return false;
		//		}

		System.out.println("Full match on passage "+passage.getTestFile()+" offset"+passage.getTestGraph().getOffset());

		return true;
	}

	public static boolean isWithinPassage(int offset, int length, int refOffset, int refLength) {
		if(offset+5 < refOffset) {
			return false;
		}
		if(offset+length-5 > refOffset+refLength) {
			return false;
		}

		return (refOffset < offset+5) && (offset+length-5 < refOffset+refLength);
	}
}
