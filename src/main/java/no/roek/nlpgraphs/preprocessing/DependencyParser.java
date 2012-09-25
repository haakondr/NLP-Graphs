package no.roek.nlpgraphs.preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.document.GraphPair;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.TextPair;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.GraphUtils;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;



public class DependencyParser extends Thread {

	private final BlockingQueue<Job> queue;
	private final BlockingQueue<Job> distQueue;
	private MaltParserService maltService;

	public DependencyParser(BlockingQueue<Job> queue, BlockingQueue<Job> distQueue, String maltParams) {
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
			parsedTokens[6] = sentence.getNumber()+"_"+parsedTokens[6];
			System.out.println(parsedTokens);
			
			return GraphUtils.getGraph(parsedTokens, sentence);
		} catch (MaltChainedException e) {
			e.printStackTrace();
			return null;
		}
	}

	//	public Node getNode(String word, HashMap<String, Node> nodes) {
	//		String[] token = word.split("\t");
	//		Node node = new Node(token[0], new String[] {token[1], token[4]});
	//		nodes.put(node.getId(), node);
	//		return node;
	//	}

	//	public void consume(ParseJob posfile) throws MaltChainedException, NullPointerException {
	////		List<String> parsedTokens = new ArrayList<>();
	//		int sentenceNumber = 1;
	//		JSONObject out = new JSONObject();
	//		try {
	//			out.put("filename", posfile.getPath().getFileName().toString());
	//			JSONArray jsonSentences = new JSONArray();
	//
	//			for (NLPSentence sentence : posfile.getSentences()) {
	//				String[] parsedSentences = maltService.parseTokens(sentence.getPostags());
	//
	//				JSONObject jsonSentence = new JSONObject();
	//				jsonSentence.put("sentenceNumber", sentenceNumber);
	//				jsonSentence.put("originalText", sentence.getText());
	//				jsonSentence.put("offset", sentence.getStart());
	//				jsonSentence.put("length", sentence.getLength());
	//				JSONArray jsonTokens = new JSONArray();
	//				for (String parsedToken : parsedSentences) {
	//					String[] token = parsedToken.split("\t");
	//					
	//					JSONObject jsonToken = new JSONObject();
	//					jsonToken.put("id", token[0]);
	//					jsonToken.put("word", token[1]);
	//					jsonToken.put("pos", token[4]);
	//					jsonToken.put("rel", sentenceNumber+"_"+token[6]);
	//					jsonToken.put("deprel", token[7]);
	//					jsonTokens.put(jsonToken);
	//				}
	//				jsonSentence.put("tokens", jsonTokens);
	//				sentenceNumber++;
	//				jsonSentences.put(jsonSentence);
	//				out.put("sentences", jsonSentences);
	//			}
	//		} catch (JSONException e) {
	//			e.printStackTrace();
	//		}
	//		Fileutils.writeToFile(outDir+posfile.getRelPath(), out.toString());
	//		System.out.println("Done dependency parsing file "+posfile.getRelPath());
	//	}


}
