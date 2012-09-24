package no.roek.nlpgraphs.search;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.jobs.PlagJob;
import no.roek.nlpgraphs.jobs.PostagJob;
import no.roek.nlpgraphs.misc.SentenceUtils;

public class SentenceRetrievalWorker extends Thread {
	
	private BlockingQueue<PlagJob> queue;
	private BlockingQueue<PostagJob> parseQueue;
	
	public SentenceRetrievalWorker(BlockingQueue<PlagJob> queue, BlockingQueue<PostagJob> parseQueue) {
		this.queue = queue;
		this.parseQueue = parseQueue;
	}
	
	@Override
	public void run() {
		boolean running = true;
		
		while(running) {
			try {
				PlagJob job = queue.poll(2000, TimeUnit.SECONDS);
				parseQueue.put(getParseJob(job));
			} catch (InterruptedException e) {
				e.printStackTrace();
				running = false;
			}catch (NullPointerException e) {
				System.out.println(Thread.currentThread().getName()+" timed out after 2000 seconds with nothing from DocumentRetrievalWorker threads");
				running = false;
			}
		}
	}
	
	public PostagJob getParseJob(PlagJob job) {
		PostagJob parseJob = new PostagJob(job.getFile());
		for (String simDoc : job.getSimilarDocuments()) {
			parseJob.addAllTextPairs(SentenceUtils.getSimilarSentences(job.getFile(), simDoc));
		}
		
		return parseJob;
	}
}
