package no.roek.nlpgraphs.postprocessing;

import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.detailedretrieval.PassageMerger;
import no.roek.nlpgraphs.detailedretrieval.PlagiarismFinder;
import no.roek.nlpgraphs.detailedretrieval.PlagiarismReference;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlagiarismFinderTest {

	private PlagiarismReference ref1, ref2, ref3;
	
	@Before
	public void setup() {
		ref1 = new PlagiarismReference("suspicious-document0001.txt", "detected-plagiarism", 100, 150, "source-document0005.txt", 200, 280);
		ref2 = new PlagiarismReference("suspicious-document0001.txt", "detected-plagiarism", 180, 250, "source-document0005.txt", 0, 180);
		ref3 = new PlagiarismReference("suspicious-document0001.txt", "detected-plagiarism", 160, 250, "source-document0005.txt", 0, 80);
	}
	
	@Test
	public void shouldMergePassages() {
		assertEquals(true, PassageMerger.shouldMergePassages(ref1, ref2));
		assertEquals(true, PassageMerger.shouldMergePassages(ref2, ref1));
		assertEquals(true, PassageMerger.shouldMergePassages(ref2, ref3));
	}
	
	@Test
	public void shouldNotMergePassages() {
		assertEquals(false, PassageMerger.shouldMergePassages(ref1, ref3));
		assertEquals(false, PassageMerger.shouldMergePassages(ref3, ref1));
	}
	
	@Test
	public void shouldCorrectlyMergePassages() {
		PlagiarismReference ref4 = new PlagiarismReference("suspicious-document0001.txt", "detected-plagiarism", 120, 300, "source-document0005.txt", 0, 110);
		PassageMerger.mergePassage(ref4, ref1);
		assertEquals(100, ref4.getOffsetInt());
		assertEquals(420, ref4.getEndInt());
		
		assertEquals(0, ref4.getSourceOffsetInt());
		assertEquals(480, ref4.getSourceEndInt());
	}
	
	@Test
	public void shouldMergeAllPassages() {
		List<PlagiarismReference> refs = new ArrayList<>();
		refs.add(ref1);
		refs.add(ref2);
		refs.add(ref3);
		refs.add(new PlagiarismReference("suspicious-document0001.txt", "detected-plagiarism", 600, 300, "source-document0005.txt", 0, 110));
		refs.add(new PlagiarismReference("suspicious-document0001.txt", "detected-plagiarism", 600, 300, "othername5.txt", 0, 110));
		List<PlagiarismReference> mergedReferences = PassageMerger.mergePassages(refs);

		assertEquals(3, mergedReferences.size());
	}
}
