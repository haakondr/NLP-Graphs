package no.roek.nlpgraphs.detailedretrieval;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;
import no.roek.nlpgraphs.misc.GraphUtils;

import com.konstantinosnedas.HungarianAlgorithm;


public class GraphEditDistance {

	private double[][] costMatrix;
	private final double SUBSTITUTE_COST;
	private final double INSERT_COST;
	private final double DELETE_COST;
	private Graph g1, g2;


	public GraphEditDistance(Graph g1, Graph g2, double subCost, double insCost, double delCost) {
		this.SUBSTITUTE_COST = subCost;
		this.INSERT_COST = insCost;
		this.DELETE_COST = delCost;
		this.g1 = g1;
		this.g2 = g2;
		this.costMatrix = createCostMatrix(g1, g2);
	}

	public GraphEditDistance(Graph g1, Graph g2) {
		this(g1, g2, 1, 1, 1);
	}

	public double getNormalizedDistance() {
		/**
		 * Retrieves the approximated graph edit distance between the two graphs g1 & g2.
		 * The distance is normalized on graph length
		 */
		double graphLength = (g1.getLength()+g2.getLength())/2;
		return getDistance() / graphLength;
	}

	public double getDistance() {
		/**
		 * Retrieves the approximated graph edit distance between the two graphs g1 & g2.
		 */
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(this.costMatrix, "min");

		double sum = 0; 
		for (int i=0; i<assignment.length; i++){
			sum =  (sum + costMatrix[assignment[i][0]][assignment[i][1]]);
		}

		return sum;
	}

	public List<String> getEditPath(boolean printCost) {
		List<String> editPaths = new ArrayList<>();
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(this.costMatrix, "min");


		for (int i = 0; i < assignment.length; i++) {
			String from = getEditPathAttribute(assignment[i][0], g1);
			String to = getEditPathAttribute(assignment[i][1], g2);

			double cost = costMatrix[assignment[i][0]][assignment[i][1]];
			if(cost != 0) {
				if(printCost) {
					editPaths.add("("+from+" -> "+to+") = "+cost);
				}else {
					editPaths.add("("+from+" -> "+to+")");
				}
			}
		}

		return editPaths;
	}

	private String getEditPathAttribute(int nodeNumber, Graph g) {
		if(nodeNumber < g.getNodes().size()) {
			Node n= g.getNode(nodeNumber);
			return n.getAttributes().get(0);
		}else {
			return "ε";
		}
	}

	//	public double getDistance() {
	//		/**
	//		 * Retrieves the graph edit distance of graph g1 & g2,
	//		 * using the Jonker-Volgenant algorithm to retrieve the (seemingly) optimal cost assignment of the cost matrix.
	//		 */
	//		JVC jvc = JVC.solve(this.costMatrix);
	//		return jvc.getCost();
	//	}

