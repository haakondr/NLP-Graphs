package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.search.SentenceUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.maltparser.MaltParserService;
import org.maltparser.core.exception.MaltChainedException;

import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.WordLemmaTag;
import edu.stanford.nlp.ling.WordTag;
import edu.stanford.nlp.process.Morphology;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class ParseUtils {

	public static ParseJob posTagFile(File file, MaxentTagger tagger) {
		ParseJob parseJob = new ParseJob(file.toPath());

		for (NLPSentence sentence : SentenceUtils.getSentences(file.toString())) {
			sentence.setPostags(getPosTagString(sentence, tagger));
			parseJob.addSentence(sentence);
		} 

		return parseJob;
	}
	
	public static String[] getPosTagString(NLPSentence sentence, MaxentTagger tagger) {
		List<TaggedWord> taggedSentence = tagger.tagSentence(sentence.getWords());
		Morphology m =new Morphology();

		List<String> temp = new ArrayList<>();
		int i = 1;
		for (TaggedWord token : taggedSentence) {
			WordLemmaTag lemma = m.lemmatize(new WordTag(token.word(), token.tag()));
			
			temp.add(i+"\t"+token.word()+"\t"+lemma.lemma()+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
			i++;
		}

		return temp.toArray(new String[0]);
	}
	
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
