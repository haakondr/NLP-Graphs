package no.roek.nlpgraphs.candretrieval;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.misc.SentenceUtils;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.similar.MoreLikeThis;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.util.Version;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;


public class CandidateRetrievalService {

	private FSDirectory index;
	private IndexWriterConfig indexWriterConfig;
	private IndexWriter writer;
	private String INDEX_DIR;
	private ConfigService cs;

	public CandidateRetrievalService(Path dir)  {
		cs = new ConfigService();
		INDEX_DIR = cs.getIndexDir();
		indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
		File indexDir = new File(INDEX_DIR+dir.getFileName().toString());
		

		try {
			if(indexDir.exists()) {
				index = FSDirectory.open(indexDir);
			}else {
				index = createIndex(dir);
				writer = new IndexWriter(index, indexWriterConfig);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private FSDirectory createIndex(Path dir) throws IOException {
		Path temp = Paths.get(INDEX_DIR+dir.getFileName().toString());
		return new NIOFSDirectory(temp.toFile());
	}


	public synchronized void closeWriter() {
		try {
			writer.close();
		} catch (CorruptIndexException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void addSentence(BasicDBObject dbSentence) {
		String filename = dbSentence.getString("filename");
		System.out.println(filename);
		String sentenceNumber = dbSentence.getString("sentenceNumber");
		BasicDBList dbTokens = (BasicDBList) dbSentence.get("tokens");
		StringBuilder sb = new StringBuilder();
		for (Object temp : dbTokens) {
			BasicDBObject dbToken = (BasicDBObject) temp;
			sb.append(dbToken.getString("lemma")+" ");
		}
		
		addSentence(filename, sentenceNumber, sb.toString());
	}
	
	public void addSentence(String filename, String sentenceNumber, String lemmas) {
		if(lemmas.length() > 80) {
			Document sentence = getSentence(filename, sentenceNumber, lemmas);
			try{
				writer.addDocument(sentence);
			} catch (CorruptIndexException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}			
		}
	}
	
	public Document getSentence(String filename, String sentenceNumber, String lemmas) {
		Document doc = new Document();
		
		doc.add(new Field("LEMMAS", lemmas, org.apache.lucene.document.Field.Store.NO, 
				org.apache.lucene.document.Field.Index.ANALYZED, org.apache.lucene.document.Field.TermVector.YES));
		doc.add(new Field("FILENAME", filename, org.apache.lucene.document.Field.Store.YES, org.apache.lucene.document.Field.Index.NO));
		doc.add(new Field("SENTENCE_NUMBER", sentenceNumber, org.apache.lucene.document.Field.Store.YES, org.apache.lucene.document.Field.Index.NO));

		return doc;
	}
	
	
	
//	public void addDocument(List<NLPSentence> sentences) {
//		/**
//		 * Adds all sentences from a list to the index.
//		 * Should be thread safe and can be called from multiple threads simultaneously.
//		 */
//		for (NLPSentence nlpSentence : sentences) {
//			if(nlpSentence.getLength() > 80) {
//				Document doc = getSentence(nlpSentence);
//				try {
//					writer.addDocument(doc);
//				} catch (CorruptIndexException e) {
//					e.printStackTrace();
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//		}
//	}

//	public Document getSentence(NLPSentence sentence) {
//		Document doc = new Document();
//		doc.add(new Field("LEMMAS", sentence.getLemmas(), org.apache.lucene.document.Field.Store.NO, 
//				org.apache.lucene.document.Field.Index.ANALYZED, org.apache.lucene.document.Field.TermVector.YES));
//		doc.add(new Field("FILENAME", sentence.getFilename(), org.apache.lucene.document.Field.Store.YES, org.apache.lucene.document.Field.Index.NO));
//		doc.add(new Field("SENTENCE_NUMBER", Integer.toString(sentence.getNumber()), org.apache.lucene.document.Field.Store.YES, org.apache.lucene.document.Field.Index.NO));
//
//		return doc;
//	}

	public List<PlagiarismPassage> getSimilarSentences(String filename, int retrievalCount) throws CorruptIndexException, IOException {
		/**
		 * Retrieves the n most similar sentences for every sentence in a file.
		 */
		IndexReader ir = IndexReader.open(index);
		IndexSearcher is = new IndexSearcher(ir);

		MoreLikeThis mlt = new MoreLikeThis(ir);
		mlt.setMinTermFreq(1);
		mlt.setMinDocFreq(1);
		//TODO: set stopword set mlt.setStopWords()
		//TODO: weight synonyms lower than exact match? How?
		mlt.setFieldNames(new String[] {"LEMMAS"});

		List<PlagiarismPassage> simDocs = new LinkedList<>();
		int n = 0;
		for(NLPSentence testSentence : SentenceUtils.getSentencesFromParsedFile(filename)) {
			if(testSentence.getLength()<80) {
				continue;
			}
			StringReader sr = new StringReader(testSentence.getLemmas());
			Query query = mlt.like(sr, "LEMMAS");
			ScoreDoc[] hits = is.search(query, retrievalCount).scoreDocs;
			for (ScoreDoc scoreDoc : hits) {
				int i = getIndexToInsert(scoreDoc, simDocs, n, retrievalCount);
				if(i != -1) {
					Document trainDoc = is.doc(scoreDoc.doc);
					PlagiarismPassage sp = new PlagiarismPassage(trainDoc.get("FILENAME"), Integer.parseInt(trainDoc.get("SENTENCE_NUMBER")), testSentence.getFilename(), testSentence.getNumber(), scoreDoc.score);
					simDocs.add(i, sp);

					n = simDocs.size();
					if(n > retrievalCount) {
						simDocs.remove(n-1);
						n = simDocs.size();
					}
				}
			}
		}
		is.close();

		return simDocs;
	}

	private int getIndexToInsert(ScoreDoc doc, List<PlagiarismPassage> simDocs, int n, int retrievalCount) {
		if(n == 0) {
			return 0;
		}

		if(doc.score < simDocs.get(n-1).getSimilarity()) {
			return -1;
		}

		for (int i = n-1; i >= 0; i--) {
			if(doc.score < simDocs.get(i).getSimilarity()) {
				return i+1;
			}
		}
		return 0;
	}
}
