package no.roek.nlpgraphs;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import no.roek.nlpgraphs.jobs.ParseJob;
import no.roek.nlpgraphs.jobs.PlagJob;
import no.roek.nlpgraphs.jobs.PostagJob;
import no.roek.nlpgraphs.jobs.SimilarityJob;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;
import no.roek.nlpgraphs.search.DocumentRetrievalWorker;
import no.roek.nlpgraphs.search.SentenceRetrievalWorker;

public class App {

	private static String parsedFilesDir, trainDir, testDir, dataDir;

	public static void main(String[] args) throws InterruptedException {
		dataDir = ConfigService.getDataDir();
		testDir = ConfigService.getTestDir();
		trainDir = ConfigService.getTrainDir();
		
		BlockingQueue<PlagJob> documentRetrievalQueue = new LinkedBlockingQueue<>();
		new DocumentRetrievalWorker(documentRetrievalQueue, dataDir, parsedFilesDir, trainDir, testDir).start();
		
		BlockingQueue<PostagJob> posTagQueue = new LinkedBlockingQueue<>();
		new SentenceRetrievalWorker(documentRetrievalQueue, posTagQueue).start();
		
		BlockingQueue<ParseJob> parseQueue  = new LinkedBlockingQueue<>();
		for (int i = 0; i < 3; i++) {
			new PosTagProducer(posTagQueue, parseQueue,  "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log").start();
		}
		
		BlockingQueue<SimilarityJob> distQueue = new LinkedBlockingQueue<>();
		for (int i = 0; i < 3; i++) {
			new DependencyParser(parseQueue, distQueue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log").start();
		}
		
		//TODO: lag maltparser threads
		//TODO: lag plagiarismworker threads
		
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
