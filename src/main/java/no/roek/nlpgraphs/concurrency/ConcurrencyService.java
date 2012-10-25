package no.roek.nlpgraphs.concurrency;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.ProgressPrinter;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;
import no.roek.nlpgraphs.search.PerfectDocumentRetrievalWorker;
import no.roek.nlpgraphs.search.SentenceRetrievalWorker;

public class ConcurrencyService {

	private File[] unparsedFiles;
	private LinkedBlockingQueue<ParseJob> parseQueue;
	private ConfigService cs;
	private DependencyParser[] dependencyParserThreads;
	private PosTagProducer[] posTagThreads;
	private PlagiarismWorker[] plagThreads;
	private int dependencyParserCount, posTagCount, sentenceRetrievalThreads, plagThreadCount;
	private ProgressPrinter progressPrinter;
	private String dataDir, trainDir, testDir;

	public ConcurrencyService() {
		cs = new ConfigService();
		dataDir = cs.getDataDir();
		trainDir = cs.getTrainDir();
		testDir = cs.getTestDir();
		this.unparsedFiles = Fileutils.getUnparsedFiles(dataDir, cs.getParsedFilesDir());
	}
	
	public void start() {
		if(!(unparsedFiles.length==0)) {
			preprocess();
		}else {
			postProcess();
		}
	}

	private void preprocess() {
		System.out.println("Starting preprocessing of "+unparsedFiles.length+" files.");
		
		BlockingQueue<File> posTagQueue = new LinkedBlockingQueue<>();
		
		for (File file : unparsedFiles) {
			try {
				posTagQueue.put(file);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		posTagCount = cs.getPOSTaggerThreadCount();
		parseQueue = new LinkedBlockingQueue<>();
		posTagThreads = new PosTagProducer[posTagCount];

		for (int i = 0; i < posTagCount; i++) {
			PosTagProducer producer = new PosTagProducer(posTagQueue, parseQueue);
			producer.start();
			posTagThreads[i] = producer;
		}

		dependencyParserCount = cs.getMaltParserThreadCount();
		progressPrinter = new ProgressPrinter(unparsedFiles.length);
		dependencyParserThreads = new DependencyParser[dependencyParserCount];
		for (int i = 0; i < dependencyParserCount; i++) {
			DependencyParser parser = new DependencyParser(parseQueue, cs.getMaltParams(), this);
			parser.start();
			dependencyParserThreads[i] = parser;
		}
	}

	public ProgressPrinter getProgressPrinter() {
		return progressPrinter;
	}

	public synchronized void depParseJobDone(DependencyParser parser, String text) {
		progressPrinter.printProgressbar(text);
		dependencyParserCount--;
		if(progressPrinter.isDone()) {
			parser.kill();
		}
		
		if(dependencyParserCount == 0) {
			System.out.println("Dependency parsing done. Starting plagiarism search..");
			postProcess();
		}
	}

	public void postProcess() {
		System.out.println("starting plagiarism search..");
		BlockingQueue<SentenceRetrievalJob> documentRetrievalQueue = new LinkedBlockingQueue<>(10);

		//TODO: replace with a real doc retrieval worker
		new PerfectDocumentRetrievalWorker(documentRetrievalQueue, dataDir, trainDir, testDir).start();

		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>(10);

		sentenceRetrievalThreads = cs.getSentenceRetrievalThreads();
		for (int i = 0; i < sentenceRetrievalThreads ; i++) {
			new SentenceRetrievalWorker(documentRetrievalQueue, plagQueue).start();
		}

		progressPrinter = new ProgressPrinter(Fileutils.getFileCount(dataDir+testDir));
		plagThreadCount = cs.getPlagiarismThreads();
		plagThreads = new PlagiarismWorker[plagThreadCount];
		for (int i = 0; i < plagThreadCount; i++) {
			plagThreads[i] = new PlagiarismWorker(plagQueue, this);
			plagThreads[i].start();
		}
	}
	
	public synchronized void plagJobDone(PlagiarismWorker worker, String text) {
		progressPrinter.printProgressbar(text);
		if(progressPrinter.isDone()) {
			worker.kill();
			plagThreadCount--;
		}
		
		if(plagThreadCount == 0) {
			System.out.println("Plagiarism search done. exiting");
			System.exit(0);
		}
	}
}
