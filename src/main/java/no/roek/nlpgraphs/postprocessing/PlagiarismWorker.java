package no.roek.nlpgraphs.postprocessing;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;

import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.queryParser.ParseException;


import no.roek.nlpgraphs.algorithm.GraphEditDistance;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.search.DocumentRetrievalService;

public class PlagiarismWorker extends RecursiveTask<List<String>>{

	private Graph[] train;
	private List<File> testFiles;
	List<PlagiarismWorker> forks = new ArrayList<>();
	private int jobsLeft;
	private Path originalDir;

	public PlagiarismWorker(Graph[] train, List<File> test, int jobsLeft, Path originalTrainDir) {
		this.testFiles = test;
		this.train = train;
		this.jobsLeft = jobsLeft;
		this.originalDir = originalTrainDir;
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
			
			PlagiarismWorker p1 = new PlagiarismWorker(train, testFiles.subList(0, split), jobsLeft -2, originalDir);
			p1.fork();
			PlagiarismWorker p2 = new PlagiarismWorker(train, testFiles.subList(split, testFiles.size()), jobsLeft -2, originalDir);
			results.addAll(p2.compute());
			results.addAll(p1.join());
		}
		
		return results;
	}

	private String getDistance(File file) {
		Graph test = GraphUtils.parseGraph(file.toPath());
		System.out.println(Thread.currentThread().getName()+" checking "+test.getFilename()+" for plagiarism");
		List<Double> distances = new ArrayList<>();
		for (Graph trainGraph : getSimilarGraphs(file, 3)) {
			GraphEditDistance ged = new GraphEditDistance(test, trainGraph);
			distances.add(ged.getDistance());
		}
		String lowest = Double.toString(getLowest(distances));
		System.out.println(test.getFilename()+" has plagiarism of "+lowest);
		return test.getFilename()+"\t"+ lowest;
	}
	
	private List<Graph> getSimilarGraphs(File file, int recall) {
		List<Graph> graphs = new ArrayList<>();
		for (String filename : getSimilarDocuments(file, recall)) {
			graphs.add(GraphUtils.parseGraph(Paths.get("out/train/"+filename)));
		}
		
		return graphs;
	}
	
	private List<String> getSimilarDocuments(File file, int recall) {
		try {
			DocumentRetrievalService drs = new DocumentRetrievalService(Paths.get(originalDir.toString()+"/train/"));
			String filename = originalDir.toString()+"/test/"+file.getName();
			return drs.getSimilarDocuments(Fileutils.getText(Paths.get(filename)), recall);
		} catch (IOException | ParseException e) {
			e.printStackTrace();
			return null;
		}
	}

	private double getLowest(List<Double> distances) {
		Collections.sort(distances);
		return distances.get(0);
	}
}
