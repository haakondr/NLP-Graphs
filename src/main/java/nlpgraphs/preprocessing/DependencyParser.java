package nlpgraphs.preprocessing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import nlpgraphs.classes.DocumentFile;
import nlpgraphs.classes.Sentence;
import nlpgraphs.misc.Fileutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;


import edu.stanford.nlp.util.StringUtils;



public class DependencyParser implements Runnable {

	private final BlockingQueue<DocumentFile> queue;
	private MaltParserService maltService;
	private String outDir;
	private int producerThreadsCount;
	private int finishedProducerThreads;

	public DependencyParser(BlockingQueue<DocumentFile> queue, String maltParams, String outDir, int producerThreadsCount) {
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
		boolean run = true;
		while(run) {
			try {
				DocumentFile posfile = queue.poll(2000, TimeUnit.SECONDS);
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
				run = false;
			}
		}

	}
	public void consume(DocumentFile posfile) throws MaltChainedException, NullPointerException {
		List<String> parsedTokens = new ArrayList<>();
		int sentenceNumber = 1;
		JSONObject out = new JSONObject();
		try {
			out.put("filename", posfile.getPath().getFileName().toString());
			JSONArray jsonSentences = new JSONArray();

			for (Sentence sentence : posfile.getSentences()) {
				String[] parsedSentences = maltService.parseTokens(sentence.getPostags());

				JSONObject jsonSentence = new JSONObject();
				jsonSentence.put("sentenceNumber", sentenceNumber);
				jsonSentence.put("originalText", sentence.getText());
				jsonSentence.put("offset", sentence.getOffset());
				jsonSentence.put("length", sentence.getLength());
				JSONArray jsonTokens = new JSONArray();
				for (String parsedToken : parsedSentences) {
					String[] token = parsedToken.split("\t");
					
					JSONObject jsonToken = new JSONObject();
					jsonToken.put("id", token[0]);
					jsonToken.put("word", token[1]);
					jsonToken.put("pos", token[4]);
					jsonToken.put("rel", sentenceNumber+"_"+token[6]);
					jsonToken.put("deprel", token[7]);
					jsonTokens.put(jsonToken);
				}
				jsonSentence.put("tokens", jsonTokens);
				sentenceNumber++;
				jsonSentences.put(jsonSentence);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
//		Fileutils.writeToFile(outDir+posfile.getRelPath(), parsedTokens.toArray(new String[0]));
		Fileutils.writeToFile(outDir+posfile.getRelPath(), out.toString());
		System.out.println("Done dependency parsing file "+posfile.getRelPath());
	}


}
