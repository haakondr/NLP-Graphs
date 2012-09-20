package no.roek.nlpgraphs.postprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;

import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

public class CandidateRetrievalWorker extends Thread {

	private BlockingQueue<PlagJob> queue;
	private String testDir, dataDir;
	private DocumentRetrievalService drs;
	private int documentRecall;
	
	public CandidateRetrievalWorker(BlockingQueue<PlagJob> queue, String dataDir, String parsedDataDir, String trainDir, String testDir) {
		this.queue = queue;
		this.testDir = testDir;
		this.dataDir = dataDir;
		this.documentRecall = ConfigService.getDocumentRecall();
		try {
			drs = new DocumentRetrievalService(Paths.get(dataDir+trainDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		for (File testFile : Fileutils.getFiles(Paths.get(dataDir+testDir))) {
			try {
				List<String> similarDocs = drs.getSimilarDocuments(testFile.toString(), documentRecall);
				queue.put(new PlagJob(testFile.toPath().getFileName().toString(), similarDocs.toArray(new String[0])));
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
