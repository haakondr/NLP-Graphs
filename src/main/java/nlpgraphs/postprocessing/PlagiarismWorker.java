package nlpgraphs.postprocessing;

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
	private int jobsLeft;

	public PlagiarismWorker(Graph[] train, List<File> test, int jobsLeft) {
		this.testFiles = test;
		this.train = train;
		this.jobsLeft = jobsLeft;
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
			
			PlagiarismWorker p1 = new PlagiarismWorker(train, testFiles.subList(0, split), jobsLeft -2);
			p1.fork();
			PlagiarismWorker p2 = new PlagiarismWorker(train, testFiles.subList(split, testFiles.size()), jobsLeft -2);
			results.addAll(p2.compute());
			results.addAll(p1.join());
		}
		
		return results;
	}

	private String getDistance(File file) {
		Graph test = GraphUtils.parseGraph(file.toPath());
		System.out.println(Thread.currentThread().getName()+" checking "+test.getFilename()+" for plagiarism");
		List<Double> distances = new ArrayList<>();
		for (Graph trainGraph : train) {
			GraphEditDistance ged = new GraphEditDistance(test, trainGraph);
			distances.add(ged.getDistance());
		}
		String lowest = Double.toString(getLowest(distances));
		System.out.println(test.getFilename()+" has plagiarism of "+lowest);
		return test.getFilename()+"\t"+ lowest;
	}

	private double getLowest(List<Double> distances) {
		Collections.sort(distances);
		return distances.get(0);
	}
}
