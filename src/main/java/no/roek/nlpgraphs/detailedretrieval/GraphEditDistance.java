package no.roek.nlpgraphs.detailedretrieval;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import no.roek.nlpgraphs.graph.Edge;
import no.roek.nlpgraphs.graph.Graph;
import no.roek.nlpgraphs.graph.Node;

import com.konstantinosnedas.HungarianAlgorithm;


public class GraphEditDistance {

	private double[][] costMatrix;
	protected final double SUBSTITUTE_COST;
	protected final double INSERT_COST;
	protected final double DELETE_COST;
	private Graph g1, g2;


	public GraphEditDistance(Graph g1, Graph g2, double subCost, double insCost, double delCost) {
		this.SUBSTITUTE_COST = subCost;
		this.INSERT_COST = insCost;
		this.DELETE_COST = delCost;
		this.g1 = g1;
		this.g2 = g2;

	}

	public GraphEditDistance(Graph g1, Graph g2) {
		this(g1, g2, 2, 1, 1);
	}

	public double getNormalizedDistance() {
		/**
		 * Retrieves the approximated graph edit distance between the two graphs g1 & g2.
		 * The distance is normalized on graph length
		 */
		double graphLength = (g1.getSize()+g2.getSize())/2;
		return getDistance() / graphLength;
	}

	public double getDistance() {
		/**
		 * Retrieves the approximated graph edit distance between the two graphs g1 & g2.
		 */
		this.costMatrix = createCostMatrix(g1, g2);
		int[][] assignment = HungarianAlgorithm.hgAlgorithm(this.costMatrix, "min");

		double sum = 0; 
		for (int i=0; i<assignment.length; i++){
			sum =  (sum + costMatrix[assignment[i][0]][assignment[i][1]]);
		}

		return sum;
	}
	
