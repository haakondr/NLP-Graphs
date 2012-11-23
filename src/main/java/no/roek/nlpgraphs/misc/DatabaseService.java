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

	private DB db;
	public DatabaseService() {
		try {
			Mongo m = new Mongo("localhost");
			m.setWriteConcern(WriteConcern.SAFE);
			db = m.getDB("nlp-graphs");
		} catch (UnknownHostException e) {
			System.out.println("Database not found");
			e.printStackTrace();
		}
	}

	public void addSentence(String filename, BasicDBObject dbSentence) {
		DBCollection coll = db.getCollection(filename);
		coll.insert(dbSentence);
	}

	public BasicDBObject getSentence(String filename, int sentenceNumber) {
		return getSentence(filename, Integer.toString(sentenceNumber));
	}

	public BasicDBObject getSentence(String filename, String sentenceNumber) {
		DBCollection coll = db.getCollection(filename);
		BasicDBObject query = new BasicDBObject();
		query.put("sentenceNumber", sentenceNumber);

		//TODO: test if only one is returned
		return (BasicDBObject)coll.findOne(query);
	}

	public void addIndex(String filename) {
		DBCollection coll = db.getCollection(filename);
		coll.ensureIndex(new BasicDBObject("sentenceNumber", 1).append("unique", true));
	}

	public List<String> getTrainFiles() {
		List<String> trainFiles = new ArrayList<>();
		for(String file : db.getCollectionNames()) {
			if(file.startsWith("source-document")) {
				trainFiles.add(file);
			}
		}

		return trainFiles;
	}

	public List<String> getTestFiles() {
		List<String> testFiles = new ArrayList<>();
		for(String file : db.getCollectionNames()) {
			if(file.startsWith("suspicious-document")) {
				testFiles.add(file);
			}
		}

		return testFiles;
	}

	public List<String> getUnparsedFiles(File[] files) {
		List<String> unparsedFiles = new ArrayList<>();
		Set<String> parsedFiles = db.getCollectionNames();
		for(File f : files) {
			if(!contains(f, parsedFiles)) {
				unparsedFiles.add(f.toPath().getFileName().toString());
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
