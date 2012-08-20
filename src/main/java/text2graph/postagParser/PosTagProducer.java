package text2graph.postagParser;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import text2graph.dependencyParser.PlagFile;


import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer implements Runnable{

	private final BlockingQueue<POSFile> queue;
	private PlagFile[] files;
	private MaxentTagger tagger;

	public PosTagProducer(BlockingQueue<POSFile> queue, PlagFile[] files, String taggerParams){
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
		for (PlagFile file : files) {
			
			System.out.println(Thread.currentThread().getName()+" producing file "+file.getRelPath());
			POSFile taggedFile = tagFile(file);
			System.out.println(Thread.currentThread().getName()+" done parsing file "+file.getRelPath());
			try {
				queue.put(taggedFile);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	public POSFile tagFile(PlagFile file) {
		POSFile taggedFile = new POSFile(file);

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
				taggedFile.addSentence(temp.toArray(new String[0]));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return taggedFile;
	}

}
