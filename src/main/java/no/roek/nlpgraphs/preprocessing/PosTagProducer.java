package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.misc.ConfigService;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer extends Thread {

	private final BlockingQueue<ParseJob> queue;
	private BlockingQueue<File> unparsedFiles;
	private POSTagParser parser;
		
	
	public PosTagProducer(BlockingQueue<File> unparsedFiles, BlockingQueue<ParseJob> queue){
		this.queue = queue;
		this.unparsedFiles = unparsedFiles;
		this.parser = new POSTagParser();
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				File file = unparsedFiles.poll(20, TimeUnit.SECONDS);
				if(file != null) {
					file.getParentFile().mkdirs();
					ParseJob parseJob = parser.posTagFile(file.toPath());
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
