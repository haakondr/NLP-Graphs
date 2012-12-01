package no.roek.nlpgraphs.detailedretrieval;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.application.PlagiarismSearch;
import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.DatabaseService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.misc.XMLUtils;

import org.apache.commons.io.IOUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

public class PlagiarismWorker extends Thread {

	private BlockingQueue<PlagiarismJob> queue;
	private PlagiarismFinder plagFinder;
	private PlagiarismSearch concurrencyService;
	private String resultsDir, dir;
	private boolean running;

	public PlagiarismWorker(BlockingQueue<PlagiarismJob> queue, PlagiarismSearch concurrencyService, DatabaseService db) {
		this.queue = queue;
		this.plagFinder = new PlagiarismFinder(db);
		this.concurrencyService = concurrencyService;
		ConfigService cs = new ConfigService();
		this.resultsDir = cs.getResultsDir();
		this.dir = "plagthreshold_"+cs.getPlagiarismThreshold()+"/";
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			try {
				PlagiarismJob job = queue.take();
				if(job.isLastInQueue()) {
					running = false;
					break;
				}
				List<PlagiarismReference> plagReferences = plagFinder.findPlagiarism(job);
				XMLUtils.writeResults(resultsDir+dir, job.getFile().getFileName().toString(), plagFinder.mergePassages(plagReferences));
				concurrencyService.plagJobDone(this, "queue: "+queue.size());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public synchronized void kill() {
		try {
			PlagiarismJob job = new PlagiarismJob("kill");
			job.setLastInQueue(true);
			queue.put(job);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
