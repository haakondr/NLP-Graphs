package no.roek.nlpgraphs.postprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import no.roek.nlpgraphs.algorithm.GraphEditDistance;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;

import org.apache.lucene.queryParser.ParseException;

public class PlagiarismWorker extends RecursiveTask<List<String>>{

	private List<File> testFiles;
	List<PlagiarismWorker> forks = new ArrayList<>();
	private int jobsLeft;
	private Path originalDir;
	private String parsedData, trainDir, testDir;

	public PlagiarismWorker(List<File> test, int jobsLeft, Path originalTrainDir, String parsedData, String trainDir, String testDir) {
		this.testFiles = test;
		this.jobsLeft = jobsLeft;
		this.originalDir = originalTrainDir;
		this.parsedData = parsedData;
		this.trainDir = trainDir;
		this.testDir = testDir;
	}

	@Override
	protected List<String> compute() {
		List<String> results = new ArrayList<>();
		if(jobsLeft < 2) {
			for (File testFile : testFiles) {
				results.add(getDistance(testFile));
			}
		}else {
			int split = testFiles.size() / 2;
			
			PlagiarismWorker p1 = new PlagiarismWorker(testFiles.subList(0, split), jobsLeft -2, originalDir, parsedData, trainDir, testDir);
			p1.fork();
			PlagiarismWorker p2 = new PlagiarismWorker(testFiles.subList(split, testFiles.size()), jobsLeft -2, originalDir, parsedData, trainDir, testDir);
			results.addAll(p2.compute());
			results.addAll(p1.join());
		}
		
		return results;
	}

	private String getDistance(File file) {
		Graph test = GraphUtils.parseGraph(file.toString());
		System.out.println(Thread.currentThread().getName()+" checking "+test.getFilename()+" for plagiarism");
		List<Double> distances = new ArrayList<>();
		for (Graph trainGraph : getSimilarGraphs(file, 3)) {
			GraphEditDistance ged = new GraphEditDistance(test, trainGraph);
			distances.add(ged.getDistance());
		}
		double lowest = Collections.min(distances);
		System.out.println(test.getFilename()+" has plagiarism of "+lowest);
		return test.getFilename()+"\t"+ lowest;
	}
	
	private List<Graph> getSimilarGraphs(File file, int recall) {
		List<Graph> graphs = new ArrayList<>();
		for (String filename : getSimilarDocuments(file, recall)) {
			graphs.add(GraphUtils.parseGraph(parsedData+trainDir+filename));
		}
		
		return graphs;
	}
	
	private List<String> getSimilarDocuments(File file, int recall) {
		try {
			DocumentRetrievalService drs = new DocumentRetrievalService(Paths.get(originalDir.toString()+trainDir));
			String filename = originalDir.toString()+testDir+file.getName();
			return drs.getSimilarDocuments(Fileutils.getText(Paths.get(filename)), recall);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}
	}
}
