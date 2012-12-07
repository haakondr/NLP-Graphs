package no.roek.nlpgraphs.candidateretrieval;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.application.PlagiarismSearch;
import no.roek.nlpgraphs.detailedanalysis.PlagiarismJob;
import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.DatabaseService;
import no.roek.nlpgraphs.misc.Fileutils;

public class SentenceRetrievalWorker extends Thread {

	private BlockingQueue<String> retrievalQueue;
	private CandidateRetrievalService crs;
	private DatabaseService db;
	private PlagiarismSearch searcher;
	private int retrievalCount;

	public SentenceRetrievalWorker(CandidateRetrievalService crs, int retrievalCount, BlockingQueue<String> retrievalQueue, DatabaseService db, PlagiarismSearch searcher) {
		this.crs = crs;
		this.retrievalQueue = retrievalQueue;
		this.db = db;
		this.searcher = searcher;
		this.retrievalCount = retrievalCount;
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				String file = retrievalQueue.take();
				if(file == null) {
					System.out.println("No files in queue. "+Thread.currentThread().getName()+" stopping..");
					running = false;
				}else {
					PlagiarismJob job = getJob(file);
					db.addCandidatePassage(job.toDBObject());
					searcher.candretJobDone("");
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				running = false;
			}
		}
	}

	public PlagiarismJob getJob(String file) {
		PlagiarismJob plagJob = new PlagiarismJob(Paths.get(file));
		try {
			for(PlagiarismPassage sp : crs.getSimilarSentences(file.toString(), retrievalCount, db)) {
				plagJob.addTextPair(sp);
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}
		
		return plagJob;
	}
	
	public void kill() {
		try {
			retrievalQueue.put(null);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
