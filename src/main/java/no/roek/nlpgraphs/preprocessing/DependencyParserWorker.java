package no.roek.nlpgraphs.preprocessing;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.application.PlagiarismSearch;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.DatabaseService;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

public class DependencyParserWorker extends Thread{
	private final BlockingQueue<ParseJob> queue;
	private PlagiarismSearch concurrencyService;
	private boolean running;
	private DependencyParser parser;
	private DatabaseService db;
	
	public DependencyParserWorker(BlockingQueue<ParseJob> queue,  String maltParams, PlagiarismSearch concurrencyService, DatabaseService db) {
		this.queue = queue;
		this.concurrencyService = concurrencyService;
		this.parser = new DependencyParser();
		this.db = db;
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				ParseJob job = queue.take();
				db.addIndex(job.getFilename());
				parser.dependencyParse(job, db);
				concurrencyService.depParseJobDone(this, "parse queue: "+queue.size());
			} catch (InterruptedException | NullPointerException e) {
				e.printStackTrace();
				running = false;
			}
		}
		System.out.println("Stopping "+Thread.currentThread().getName()+": all files are parsed.");
	}
	
	public synchronized void kill() {
		running = false;
	}
}
