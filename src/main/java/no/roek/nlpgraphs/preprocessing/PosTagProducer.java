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
	private File[] files;

	public PosTagProducer(BlockingQueue<ParseJob> queue, File[] files){
		this.queue = queue;
		this.files = files;

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
				for (File file : files) {
					List<ParseJob> jobs = tagFile(file);
					for (ParseJob parseJob : jobs) {
						queue.put(parseJob);
					}
					jobs = null;
					System.out.println(Thread.currentThread().getName()+" done pos-tagging file "+file.getName()+". currently "+queue.size()+ " files in queue.");
				}

				for (int i = 0; i < 100; i++) {
					ParseJob poisonPill = new ParseJob("threads should terminate when this job is encountered");
					poisonPill.setLastInQueue(true);
					queue.put(poisonPill);
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
