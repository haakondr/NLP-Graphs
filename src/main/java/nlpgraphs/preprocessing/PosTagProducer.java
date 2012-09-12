package nlpgraphs.preprocessing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import nlpgraphs.classes.DocumentFile;
import nlpgraphs.classes.Sentence;


import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class PosTagProducer implements Runnable{

	private final BlockingQueue<DocumentFile> queue;
	private DocumentFile[] files;
	private MaxentTagger tagger;

	public PosTagProducer(BlockingQueue<DocumentFile> queue, DocumentFile[] files, String taggerParams){
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

		for (DocumentFile file : files) {
			i++;
			DocumentFile taggedFile = tagFile(file, i==filecount);
			System.out.println(Thread.currentThread().getName()+": done POS-tagging file "+file.getRelPath());
			try {
				queue.put(taggedFile);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}


	public DocumentFile tagFile(DocumentFile file, boolean isLastInQueue) {
		file.setLastInQueue(isLastInQueue);

		try {
			//TODO: hent ut setninger manuelt, lag HasWords og tagSentence. ingen grunn til å bruke tokenizeText da (?)
			List<List<HasWord>> sentences = MaxentTagger.tokenizeText(new BufferedReader(new FileReader(file.getPath().toString())));
			int sentenceNumber = 1;
			for (List<HasWord> sentence : sentences) {
				List<TaggedWord> taggedSentence = tagger.tagSentence(sentence);
				List<String> temp = new ArrayList<>();


				int i = 1;
				for (TaggedWord token : taggedSentence) {

					temp.add(sentenceNumber+"_"+i+"\t"+token.word()+"\t"+"_"+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
					i++;
				}
				sentenceNumber++;
				file.addSentence(createSentence(file.getOriginalText(), sentenceNumber, taggedSentence, temp));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return file;
	}
	
	public Sentence createSentence(String originalText, int sentenceNumber,List<TaggedWord> taggedSentence, List<String> taggedTokens) {
		int begin = taggedSentence.get(0).beginPosition();
		int end = taggedSentence.get(taggedSentence.size()-1).endPosition();
		String origSentence = originalText.substring(begin, end);
		System.out.println(origSentence);
		System.out.println();
		return new Sentence(sentenceNumber, begin, origSentence,taggedTokens.toArray(new String[0]));
	}
}
