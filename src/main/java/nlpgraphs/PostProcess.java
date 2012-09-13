package nlpgraphs;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ForkJoinPool;

import nlpgraphs.graph.Graph;
import nlpgraphs.misc.Fileutils;
import nlpgraphs.misc.GraphUtils;
import nlpgraphs.postprocessing.PlagiarismWorker;

public class PostProcess {

	public static void main(String[] args) {
		File[] trainFiles = Fileutils.getFiles(Paths.get(args[0]));
		List<Graph> trainGraphs = new ArrayList<>();
		for (File file : trainFiles) {
			trainGraphs.add(GraphUtils.parseGraph(file.toPath()));
		}
		File[] test = Fileutils.getFiles(Paths.get(args[1]));

		int cpuCount = Runtime.getRuntime().availableProcessors() - 2;
		int threads = cpuCount;
		if(test.length < cpuCount) {
			threads = test.length;
		}else if(cpuCount < 2) {
			cpuCount = 1;
		}

		System.out.println("using "+cpuCount+" threads");
		PlagiarismWorker worker = new PlagiarismWorker(trainGraphs.toArray(new Graph[0]), Arrays.asList(test), threads, Paths.get(args[2]));

		ForkJoinPool pool = new ForkJoinPool();
		List<String> results = pool.invoke(worker);

		Fileutils.writeToFile("plag.txt", results.toArray(new String[0]));
	}
}