	private double[][] createCostMatrix(Graph g1, Graph g2) {
		/**
		 * Creates the cost matrix used as input to Munkres algorithm.
		 * The matrix consists of 4 sectors: upper left, upper right, bottom left, bottom right.
		 * Upper left represents the cost of all N x N node substitutions. Upper right node deletions
		 * Bottom left node insertions. 
		 * Bottom right represents delete -> delete operations which should have any cost, and is filled with zeros.
		 */
		int n = Math.max(g1.getNodes().size(), g2.getNodes().size());

		double[][] costMatrix = new double[n*2][n*2];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				try {
					costMatrix[i][j] = getSubstituteCost(g1.getNode(i), g2.getNode(j));
				}catch (IndexOutOfBoundsException e) {
					costMatrix[i][j] = INSERT_COST;
				}
				costMatrix[i+n][j] = getInsertCost(i, j);
				costMatrix[i][j+n] = getDeleteCost(i, j);
			}
		}

		return costMatrix;
	}

	private double getInsertCost(int i, int j) {
		if(i == j) {
			return INSERT_COST;
		}
		return Double.MAX_VALUE;
	}

	private double getDeleteCost(int i, int j) {
		if(i == j) {
			return DELETE_COST;
		}
		return Double.MAX_VALUE;
	}

	public double getSubstituteCost(Node node1, Node node2) {
		return (getRelabelCost(node1, node2) + getEdgeDiff(node1, node2)) / 2;
	}

	public double getRelabelCost(Node node1, Node node2) {
		//TODO: check if attribute length is same for both nodes
		List<String> attr1 = node1.getAttributes();
		List<String> attr2 = node2.getAttributes();

		double diff = 0, n = attr1.size();
		for (int i = 0; i < 1; i++) {
			if(!attr1.get(i).equalsIgnoreCase(attr2.get(i))) {
				diff ++;
			}
		}

		return (diff/n) * SUBSTITUTE_COST;
	}

	public double getEdgeDiff(Node node1, Node node2) {
		List<Edge> edges1 = g1.getEdges(node1.getId());
		List<Edge> edges2 = g2.getEdges(node2.getId());
		List<Edge> diff1 =  edgeDiff(edges1, edges2, g1, g2);
		List<Edge> diff2 = edgeDiff(edges2, edges1, g2, g1);

		double len = (edges1.size() + edges2.size());
		return (diff1.size()+diff2.size()) / len;
	}



	private List<Edge> edgeDiff(List<Edge> edges1 , List<Edge> edges2, Graph fromGraph, Graph toGraph) {
		//TODO: include getGraph in edge, so it is possible to access graphs from edges?
		/**
		 * Returns the difference between two lists of edges. 
		 * If two edges connect to the same node with just one node between them, then they are considered equal.
		 * Example g1 = 1 -> 2 -> 3, g2 = 1 -> 3, where the edge (1,2) in g1 equals the edge (1,3) in g2.
		 * @param edges1	The first list of edges
		 * @param edges2	The second list of edges
		 * @param fromGraph The graph which contains edges1
		 * @param toGraph	The graph which contains edges2
		 */
		List<Edge> edgeDiff = new ArrayList<>();
		for(Edge e1 : edges1) {
			if(!contains(e1, edges2, fromGraph, toGraph)) {
				edgeDiff.add(e1);
			}
		}

		return edgeDiff;
	}

	private boolean contains(Edge e1, List<Edge> edges, Graph fromGraph, Graph toGraph) {
		for (Edge e2 : edges) {
			if(e1.equals(e2) || leadsToSameNode(e1, e2, toGraph) || leadsToSameNode(e2, e1, fromGraph)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean leadsToSameNode(Edge e1, Edge e2, Graph g, int recursiveCalls) {
		/**
		 * Checks if edge e2 leads to the same node as edge e1
		 * Example g1 = 1 -> 2 -> 3, g2 = 1 -> 3, where the edge (1,2) in g1 equals the edge (1,3) in g2.
		 */
		if(recursiveCalls>2) {
			return false;
		}
		
		List<Edge> nextEdges = g.getEdges(e2.getTo());
		if(nextEdges==null) {
			return false;
		}
		for(Edge next: nextEdges){
			if(e1.getTo().equals(next.getTo()) && e1.getFrom().equals(e2.getFrom())){
				return true;
			}
			if(leadsToSameNode(e1, next, g, recursiveCalls+1)) {
				return true;
			}
		}
		return false;
	}
	
	private boolean leadsToSameNode(Edge e1, Edge e2, Graph g) {
		return leadsToSameNode(e1, e2, g, 0);
	}

	public void printMatrix() {
		System.out.println("-------------");
		System.out.println("Cost matrix: ");
		for (int i = 0; i < costMatrix.length; i++) {
			for (int j = 0; j < costMatrix.length; j++) {
				if(costMatrix[i][j] == Double.MAX_VALUE) {
					System.out.print("inf\t");
				}else{
					System.out.print(String.format("%.2f", costMatrix[i][j])+"\t");
				}
			}
			System.out.println();
		}
	}
}
