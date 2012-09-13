package nlpgraphs.preprocessing;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import nlpgraphs.document.DocumentFile;
import nlpgraphs.document.NLPSentence;
import nlpgraphs.misc.SentenceUtils;


import edu.stanford.nlp.ling.TaggedWord;
import edu.stanford.nlp.ling.Word;
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
		file.setSentences(SentenceUtils.getSentences(file.getPath().toString()));

		int sentenceNumber = 1, wordnumber = 1;
		for (NLPSentence sentence : file.getSentences()) {
			try {
				List<TaggedWord> taggedSentence = tagger.tagSentence(sentence.getWords());
				List<String> temp = new ArrayList<>();

				int i = 1;
				for (TaggedWord token : taggedSentence) {
					wordnumber++;

					temp.add(sentenceNumber+"_"+i+"\t"+token.word()+"\t"+"_"+"\t"+token.tag()+"\t"+token.tag()+"\t"+"_");
					i++;
				}
				sentenceNumber++;
				sentence.setPostags(temp.toArray(new String[0]));
			}catch (IndexOutOfBoundsException e) {
				e.printStackTrace();
				for (Word word : sentence.getWords()) {
					System.out.print(word.word());
				}
				System.out.println(file.getPath().toString()+" sentence number: "+sentenceNumber+" wordnumber: "+wordnumber);
				System.out.println("previous sentence: "+file.getSentences().get(sentenceNumber-2).toString());
				System.out.println("sentence length:" +sentence.getLength());
			}
		} 

		return file;

	}
}
