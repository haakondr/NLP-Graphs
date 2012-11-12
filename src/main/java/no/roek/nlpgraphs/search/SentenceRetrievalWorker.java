package no.roek.nlpgraphs.search;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.PlagiarismJob;
import no.roek.nlpgraphs.document.SentencePair;

public class SentenceRetrievalWorker extends Thread {

	private BlockingQueue<PlagiarismJob> queue;
	private BlockingQueue<File> retrievalQueue;
	private CandidateRetrievalService crs;
	

	public SentenceRetrievalWorker(CandidateRetrievalService crs, BlockingQueue<File> retrievalQueue, BlockingQueue<PlagiarismJob> queue) {
		this.queue = queue;
		this.crs = crs;
		this.retrievalQueue = retrievalQueue;
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				File file = retrievalQueue.take();
				if(file == null) {
					System.out.println("No files in queue. "+Thread.currentThread().getName()+" stopping..");
					running = false;
				}else {
					queue.put(getParseJob(file));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				running = false;
			}
		}
	}

	public PlagiarismJob getParseJob(File file) {
		PlagiarismJob plagJob = new PlagiarismJob(file.toPath());
		try {
			for(SentencePair sp : crs.getSimilarSentences(file.toString(), 150)) {
				plagJob.addTextPair(sp);
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}
		
		return plagJob;
	}
}
