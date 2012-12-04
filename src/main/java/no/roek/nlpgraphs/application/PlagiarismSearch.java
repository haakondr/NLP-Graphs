package no.roek.nlpgraphs.application;

import java.io.File;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.mongodb.DBCursor;

import no.roek.nlpgraphs.candretrieval.CandidateRetrievalService;
import no.roek.nlpgraphs.candretrieval.IndexBuilder;
import no.roek.nlpgraphs.candretrieval.SentenceRetrievalWorker;
import no.roek.nlpgraphs.detailedretrieval.PlagiarismJob;
import no.roek.nlpgraphs.detailedretrieval.PlagiarismWorker;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.DatabaseService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.ProgressPrinter;
import no.roek.nlpgraphs.preprocessing.DependencyParserWorker;
import no.roek.nlpgraphs.preprocessing.ParseJob;
import no.roek.nlpgraphs.preprocessing.PosTagWorker;

public class PlagiarismSearch {

	private DatabaseService db;
	private LinkedBlockingQueue<ParseJob> parseQueue;
	private ConfigService cs;
	private DependencyParserWorker[] dependencyParserThreads;
	private PosTagWorker[] posTagThreads;
	private PlagiarismWorker[] plagThreads;
	private IndexBuilder[] indexBuilderThreads;
	private SentenceRetrievalWorker[] candretThreads;
	private int dependencyParserCount, posTagCount, plagThreadCount;
	private ProgressPrinter progressPrinter;
	private String dataDir, trainDir, testDir;
	private CandidateRetrievalService  crs;

	public PlagiarismSearch() {
		cs = new ConfigService();
		dataDir = cs.getDataDir();
		trainDir = cs.getTrainDir();
		testDir = cs.getTestDir();
		db = new DatabaseService();
	}

