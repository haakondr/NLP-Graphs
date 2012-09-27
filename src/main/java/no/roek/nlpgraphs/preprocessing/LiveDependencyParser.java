package no.roek.nlpgraphs.preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.GraphPair;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.TextPair;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.GraphUtils;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;



public class LiveDependencyParser extends Thread {

	private final BlockingQueue<Job> queue;
	private final BlockingQueue<Job> distQueue;
	private MaltParserService maltService;

	public LiveDependencyParser(BlockingQueue<Job> queue, BlockingQueue<Job> distQueue, String maltParams) {
		this.queue = queue;
		this.distQueue = distQueue;
		try {
			this.maltService = new MaltParserService();
			maltService.initializeParserModel(maltParams);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				Job job = queue.take();
				if(job.isLastInQueue()) {
					running = false;
					break;
				}
				distQueue.put(consume(job));
			} catch (InterruptedException e) {
				e.printStackTrace();
				running = false;
			}
		}

	}

	public Job consume(Job job) {
		List<GraphPair> graphPairs = new ArrayList<>();
		for (TextPair pair : job.getTextPairs()) {
			Graph test = getGraph(pair.getTestSentence());
			Graph train = getGraph(pair.getTrainSentence());
			graphPairs.add(new GraphPair(test, train));
		}
		job.setGraphPairs(graphPairs);

		return job;
	}

	public Graph getGraph(NLPSentence sentence) {
		try {
			String[] parsedTokens = maltService.parseTokens(sentence.getPostags());
			return GraphUtils.getGraph(parsedTokens, sentence);
		} catch (MaltChainedException e) {
			e.printStackTrace();
			return null;
		}
	}
}
