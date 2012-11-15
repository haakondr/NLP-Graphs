package no.roek.nlpgraphs.preprocessing;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.App;
import no.roek.nlpgraphs.misc.ConfigService;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

public class DependencyProducer extends Thread{
	private final BlockingQueue<ParseJob> queue;
	private String parsedFilesDir;
	private App concurrencyService;
	private boolean running;
	private DependencyParser parser;
	
	public DependencyProducer(BlockingQueue<ParseJob> queue,  String maltParams, App concurrencyService) {
		this.queue = queue;
		ConfigService cs = new ConfigService();
		this.parsedFilesDir = cs.getParsedFilesDir();
		this.concurrencyService = concurrencyService;
		this.parser = new DependencyParser();
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				ParseJob job = queue.take();
				parser.dependencyParse(job, parsedFilesDir);
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
