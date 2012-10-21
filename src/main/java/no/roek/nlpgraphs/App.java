package no.roek.nlpgraphs;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;
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

//	private static String trainDir, testDir, dataDir, parsedFilesDir, maltParams;
//	private static int posThreads, maltThreads;
	private static String trainDir, testDir, dataDir, parsedFilesDir;
	private static ConfigService cs;

	public static void main(String[] args) throws InterruptedException {
		cs = new ConfigService();
		dataDir = cs.getDataDir();
		trainDir = cs.getTrainDir();
		testDir = cs.getTestDir();
		parsedFilesDir = cs.getParsedFilesDir();
//		posThreads = cs.getPOSTaggerThreadCount();
//		maltThreads = cs.getMaltParserThreadCount();
//		maltParams = cs.getMaltParams();
//		
//		cs.close();

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
		File[] testFiles = Fileutils.getUnparsedFiles(dataDir, parsedFilesDir);
		
		File[][] testChunks = Fileutils.getChunks(testFiles, cs.getPOSTaggerThreadCount());
		ProgressPrinter progressPrinter = new ProgressPrinter(testFiles.length);

		System.out.println("preprocessing dir "+dataDir+" with "+cs.getPOSTaggerThreadCount()+" pos tagger threads, with "+testFiles.length+" files");
		BlockingQueue<ParseJob> queue = new LinkedBlockingQueue<ParseJob>(20);

		int j = 0;
		for (int i = 0; i < testChunks.length; i++) {
			j++;
			PosTagProducer produer = new PosTagProducer(queue, testChunks[i]);
			produer.setName("POSTagProducer-"+j);
			produer.start();
		}

		int maltThreads = cs.getMaltParserThreadCount();
		System.out.println("preprocessing with "+maltThreads+" dependency parser threads");
		for (int i = 0; i < maltThreads; i++) {
			DependencyParser dependencyParser = new DependencyParser(queue, cs.getMaltParams(), progressPrinter);
			dependencyParser.setName("DependencyParser-"+i);
			dependencyParser.start();
		}
	}

	public static void postProcess() {
		BlockingQueue<SentenceRetrievalJob> documentRetrievalQueue = new LinkedBlockingQueue<>(10);
		new PerfectDocumentRetrievalWorker(documentRetrievalQueue, dataDir, trainDir, testDir).start();
		
		BlockingQueue<PlagiarismJob> plagQueue = new LinkedBlockingQueue<>(10);
		
		int sentenceRetrievalThreads = cs.getSentenceRetrievalThreads();
		for (int i = 0; i < sentenceRetrievalThreads ; i++) {
			new SentenceRetrievalWorker(documentRetrievalQueue, plagQueue).start();
		}
		
		ProgressPrinter progressPrinter = new ProgressPrinter(Fileutils.getFileCount(dataDir+testDir));
		int plagThreads = cs.getPlagiarismThreads();
		for (int i = 0; i < plagThreads; i++) {
			new PlagiarismWorker(plagQueue, progressPrinter).start();
		}
	}
}
