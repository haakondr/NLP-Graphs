package no.roek.nlpgraphs.search;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.SentenceUtils;

public class SentenceRetrievalWorker extends Thread {

	private BlockingQueue<Job> queue;
	private BlockingQueue<Job> parseQueue;
	private String trainDir, dataDir;
	

	public SentenceRetrievalWorker(BlockingQueue<Job> queue, BlockingQueue<Job> parseQueue) {
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
				Job job = queue.take();
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

	public Job getParseJob(Job job) {
		for (String simDoc : job.getSimilarDocuments()) {
			job.addAllTextPairs(SentenceUtils.getSimilarSentences(job.getFile().toString(), dataDir+trainDir+simDoc));
		}

		return job;
	}
}
