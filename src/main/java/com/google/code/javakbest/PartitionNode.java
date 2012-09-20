package com.google.code.javakbest;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author David Miguel Antunes <davidmiguel [ at ] antunes.net>
 */
public class PartitionNode {

    public static List<Node> partitionNode(Node node_in, double[][] costMat) {
        List<Node> nodes_out = new ArrayList<Node>();
        int u = node_in.unspecified.size();

//    %iterate u times to permute held out assignments
        for (int ii = 0; ii < u - 1; ii++) {
//    for ii = 1:(u-1)
//        %keep fixed assignments
            Node node_out_tmp = new Node();
//        node_out_tmp{1} = node_in{1,1};
            node_out_tmp.fixed = new ArrayList<Entry<Integer, Integer>>(node_in.fixed);
//
//        %keep excluded assignements
//        node_out_tmp{2} = node_in{1,2};
            node_out_tmp.excluded = new ArrayList<Entry<Integer, Integer>>(node_in.excluded);
//
//        %if there are unspecified assignments, permute and add to list of exclusions:
            if (ii > 0) {
//            node_out_tmp{1} = [node_out_tmp{1}; node_in{1,3}(1:(ii-1),:)];
                node_out_tmp.fixed.addAll(new ArrayList<Entry<Integer, Integer>>(node_in.unspecified.subList(0, ii)));
//        end
            }
//
//        %if there are unspecified assignments, permute and add to list of exclusions:
//        node_out_tmp{2} = [node_out_tmp{2}; node_in{1,3}(ii,:)];
            Collections.sort(node_in.unspecified, Node.ENTRY_COMPARATOR);
            node_out_tmp.excluded.add(node_in.unspecified.get(ii));
//
//        %add the rest of unspecified:
//        tmp = node_in{1,3}((ii+1):end,:);
//        node_out_tmp{3} = tmp;
            node_out_tmp.unspecified.addAll(new ArrayList<Entry<Integer, Integer>>(node_in.unspecified.subList(ii + 1, node_in.unspecified.size())));
//     
//        node_out_tmp2 = calc_node_cost(node_out_tmp,cost_mat,opt_fun);
            Node node_out_tmp2 = calc_node_cost(node_out_tmp, costMat);
//        if ~isempty(node_out_tmp2)
//            nodes_out(ii,1:4) = node_out_tmp2;  
            nodes_out.add(node_out_tmp2);
//        end
//    end
        }
//    
//    if ~exist('nodes_out')
        if (nodes_out.isEmpty()) {
//        nodes_out = cell(1,4);
//        nodes_out{1,4} = 1e5;   %something arbitrarily large %todo: remove this
            Node n = new Node();
            n.cost = Murty.BIG;
            nodes_out.add(n);
//    end
        }
        return nodes_out;
    }

    private static Node calc_node_cost(Node node_in, double[][] cost_mat) {
//fixed_assignments = node_in{1,1};
//excluded_assignments = node_in{1,2};
        List<Entry<Integer, Integer>> fixed = node_in.fixed;
        List<Entry<Integer, Integer>> excluded = node_in.excluded;
//mask = zeros(size(cost_mat));
        boolean[][] mask = new boolean[cost_mat.length][cost_mat[0].length];
//cost_sum = 0;
        double cost_sum = 0;
//mask_num = inf;
//
//if size(fixed_assignments > 0)
//    for ii = 1:size(fixed_assignments,1)
        for (Entry<Integer, Integer> assignment : fixed) {
//        cost_sum = cost_sum + cost_mat(fixed_assignments(ii,1),fixed_assignments(ii,2));
            cost_sum = cost_sum + cost_mat[assignment.getKey()][assignment.getValue()];
//        %mask out all entries in same rows/cols as fixed assigments
//        mask(fixed_assignments(ii,1),:) = mask_num;
            for (int c = 0; c < mask[0].length; c++) {
                mask[assignment.getKey()][c] = true;
            }
//        mask(:,fixed_assignments(ii,2)) = mask_num;
            for (int l = 0; l < mask.length; l++) {
                mask[l][assignment.getValue()] = true;
            }
        }
//    end
//end
//
//%add excluded assignments to mask
//if size(excluded_assignments > 0)
//    for kk = 1:size(excluded_assignments,1)
        for (Entry<Integer, Integer> assignment : excluded) {
//        mask(excluded_assignments(kk,1),excluded_assignments(kk,2))= mask_num;
            mask[assignment.getKey()][assignment.getValue()] = true;
//    end
        }
//end
//
        Node node_out = new Node();
//[row,col] = find(mask == 0);
//if (size(unique(row),1) == size(unique(col),1))%detect degenerate conditions in else
        if (!degenerate(mask)) {
//    % calc optimal assignment using masked cost matrix.  
//    % todo: add support for JVC or better algorithm emt,1/28/11
//    % todo: pass in algorithm as a function handle
//    [assignment,cost] = opt_fun(cost_mat + mask);
            double[][] cost_mat_masked = new double[cost_mat.length][cost_mat[0].length];
            for (int l = 0; l < cost_mat.length; l++) {
                for (int c = 0; c < cost_mat[0].length; c++) {
                    cost_mat_masked[l][c] = mask[l][c] ? Murty.BIG : cost_mat[l][c];
                }
            }

//            String msg = "";
//            msg += "== cost matrix ==" + "\n";
//            for (int l = 0; l < cost_mat_masked.length; l++) {
//                for (int c = 0; c < cost_mat_masked[0].length; c++) {
//                    msg += cost_mat_masked[l][c] + "\t";
//                }
//                msg += "\n";
//            }
//            System.out.print(msg);

            JVC jvc = JVC.solve(cost_mat_masked);

//            for (int j = 0; j < jvc.getRowsol().length; j++) {
//                int col = jvc.getRowsol()[j];
//                System.out.print((col + 1) + " ");
//            }
//            System.out.println("\n");
//
//    %NOTE: row/col transposition below so assignment is sorted by rows
//    [col,row] = find(assignment');
//
//    node_out = node_in;
            node_out = node_in.clone();
//    %add the sum of the costs from fixed assignments and the rest
//
//    cost_out = cost_sum + cost;
//    node_out{3} = [row col];
            node_out.unspecified.clear();
            for (int i = 0; i < jvc.getRowsol().length; i++) {
                node_out.unspecified.add(new SimpleEntry<Integer, Integer>(i, jvc.getRowsol()[i]));
            }
//    node_out{4} = cost_out;
            node_out.cost = cost_sum + jvc.getCost();
//else
        } else {
//    node_out{4} = 1e5;
            node_out.cost = Murty.BIG;
        }
//end
        return node_out;
    }

    private static boolean degenerate(boolean[][] mask) {
        int rowCnt = 0, colCnt = 0;
        outter:
        for (int l = 0; l < mask.length; l++) {
            for (int c = 0; c < mask[0].length; c++) {
                if (mask[l][c] == false) {
                    rowCnt++;
                    continue outter;
                }
            }
        }
        outter2:
        for (int c = 0; c < mask[0].length; c++) {
            for (int l = 0; l < mask.length; l++) {
                if (mask[l][c] == false) {
                    colCnt++;
                    continue outter2;
                }
            }
        }
        return rowCnt != colCnt;
    }
}