	public double[][] getCostMatrix() {
		if(costMatrix==null) {
			this.costMatrix = createCostMatrix(g1, g2);
		}
		return costMatrix;
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
		 * Upper left represents the cost of all N x M node substitutions. 
		 * Upper right node deletions
		 * Bottom left node insertions. 
		 * Bottom right represents delete -> delete operations which should have any cost, and is filled with zeros.
		 */
		int n = g1.getNodes().size();
		int m = g2.getNodes().size();

		double[][] costMatrix = new double[n+m][n+m];

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				costMatrix[i][j] = getSubstituteCost(g1.getNode(i), g2.getNode(j));
			}
		}

		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				costMatrix[i+n][j] = getInsertCost(i, j);
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				costMatrix[j][i+m] = getDeleteCost(i, j);
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
		double diff = (getRelabelCost(node1, node2) + getEdgeDiff(node1, node2)) / 2;
		return diff * SUBSTITUTE_COST;
	}

	public double getRelabelCost(Node node1, Node node2) {
		//TODO: check if attribute length is same for both nodes
		//		List<String> attr1 = node1.getAttributes();
		//		List<String> attr2 = node2.getAttributes();

		double diff = 0;
		if(!node1.equals(node2)) {
			diff = getPosWeight(node1, node2);
		}
		//		double diff = 0, n = attr1.size();
		//		for (int i = 0; i < 1; i++) {
		//			if(!attr1.get(i).equalsIgnoreCase(attr2.get(i))) {
		//				diff += getPosWeight(node1, node2);
		//			}
		//		}

		return diff ;
	}

	public double getPosWeight(Node node1, Node node2) {
		return 1;
		//TODO: lookup matrix for [pos1][pos2]
		//		return 1;
	}

	public double getEdgeDiff(Node node1, Node node2) {
		//TODO: add deprel weights
		List<Edge> edges1 = g1.getEdges(node1);
		List<Edge> edges2 = g2.getEdges(node2);
		if(edges1.size() == 0 || edges2.size() == 0) {
			//TODO: return the weight for each edge here
			return edges1.size() + edges2.size();
		}
		int n = edges1.size();
		int m = edges2.size();
		double[][] edgeCostMatrix = new double[n+m][m+n];
		for (int i = 0; i < n; i++) {
			for (int j = 0; j < m; j++) {
				edgeCostMatrix[i][j] = getEdgeEditCost(edges1.get(i), edges2.get(j));
			}
		}
		
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < m; j++) {
				edgeCostMatrix[i+n][j] = getInsertCost(i, j);
			}
		}

		for (int i = 0; i < n; i++) {
			for (int j = 0; j < n; j++) {
				edgeCostMatrix[j][i+m] = getDeleteCost(i, j);
			}
		}

		int[][] assignment = HungarianAlgorithm.hgAlgorithm(edgeCostMatrix, "min");
		double sum = 0; 
		for (int i=0; i<assignment.length; i++){
			sum += edgeCostMatrix[assignment[i][0]][assignment[i][1]];
		}

		return sum / ((n+m));
	}

	public double getEdgeEditCost(Edge edge1, Edge edge2) {
		if(edge1.equals(edge2)) {
			return 0;
		}
		return 1;
		//TODO: lookup [deprel1][deprel2] cost here
	}


	//	public double getEdgeDiff(Node node1, Node node2) {
	//		List<Edge> edges1 = g1.getEdges(node1.getId());
	//		List<Edge> edges2 = g2.getEdges(node2.getId());
	//		List<Edge> diff1 =  edgeDiff(edges1, edges2, g1, g2);
	//		List<Edge> diff2 = edgeDiff(edges2, edges1, g2, g1);
	//
	//		double len = (edges1.size() + edges2.size());
	//		return (diff1.size()+diff2.size()) / len;
	//	}



	//	private List<Edge> edgeDiff(List<Edge> edges1 , List<Edge> edges2, Graph fromGraph, Graph toGraph) {
	//		//TODO: include getGraph in edge, so it is possible to access graphs from edges?
	//		/**
	//		 * Returns the difference between two lists of edges. 
	//		 * If two edges connect to the same node with just one node between them, then they are considered equal.
	//		 * Example g1 = 1 -> 2 -> 3, g2 = 1 -> 3, where the edge (1,2) in g1 equals the edge (1,3) in g2.
	//		 * @param edges1	The first list of edges
	//		 * @param edges2	The second list of edges
	//		 * @param fromGraph The graph which contains edges1
	//		 * @param toGraph	The graph which contains edges2
	//		 */
	//		List<Edge> edgeDiff = new ArrayList<>();
	//		for(Edge e1 : edges1) {
	//			if(!contains(e1, edges2, fromGraph, toGraph)) {
	//				edgeDiff.add(e1);
	//			}
	//		}
	//
	//		return edgeDiff;
	//	}
	//
	//	private boolean contains(Edge e1, List<Edge> edges, Graph fromGraph, Graph toGraph) {
	//		for (Edge e2 : edges) {
	//			if(e1.equals(e2) || leadsToSameNode(e1, e2, toGraph) || leadsToSameNode(e2, e1, fromGraph)) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	//	
	//	private boolean leadsToSameNode(Edge e1, Edge e2, Graph g, int recursiveCalls) {
	//		/**
	//		 * Checks if edge e2 leads to the same node as edge e1
	//		 * Example g1 = 1 -> 2 -> 3, g2 = 1 -> 3, where the edge (1,2) in g1 equals the edge (1,3) in g2.
	//		 * A limit of 3 recursive calls has been set, to avoid unpredictable runtime. 
	//		 * Edges leading to a node many steps ahead are probably not that similar anyway.
	//		 */
	//		if(recursiveCalls>3) {
	//			return false;
	//		}
	//		
	//		List<Edge> nextEdges = g.getEdges(e2.getTo());
	//		if(nextEdges==null) {
	//			return false;
	//		}
	//		for(Edge next: nextEdges){
	//			if(e1.getTo().equals(next.getTo()) && e1.getFrom().equals(e2.getFrom())){
	//				return true;
	//			}
	//			if(leadsToSameNode(e1, next, g, recursiveCalls+1)) {
	//				return true;
	//			}
	//		}
	//		return false;
	//	}
	//	
	//	private boolean leadsToSameNode(Edge e1, Edge e2, Graph g) {
	//		return leadsToSameNode(e1, e2, g, 0);
	//	}

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
