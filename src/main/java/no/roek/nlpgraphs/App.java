package no.roek.nlpgraphs;

import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.LiveDependencyParser;
import no.roek.nlpgraphs.preprocessing.LivePosTagProducer;
import no.roek.nlpgraphs.search.PerfectDocumentRetrievalWorker;
import no.roek.nlpgraphs.search.SentenceRetrievalWorker;

public class App {

	private static String trainDir, testDir, dataDir;

	public static void main(String[] args) throws InterruptedException {
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

	public static void preprocess() {
		String parsedFilesDir = ConfigService.getParsedFilesDir();
		
		//TODO: rewrite to consider subfolders?
		DocumentFile[] files = Fileutils.getFileList(dataDir);
		
		
		
		
		BlockingQueue<ParseJob> queue = new LinkedBlockingQueue<ParseJob>(100);
		
		for (int i = 0; i < 5; i++) {
//			new PosTagProducer(queue).start();
		}
		
		for (int i = 0; i < 5; i++) {
			new DependencyParser(queue, ConfigService.getMaltParams()).start();
		}
		
	}
//	public static Thread preprocess(String input) {
//		BlockingQueue<DocumentFile> queue = new LinkedBlockingQueue<DocumentFile>(10);
//		DocumentFile[] files = Fileutils.getUnparsedFiles(Paths.get(input), parsedFilesDir);
//
//		int cpuCount = Runtime.getRuntime().availableProcessors();
//		int threadCount = 1;
//		if((files.length > 10 && cpuCount > 4)) {
//			threadCount = (cpuCount < 7) ? 2 : 7;
//		}
//
//		DependencyParser consumer  = new DependencyParser(queue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log", parsedFilesDir, threadCount);
//
//		DocumentFile[][] chunks = Fileutils.getChunks(files, threadCount);
//		System.out.println("thread count: "+threadCount+" chunks: "+chunks.length);
//
//		if(threadCount > chunks.length) {
//			threadCount = chunks.length;
//		}
//
//		for (int i = 0; i < threadCount; i++) {
//			PosTagProducer producer = new PosTagProducer(queue, chunks[i], "english-left3words-distsim.tagger");
//			new Thread(producer, "PosTagProducer: "+i).start();
//		}
//
//		Thread consumerThread = new Thread(consumer, "maltparserConsumer");
//		consumerThread.start();
//
//		return consumerThread;
//	}

//	private static void postProcess() {
//		System.out.println("starting postprocessing");
//		
//		BlockingQueue<PlagJob> queue = new LinkedBlockingQueue<>();
//		
//		new DocumentRetrievalWorker(queue, dataDir, parsedFilesDir, trainDir, testDir).start();
//		
//		for (int i = 0; i < getThreadCount(); i++) {
//			PlagiarismWorker consumer = new PlagiarismWorker(queue);
//			consumer.setName("PlagiarismWorker-"+i);
//			consumer.start();
//		}
//	}
	
	private static int getThreadCount() {
		int threads = Runtime.getRuntime().availableProcessors() - 2;
		if(threads < 2) {
			return 1;
		}
		
		return threads;
	}
}
