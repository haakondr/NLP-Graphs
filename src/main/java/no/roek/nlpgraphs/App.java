package no.roek.nlpgraphs;

import java.io.File;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.postprocessing.CandidateRetrievalWorker;
import no.roek.nlpgraphs.postprocessing.PlagJob;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;

public class App {

	private static String parsedFilesDir, trainDir, testDir, dataDir;

	public static void main(String[] args) throws InterruptedException {
		init();

		if(shouldPreprocess()) {
			preprocess(dataDir).join();
		}
		postProcess();
	}

	public static boolean shouldPreprocess() {
		DocumentFile[] files = Fileutils.getUnparsedFiles(Paths.get(dataDir), parsedFilesDir);
		return !(files.length == 0);
	}


	private static void init() {
		parsedFilesDir = ConfigService.getParsedFilesDir();
		dataDir = ConfigService.getDataDir();
		testDir = ConfigService.getTestDir();
		trainDir = ConfigService.getTrainDir();
	}

	public static Thread preprocess(String input) {
		BlockingQueue<DocumentFile> queue = new LinkedBlockingQueue<DocumentFile>(10);
		DocumentFile[] files = Fileutils.getUnparsedFiles(Paths.get(input), parsedFilesDir);

		int cpuCount = Runtime.getRuntime().availableProcessors();
		int threadCount = 1;
		if((files.length > 10 && cpuCount > 4)) {
			threadCount = (cpuCount < 7) ? 2 : 7;
		}

		DependencyParser consumer  = new DependencyParser(queue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log", parsedFilesDir, threadCount);

		DocumentFile[][] chunks = Fileutils.getChunks(files, threadCount);
		System.out.println("thread count: "+threadCount+" chunks: "+chunks.length);

		if(threadCount > chunks.length) {
			threadCount = chunks.length;
		}

		for (int i = 0; i < threadCount; i++) {
			PosTagProducer producer = new PosTagProducer(queue, chunks[i], "english-left3words-distsim.tagger");
			new Thread(producer, "PosTagProducer: "+i).start();
		}

		Thread consumerThread = new Thread(consumer, "maltparserConsumer");
		consumerThread.start();

		return consumerThread;
	}

	private static void postProcess() {
		System.out.println("starting postprocessing");
		
		BlockingQueue<PlagJob> queue = new LinkedBlockingQueue<>();
		
		new CandidateRetrievalWorker(queue, dataDir, parsedFilesDir, trainDir, testDir).start();
		
		for (int i = 0; i < getThreadCount(); i++) {
			PlagiarismWorker consumer = new PlagiarismWorker(queue);
			consumer.setName("PlagiarismWorker-"+i);
			consumer.start();
		}
	}
	
	private static int getThreadCount() {
		int threads = Runtime.getRuntime().availableProcessors() - 2;
		if(threads < 2) {
			return 1;
		}
		
		return threads;
	}
}
