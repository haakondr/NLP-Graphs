package no.roek.nlpgraphs.candidate.retrieval;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.App;
import no.roek.nlpgraphs.misc.SentenceUtils;

public class IndexBuilder extends Thread {

	private BlockingQueue<String> documentQueue;
	private CandidateRetrievalService crs;
	private boolean running;
	private App concurrencyService;

	public IndexBuilder(BlockingQueue<String> documentQueue, CandidateRetrievalService crs, App concurrencyService) {
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
					crs.addDocument(SentenceUtils.getSentencesFromParsedFile(filename));
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
