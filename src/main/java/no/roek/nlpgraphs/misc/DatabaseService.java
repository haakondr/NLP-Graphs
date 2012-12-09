package no.roek.nlpgraphs.misc;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Pattern;

import org.json.JSONObject;

import no.roek.nlpgraphs.detailedanalysis.PlagiarismJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.PlagiarismPassage;
import no.roek.nlpgraphs.document.WordToken;

import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class DatabaseService {

	private DB db;
	private final String sourceCollectionName = "source_sentences";
	private final String suspiciousCollectionName = "suspicious_sentences";
	private final String suspiciousDocsCollection = "suspicious_documents";
	private final String sourceDocsCollection = "source_documents";
	private final String candidateCollection = "candidate_passages_150";
	private DBCollection suspiciousColl, sourceColl;
	
	public DatabaseService(String dbname, String dblocation) {
		try {
			Mongo m = new Mongo(dblocation);
			m.setWriteConcern(WriteConcern.NORMAL);
			db = m.getDB(dbname);
			
			suspiciousColl = db.getCollection(suspiciousCollectionName);
			sourceColl = db.getCollection(sourceCollectionName);
			addIndex(sourceCollectionName);
			addIndex(suspiciousCollectionName);
			addIndex(candidateCollection);
			
		} catch (UnknownHostException e) {
			System.out.println("Database not found");
			e.printStackTrace();
		}
	}

	public void addSentence(BasicDBObject dbSentence) {
		DBCollection coll = getSentenceColl(dbSentence.getString("filename"));
		coll.insert(dbSentence);
	}
	
	public void addDocument(String filename) {
		BasicDBObject dbObject = new BasicDBObject();
		dbObject.put("filename", filename);
		String collName = getDocumentColl(filename);
		DBCollection coll = db.getCollection(collName);
		coll.insert(dbObject);
	}
	
	public void addCandidatePassage(BasicDBList passages) {
		DBCollection coll = db.getCollection(candidateCollection);
		for (Object object : passages) {
			coll.insert((DBObject)object);
		}
	}
	
	
	public BasicDBObject getSentence(String filename, int sentenceNumber) {
		return getSentence(filename, Integer.toString(sentenceNumber));
	}
	
	public BasicDBObject getSentence(String sentenceId) {
		int i = sentenceId.lastIndexOf("-");
		String sentenceNumber = sentenceId.substring(i+1);
		String filename = sentenceId.substring(0, i);
		return getSentence(filename, sentenceNumber);
	}

	public BasicDBObject getSentence(String filename, String sentenceNumber) {
		DBCollection coll = getSentenceColl(filename);
		BasicDBObject query = new BasicDBObject("id", filename+"-"+sentenceNumber);

		//TODO: test if only one is returned
		return (BasicDBObject)coll.findOne(query);
	}
	
	public List<NLPSentence> getAllSentences(String filename) {
		DBCollection coll = getSentenceColl(filename);
		Pattern p = Pattern.compile("^"+filename+"-*");
		BasicDBObject query = new BasicDBObject("id", p);
		
		List<NLPSentence> sentences = new ArrayList<>();
		DBCursor cursor = coll.find(query);
		while(cursor.hasNext()) {
			BasicDBObject obj = (BasicDBObject) cursor.next();
			List<WordToken> words = new ArrayList<>();
			BasicDBList dbTokens = (BasicDBList) obj.get("tokens");
			for (Object object : dbTokens) {
				BasicDBObject dbToken = (BasicDBObject) object;
				words.add(new WordToken(dbToken.getString("word"), dbToken.getString("lemma"), dbToken.getString("pos")));
			}
			sentences.add(new NLPSentence(obj.getString("filename"), obj.getInt("sentenceNumber"), obj.getInt("offset"), obj.getInt("length"), words));
		}
		cursor.close();
		
		return sentences;
	}
	
	private DBCollection getSentenceColl(String filename) {
		if(filename.startsWith("source-document")) {
			return sourceColl;
		}else if(filename.startsWith("suspicious-document")) {
			return suspiciousColl;
		}else {
			return null;
		}
	}
	
	private String getDocumentColl(String filename) {
		if(filename.startsWith("source-document")) {
			return sourceDocsCollection;
		}else if(filename.startsWith("suspicious-document")) {
			return suspiciousDocsCollection;
		}else {
			return null;
		}
	}

	private void addIndex(String collection) {
		DBCollection coll = db.getCollection(collection);
		coll.ensureIndex(new BasicDBObject("id", 1).append("unique", true));
	}

	public List<String> getFiles() {
		List<String> files = new ArrayList<>();
		files.addAll(getFiles(sourceDocsCollection));
		files.addAll(getFiles(suspiciousDocsCollection));
		
		return files;
	}
	
	public Set<String> getAll(String collection, String field) {
		Set<String> files = new HashSet<>();
		DBCollection coll = db.getCollection(collection);
		DBCursor cursor = coll.find();
		while(cursor.hasNext()) {
			files.add(cursor.next().get(field).toString());
		}
		
		cursor.close();
		return files;
	}
	

	public Set<String> getSourceSentenceIds() {
		return getAll(sourceCollectionName, "id");
	}
	
	public DBCursor getSourceSentencesCursor() {
		return sourceColl.find();
	}
	
	public void retrieveAllPassages(BlockingQueue<PlagiarismJob> queue, Set<String> filesDone) {
		DBCollection coll = db.getCollection(candidateCollection);
		DBCursor cursor = coll.find();
		while(cursor.hasNext()) {
			DBObject temp = cursor.next();
			String filename = temp.get("id").toString();
			if(filesDone.contains(filename)) {
				continue;
			}
			PlagiarismJob job = new PlagiarismJob(filename);
			BasicDBList passages = (BasicDBList)temp.get("passages");

			for (Object obj : passages) {
				BasicDBObject dbObject = (BasicDBObject) obj;
				PlagiarismPassage passage = new PlagiarismPassage(dbObject.getString("source_file"), dbObject.getInt("source_sentence"), 
						dbObject.getString("suspicious_file"), dbObject.getInt("suspicious_sentence"));
				job.addTextPair(passage);
				
			}
			try {
				queue.put(job);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	public Set<String> getFiles(String collection) {
		Set<String> files = new HashSet<>();
		DBCollection coll = db.getCollection(collection);
		DBCursor cursor = coll.find();
		while(cursor.hasNext()) {
			files.add(cursor.next().get("filename").toString());
		}
		
		cursor.close();
		return files;
	}

	public Set<String> getUnparsedFiles(Set<String> files) {
		Set<String> parsedFiles = getFiles(sourceDocsCollection);
		parsedFiles.addAll(getFiles(suspiciousDocsCollection));
		files.removeAll(parsedFiles);
		
		return files;
	}
	
	private Set<String> getAll(DBCursor cursor) {
		Set<String> strings = new HashSet<>();
		while(cursor.hasNext()) {
			strings.add(cursor.next().toString());
		}
		
		return strings;
	}
}
