package nlpgraphs.preprocessing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import nlpgraphs.misc.Fileutils;
import nlpgraphs.misc.POSFile;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;



public class DependencyParser implements Runnable {

	private final BlockingQueue<POSFile> queue;
	private MaltParserService maltService;
	private String outDir;
	private int producerThreadsCount;
	private int finishedProducerThreads;

	public DependencyParser(BlockingQueue<POSFile> queue, String maltParams, String outDir, int producerThreadsCount) {
		this.queue = queue;
		this.outDir = outDir;
		this.producerThreadsCount = producerThreadsCount;
		try {
			this.maltService = new MaltParserService();
			maltService.initializeParserModel(maltParams);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while(true) {
			try {
				POSFile posfile = queue.poll(2000, TimeUnit.SECONDS);
				consume(posfile);
				
				if(posfile.isLastInQueue()) {
					finishedProducerThreads++;
					if(finishedProducerThreads == producerThreadsCount) {
						System.out.println("All producer threads done, stopping consumer thread.");
						System.exit(0);
					}
				}
			} catch (InterruptedException | MaltChainedException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println("Consumer timed out after 10000 seconds with nothing from producer threads");
				System.exit(0);
			}
		}

	}
	public void consume(POSFile posfile) throws MaltChainedException, NullPointerException {
		List<String> parsedTokens = new ArrayList<>();
		for (String[] sentence : posfile.getSentences()) {
			parsedTokens.addAll(Arrays.asList(maltService.parseTokens(sentence)));
		}
		Fileutils.writeToFile(outDir+posfile.getRelPath(), parsedTokens.toArray(new String[0]));
		System.out.println("Done dependency parsing file "+posfile.getRelPath());
	}

}
