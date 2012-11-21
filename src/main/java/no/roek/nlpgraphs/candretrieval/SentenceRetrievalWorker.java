package no.roek.nlpgraphs.candretrieval;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.detailedretrieval.PlagiarismJob;
import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

public class SentenceRetrievalWorker extends Thread {

	private BlockingQueue<PlagiarismJob> queue;
	private BlockingQueue<File> retrievalQueue;
	private CandidateRetrievalService crs;
	private String candretDir;
	

	public SentenceRetrievalWorker(CandidateRetrievalService crs, BlockingQueue<File> retrievalQueue, BlockingQueue<PlagiarismJob> queue) {
		this.queue = queue;
		this.crs = crs;
		this.retrievalQueue = retrievalQueue;
		ConfigService cs = new ConfigService();
		candretDir = cs.getCandRetDir();
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
					PlagiarismJob job = getParseJob(file);
					Fileutils.writeToFile(candretDir+file.toPath().getFileName().toString(), job.toJson());
//					queue.put(job);
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
			for(PlagiarismPassage sp : crs.getSimilarSentences(file.toString(), 150)) {
				plagJob.addTextPair(sp);
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}
		
		return plagJob;
	}
}
