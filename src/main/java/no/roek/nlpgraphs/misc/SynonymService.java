//package no.roek.nlpgraphs.misc;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//import edu.mit.jwi.Dictionary;
//import edu.mit.jwi.IDictionary;
//import edu.mit.jwi.item.IIndexWord;
//import edu.mit.jwi.item.IWord;
//import edu.mit.jwi.item.IWordID;
//import edu.mit.jwi.item.POS;
//
//public class SynonymService {
//
//	private IDictionary dict;
//
//	public SynonymService() {
//		ConfigService cs = new ConfigService();
//		dict = new Dictionary(new File(cs.getWordNetDir()+"dict"));
//	}
//
//	public Set<String> getSynonyms(String lemma, String pos) {
//		Set<String> synonyms = new HashSet<>();
//		POS jwiPos = getJWIPOSTag(pos);
//		if(jwiPos!= null) {
//			try {
//				dict.open();
//				IIndexWord idxWord = dict.getIndexWord(lemma, jwiPos);
//				if(idxWord!=null) {
//					for(IWordID wordId : idxWord.getWordIDs()) {
//						IWord word = dict.getWord(wordId);
//						
//						for(IWord synonym : word.getSynset().getWords()) {
//							String temp = synonym.getLemma();
//							if(!temp.equalsIgnoreCase(lemma)) {
//								if(!temp.contains("_")) {
//									synonyms.add(synonym.getLemma());
//								}
//							}
//						}
//					}
//				}
//			} catch (IOException e) {
//				e.printStackTrace();
//			} finally {
//				dict.close();
//			}
//		}
//
//		return synonyms;
//	}
//
//	private  POS getJWIPOSTag(String pos) {
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
//}
