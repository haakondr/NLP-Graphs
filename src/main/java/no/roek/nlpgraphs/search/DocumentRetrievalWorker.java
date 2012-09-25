package no.roek.nlpgraphs.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

public class DocumentRetrievalWorker extends Thread {

	private BlockingQueue<Job> queue;
	private String testDir, dataDir;
	private CandidateRetrievalService drs;
	private int documentRecall;

	public DocumentRetrievalWorker(BlockingQueue<Job> queue, String dataDir, String parsedDataDir, String trainDir, String testDir) {
		this.queue = queue;
		this.testDir = testDir;
		this.dataDir = dataDir;
		this.documentRecall = ConfigService.getDocumentRecall();
		try {
			drs = new CandidateRetrievalService(Paths.get(dataDir+trainDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		try {
			for (File testFile : Fileutils.getFiles(Paths.get(dataDir+testDir))) {
				List<String> similarDocs = drs.getSimilarDocuments(testFile.toString(), documentRecall);
				queue.put(new Job(testFile.toPath().toString(), similarDocs.toArray(new String[0])));
			}
			
			
			for (int i = 0; i < 100; i++) {
				Job poisonPill = new Job("threads should terminate when this job is encountered");
				poisonPill.setLastInQueue(true);
				queue.put(poisonPill);
			}
			
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
}
