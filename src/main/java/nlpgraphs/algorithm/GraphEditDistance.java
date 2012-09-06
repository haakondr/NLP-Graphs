package nlpgraphs.algorithm;

import java.util.List;

import com.google.code.javakbest.JVC;

import nlpgraphs.classes.Graph;
import nlpgraphs.classes.Node;
import nlpgraphs.misc.Utils;


public class GraphEditDistance {

	private double[][] costMatrix;
	private final double SUBSTITUTE_COST;
	private final double INSERT_COST;
	private final double DELETE_COST;


	public GraphEditDistance(Graph g1, Graph g2, double subCost, double insCost, double delCost) {
		this.SUBSTITUTE_COST = subCost;
		this.INSERT_COST = insCost;
		this.DELETE_COST = delCost;
		this.costMatrix = createCostMatrix(g1, g2);
	}
	
//	public double getDistance() {
//		int[][] assignment = HungarianAlgorithm.hgAlgorithm(this.costMatrix, "min");
//
//		double sum = 0; 
//		for (int i=0; i<assignment.length; i++){
//			sum =  (sum + costMatrix[assignment[i][0]][assignment[i][1]]);
//		}
//		
//		return sum;
//	}
	
	public double getDistance() {
		/**
		 * Retrieves the graph edit distance of graph g1 & g2,
		 * using the Jonker-Volgenant algorithm to retrieve the (seemingly) optimal cost assignment of the cost matrix.
		 */
		JVC jvc = JVC.solve(this.costMatrix);
		return jvc.getCost();
	}
	
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

	private double getSubstituteCost(Node node1, Node node2) {
		return getRelabelCost(node1, node2) + getEdgeDiff(node1, node2);
	}

	private double getRelabelCost(Node node1, Node node2) {
		//TODO: check if attribute length is same for both nodes
		List<String> attr1 = node1.getAttributes();
		List<String> attr2 = node2.getAttributes();
		
		int diff = 0;
		for (int i = 0; i < attr1.size(); i++) {
			if(!attr1.get(i).equalsIgnoreCase(attr2.get(i))) {
				diff ++;
			}
		}
		
		return diff * SUBSTITUTE_COST;
	}

	private double getEdgeDiff(Node node1, Node node2) {
		return Utils.listDiff(node1.getEdges(), node2.getEdges());
	}

	public void printMatrix() {
		System.out.println("-------------");
		System.out.println("Cost matrix: ");
		for (int i = 0; i < costMatrix.length; i++) {
			for (int j = 0; j < costMatrix.length; j++) {
				if(costMatrix[i][j] == Double.MAX_VALUE) {
					System.out.print("inf\t");
				}else{
					System.out.print(costMatrix[i][j]+"\t");
				}
			}
			System.out.println();
		}
	}
}
