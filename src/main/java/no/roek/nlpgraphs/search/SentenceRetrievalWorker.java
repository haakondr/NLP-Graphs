package no.roek.nlpgraphs.search;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.PlagiarismJob;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.SentenceUtils;

public class SentenceRetrievalWorker extends Thread {

	private BlockingQueue<PlagiarismJob> queue;
	private BlockingQueue<PlagiarismJob> parseQueue;
	private String trainDir, dataDir;
	

	public SentenceRetrievalWorker(BlockingQueue<PlagiarismJob> queue, BlockingQueue<PlagiarismJob> parseQueue) {
		this.queue = queue;
		this.parseQueue = parseQueue;
		this.trainDir = ConfigService.getTrainDir();
		this.dataDir = ConfigService.getDataDir();
	}

	@Override
	public void run() {
		boolean running = true;

		while(running) {
			try {
				PlagiarismJob job = queue.take();
				if(job.isLastInQueue()) {
					running = false;
					break;
				}
				parseQueue.put(getParseJob(job));
			} catch (InterruptedException e) {
				e.printStackTrace();
				running = false;
			}
		}
	}

	public PlagiarismJob getParseJob(PlagiarismJob job) {
		for (String simDoc : job.getSimilarDocuments()) {
			job.addAllTextPairs(SentenceUtils.getSimilarSentences(job.getFile().toString(), dataDir+trainDir+simDoc));
		}

		return job;
	}
}
