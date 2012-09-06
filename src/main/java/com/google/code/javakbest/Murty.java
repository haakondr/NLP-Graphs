package com.google.code.javakbest;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author David Miguel Antunes <davidmiguel [ at ] antunes.net>
 */
public class Murty {

    public static final double BIG = 10e4;

    public static List<int[]> solve(double[][] costMat, int k) {

        if (costMat.length > costMat[0].length) {
            throw new RuntimeException("Number of columns in assignment problem should be greater than the number of rows");
        }
        k = Math.min(k, costMat[0].length);
        int originalNRows = costMat.length;
        {
            double[][] newCostMat = new double[costMat[0].length][costMat[0].length];
            double min = Double.MAX_VALUE;
            for (int l = 0; l < costMat.length; l++) {
                for (int c = 0; c < costMat[0].length; c++) {
                    min = Math.min(min, costMat[l][c]);
                    newCostMat[l][c] = costMat[l][c];
                }
            }
            for (int l = costMat.length; l < newCostMat.length; l++) {
                for (int c = 0; c < newCostMat[0].length; c++) {
                    newCostMat[l][c] = min;
                }
            }
            costMat = newCostMat;
        }

//% find initial optimal assignment
//[assignment,cost] = opt_fun(cost_mat);
        JVC jvc = JVC.solve(costMat);
//[col,row] = find(assignment');
//nodes = {[], [],[row col], cost};
        Node first = new Node();
        for (int i = 0; i < jvc.getRowsol().length; i++) {
            first.unspecified.add(new SimpleEntry<Integer, Integer>(i, jvc.getRowsol()[i]));
        }
        first.cost = jvc.getCost();
        List<Node> nodes = new ArrayList<Node>();
        nodes.add(first.clone());
//optimal_nodes = {[], [],[row col], cost};
        List<Node> optimal_nodes = new ArrayList<Node>();
        optimal_nodes.add(first.clone());
//
//%partition the optimal node
//nodes_list = partition_node(nodes,cost_mat,opt_fun);
        List<Node> nodes_list = PartitionNode.partitionNode(first, costMat);
//
//while (size(optimal_nodes,1) < k) 
        while (optimal_nodes.size() < k) {
//    %find the best node among those in the node list
//    costs = cell2mat(nodes_list(:,4));
//    [min_cost,ind] = min(costs);
            int min = min(nodes_list);
//    
//    %copy best node from list into optimal set
//    optimal_nodes(end+1,:) = nodes_list(ind,:);
            optimal_nodes.add(nodes_list.get(min));
//    
//%     nodes_list(ind,:);  %for debug
//    nodes_tmp = partition_node(nodes_list(ind,:),cost_mat,opt_fun);
            List<Node> nodes_tmp = PartitionNode.partitionNode(nodes_list.get(min), costMat);
//    nodes_list(ind,:) = []; %remove best node from list
            nodes_list.remove(min);
//
//    %add new partitioned nodes to list
//    nodes_list = [nodes_list; nodes_tmp]; 
            nodes_list.addAll(nodes_tmp);
//%     disp('==========================================================');  %for debug
//    
//end
        }
//
        List<int[]> rslt = new ArrayList<int[]>();
//%format output data structure
//assignment_list = cell(size(optimal_nodes,1),2);
//for ii = 1:size(optimal_nodes,1)
        for (Node node : optimal_nodes) {
            int[] colsol = new int[costMat.length];
            for (Entry<Integer, Integer> assignment : node.fixed) {
                colsol[assignment.getKey()] = assignment.getValue();
            }
            for (Entry<Integer, Integer> assignment : node.unspecified) {
                colsol[assignment.getKey()] = assignment.getValue();
            }
            rslt.add(colsol);
//    assignment_list{ii,1} = [optimal_nodes{ii,1}; optimal_nodes{ii,3}]; 
//    assignment_list{ii,2} = optimal_nodes{ii,4};
//end
        }

        for (int i = 0; i < rslt.size(); i++) {
            int[] full = rslt.get(i);
            int[] useful = new int[originalNRows];
            System.arraycopy(full, 0, useful, 0, useful.length);
            rslt.set(i, useful);
        }

        return rslt;
    }

    private static int min(List<Node> nodes_list) {
        double min = Double.MAX_VALUE;
        int idx = -1;
        for (int i = 0; i < nodes_list.size(); i++) {
            Node node = nodes_list.get(i);
            if (node.cost < min) {
                min = node.cost;
                idx = i;
            }
        }
        return idx;
    }
}
