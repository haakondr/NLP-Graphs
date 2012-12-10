package no.roek.nlpgraphs.detailedanalysis;
//package no.roek.nlpgraphs.detailedretrieval;
//
//import java.util.Map;
//import java.util.Set;
//
//import no.roek.nlpgraphs.graph.Graph;
//import no.roek.nlpgraphs.graph.Node;
//import no.roek.nlpgraphs.misc.SetUtils;
//import no.roek.nlpgraphs.misc.SynonymService;
//
//public class SynonymGraphEditDistance extends GraphEditDistance {
//
//	private SynonymService synonymService;
//	
//	public SynonymGraphEditDistance(Graph g1, Graph g2, double subCost, double insCost, double delCost, Map<String, Double> posEditWeights) {
//		super(g1, g2, subCost, insCost, delCost, posEditWeights);
//		synonymService = new SynonymService();
//	}
//	
//	public SynonymGraphEditDistance(Graph g1, Graph g2, Map<String, Double> posEditWeights) {
//		this(g1, g2, 2, 1, 1, posEditWeights);
//	}
//	
//	@Override
//	public double getSubstituteCost(Node node1, Node node2) {
//		double edgeDiff = getEdgeDiff(node1, node2);
////		double relabelCost = getSynonymRelabelCost(node1, node2, shouldLookupSynonyms(edgeDiff, node1, node2));
//		double relabelCost = getSynonymRelabelCost(node1, node2, true);
//		double normDiff = (edgeDiff + relabelCost) / 2;
//		return normDiff * SUBSTITUTE_COST;
//	}
//
//	public boolean shouldLookupSynonyms(double edgeDiff, Node node1, Node node2) {
//		if(edgeDiff == 0) {
//			return node1.equalsAttributes(node2);
//		}
//		
//		return false;
//	}
//	
//	public double getSynonymRelabelCost(Node node1, Node node2, boolean lookupSynonyms) {
//		if(node1.equals(node2)) {
//			return 0;
//		}else if(lookupSynonyms) {
//			if(equalSynonyms(node1, node2)){
//				return 0;
//			}
//		}
//		
//		return getPosWeight(node1, node2);
//		
//	}
//	
//	private boolean equalSynonyms(Node node1, Node node2) {
//		Set<String> syn1 = synonymService.getSynonyms(node1.getLabel(), node1.getAttributes().get(0));
//		Set<String> syn2 = synonymService.getSynonyms(node2.getLabel(), node2.getAttributes().get(0));
//		int intersection = SetUtils.intersectionSize(syn1, syn2);
//		return intersection > 0;
//	}
//}
