package no.roek.nlpgraphs.application;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mongodb.BasicDBObject;

import no.roek.nlpgraphs.detailedretrieval.GraphEditDistance;
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

		List<Double> plagiarismDistances = new ArrayList<>();

		
		for(File file : Fileutils.getFiles("resources/plagiarised_passages/")) {
			List<String> lines = Fileutils.getTextLines(file.toString());
			int i = 0, n = lines.size();
			while(hasNext(i, n)) {
				Graph g1 = getGraph(lines.get(i), postagger, depParser);
				Graph g2 = getGraph(lines.get(i+1), postagger, depParser);
				GraphEditDistance ged = new GraphEditDistance(g1, g2, posEditWeights, deprelEditWeights);
				double dist = ged.getNormalizedDistance();
				plagiarismDistances.add(dist);
				i+=3;
			}
		}

		double temp = 0;
		for (double double1 : plagiarismDistances) {
			temp += double1;
		}
		
		System.out.println("avg: "+temp / plagiarismDistances.size()+" for "+plagiarismDistances.size()+" passages");
	}
	
	private static boolean hasNext(int current, int n) {
		return (current+2)<=n;
	}



	public static Graph getGraph(String text, POSTagParser postagger, DependencyParser depParser) {
		BasicDBObject dbObj = depParser.parseSentence(postagger.postagSentence(text), "test", 0,0,0);
		return GraphUtils.getGraph(dbObj);
	}
}
