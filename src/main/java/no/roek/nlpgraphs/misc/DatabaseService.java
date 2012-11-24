package no.roek.nlpgraphs.misc;

import java.io.File;
import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.roek.nlpgraphs.document.NLPSentence;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class DatabaseService {

	private DB db;
	private final String sourceCollection = "source-sentences";
	private final String suspiciousCollection = "suspicious-sentences";
	private final String suspiciousDocsCollection = "suspicious-documents";
	private final String sourceDocsCollection = "source-documents";
	
	public DatabaseService() {
		try {
			Mongo m = new Mongo("localhost");
			m.setWriteConcern(WriteConcern.SAFE);
			addIndex(sourceCollection);
			addIndex(suspiciousCollection);
			addIndex(suspiciousDocsCollection);
			addIndex(sourceDocsCollection);
			
		} catch (UnknownHostException e) {
			System.out.println("Database not found");
			e.printStackTrace();
		}
	}

	public void addSentence(BasicDBObject dbSentence) {
		String collName = getSentenceColl(dbSentence.getString("filename"));
		DBCollection coll = db.getCollection(collName);
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

	public BasicDBObject getSentence(String filename, String sentenceNumber) {
		DBCollection coll = db.getCollection(getSentenceColl(filename));
		BasicDBObject query = new BasicDBObject();
		query.put("id", filename+"-"+sentenceNumber);

		//TODO: test if only one is returned
		return (BasicDBObject)coll.findOne(query);
	}
	
	private String getSentenceColl(String filename) {
		if(filename.startsWith("source-document")) {
			return sourceCollection;
		}else if(filename.startsWith("suspicious-document")) {
			return suspiciousCollection;
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
	
	public List<String> getFiles(String collection) {
		List<String> files = new ArrayList<>();
		DBCollection coll = db.getCollection(collection);
		DBCursor cursor = coll.find();
		while(cursor.hasNext()) {
			files.add(cursor.next().get("filename").toString());
		}
		
		cursor.close();
		return files;
	}

	public List<String> getUnparsedFiles(String[] files) {
		List<String> unparsedFiles = new ArrayList<>();
		BasicDBObject query = new BasicDBObject();
		query.put("filename", new BasicDBObject("$nin", files));
		
		DBCursor cursor = db.getCollection(sourceDocsCollection).find(query);
		unparsedFiles.addAll(getAll(cursor));
		DBCursor cursor2 = db.getCollection(suspiciousDocsCollection).find(query);
		unparsedFiles.addAll(getAll(cursor2));
		
		return unparsedFiles;
	}
	
	private List<String> getAll(DBCursor cursor) {
		List<String> strings = new ArrayList<>();
		while(cursor.hasNext()) {
			strings.add(cursor.next().toString());
		}
		
		return strings;
	}
}
