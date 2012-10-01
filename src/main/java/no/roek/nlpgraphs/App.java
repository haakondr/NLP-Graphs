package no.roek.nlpgraphs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.concurrency.PlagiarismJob;
import no.roek.nlpgraphs.concurrency.SentenceRetrievalJob;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.ProgressPrinter;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;
import no.roek.nlpgraphs.search.PerfectDocumentRetrievalWorker;
import no.roek.nlpgraphs.search.SentenceRetrievalWorker;

public class App {

	private static String trainDir, testDir, dataDir, parsedFilesDir;

	public static void main(String[] args) throws InterruptedException {
		dataDir = ConfigService.getDataDir();
		trainDir = ConfigService.getTrainDir();
		testDir = ConfigService.getTestDir();
		parsedFilesDir = ConfigService.getParsedFilesDir();

		if(shouldPreprocess()) {
			System.out.println("Starting preprocessing");
			preprocess();
		}else {
			System.out.println("Starting plagiarism search");
			postProcess();
		}
	}

	public static boolean shouldPreprocess() {
		System.out.println("Would you like to preprocess the data first? This should be done on first run. [yes/no/exit]");
		BufferedReader bi = new BufferedReader(new InputStreamReader(System.in));
		String line;
		try {
			while ((line = bi.readLine()) != null) {
				if(line.equalsIgnoreCase("Y") || line.equalsIgnoreCase("yes")) {
					return true;
				}else if(line.equalsIgnoreCase("N") || line.equalsIgnoreCase("no")) {
					return false;
				}else if(line.equalsIgnoreCase("EXIT")) {
					System.out.println("Exiting app");
					System.exit(0);
				}else {
					System.out.println("Invalid answer. [Y/N/EXIT] is allowed.");
					return shouldPreprocess();
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Exiting app");
		System.exit(0);
		return false;
	}


	public static void preprocess() {
		int posThreads = ConfigService.getPOSTaggerThreadCount();
		
		File[] testFiles = Fileutils.getUnparsedFiles(dataDir, parsedFilesDir);
		
		File[][] testChunks = Fileutils.getChunks(testFiles, posThreads);
		ProgressPrinter progressPrinter = new ProgressPrinter(testFiles.length);

		System.out.println("preprocessing dir "+dataDir+" with "+posThreads+" pos tagger threads, with "+testFiles.length+" files");
		BlockingQueue<ParseJob> queue = new LinkedBlockingQueue<ParseJob>(20);

		int j = 0;
		for (int i = 0; i < testChunks.length; i++) {
			j++;
			PosTagProducer produer = new PosTagProducer(queue, testChunks[i]);
			produer.setName("POSTagProducer-"+j);
			produer.start();
		}

		int maltThreads = ConfigService.getMaltParserThreadCount();
		System.out.println("preprocessing with "+maltThreads+" dependency parser threads");
		for (int i = 0; i < maltThreads; i++) {
			DependencyParser dependencyParser = new DependencyParser(queue, ConfigService.getMaltParams(), progressPrinter);
			dependencyParser.setName("DependencyParser-"+i);
			dependencyParser.start();
		}
	}

	public static void postProcess() {
		BlockingQueue<SentenceRetrievalJob> documentRetrievalQueue = new LinkedBlockingQueue<>(10);
		new PerfectDocumentRetrievalWorker(documentRetrievalQueue, dataDir, trainDir, testDir);
		
		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>(10);
		
		int sentenceRetrievalThreads = ConfigService.getSentenceRetrievalThreads();
		for (int i = 0; i < sentenceRetrievalThreads ; i++) {
			new SentenceRetrievalWorker(documentRetrievalQueue, plagQueue).start();
		}
		
		ProgressPrinter progressPrinter = new ProgressPrinter(Fileutils.getFileCount(dataDir+testDir));
		int plagThreads = ConfigService.getPlagiarismThreads();
		for (int i = 0; i < plagThreads; i++) {
			new PlagiarismWorker(plagQueue, progressPrinter).start();
		}
	}
	
//	public static void postProcess() {
//		//TODO: rewrite so parsed data is retrieved from file
//
//		BlockingQueue<PlagiarismJob> documentRetrievalQueue = new LinkedBlockingQueue<>(100);
//		new PerfectDocumentRetrievalWorker(documentRetrievalQueue, dataDir, trainDir, testDir).start();
//
//		BlockingQueue<PlagiarismJob> posTagQueue = new LinkedBlockingQueue<>(100);
//		for (int i = 0; i < 2; i++) {
//			new SentenceRetrievalWorker(documentRetrievalQueue, posTagQueue).start();
//		}
//
//		BlockingQueue<PlagiarismJob> parseQueue  = new LinkedBlockingQueue<>(100);
//		for (int i = 0; i < 7; i++) {
//			new LivePosTagProducer(posTagQueue, parseQueue,  "english-left3words-distsim.tagger").start();
//		}
//
//		BlockingQueue<PlagiarismJob> distQueue = new LinkedBlockingQueue<>(100);
//		for (int i = 0; i < 7; i++) {
//			new LiveDependencyParser(parseQueue, distQueue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log").start();
//		}
//
//		for (int i = 0; i < 7; i++) {
//			new PlagiarismWorker(distQueue).start();
//		}
//		//TODO: print ut candidate retrieval success i run
//		//TODO: append results til en log istedenfor å skrive over
//	}
}
