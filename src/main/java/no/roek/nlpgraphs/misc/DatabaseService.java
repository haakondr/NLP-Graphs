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

import no.roek.nlpgraphs.detailedretrieval.PlagiarismJob;
import no.roek.nlpgraphs.document.NLPSentence;
import no.roek.nlpgraphs.document.PlagiarismPassage;

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
	private final String candidateCollection = "candidate_passages";
	private DBCollection suspiciousColl, sourceColl;
	
	public DatabaseService() {
		try {
			Mongo m = new Mongo("localhost");
			m.setWriteConcern(WriteConcern.NORMAL);
			db = m.getDB("pan10");
			
			suspiciousColl = db.getCollection(suspiciousCollectionName);
			sourceColl = db.getCollection(sourceCollectionName);
			addIndex(sourceCollectionName);
			addIndex(suspiciousCollectionName);
			
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
		BasicDBObject query = new BasicDBObject();
		query.put("id", filename+"-"+sentenceNumber);

		//TODO: test if only one is returned
		return (BasicDBObject)coll.findOne(query);
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
						dbObject.getString("suspicous_file"), dbObject.getInt("suspicous_sentence"), dbObject.getDouble("candret_score"));
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