	public void preprocess() {
		Set<String> files = db.getUnparsedFiles(Fileutils.getFileNames(dataDir));
		if(files.size() == 0) {
			System.out.println("All files are parsed. Exiting");
			System.exit(0);
		}
		System.out.println("Starting preprocessing of "+files.size()+" files.");

		BlockingQueue<String> posTagQueue = new LinkedBlockingQueue<>();

		for (String file : files) {
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
		progressPrinter = new ProgressPrinter(files.size());
		dependencyParserThreads = new DependencyParserWorker[dependencyParserCount];
		for (int i = 0; i < dependencyParserCount; i++) {
			dependencyParserThreads[i] =  new DependencyParserWorker(parseQueue, cs.getMaltParams(), this, db);
			dependencyParserThreads[i].setName("Dependency-parser-"+i);
			dependencyParserThreads[i].start();
		}
	}

	public ProgressPrinter getProgressPrinter() {
		return progressPrinter;
	}

	public void depParseJobDone(DependencyParserWorker parser, String text) {
		progressPrinter.printProgressbar(text);

		if(progressPrinter.isDone()) {
			for(DependencyParserWorker thread : dependencyParserThreads) {
				thread.kill();
			}

			System.out.println("Preprocessing done. Exiting)");

		}
	}

	public void createIndex() {
		BlockingQueue<String> documentQueue = new LinkedBlockingQueue<>();
		
		DBCursor cursor = db.getSourceSentencesCursor();
		
		progressPrinter = new ProgressPrinter(cursor.size());
		crs = new CandidateRetrievalService(Paths.get(trainDir));

		indexBuilderThreads = new IndexBuilder[cs.getIndexBuilderThreads()];
		for (int i = 0; i < indexBuilderThreads.length; i++) {
			indexBuilderThreads[i] = new IndexBuilder(documentQueue, crs, this, db);
			indexBuilderThreads[i].setName("IndexBuilder-"+i);
			indexBuilderThreads[i].start();
		}
		
		while(cursor.hasNext()) {
			try{
				documentQueue.put(cursor.next().get("id").toString());
			}catch(InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		cursor.close();
	}

	public void indexBuilderDone() {
		for(IndexBuilder thread : indexBuilderThreads) {
			thread.kill();
		}

		crs.closeWriter();

		System.out.println("Index building done.. ");
		App.main(null);
	}
	
	public void indexBuilderJobDone() {
		progressPrinter.printProgressbar("");
		if(progressPrinter.isDone()) {
			for(IndexBuilder thread : indexBuilderThreads) {
				thread.kill();
			}

			crs.closeWriter();

			System.out.println("Index building done.. ");
			App.main(null);
		}

	}


	public void startCandidateRetrieval() {
		System.out.println("Starting candidate retrieval phase. The results will be stored to the database");
		BlockingQueue<String> retrievalQueue = new LinkedBlockingQueue<>();

		for (String file : Fileutils.getFilesNotDone(db.getFiles("suspicious_documents"), cs.getResultsDir(), "xml")) {
			try {
				retrievalQueue.put(file);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		progressPrinter = new ProgressPrinter(retrievalQueue.size());

		CandidateRetrievalService crs = new CandidateRetrievalService(Paths.get(trainDir));
		
		candretThreads = new SentenceRetrievalWorker[cs.getSentenceRetrievalThreads()];
		for (int i = 0; i < cs.getSentenceRetrievalThreads() ; i++) {
			candretThreads[i] =  new SentenceRetrievalWorker(crs, cs.getRetrievalCount(), retrievalQueue, db, this);
			candretThreads[i].setName("SentenceRetrieval-Thread-"+i);
			candretThreads[i].start();
			
		}
	}

	public void candretJobDone(String text) {
		progressPrinter.printProgressbar(text);
		if(progressPrinter.isDone()) {
	
			for (SentenceRetrievalWorker thread : candretThreads) {
				thread.kill();
			}
			
			System.out.println("\nCandidate retrieval search done. ");
			App.main(null);
		}
	}
	//	public void startPlagiarismSearch() {
	//		//TODO: fix new resultsdir
	//		System.out.println("starting plagiarism search..");
	//		BlockingQueue<String> retrievalQueue = new LinkedBlockingQueue<>();
	//
	//		for (String file : Fileutils.getFilesNotDone(db.getFiles("suspicious-documents"), cs.getResultsDir(), "xml")) {
	//			try {
	//				retrievalQueue.put(file);
	//			} catch (InterruptedException e) {
	//				e.printStackTrace();
	//			}
	//		}
	//
	//		progressPrinter = new ProgressPrinter(retrievalQueue.size());
	//
	//		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>(10);
	//		CandidateRetrievalService crs = new CandidateRetrievalService(Paths.get(trainDir));
	//
	//		for (int i = 0; i < cs.getSentenceRetrievalThreads() ; i++) {
	//			SentenceRetrievalWorker worker = new SentenceRetrievalWorker(crs, retrievalQueue, plagQueue);
	//			worker.setName("SentenceRetrieval-Thread-"+i);
	//			worker.start();
	//		}
	//
	//		startPlagiarismSearch(plagQueue);
	//	}

	public void startPlagiarismSearchWithoutCandret() {
		System.out.println("starting plagiarism search with candidate retrieval results from the database..");
		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>();
		String dir = "plagthreshold_"+cs.getPlagiarismThreshold()+"/";
		new File(cs.getResultsDir()+dir).mkdirs();
		Set<String> filesDone = Fileutils.getFileNames(cs.getResultsDir()+dir, "txt");

		System.out.println(filesDone.size()+" files already done.");
		db.retrieveAllPassages(plagQueue, filesDone);

		progressPrinter = new ProgressPrinter(plagQueue.size());
		startPlagiarismSearch(plagQueue);
	}

	private void startPlagiarismSearch(BlockingQueue<PlagiarismJob> plagQueue) {
		plagThreadCount = cs.getPlagiarismThreads();
		plagThreads = new PlagiarismWorker[plagThreadCount];
		for (int i = 0; i < plagThreadCount; i++) {
			plagThreads[i] = new PlagiarismWorker(plagQueue, this, db);
			plagThreads[i].setName("Plagiarism-thread-"+i);
			plagThreads[i].start();
		}
	}

	public void plagJobDone(PlagiarismWorker worker, String text) {
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
