package no.roek.nlpgraphs.preprocessing;

import java.util.concurrent.BlockingQueue;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.ProgressPrinter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

public class DependencyParser extends Thread{
	private final BlockingQueue<ParseJob> queue;
	private MaltParserService maltService;
	private String parsedFilesDir;
	private ProgressPrinter progressPrinter;
	
	public DependencyParser(BlockingQueue<ParseJob> queue,  String maltParams, ProgressPrinter progressPrinter) {
		this.queue = queue;
		this.parsedFilesDir = ConfigService.getParsedFilesDir();
		this.progressPrinter = progressPrinter;
		
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
				progressPrinter.printProgressbar();

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

			JSONArray jsonSentences = new JSONArray();
			for (NLPSentence sentence : posfile.getSentences()) {
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
				jsonSentences.put(jsonSentence);
			}

			out.put("sentences", jsonSentences);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Fileutils.writeToFile(parsedFilesDir+posfile.getParentDir()+posfile.getFilename(), out.toString());
	}
}
