package nlpgraphs;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import nlpgraphs.classes.POSFile;
import nlpgraphs.graph.Graph;
import nlpgraphs.misc.Fileutils;
import nlpgraphs.misc.GraphUtils;
import nlpgraphs.preprocessing.DependencyParser;
import nlpgraphs.preprocessing.PosTagProducer;


public class App {
    public static void main( String[] args ) {
    	preprocess(args[0], args[1]);
    	
    }
    
    private static void preprocess(String input, String output) {
		BlockingQueue<POSFile> queue = new LinkedBlockingQueue<POSFile>();
		
		//TODO: spør om man skal fortsette fra tidligere runs, eller starte på nytt 
		POSFile[] files = Fileutils.getUnparsedFiles(Paths.get(input), output);

		int cpuCount = Runtime.getRuntime().availableProcessors();
		int threadCount = 1;
		if((files.length > 10 && cpuCount > 4)) {
			threadCount = (cpuCount < 10) ? 2 : 10;
		}
		
		DependencyParser consumer  = new DependencyParser(queue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log", output, threadCount);
		
		POSFile[][] chunks = Fileutils.getChunks(files, threadCount);
		System.out.println("thread count: "+threadCount+" chunks: "+chunks.length);

		for (int i = 0; i < threadCount; i++) {
			PosTagProducer producer = new PosTagProducer(queue, chunks[i], "english-left3words-distsim.tagger");
			new Thread(producer, "PosTagProducer: "+i).start();
		}

		new Thread(consumer, "maltparserConsumer").start();
    }
    
    private static void postProcess(String trainDir, String testDir) {
    	File[] trainFiles = Fileutils.getFiles(Paths.get(trainDir));
    	List<Graph> trainGraphs = new ArrayList<>();
    	for (File file : trainFiles) {
			trainGraphs.add(GraphUtils.parseGraph(file.toString()));
		}
    	File[] test = Fileutils.getFiles(Paths.get(testDir));
    	
    	new PlagiarismWorker(trainGraphs.toArray(new Graph[0]), Arrays.asList(test));
    }
}
