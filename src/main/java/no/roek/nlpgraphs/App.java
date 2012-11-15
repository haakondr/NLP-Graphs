package no.roek.nlpgraphs;

import java.io.File;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.candidate.retrieval.CandidateRetrievalService;
import no.roek.nlpgraphs.candidate.retrieval.IndexBuilder;
import no.roek.nlpgraphs.candidate.retrieval.SentenceRetrievalWorker;
import no.roek.nlpgraphs.detailed.retrieval.PlagiarismJob;
import no.roek.nlpgraphs.detailed.retrieval.PlagiarismWorker;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.ProgressPrinter;
import no.roek.nlpgraphs.preprocessing.DependencyParserWorker;
import no.roek.nlpgraphs.preprocessing.ParseJob;
import no.roek.nlpgraphs.preprocessing.PosTagWorker;

public class App {
	
	private File[] unparsedFiles;
	private LinkedBlockingQueue<ParseJob> parseQueue;
	private ConfigService cs;
	private DependencyParserWorker[] dependencyParserThreads;
	private PosTagWorker[] posTagThreads;
	private PlagiarismWorker[] plagThreads;
	private IndexBuilder[] indexBuilderThreads;
	private int dependencyParserCount, posTagCount, plagThreadCount;
	private ProgressPrinter progressPrinter;
	private String dataDir, trainDir, testDir, parsedFilesDir;
	private CandidateRetrievalService  crs;

	public App() {
		cs = new ConfigService();
		dataDir = cs.getDataDir();
		trainDir = cs.getTrainDir();
		testDir = cs.getTestDir();
		parsedFilesDir = cs.getParsedFilesDir();
		this.unparsedFiles = Fileutils.getFilesNotDone(dataDir, cs.getParsedFilesDir());
	}

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
		parseQueue = new LinkedBlockingQueue<>(15);
		posTagThreads = new PosTagWorker[posTagCount];

		for (int i = 0; i < posTagCount; i++) {
			posTagThreads[i] = new PosTagWorker(posTagQueue, parseQueue);
			posTagThreads[i].setName("Postag-thread-"+i);
			posTagThreads[i].start();
		}

		dependencyParserCount = cs.getMaltParserThreadCount();
		progressPrinter = new ProgressPrinter(unparsedFiles.length);
		dependencyParserThreads = new DependencyParserWorker[dependencyParserCount];
		for (int i = 0; i < dependencyParserCount; i++) {
			dependencyParserThreads[i] =  new DependencyParserWorker(parseQueue, cs.getMaltParams(), this);
			dependencyParserThreads[i].setName("Dependency-parser-"+i);
			dependencyParserThreads[i].start();
		}
	}

	public ProgressPrinter getProgressPrinter() {
		return progressPrinter;
	}

	public synchronized void depParseJobDone(DependencyParserWorker parser, String text) {
		progressPrinter.printProgressbar(text);

		if(progressPrinter.isDone()) {
			dependencyParserCount--;
			parser.kill();
		}

		if(dependencyParserCount == 0) {
			System.out.println("Preprocessing done. Starting next step..");
			RunApp.main(null);
		}
	}

	public boolean shouldCreateIndex() {
		File indexDir = new File("lucene/"+trainDir);
		return !indexDir.exists();
	}

	public void createIndex() {
		BlockingQueue<String> documentQueue = new LinkedBlockingQueue<>();
		for (File f : Fileutils.getFiles(parsedFilesDir+trainDir)) {
			try {
				documentQueue.put(f.toString());
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		progressPrinter = new ProgressPrinter(documentQueue.size());

		crs = new CandidateRetrievalService(Paths.get(trainDir));

		indexBuilderThreads = new IndexBuilder[cs.getIndexBuilderThreads()];
		for (int i = 0; i < indexBuilderThreads.length; i++) {
			indexBuilderThreads[i] = new IndexBuilder(documentQueue, crs, this);
			indexBuilderThreads[i].setName("IndexBuilder-"+i);
			indexBuilderThreads[i].start();
		}
	}

	public synchronized void indexBuilderJobDone() {
		progressPrinter.printProgressbar("IndexBuilder");
		if(progressPrinter.isDone()) {
			for(IndexBuilder thread : indexBuilderThreads) {
				thread.kill();
			}
			
			crs.closeWriter();

			System.out.println("Index building done.. Starting plagiarism search.");
			RunApp.main(null);
		}

	}

	public void PlagiarismSearch() {
		System.out.println("starting plagiarism search..");
		BlockingQueue<File> retrievalQueue = new LinkedBlockingQueue<>();
		
		for (File file : Fileutils.getFilesNotDone(parsedFilesDir+testDir, cs.getResultsDir())) {
			try {
				retrievalQueue.put(file);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		progressPrinter = new ProgressPrinter(retrievalQueue.size());
		
		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>(10);
		CandidateRetrievalService crs = new CandidateRetrievalService(Paths.get(trainDir));

		for (int i = 0; i < cs.getSentenceRetrievalThreads() ; i++) {
			SentenceRetrievalWorker worker = new SentenceRetrievalWorker(crs, retrievalQueue, plagQueue);
			worker.setName("SentenceRetrieval-Thread-"+i);
			worker.start();
		}
		
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
