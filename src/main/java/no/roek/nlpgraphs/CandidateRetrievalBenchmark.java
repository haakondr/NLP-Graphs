package no.roek.nlpgraphs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import no.roek.nlpgraphs.document.PlagiarismReference;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.XMLUtils;
import no.roek.nlpgraphs.postprocessing.DocumentRetrievalService;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;



public class CandidateRetrievalBenchmark {



	public static void main(String[] args) throws CorruptIndexException, IOException, ParseException {

		String dataDir = ConfigService.getDataDir();
		Path trainDir = Paths.get(dataDir + ConfigService.getTrainDir());
		DocumentRetrievalService drs = new DocumentRetrievalService(trainDir);

		double correct = 0, total = 0;
		Path testDir = Paths.get(dataDir + ConfigService.getTestDir());
		for (File testFile : Fileutils.getFiles(testDir)) {
			List<String> similarFiles = drs.getSimilarDocuments(testFile.toPath().toString(), 10);

			String file = testFile.toPath().getFileName().toString();
			String annotationsFile = file.substring(0, file.indexOf(".")) + ".xml";
			if(containsAllPlagiarisedFiles(Paths.get("data/annotations/"+annotationsFile), similarFiles)) {
				correct++;
			}

			total++;
		}

		System.out.println("candidate retrieval was "+ (correct/total)*100+ "% correct.");
	}

	private static boolean containsAllPlagiarisedFiles(Path annotationFile, List<String> similarFiles) {
		List<PlagiarismReference> plagiarisms = XMLUtils.getPlagiarismReferences(annotationFile.toString());
		
		for (PlagiarismReference plagiarismReference : plagiarisms) {
			if(!isInCollection(plagiarismReference.getSourceReference(), similarFiles)) {
				return false;
			}
			
		}
		return true;
	}



	private static boolean isInCollection(String doc, List<String> collection) {
		boolean in = false;
		for (String string : collection) {
			if(doc.equals(string)) {
				in = true;
			}
		}

		return in;
	}
}
