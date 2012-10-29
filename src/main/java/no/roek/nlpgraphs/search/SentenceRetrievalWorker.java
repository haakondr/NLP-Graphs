package no.roek.nlpgraphs.search;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;

import org.apache.lucene.index.CorruptIndexException;

import no.roek.nlpgraphs.concurrency.PlagiarismJob;
import no.roek.nlpgraphs.concurrency.SentenceRetrievalJob;
import no.roek.nlpgraphs.document.SentencePair;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;

public class SentenceRetrievalWorker extends Thread {

	private BlockingQueue<PlagiarismJob> queue;
	private BlockingQueue<File> retrievalQueue;
//	private String trainDir, testDir, dataDir, parsedDir;
	private CandidateRetrievalService crs;
	

	public SentenceRetrievalWorker(CandidateRetrievalService crs, BlockingQueue<File> retrievalQueue, BlockingQueue<PlagiarismJob> queue) {
		this.queue = queue;
		this.crs = crs;
		this.retrievalQueue = retrievalQueue;
//		ConfigService cs = new ConfigService();
//		this.trainDir = cs.getTrainDir();
//		this.testDir = cs.getTestDir();
//		this.dataDir = cs.getDataDir();
//		this.parsedDir = cs.getParsedFilesDir();
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				File file = retrievalQueue.take();
				if(file == null) {
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
			for(SentencePair sentence : crs.getSimilarSentences(file.toString(), 50)) {
				plagJob.addTextPair(sentence);
			}
		} catch ( IOException e) {
			e.printStackTrace();
		}
		
		return plagJob;
	}
}
