package no.roek.nlpgraphs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;


import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.postprocessing.PlagiarismWorker;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.PosTagProducer;

public class App {
	
	private static String parsedFilesDir, trainDir, testDir, dataDir, resultsFile;
	
	public static void main(String[] args) {
		init();

		preprocess(dataDir);
		postProcess(dataDir+trainDir, parsedFilesDir+trainDir, parsedFilesDir+testDir);
	}
	
	private static void init() {
		Properties configFile = new Properties();
		InputStream is;
		try {
			is = new FileInputStream("app.properties");
		
		configFile.load(is);
		
		parsedFilesDir = configFile.getProperty("PARSED_DIR");
		dataDir = configFile.getProperty("DATA_DIR");
		testDir = configFile.getProperty("TEST_DIR");
		trainDir = configFile.getProperty("TRAIN_DIR");
		resultsFile = configFile.getProperty("RESULTS_FILE");
		
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static void preprocess(String input) {
		BlockingQueue<DocumentFile> queue = new LinkedBlockingQueue<DocumentFile>();
		DocumentFile[] files = Fileutils.getUnparsedFiles(Paths.get(input), parsedFilesDir);
		
		int cpuCount = Runtime.getRuntime().availableProcessors();
		int threadCount = 1;
		if((files.length > 10 && cpuCount > 4)) {
			threadCount = (cpuCount < 10) ? 2 : 10;
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

		new Thread(consumer, "maltparserConsumer").start();
    }
	
	private static void postProcess(String originalTrainDir, String trainDir, String testDir) {
		File[] trainFiles = Fileutils.getFiles(Paths.get(trainDir));
		List<Graph> trainGraphs = new ArrayList<>();
		for (File file : trainFiles) {
			trainGraphs.add(GraphUtils.parseGraph(file.toPath()));
		}
		File[] test = Fileutils.getFiles(Paths.get(testDir));

		int cpuCount = Runtime.getRuntime().availableProcessors() - 2;
		int threads = cpuCount;
		if(test.length < cpuCount) {
			threads = test.length;
		}else if(cpuCount < 2) {
			cpuCount = 1;
		}

		System.out.println("using "+cpuCount+" threads");
		PlagiarismWorker worker = new PlagiarismWorker(trainGraphs.toArray(new Graph[0]), Arrays.asList(test), threads, Paths.get(originalTrainDir));

		ForkJoinPool pool = new ForkJoinPool();
		List<String> results = pool.invoke(worker);

		Fileutils.writeToFile(resultsFile, results.toArray(new String[0]));
	}
}
