package nlpgraphs;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;


import nlpgraphs.algorithm.GraphEditDistance;
import nlpgraphs.graph.Graph;
import nlpgraphs.misc.GraphUtils;

public class PlagiarismWorker extends RecursiveTask<List<String>>{

	private Graph[] train;
	private List<File> testFiles;
	List<PlagiarismWorker> forks = new ArrayList<>();
	private int threshold;

	public PlagiarismWorker(Graph[] train, List<File> test, int threshold) {
		this.testFiles = test;
		this.train = train;
		this.threshold = threshold;
	}

	@Override
	protected List<String> compute() {
		List<String> results = new ArrayList<>();
		if(train.length < threshold) {
			for (File testFile : testFiles) {
				results.add(getDistance(testFile));
			}
		}else {
			int split = testFiles.size() / 2;
			
			PlagiarismWorker p1 = new PlagiarismWorker(train, testFiles.subList(0, split));
			p1.fork();
			
			PlagiarismWorker p2 = new PlagiarismWorker(train, testFiles.subList(split, testFiles.size()));
			
			
			results.addAll(p2.compute());
			results.addAll(p1.join());
		}
		
		return results;
	}

	private String getDistance(File file) {
		Graph test = GraphUtils.parseGraph(file.toString());

		List<Double> distances = new ArrayList<>();
		for (Graph trainGraph : train) {
			GraphEditDistance ged = new GraphEditDistance(test, trainGraph);
			distances.add(ged.getDistance());
		}
		String lowest = Double.toString(getLowest(distances));
		return test.getFile().toString()+"\t"+ lowest;
	}

	private double getLowest(List<Double> distances) {
		Collections.sort(distances);
		return distances.get(0);
	}
}
