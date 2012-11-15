package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;

import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;


import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

public class DependencyParser {

	private MaltParserService maltService;
	private IDictionary dict;

	public DependencyParser() {
		ConfigService cs = new ConfigService();
		String wordnetDir = cs.getWordNetDir();
		dict = new Dictionary(new File(wordnetDir+"dict"));
		try {
			dict.open() ;
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			this.maltService = new MaltParserService();
			maltService.initializeParserModel(cs.getMaltParams());
		} catch (MaltChainedException e) {
			e.printStackTrace();
		}
	}

	public void dependencyParse(ParseJob job, String outDir) throws MaltChainedException, NullPointerException {
		try {
			File dir = new File(outDir+job.getParentDir()+job.getFilename());
			if(!dir.exists()) {
				dir.mkdir();
			}
			
			for (NLPSentence sentence : job.getSentences()) {
				String[] parsedSentences = maltService.parseTokens(sentence.getPostags());

				JSONObject jsonSentence = sentence.toJson();
				JSONArray jsonTokens = new JSONArray();
				for (String parsedToken : parsedSentences) {
					String[] token = parsedToken.split("\t");

					JSONObject jsonToken = new JSONObject();
					jsonToken.put("id", token[0]);
					jsonToken.put("word", token[1]);
					jsonToken.put("lemma", token[2]);
					jsonToken.put("pos", token[4]);
					jsonToken.put("rel", token[6]);
					jsonToken.put("deprel", token[7]);
					jsonTokens.put(jsonToken);
				}
				jsonSentence.put("tokens", jsonTokens);
				jsonSentence.put("filename", job.getFilename());
				Fileutils.writeToFile(outDir+job.getParentDir()+job.getFilename()+"/"+sentence.getNumber(), jsonSentence.toString());
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	
//TODO: synonyms are omitted untill they prove useful. Currently the synsets are too large
//	public JSONArray getSynonyms(String lemma, String pos) {
//		JSONArray jsonSynonyms = new JSONArray();
//		POS jwiPos = getJWIPOSTag(pos);
//		if(jwiPos!= null) {
//			IIndexWord idxWord = dict.getIndexWord(lemma, jwiPos);
//
//			if(idxWord!=null) {
//				for(IWordID wordId : idxWord.getWordIDs()) {
//					IWord word = dict.getWord(wordId);
//
//					for(IWord synonym : word.getSynset().getWords()) {
//						String temp = synonym.getLemma();
//						if(!temp.equalsIgnoreCase(lemma)) {
//							if(!temp.contains("_")) {
//								jsonSynonyms.put(synonym.getLemma());
//							}
//						}
//					}
//				}
//			}
//		}
//
//		return jsonSynonyms;
//	}
//
//	private POS getJWIPOSTag(String pos) {
//		if(pos.equals("NN") || pos.equals("NNS")) {
//			return POS.NOUN;
//		}else if(pos.startsWith("VB")) {
//			return POS.VERB;
//		}else if(pos.startsWith("RB")) {
//			return POS.ADVERB;
//		}else if(pos.startsWith("JJ")) {
//			return POS.ADJECTIVE;
//		}
//
//		return null;
//	}
}
