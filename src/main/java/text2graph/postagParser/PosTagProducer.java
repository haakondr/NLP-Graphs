package text2graph.postagParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import text2graph.misc.POSFile;

import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer implements Runnable{

	private final BlockingQueue<POSFile> queue;
	private POSFile[] files;
	private MaxentTagger tagger;

	public PosTagProducer(BlockingQueue<POSFile> queue, POSFile[] files, String taggerParams){
		this.queue = queue;
		this.files = files;
		try {
			this.tagger = new MaxentTagger(taggerParams);
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		int i = 0;
		int filecount = files.length;
		
		for (POSFile file : files) {
			i++;
			POSFile taggedFile = tagFile(file, i==filecount);
			System.out.println(Thread.currentThread().getName()+": done POS-tagging file "+file.getRelPath());
			try {
				queue.put(taggedFile);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	public POSFile tagFile(POSFile file, boolean isLastInQueue) {
		file.setLastInQueue(isLastInQueue);
		
		try {
			List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(file.getPath())));

			for (List<HasWord> sentence : sentences) {
				List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
				List<String> temp = new ArrayList<>();
				
				int i = 1;
				for (TaggedWord token : taggedSentence) {
					temp.add(i+"\t"+token.word()+"\t"+"_"+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
					i++;
				}
				file.addSentence(temp.toArray(new String[0]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return file;
	}

}
