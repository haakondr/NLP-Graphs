package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.misc.ConfigService;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer extends Thread {

	private final BlockingQueue<ParseJob> queue;
	private BlockingQueue<File> unparsedFiles;
	private MaxentTagger tagger;

	public PosTagProducer(BlockingQueue<File> unparsedFiles, BlockingQueue<ParseJob> queue){
		this.queue = queue;
		this.unparsedFiles = unparsedFiles;
		ConfigService cs = new ConfigService();
		try {
			this.tagger = new MaxentTagger(cs.getPOSTaggerParams());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				File file = unparsedFiles.poll();
				if(file != null) {
					file.getParentFile().mkdirs();
					ParseJob parseJob = ParseUtils.posTagFile(file, tagger);
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
