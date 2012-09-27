package no.roek.nlpgraphs;

import java.io.File;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.LiveDependencyParser;
import no.roek.nlpgraphs.preprocessing.LivePosTagProducer;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;
import no.roek.nlpgraphs.search.PerfectDocumentRetrievalWorker;
import no.roek.nlpgraphs.search.SentenceRetrievalWorker;

public class App {

	private static String trainDir, testDir, dataDir;

	public static void main(String[] args) throws InterruptedException {
		if(args[0].equals("--preprocess") || args[0].equals("-pp")) {
			System.out.println("Starting preprocessing");
			preprocess();
		}else {
			System.out.println("Starting plagiarism search");
			postProcess();
		}
	}

	public static void preprocess() {
		int posThreads = ConfigService.getPOSTaggerThreadCount();
		List<File[]> chunks = Fileutils.getChunks(Fileutils.getFiles(ConfigService.getDataDir()), posThreads);
		
		BlockingQueue<ParseJob> queue = new LinkedBlockingQueue<ParseJob>(100);

		for (int i = 0; i < posThreads; i++) {
			new PosTagProducer(queue, chunks.get(i)).start();
		}

		int maltThreads = ConfigService.getMaltParserThreadCount();
		for (int i = 0; i < maltThreads; i++) {
			new DependencyParser(queue, ConfigService.getMaltParams()).start();
		}
	}
	
	public static void postProcess() {
		//TODO: rewrite so parsed data is retrieved from file
		dataDir = ConfigService.getDataDir();
		testDir = ConfigService.getTestDir();
		trainDir = ConfigService.getTrainDir();
		
		BlockingQueue<Job> documentRetrievalQueue = new LinkedBlockingQueue<>(100);
		new PerfectDocumentRetrievalWorker(documentRetrievalQueue, dataDir, trainDir, testDir).start();
		
		BlockingQueue<Job> posTagQueue = new LinkedBlockingQueue<>(100);
		for (int i = 0; i < 2; i++) {
			new SentenceRetrievalWorker(documentRetrievalQueue, posTagQueue).start();
		}
				
		BlockingQueue<Job> parseQueue  = new LinkedBlockingQueue<>(100);
		for (int i = 0; i < 7; i++) {
			new LivePosTagProducer(posTagQueue, parseQueue,  "english-left3words-distsim.tagger").start();
		}
		
		BlockingQueue<Job> distQueue = new LinkedBlockingQueue<>(100);
		for (int i = 0; i < 7; i++) {
			new LiveDependencyParser(parseQueue, distQueue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log").start();
		}
		
		for (int i = 0; i < 7; i++) {
			new PlagiarismWorker(distQueue).start();
		}
		//TODO: print ut candidate retrieval success i run
		//TODO: append results til en log istedenfor å skrive over
	}
}
