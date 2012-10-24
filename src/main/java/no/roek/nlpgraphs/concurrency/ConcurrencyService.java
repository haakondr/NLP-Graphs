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
	private int dependencyParserCount, posTagCount;
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

	public synchronized void dependencyParsingDone() {
		dependencyParserCount--;
		if(dependencyParserCount == 0) {
			postProcess();
		}
	}

	public void postProcess() {
		System.out.println("starting plagiarism search..");
		BlockingQueue<SentenceRetrievalJob> documentRetrievalQueue = new LinkedBlockingQueue<>(10);

		//TODO: replace with a real doc retrieval worker
		new PerfectDocumentRetrievalWorker(documentRetrievalQueue, dataDir, trainDir, testDir).start();

		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>(10);

		int sentenceRetrievalThreads = cs.getSentenceRetrievalThreads();
		for (int i = 0; i < sentenceRetrievalThreads ; i++) {
			new SentenceRetrievalWorker(documentRetrievalQueue, plagQueue).start();
		}

		progressPrinter = new ProgressPrinter(Fileutils.getFileCount(dataDir+testDir));
		int plagThreads = cs.getPlagiarismThreads();
		for (int i = 0; i < plagThreads; i++) {
			new PlagiarismWorker(plagQueue, progressPrinter).start();
		}
	}
}
