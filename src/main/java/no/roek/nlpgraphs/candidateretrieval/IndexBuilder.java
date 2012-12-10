package no.roek.nlpgraphs.candidateretrieval;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.application.PlagiarismSearch;
import no.roek.nlpgraphs.misc.DatabaseService;

public class IndexBuilder extends Thread {

	private BlockingQueue<String> documentQueue;
	private CandidateRetrievalService crs;
	private boolean running;
	private PlagiarismSearch concurrencyService;
	private DatabaseService db;

	public IndexBuilder(BlockingQueue<String> documentQueue, CandidateRetrievalService crs, PlagiarismSearch concurrencyService, DatabaseService db) {
		this.crs = crs;
		this.documentQueue = documentQueue;
		this.concurrencyService = concurrencyService;
		this.db = db;
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				String sentenceId = documentQueue.poll(100, TimeUnit.SECONDS);
				
				
				if(sentenceId == null) {
					concurrencyService.indexBuilderDone();
					running = false;
				}else if(sentenceId.equals("die")) {
					running = false;
				}else {
					crs.addSentence(db.getSentence(sentenceId));
				}
				concurrencyService.indexBuilderJobDone();
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
