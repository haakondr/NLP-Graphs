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
import com.mongodb.DBObject;
import com.mongodb.Mongo;
import com.mongodb.WriteConcern;

public class DatabaseService {

	private DB suspiciousDB, sourceDB;
	
	public DatabaseService() {
		try {
			Mongo m = new Mongo("localhost");
			m.setWriteConcern(WriteConcern.SAFE);
			suspiciousDB = m.getDB("suspicious-documents");
			sourceDB = m.getDB("source-documents");
			
		} catch (UnknownHostException e) {
			System.out.println("Database not found");
			e.printStackTrace();
		}
	}

	public void addSentence(String filename, BasicDBObject dbSentence) {
		DBCollection coll =getDB(filename).getCollection(filename);
		coll.insert(dbSentence);
	}

	public BasicDBObject getSentence(String filename, int sentenceNumber) {
		return getSentence(filename, Integer.toString(sentenceNumber));
	}

	public BasicDBObject getSentence(String filename, String sentenceNumber) {
		DBCollection coll = getDB(filename).getCollection(filename);
		BasicDBObject query = new BasicDBObject();
		query.put("sentenceNumber", sentenceNumber);

		//TODO: test if only one is returned
		return (BasicDBObject)coll.findOne(query);
	}
	
	public DB getDB(String filename) {
		if(filename.startsWith("source-document")) {
			return sourceDB;
		}else if(filename.startsWith("suspicious-document")) {
			return suspiciousDB;
		}else {
			return null;
		}
	}

	public void addIndex(String filename) {
		DBCollection coll = getDB(filename).getCollection(filename);
		coll.ensureIndex(new BasicDBObject("sentenceNumber", 1).append("unique", true));
	}

	public Set<String> getFiles(String dbname) {
		return getDB(dbname).getCollectionNames();
	}

	public List<String> getUnparsedFiles(File[] files) {
		List<String> unparsedFiles = new ArrayList<>();
		Set<String> parsedFiles = suspiciousDB.getCollectionNames();
		parsedFiles.addAll(sourceDB.getCollectionNames());
		for(File f : files) {
			if(!contains(f, parsedFiles)) {
				unparsedFiles.add(f.toString());
			}
		}

		return unparsedFiles;
	}

	private boolean contains(File file, Set<String> parsedFiles) {
		for(String parsedFile : parsedFiles) {
			if(file.toPath().getFileName().toString().equals(parsedFile)) {
				return true;
			}
		}
		return false;
	}
}
