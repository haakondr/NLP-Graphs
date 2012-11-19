package no.roek.nlpgraphs.candretrieval;

import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.PlagiarismSearch;
import no.roek.nlpgraphs.misc.SentenceUtils;

public class IndexBuilder extends Thread {

	private BlockingQueue<String> documentQueue;
	private CandidateRetrievalService crs;
	private boolean running;
	private PlagiarismSearch concurrencyService;

	public IndexBuilder(BlockingQueue<String> documentQueue, CandidateRetrievalService crs, PlagiarismSearch concurrencyService) {
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
					
					crs.addDocument(SentenceUtils.getSentencesFromParsedFile(Paths.get(filename).getFileName().toString()));
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
