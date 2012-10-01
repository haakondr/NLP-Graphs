package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.misc.ConfigService;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer extends Thread {

	private final BlockingQueue<ParseJob> queue;
	private MaxentTagger tagger;
	private File[] files;

	public PosTagProducer(BlockingQueue<ParseJob> queue, File[] files){
		this.queue = queue;
		this.files = files;

		try {
			this.tagger = new MaxentTagger(ConfigService.getPOSTaggerParams());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				for (File file : files) {
					file.getParentFile().mkdirs();

					ParseJob parseJob = ParseUtils.posTagFile(file, tagger);
					queue.put(parseJob);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		System.out.println("stopping "+Thread.currentThread().getName()+" after postagging "+files.length+" files");
	}
}
