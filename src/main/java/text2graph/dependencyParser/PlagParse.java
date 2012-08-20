package text2graph.dependencyParser;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import text2graph.Utils;
import text2graph.postagParser.POSFile;
import text2graph.postagParser.PosTagConsumer;
import text2graph.postagParser.PosTagProducer;




public class PlagParse {

	public static void main(String[] args) throws ClassNotFoundException, IOException {

		BlockingQueue<POSFile> queue = new LinkedBlockingQueue<POSFile>();
		PlagFile[] files = Utils.getUnparsedFiles(args[0], args[1]);

		PosTagConsumer consumer  = new PosTagConsumer(queue, "-c engmalt.linear-1.7.mco -m parse -w . -lfi parser.log", args[1]);


//		int cpuCount = Runtime.getRuntime().availableProcessors();
		int threadCount = 1;
//		if((files.length > 10 && cpuCount > 4)) {
//			System.out.println(cpuCount);
//			threadCount = (cpuCount < 10) ? 2 : 10;
//		}
		PlagFile[][] chunks = Utils.getChunks(files, threadCount);
		System.out.println("thread count: "+threadCount+" chunks: "+chunks.length);

		for (int i = 0; i < threadCount; i++) {
			PosTagProducer producer = new PosTagProducer(queue, chunks[i], "english-left3words-distsim.tagger");
			new Thread(producer, "PosTagProducer: "+i).start();
		}

		new Thread(consumer, "maltparserConsumer").start();
	}
}