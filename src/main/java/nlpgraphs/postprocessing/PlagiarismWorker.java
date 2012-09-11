package nlpgraphs.postprocessing;

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


import nlpgraphs.algorithm.GraphEditDistance;
import nlpgraphs.graph.Graph;
import nlpgraphs.misc.Fileutils;
import nlpgraphs.misc.GraphUtils;
import nlpgraphs.search.DocumentRetrievalService;

public class PlagiarismWorker extends RecursiveTask<List<String>>{

	private Graph[] train;
	private List<File> testFiles;
	List<PlagiarismWorker> forks = new ArrayList<>();
	private int jobsLeft;
	private Path trainDir;

	public PlagiarismWorker(Graph[] train, List<File> test, int jobsLeft, Path traindir) {
		this.testFiles = test;
		this.train = train;
		this.jobsLeft = jobsLeft;
		this.trainDir = traindir;
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
			
			PlagiarismWorker p1 = new PlagiarismWorker(train, testFiles.subList(0, split), jobsLeft -2, trainDir);
			p1.fork();
			PlagiarismWorker p2 = new PlagiarismWorker(train, testFiles.subList(split, testFiles.size()), jobsLeft -2, trainDir);
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
			graphs.add(GraphUtils.parseGraph(Paths.get(filename)));
		}
		
		return graphs;
	}
	
	private List<String> getSimilarDocuments(File file, int recall) {
		try {
			DocumentRetrievalService drs = new DocumentRetrievalService(trainDir);
			return drs.getSimilarDocuments(Fileutils.getText(file.toPath()), recall);
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
