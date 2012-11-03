package no.roek.nlpgraphs.preprocessing;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.ConcurrencyService;
import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.misc.ConfigService;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

public class DependencyParser extends Thread{
	private final BlockingQueue<ParseJob> queue;
	private MaltParserService maltService;
	private String parsedFilesDir;
	private ConcurrencyService concurrencyService;
	private boolean running;
	
	public DependencyParser(BlockingQueue<ParseJob> queue,  String maltParams, ConcurrencyService concurrencyService) {
		this.queue = queue;
		ConfigService cs = new ConfigService();
		this.parsedFilesDir = cs.getParsedFilesDir();
		this.concurrencyService = concurrencyService;
		
		try {
			this.maltService = new MaltParserService();
			maltService.initializeParserModel(maltParams);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				ParseJob job = queue.take();
				ParseUtils.dependencyParse(job, parsedFilesDir, maltService);
				concurrencyService.depParseJobDone(this, "parse queue: "+queue.size());
			} catch (InterruptedException | NullPointerException | MaltChainedException e) {
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
