package no.roek.nlpgraphs.search;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;


import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.document.PlagiarismReference;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.XMLUtils;

public class PerfectDocumentRetrievalWorker extends Thread {

	private BlockingQueue<Job> queue;
	private String testDir, dataDir, annotationsDir, trainDir;
	private int documentRecall;

	public PerfectDocumentRetrievalWorker(BlockingQueue<Job> queue, String dataDir, String trainDir, String testDir) {
		this.queue = queue;
		this.testDir = testDir;
		this.dataDir = dataDir;
		this.documentRecall = 1;
		annotationsDir = ConfigService.getAnnotationsDir();
		trainDir = ConfigService.getTrainDir();
	}

	@Override
	public void run() {


		try {
			for (File testFile : Fileutils.getFiles(Paths.get(dataDir+testDir))) {
				List<String> similarDocs = getSimilarDocs(testFile.toString());
				queue.put(new Job(testFile.toPath().toString(), similarDocs.toArray(new String[0])));
			}

			for (int i = 0; i < 100; i++) {
				Job poisonPill = new Job("threads should terminate when this job is encountered");
				poisonPill.setLastInQueue(true);
				queue.put(poisonPill);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public List<String> getSimilarDocs(String file) {
		List<String> simDocs = new ArrayList<>();
		
		List<PlagiarismReference> plagRefs = XMLUtils.getPlagiarismReferences(annotationsDir+file);
		for (PlagiarismReference plagiarismReference : plagRefs) {
			simDocs.add(plagiarismReference.getSourceReference());
		}
		
		return simDocs;
	}
}
