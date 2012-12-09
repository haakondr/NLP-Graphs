package no.roek.nlpgraphs.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;

import no.roek.nlpgraphs.detailedanalysis.GraphEditDistance;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.misc.ConfigService;
import no.roek.nlpgraphs.misc.EditWeightService;
import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.misc.GraphUtils;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.POSTagParser;

public class PlagiarismThresholdAnalyser {
	public static void main(String[] args) {
		ConfigService cs = new ConfigService();

		POSTagParser postagger = new POSTagParser();
		DependencyParser depParser = new DependencyParser();
		Map<String, Double> posEditWeights = EditWeightService.getEditWeights(cs.getPosSubFile(), cs.getPosInsdelFile());
		Map<String, Double> deprelEditWeights = EditWeightService.getInsDelCosts(cs.getDeprelInsdelFile());

		List<Double> plagiarised= getDistances("resources/plagiarised_passages/", postagger, depParser, posEditWeights, deprelEditWeights);
		List<Double> notPlagiarised = getDistances("resources/not_plagiarised_passages/", postagger, depParser, posEditWeights, deprelEditWeights);
		
		for (Double double1 : plagiarised) {
			System.out.println("plagiarised: "+double1);
		}
		
		for (Double double1 : notPlagiarised) {
			System.out.println("not plagiarised:"+double1);
		}
		
		System.out.println("finding optimal threshold --------------------------");
		double threshold = getOptimalThreshold(plagiarised, notPlagiarised, 5);
		double precision = getScore(plagiarised, notPlagiarised, threshold);
		System.out.println("Optimal threshold = "+threshold+". score = "+precision);
	}

	public static double getOptimalThreshold(List<Double> plagiarised, List<Double> notPlagiarised, int recursiveCalls) {
		return getOptimalThreshold(plagiarised, notPlagiarised, 0, max(plagiarised, notPlagiarised), recursiveCalls, recursiveCalls-1);
	}
	
	public static double getOptimalThreshold(List<Double> plagiarised, List<Double> notPlagiarised, double from, double to, int recursiveCalls, int recursion) {
		double i = (to-from) / 100;
		System.out.println("Checking between"+from+" and "+to);
		double bestScore = 0;
		double bestThreshold = 0;
		
		for (double threshold = from; threshold < to; threshold+=i) {
			double score = getScore(plagiarised, notPlagiarised, threshold);
			if(score > bestScore) {
				bestScore = score;
				bestThreshold  =threshold;
			}
		}
		
		System.out.println("best threshold: "+bestThreshold);
		if((recursion) == 1) {
			return bestThreshold;
		}else {
			return getOptimalThreshold(plagiarised, notPlagiarised, (bestThreshold-i*10), (bestThreshold+i*10), recursiveCalls, recursion-1);
		}
	}
	
	public static double getScore(List<Double> plagiarised, List<Double> notPlagiarised, double threshold) {
		double correct = 0;
		for (Double dist : plagiarised) {
			if(dist<threshold) {
				correct++;
			}
		}
		
		for (Double dist : notPlagiarised) {
			if(dist>threshold) {
				correct++;
			}
		}
		
		return correct / (plagiarised.size() + notPlagiarised.size());
	}
	
	
	public static double max(List<Double> list1, List<Double> list2) {
		double highest1 = Collections.max(list1);
		double highest2 = Collections.max(list2);
		return Math.max(highest1, highest2);
	}
	
	public static List<Double> getDistances(String dir, POSTagParser postagger, DependencyParser depParser, Map<String, Double> posEditWeights, Map<String, Double> deprelEditWeights) {
		List<Double> distances = new ArrayList<>();

		for(File file : Fileutils.getFiles(dir)) {
			List<String> lines = Fileutils.getTextLines(file.toString());
			int i = 0, n = lines.size();
			while(hasNext(i, n)) {
				Graph g1 = getGraph(lines.get(i), postagger, depParser);
				Graph g2 = getGraph(lines.get(i+1), postagger, depParser);
				GraphEditDistance ged = new GraphEditDistance(g1, g2, posEditWeights, deprelEditWeights);
				double dist = ged.getNormalizedDistance();
				distances.add(dist);
				i+=3;
			}
		}
		
		return distances;
	}

	private static boolean hasNext(int current, int n) {
		return (current+2)<=n;
	}



	public static Graph getGraph(String text, POSTagParser postagger, DependencyParser depParser) {
		BasicDBObject dbObj = depParser.parseSentence(postagger.postagSentence(text), "test", 0,0,0);
		return GraphUtils.getGraph(dbObj);
	}
}
