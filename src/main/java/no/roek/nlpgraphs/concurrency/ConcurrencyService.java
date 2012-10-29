package no.roek.nlpgraphs.concurrency;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.lucene.index.CorruptIndexException;

import no.roek.nlpgraphs.App;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.ProgressPrinter;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;
import no.roek.nlpgraphs.search.CandidateRetrievalService;
import no.roek.nlpgraphs.search.IndexBuilder;
import no.roek.nlpgraphs.search.PerfectDocumentRetrievalWorker;
import no.roek.nlpgraphs.search.SentenceRetrievalWorker;

public class ConcurrencyService {

	private File[] unparsedFiles;
	private LinkedBlockingQueue<ParseJob> parseQueue;
	private ConfigService cs;
	private DependencyParser[] dependencyParserThreads;
	private PosTagProducer[] posTagThreads;
	private PlagiarismWorker[] plagThreads;
	private IndexBuilder[] indexBuilderThreads;
	private int dependencyParserCount, posTagCount, sentenceRetrievalThreadCount, plagThreadCount;
	private ProgressPrinter progressPrinter;
	private String dataDir, trainDir, testDir, parsedFilesDir;

	public ConcurrencyService() {
		cs = new ConfigService();
		dataDir = cs.getDataDir();
		trainDir = cs.getTrainDir();
		testDir = cs.getTestDir();
		parsedFilesDir = cs.getParsedFilesDir();
		this.unparsedFiles = Fileutils.getUnparsedFiles(dataDir, cs.getParsedFilesDir());
	}

//	public void start() {
//		if(!(unparsedFiles.length==0)) {
//			preprocess();
//		}else {
//			postProcess();
//		}
//	}

	public boolean shouldPreprocess() {
		return (unparsedFiles.length != 0);
	}
	
	public void preprocess() {
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
			posTagThreads[i] = new PosTagProducer(posTagQueue, parseQueue);
			posTagThreads[i].setName("Postag-thread-"+i);
			posTagThreads[i].start();
		}

		dependencyParserCount = cs.getMaltParserThreadCount();
		progressPrinter = new ProgressPrinter(unparsedFiles.length);
		dependencyParserThreads = new DependencyParser[dependencyParserCount];
		for (int i = 0; i < dependencyParserCount; i++) {
			dependencyParserThreads[i] =  new DependencyParser(parseQueue, cs.getMaltParams(), this);
			dependencyParserThreads[i].setName("Dependency-parser-"+i);
			dependencyParserThreads[i].start();
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
			System.out.println("Preprocessing done. Starting next step..");
			try {
				App.main(null);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean shouldCreateIndex() {
		File indexDir = new File("lucene/"+trainDir);
		return !indexDir.exists();
	}
	
	public void createIndex() {
		BlockingQueue<String> documentQueue = new LinkedBlockingQueue<>();
		for (File f : Fileutils.getFileList(dataDir+trainDir)) {
			try {
				documentQueue.put(f.toString());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		CandidateRetrievalService crs = new CandidateRetrievalService(Paths.get(dataDir+trainDir));
		
		indexBuilderThreads = new IndexBuilder[cs.getIndexBuilderThreads()];
		for (int i = 0; i < indexBuilderThreads.length; i++) {
			indexBuilderThreads[i] = new IndexBuilder(documentQueue, crs, this);
			indexBuilderThreads[i].setName("IndexBuilder-"+i);
			indexBuilderThreads[i].start();
		}
	}
	
	public synchronized void indexBuilderJobDone() {
		progressPrinter.printProgressbar("indexbuilder progress");
		if(progressPrinter.isDone()) {
			for(IndexBuilder thread : indexBuilderThreads) {
				thread.kill();
			}
			
			try {
				System.out.println("Index building done.. Starting plagiarism search.");
				App.main(null);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void PlagiarismSearch() {
		System.out.println("starting plagiarism search..");
//		BlockingQueue<SentenceRetrievalJob> documentRetrievalQueue = new LinkedBlockingQueue<>(10);

		//TODO: replace with a real doc retrieval worker
//		new PerfectDocumentRetrievalWorker(documentRetrievalQueue, dataDir, trainDir, testDir).start();

		BlockingQueue<File> retrievalQueue = new LinkedBlockingQueue<>();
		for (File file : Fileutils.getFileList(parsedFilesDir+testDir)) {
			try {
				retrievalQueue.put(file);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>(10);
		CandidateRetrievalService crs = new CandidateRetrievalService(Paths.get(testDir));
		
		for (int i = 0; i < sentenceRetrievalThreadCount ; i++) {
			SentenceRetrievalWorker worker = new SentenceRetrievalWorker(crs, retrievalQueue, plagQueue);
			worker.setName("SentenceRetrieval-Thread-"+i);
			worker.start();
		}

		progressPrinter = new ProgressPrinter(Fileutils.getFileCount(dataDir+testDir));
		plagThreadCount = cs.getPlagiarismThreads();
		plagThreads = new PlagiarismWorker[plagThreadCount];
		for (int i = 0; i < plagThreadCount; i++) {
			plagThreads[i] = new PlagiarismWorker(plagQueue, this);
			plagThreads[i].setName("Plagiarism-thread-"+i);
			plagThreads[i].start();
		}
	}

	public synchronized void plagJobDone(PlagiarismWorker worker, String text) {
		progressPrinter.printProgressbar(text);
		if(progressPrinter.isDone()) {
			for(PlagiarismWorker plagWorker : plagThreads) {
				plagWorker.kill();
			}

			System.out.println("\nPlagiarism search done. exiting");
			System.exit(0);
		}
	}
	
	
}
