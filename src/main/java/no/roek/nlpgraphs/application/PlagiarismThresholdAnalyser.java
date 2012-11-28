package no.roek.nlpgraphs.application;

import java.io.File;
import java.util.ArrayList;
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
		
		for (File file : Fileutils.getFiles("resources/plagiarised_passages/")) {
			List<String> lines = Fileutils.getTextLines("resources/"+file.toString());
			Graph g1 = getGraph(lines.get(0), postagger, depParser);
			Graph g2 = getGraph(lines.get(0), postagger, depParser);
			GraphEditDistance ged = new GraphEditDistance(g1, g2, posEditWeights, deprelEditWeights);
			plagiarismDistances.add(ged.getNormalizedDistance());
		}
		
		for (Double double1 : plagiarismDistances) {
			System.out.println(double1);
		}
		
	}
	
	public static Graph getGraph(String text, POSTagParser postagger, DependencyParser depParser) {
		BasicDBObject dbObj = depParser.parseSentence(postagger.postagSentence(text), "test", 0,0,0);
		return GraphUtils.getGraph(dbObj);
	}
}
