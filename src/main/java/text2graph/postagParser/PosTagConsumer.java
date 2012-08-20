package text2graph.postagParser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import models.POSFile;

public class PosTagConsumer implements Runnable {

	private final BlockingQueue<POSFile> queue;
	private MaltParserService maltService;
	private String outDir;

	public PosTagConsumer(BlockingQueue<POSFile> queue, String maltParams, String outDir) {
		this.queue = queue;
		this.outDir = outDir;
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
				consume(queue.poll(10000, TimeUnit.SECONDS));
			} catch (InterruptedException | MaltChainedException e) {
				e.printStackTrace();
			} catch (NullPointerException e) {
				System.out.println("Consumer timed out after 10000 seconds with nothing from producer threads");
				System.exit(0);
			}
		}

	}
	public void consume(POSFile posfile) throws MaltChainedException, NullPointerException {
		System.out.println("Consuming file "+posfile.getRelPath());
		System.out.println("Currently "+queue.size()+" files ready to be consumed");

		List<String> parsedTokens = new ArrayList<>();
		for (String[] sentence : posfile.getSentences()) {
			parsedTokens.addAll(Arrays.asList(maltService.parseTokens(sentence)));
		}
		Utils.writeToFile(outDir+posfile.getRelPath(), parsedTokens.toArray(new String[0]));
		System.out.println("Done parsing file "+posfile.getRelPath());
	}

}
