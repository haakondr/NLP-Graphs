package no.roek.nlpgraphs.search;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import no.roek.nlpgraphs.document.DocumentFile;
import no.roek.nlpgraphs.misc.Fileutils;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;


public class DocumentRetrievalService {

	private RAMDirectory index;
	private IndexWriterConfig indexWriterConfig;
	private IndexWriter writer;
	private DocumentFile[] documents;
	private StandardAnalyzer analyzer;
	
	public DocumentRetrievalService(Path dir) throws CorruptIndexException, IOException {
		//TODO: sjekk om index finnes, for så å hente index fra fil. Hvis ikke, lag
		analyzer = new StandardAnalyzer(Version.LUCENE_36);
		createIndex(dir);
	}

	public void createIndex(Path dir) throws CorruptIndexException, IOException {
		index = new RAMDirectory();
		indexWriterConfig = new IndexWriterConfig(Version.LUCENE_36, new StandardAnalyzer(Version.LUCENE_36));
		writer = new IndexWriter(index, indexWriterConfig);
		documents = Fileutils.getFileList(dir);
		for (DocumentFile file: documents) {
			writer.addDocument(getDocument(file.getPath()));
		}
		writer.close();
	}
	
	public Document getDocument(Path file) {
		Document doc = new Document();
		doc.add(new Field("text", Fileutils.getText(file), org.apache.lucene.document.Field.Store.YES, org.apache.lucene.document.Field.Index.ANALYZED));
		doc.add(new Field("id", file.getFileName().toString(), org.apache.lucene.document.Field.Store.YES, org.apache.lucene.document.Field.Index.ANALYZED));
		return doc;
	}

	public List<String> getSimilarDocuments(String queryText, int recall) throws IOException, ParseException {
		IndexReader ir = IndexReader.open(index);
		IndexSearcher is = new IndexSearcher(ir);
		
		TopScoreDocCollector collector = TopScoreDocCollector.create(recall, true);
		Query query = new QueryParser(Version.LUCENE_36, "text", analyzer).parse(QueryParser.escape(queryText));
		is.search(query, collector);
		ScoreDoc[] hits = collector.topDocs().scoreDocs;
		
		List<String> simDocs = new ArrayList<String>();
		for (ScoreDoc scoreDoc : hits) {
			Document doc = is.doc(scoreDoc.doc);
			simDocs.add(doc.get("id"));
		}
		return simDocs;
	}
}
