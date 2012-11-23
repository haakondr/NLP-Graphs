package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.misc.ConfigService;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagWorker extends Thread {

	private final BlockingQueue<ParseJob> queue;
	private BlockingQueue<String> unparsedFiles;
	private POSTagParser parser;
		
	
	public PosTagWorker(BlockingQueue<String> unparsedFiles, BlockingQueue<ParseJob> queue){
		this.queue = queue;
		this.unparsedFiles = unparsedFiles;
		this.parser = new POSTagParser();
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				String file = unparsedFiles.poll(20, TimeUnit.SECONDS);
				if(file != null) {
					new File(file).getParentFile().mkdirs();
					ParseJob parseJob = parser.posTagFile(Paths.get(file));
					queue.put(parseJob);
				}else {
					running = false;
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("stopping postagger thread: "+Thread.currentThread().getName());
	}
}
