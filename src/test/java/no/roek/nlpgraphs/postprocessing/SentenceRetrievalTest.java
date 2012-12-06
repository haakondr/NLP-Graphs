package no.roek.nlpgraphs.postprocessing;

import java.util.List;

import no.roek.nlpgraphs.detailed.analysis.PlagiarismReference;

public class SentenceRetrievalTest {


//	@Test
//	public void shouldRetreieveSentences() {
//		ConfigService cs = new ConfigService();
//		String dataDir = cs.getDataDir();
//		String testDir = cs.getTestDir();
//		String trainDir = cs.getTrainDir();
//		String parsedDir = cs.getParsedFilesDir();
//		String susp = "suspicious-document00014.txt";
//		String source1 = "source-document06087.txt";
//		String source2 = "source-document04449.txt"; 
//
//		List<TextPair> simSentences = SentenceUtils.getSimilarSentences(dataDir, parsedDir, testDir, trainDir, susp, source1);
//		List<TextPair> simSentences2 = SentenceUtils.getSimilarSentences(dataDir, parsedDir, testDir, trainDir, susp, source2);
//
//
//		String filename = Paths.get(susp).getFileName().toString();
//		PlagiarismReference ref1 = new PlagiarismReference(filename, 1385, 254, "source-document06087.txt", 34059, 261);
//		PlagiarismReference ref2= new PlagiarismReference(filename, 6709, 277, "source-document04449.txt", 3157, 264);
//		PlagiarismReference ref3= new PlagiarismReference(filename, 9615, 235, "source-document04449.txt", 2717, 236);
//
//
//		assertEquals(true, contains(ref1, simSentences));
//		assertEquals(true, contains(ref2, simSentences2));
//		assertEquals(true, contains(ref3, simSentences2));
//	}

//	private boolean contains(PlagiarismReference ref, List<TextPair> simSentences) {
//		for (TextPair textPair : simSentences) {
//			if(textPair.getTestSentence().matchesPlagSuspiciousRef(ref) && textPair.getTrainSentence().matchesPlagSourceRef(ref)) {
//				return true;
//			}
//		}
//		return false;
//	}


}
