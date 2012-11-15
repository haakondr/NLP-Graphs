package no.roek.nlpgraphs.preprocessing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.ConfigService;
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
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class POSTagParser {

	private MaxentTagger tagger;
	private Morphology lemmatiser;
	
	public POSTagParser() {
		ConfigService cs = new ConfigService();
		lemmatiser = new Morphology();
		try {
			this.tagger = new MaxentTagger(cs.getPOSTaggerParams());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public ParseJob posTagFile(Path file) {
		
		BufferedReader reader = null;
		try {
			ParseJob parseJob = new ParseJob(file);

			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file.toFile())));
			DocumentPreprocessor dp = new DocumentPreprocessor(reader);

			//TODO: somehow keep original text? PTBTokenizer documentation mentions invertible flag in options, but how to get original text?
			TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.PTBTokenizerFactory.newCoreLabelTokenizerFactory("untokenizable=noneKeep");
//			TokenizerFactory<CoreLabel> ptbTokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "untokenizable=noneKeep, invertible=true");
			dp.setTokenizerFactory(ptbTokenizerFactory);
			
			for(NLPSentence sentence : getSentences(dp, file.getFileName().toString())) {
				parseJob.addSentence(sentence);
			}

			return parseJob;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} finally {
			IOUtils.closeQuietly(reader);
		}
		return null;
	}
	
	private List<NLPSentence> getSentences(DocumentPreprocessor dp, String filename) {
		List<NLPSentence> sentences = new ArrayList<>();
		
		int sentenceNumber = 1;
		for(List<HasWord> words : dp) {
			List<TaggedWord> taggedWords = tagger.tagSentence(words);
			if(taggedWords.size()>0) {
				int start = taggedWords.get(0).beginPosition();

				int end = taggedWords.get(taggedWords.size()-1).endPosition();
				NLPSentence sentence = new NLPSentence(filename, sentenceNumber, start, end-start);
				sentence.setPostags(getPosTagString(taggedWords));
				sentences.add(sentence);
				sentenceNumber++;
			}
		}
		
		return sentences;
	}
	
	private String[] getPosTagString(List<TaggedWord> taggedWords) {
		List<String> temp = new ArrayList<>();
		int i = 1;
		for (TaggedWord token : taggedWords) {
			WordLemmaTag lemma = lemmatiser.lemmatize(new WordTag(token.word(), token.tag()));

			temp.add(i+"\t"+token.word()+"\t"+lemma.lemma()+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
			i++;
		}

		return temp.toArray(new String[0]);
	}
}
