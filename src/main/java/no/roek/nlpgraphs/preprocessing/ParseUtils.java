package no.roek.nlpgraphs.preprocessing;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.SentenceUtils;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class ParseUtils {

//	public static ParseJob posTagFile(File file, MaxentTagger tagger) {
//		//TODO: do sentence parsing here, with stanford documentPreprocessor
//
//		BufferedReader reader;
//		try {
//			ParseJob parseJob = new ParseJob(file.toPath());
//
//			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
//			DocumentPreprocessor dp = new DocumentPreprocessor(reader);
//
//			TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "untokenizable=noneKeep");
//			dp.setTokenizerFactory(ptbTokenizerFactory);
//
//			
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} finally {
//			IOUtils.closeQuietly(reader);
//		}
//
//
//		//		for (NLPSentence sentence : SentenceUtils.getSentences(file.toString())) {
//		//			sentence.setPostags(getPosTagString(sentence, tagger));
//		//			parseJob.addSentence(sentence);
//		//		} 
//
//		return parseJob;
//	}

//	public static List<NLPSentence> getSentences(List<List<HasWord>> document, MaxentTagger tagger, String filename) {
//		List<NLPSentence> sentences = new ArrayList<>();
//		
//		int sentenceNumber = 1;
//		for(List<HasWord> words : document) {
//			List<TaggedWord> taggedWords = tagger.tagSentence(words);
//			if(taggedWords.size()>0) {
//				int start = taggedWords.get(0).beginPosition();
//
//				int end = taggedWords.get(taggedWords.size()-1).endPosition();
//				NLPSentence sentence = new NLPSentence(filename, sentenceNumber, start, end-start);
//				sentenceNumber++;
//			}
//		}
//	}
	
	
//	public static String[] getPosTagString(List<HasWord> sentence, MaxentTagger tagger) {
//
//		List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
//		Morphology m =new Morphology();
//
//		List<String> temp = new ArrayList<>();
//		int i = 1;
//		for (TaggedWord token : taggedSentence) {
//			WordLemmaTag lemma = m.lemmatize(new WordTag(token.word(), token.tag()));
//
//			temp.add(i+"\t"+token.word()+"\t"+lemma.lemma()+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
//			i++;
//		}
//
//		return temp.toArray(new String[0]);
//	}

	//	public static List<List<CoreLabel>> getSentences(String file, MaxentTagger tagger) {
	//		TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "untokenizable=noneKeep");
	//		BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
	//		DocumentPreprocessor dp = new DocumentPreprocessor(reader);
	//		dp.setTokenizerFactory(ptbTokenizerFactory);
	//		
	//		for(List<HasWord> sentence : dp) {
	//			List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
	//			for (TaggedWord taggedWord : taggedSentence) {
	//				taggedWord.beginPosition();
	//			}
	//		}
	//	}

	public static JSONObject dependencyParse(ParseJob job, String outDir, MaltParserService maltService) throws MaltChainedException, NullPointerException {
		JSONObject out = new JSONObject();
		try {
			out.put("filename", job.getFilename());

			JSONObject jsonSentences = new JSONObject();
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
				jsonSentences.put(Integer.toString(sentence.getNumber()), jsonSentence);
			}

			out.put("sentences", jsonSentences);
		} catch (JSONException e) {
			e.printStackTrace();
		}

		Fileutils.writeToFile(outDir+job.getParentDir()+job.getFilename(), out.toString());
		return out;
	}
}
