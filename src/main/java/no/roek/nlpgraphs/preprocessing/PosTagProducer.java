package no.roek.nlpgraphs.preprocessing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import no.roek.nlpgraphs.concurrency.Job;
import no.roek.nlpgraphs.concurrency.ParseJob;
import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.TextPair;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.SentenceUtils;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer extends Thread {

	private final BlockingQueue<ParseJob> queue;
	private MaxentTagger tagger;
	
	public PosTagProducer(BlockingQueue<ParseJob> queue){
		this.queue = queue;
		
		try {
			this.tagger = new MaxentTagger(ConfigService.getPOSTaggerParams());
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		boolean running = true;
		while(running) {
			try {
				File[] files = Fileutils.getFiles(ConfigService.getDataDir());
				
				int fileCount = files.length;
				for (int i = 0; i < files.length; i++) {
					List<ParseJob> jobs = tagFile(files[i]);
					for (int j = 0; j < jobs.size(); j++) {
						if(i == fileCount) {
							if(j==jobs.size()) {
								jobs.get(i).setLastInQueue(true);
							}
						}
						queue.put(jobs.get(i));
					}
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public String[] getPosTagString(NLPSentence sentence) {
		List<TaggedWord> taggedSentence = tagger.tagSentence(sentence.getWords());

		List<String> temp = new ArrayList<>();
		int i = 1;
		for (TaggedWord token : taggedSentence) {
			temp.add(i+"\t"+token.word()+"\t"+"_"+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
            i++;
		}

		return temp.toArray(new String[0]);
	}

	public List<ParseJob> tagFile(File file) {
		List<ParseJob> parseJobs = new ArrayList<>();
		
		for (NLPSentence sentence : SentenceUtils.getSentences(file.toString())) {
				ParseJob parseJob = new ParseJob(file.toPath());
				sentence.setPostags(getPosTagString(sentence));
				parseJob.setSentence(sentence);
				parseJobs.add(parseJob);
		} 

		return parseJobs;
	}
}
