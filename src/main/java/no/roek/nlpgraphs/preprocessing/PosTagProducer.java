package no.roek.nlpgraphs.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.TextPair;
import no.roek.nlpgraphs.misc.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer extends Thread {

	private final BlockingQueue<Job> queue;
	private final BlockingQueue<Job> parseQueue;
	private MaxentTagger tagger;

	public PosTagProducer(BlockingQueue<Job> queue, BlockingQueue<Job> parseQueue, String taggerParams){
		this.queue = queue;
		this.parseQueue = parseQueue;
		try {
			this.tagger = new MaxentTagger(taggerParams);
		} catch (ClassNotFoundException | IOException e) {
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
				parseQueue.put(getPosTags(job));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	public Job getPosTags(Job job) {
		//TODO: this might not update correctly. might have to create a new object? alternatively a new attr "taggedTextPairs" or something
		for(TextPair pair : job.getTextPairs()) {
			NLPSentence taggedTestSentence = getMaltString(pair.getTestSentence());
			NLPSentence taggedTrainSentence = getMaltString(pair.getTrainSentence());		
			pair.setTestSentence(getMaltString(taggedTestSentence));
			pair.setTrainSentence(getMaltString(taggedTrainSentence));
		}

		return job;
	}

	public NLPSentence getMaltString(NLPSentence sentence) {
		List<TaggedWord> taggedSentence = tagger.tagSentence(sentence.getWords());

		List<String> temp = new ArrayList<>();
		int i = 1;
		for (TaggedWord token : taggedSentence) {
			temp.add(sentence.getNumber()+"_"+i+"\t"+token.word()+"\t"+"_"+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
		}

		sentence.setPostaggedTokens(temp.toArray(new String[0]));

		return sentence;
	}

	public DocumentFile tagFile(DocumentFile file, boolean isLastInQueue) {
		file.setLastInQueue(isLastInQueue);
		file.setSentences(SentenceUtils.getSentences(file.getPath().toString()));

		int sentenceNumber = 1;
		for (NLPSentence sentence : file.getSentences()) {
			try {
				List<TaggedWord> taggedSentence = tagger.tagSentence(sentence.getWords());
				List<String> temp = new ArrayList<>();

				int i = 1;
				for (TaggedWord token : taggedSentence) {
					temp.add(sentenceNumber+"_"+i+"\t"+token.word()+"\t"+"_"+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
					i++;
				}
				sentenceNumber++;
				sentence.setPostags(temp.toArray(new String[0]));
			}catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
			}
		} 

		return file;
	}
}
