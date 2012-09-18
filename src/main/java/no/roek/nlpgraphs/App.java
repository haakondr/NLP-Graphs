package no.roek.nlpgraphs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;

public class App {

	private static String parsedFilesDir, trainDir, testDir, dataDir, resultsFile, annotationsDir;

	public static void main(String[] args) throws InterruptedException {
		init();

		if(shouldPreprocess()) {
			preprocess(dataDir).join();
		}
		postProcess(dataDir+trainDir, parsedFilesDir+trainDir, parsedFilesDir+testDir);
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
		annotationsDir = ConfigService.getAnnotationsDir();
		resultsFile = ConfigService.getResultsFile();
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

	private static void postProcess(String originalTrainDir, String trainDir, String testDir) {
		System.out.println("starting postprocessing");
		File[] test = Fileutils.getFiles(Paths.get(testDir));

		int threads = Runtime.getRuntime().availableProcessors() - 2;
		if(test.length < threads) {
			threads = test.length;
		}else if(threads < 2) {
			threads = 1;
		}
		List<File[]> chunks = Fileutils.getChunks(test, threads);
		
		System.out.println("using "+threads+" threads");
		
		for (int i = 0; i < chunks.size(); i++) {
			PlagiarismWorker worker = new PlagiarismWorker(chunks.get(i));
			worker.setName("PlagiarismWorker-"+i);
			worker.start();
		}
	}
}
