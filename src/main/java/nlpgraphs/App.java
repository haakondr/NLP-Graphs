package nlpgraphs;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import nlpgraphs.classes.POSFile;
import nlpgraphs.misc.Fileutils;
import nlpgraphs.preprocessing.DependencyParser;
import nlpgraphs.preprocessing.PosTagProducer;


public class App {
    public static void main( String[] args ) {
		BlockingQueue<POSFile> queue = new LinkedBlockingQueue<POSFile>();
		
		//TODO: spør om man skal fortsette fra tidligere runs, eller starte på nytt 
		Path input = Paths.get(args[0]);
		POSFile[] files = Fileutils.getUnparsedFiles(input, args[1]);

		int cpuCount = Runtime.getRuntime().availableProcessors();
		int threadCount = 1;
		if((files.length > 10 && cpuCount > 4)) {
			threadCount = (cpuCount < 10) ? 2 : 10;
		}
		
		DependencyParser consumer  = new DependencyParser(queue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log", args[1], threadCount);
		
		POSFile[][] chunks = Fileutils.getChunks(files, threadCount);
		System.out.println("thread count: "+threadCount+" chunks: "+chunks.length);

		for (int i = 0; i < threadCount; i++) {
			PosTagProducer producer = new PosTagProducer(queue, chunks[i], "english-left3words-distsim.tagger");
			new Thread(producer, "PosTagProducer: "+i).start();
		}

		new Thread(consumer, "maltparserConsumer").start();
    }
}
