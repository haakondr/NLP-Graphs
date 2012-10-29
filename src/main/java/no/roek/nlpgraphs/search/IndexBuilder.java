package no.roek.nlpgraphs.search;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.ConcurrencyService;

public class IndexBuilder extends Thread {

	private BlockingQueue<String> documentQueue;
	private CandidateRetrievalService crs;
	private boolean running;
	private ConcurrencyService concurrencyService;

	public IndexBuilder(BlockingQueue<String> documentQueue, CandidateRetrievalService crs, ConcurrencyService concurrencyService) {
		//TODO: remove stopwords
		this.crs = crs;
		this.documentQueue = documentQueue;
		this.concurrencyService = concurrencyService;
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				String filename = documentQueue.take();
				if(filename.equals("die")) {
					running = false;
				}else {
					crs.addDocument(SentenceUtils.getSentences(filename));
					concurrencyService.indexBuilderJobDone();
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void kill() {
		try {
			documentQueue.put("die");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
