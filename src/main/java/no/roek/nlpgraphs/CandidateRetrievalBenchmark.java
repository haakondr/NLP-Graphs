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
		
		Path dir = Paths.get(ConfigService.getTrainDir());
		DocumentRetrievalService drs = new DocumentRetrievalService(dir);

		double correct = 0, total = 0;
		Path testDir = Paths.get(dir.toString()+"suspicious-documents");
		for (File testFile : Fileutils.getFiles(testDir)) {
			List<String> similarFiles = drs.getSimilarDocuments(Fileutils.getText(testFile.toPath()), 10);
			if(containsAllPlagiarisedFiles(Paths.get("pan11/annotations/"+testFile), similarFiles)) {
				correct++;
			}

			total++;
		}

		System.out.println("candidate retrieval was "+ (correct/total)*100+ "% correct.");
	}
	
	private static boolean containsAllPlagiarisedFiles(Path annotationFile, List<String> similarFiles) {
		List<PlagiarismReference> plagiarisms = XMLUtils.getPlagiarismReferences(annotationFile.toString());
		
		for (String simFile : similarFiles) {
			boolean contains = false;
			for (PlagiarismReference plagiarismReference : plagiarisms) {
				if(plagiarismReference.getSourceReference().equals(simFile)) {
					contains = true;
				}
			}

			if(!contains) {
				return false;
			}
		}

		return true;
	}
}
