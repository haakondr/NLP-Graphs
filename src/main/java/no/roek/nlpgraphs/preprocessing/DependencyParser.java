package no.roek.nlpgraphs.preprocessing;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

public class DependencyParser extends Thread{
	private final BlockingQueue<ParseJob> queue;
	private MaltParserService maltService;
	private String parsedFilesDir;

	public DependencyParser(BlockingQueue<ParseJob> queue,  String maltParams) {
		this.queue = queue;
		this.parsedFilesDir = ConfigService.getParsedFilesDir();
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
				ParseJob job = queue.take();
				if(job.isLastInQueue()) {
					running = false;
					break;
				}
				consume(job);

			} catch (InterruptedException | NullPointerException | MaltChainedException e) {
				e.printStackTrace();
				running = false;
			}
		}

	}

	public void consume(ParseJob posfile) throws MaltChainedException, NullPointerException {
		JSONObject out = new JSONObject();
		try {
			out.put("filename", posfile.getFilename());

			NLPSentence sentence = posfile.getSentence();
			String[] parsedSentences = maltService.parseTokens(sentence.getPostags());

			JSONObject jsonSentence = sentence.toJson();
			JSONArray jsonTokens = new JSONArray();
			for (String parsedToken : parsedSentences) {
				String[] token = parsedToken.split("\t");

				JSONObject jsonToken = new JSONObject();
				jsonToken.put("id", token[0]);
				jsonToken.put("word", token[1]);
				jsonToken.put("pos", token[4]);
				jsonToken.put("rel", token[6]);
				jsonToken.put("deprel", token[7]);
				jsonTokens.put(jsonToken);
			}
			jsonSentence.put("tokens", jsonTokens);
			out.put("sentence", jsonSentence);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Fileutils.writeToFile(parsedFilesDir+posfile.getParsedFilename(), out.toString());
	}
}
