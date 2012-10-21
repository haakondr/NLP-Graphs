package no.roek.nlpgraphs.preprocessing;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.ProgressPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

public class DependencyParser extends Thread{
	private final BlockingQueue<ParseJob> queue;
	private MaltParserService maltService;
	private String parsedFilesDir;
	private ProgressPrinter progressPrinter;
	
	public DependencyParser(BlockingQueue<ParseJob> queue,  String maltParams, ProgressPrinter progressPrinter) {
		this.queue = queue;
		ConfigService cs = new ConfigService();
		this.parsedFilesDir = cs.getParsedFilesDir();
		this.progressPrinter = progressPrinter;
		
		try {
			this.maltService = new MaltParserService();
			maltService.initializeParserModel(maltParams);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				if(progressPrinter.isDone()) {
					running = false;
					System.out.println("Stopping "+Thread.currentThread().getName()+" because all files are parsed.");
					break;
				}
				
				ParseJob job = queue.take();
				ParseUtils.dependencyParse(job, parsedFilesDir, maltService);
				
				progressPrinter.printProgressbar("");
			} catch (InterruptedException | NullPointerException | MaltChainedException e) {
				e.printStackTrace();
				running = false;
			}
		}

	}
}
