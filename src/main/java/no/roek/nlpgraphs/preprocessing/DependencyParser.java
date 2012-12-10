package no.roek.nlpgraphs.preprocessing;


import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.DatabaseService;

import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


public class DependencyParser {

	private MaltParserService maltService;

	public DependencyParser() {
		ConfigService cs = new ConfigService();
		try {
			this.maltService = new MaltParserService();
			maltService.initializeParserModel(cs.getMaltParams());
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}

	public void dependencyParse(ParseJob job, DatabaseService db) {
		for(NLPSentence sentence : job.getSentences()) {
			db.addSentence(parseSentence(sentence));
		}
	}

	public BasicDBObject parseSentence(NLPSentence sentence) {
		BasicDBObject obj = new BasicDBObject();
		try {
			obj.put("id", sentence.getFilename()+"-"+sentence.getNumber());
			obj.put("filename", sentence.getFilename());
			obj.put("sentenceNumber", sentence.getNumber());
			obj.put("offset", sentence.getStart());
			obj.put("length", sentence.getLength());
			String[] parsedSentence = maltService.parseTokens(sentence.getPostags());

			BasicDBList tokenList = new BasicDBList();
			for(String parsedToken : parsedSentence) {
				tokenList.add(getToken(parsedToken));
			}

			obj.put("tokens", tokenList);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
		
		return obj;
	}

	public BasicDBObject parseSentence(String[] postagString, String filename, int sentenceNumber, int offset, int length) {
		BasicDBObject obj = new BasicDBObject();

		try {
			obj.put("filename", filename);
			obj.put("sentenceNumber", sentenceNumber);
			obj.put("offset", offset);
			obj.put("length", length);
			String[] parsedSentence = maltService.parseTokens(postagString);
			BasicDBList tokenList = new BasicDBList();
			for(String parsedToken : parsedSentence) {
				tokenList.add(getToken(parsedToken));
			}

			obj.put("tokens", tokenList);
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}

		return obj;
	}

	public BasicDBObject getToken(String parsedToken) {
		BasicDBObject obj = new BasicDBObject();
		String[] token = parsedToken.split("\t");
		obj.put("id", token[0]);
		obj.put("word", token[1]);
		obj.put("lemma", token[2]);
		obj.put("pos", token[4]);
		obj.put("rel", token[6]);
		obj.put("deprel", token[7]);

		return obj;
	}
}
