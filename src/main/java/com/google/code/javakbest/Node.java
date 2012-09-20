package com.google.code.javakbest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

/**
 *
 * @author David Miguel Antunes <davidmiguel [ at ] antunes.net>
 */
public class Node implements Cloneable {

    public static Comparator<Entry<Integer, Integer>> ENTRY_COMPARATOR = new Comparator<Entry<Integer, Integer>>() {

        public int compare(Entry<Integer, Integer> o1, Entry<Integer, Integer> o2) {
//            return o1.getValue() != o2.getValue()
//                    ? o1.getValue() - o2.getValue()
//                    : o1.getKey() - o2.getKey();
            return o1.getKey() != o2.getKey()
                    ? o1.getKey() - o2.getKey()
                    : o1.getValue() - o2.getValue();
        }
    };
    public List<Entry<Integer, Integer>> fixed = new ArrayList<Entry<Integer, Integer>>();
    public List<Entry<Integer, Integer>> excluded = new ArrayList<Entry<Integer, Integer>>();
    public List<Entry<Integer, Integer>> unspecified = new ArrayList<Entry<Integer, Integer>>();
    public double cost;

    @Override
    protected Node clone() {
        try {
            Node c = (Node) super.clone();
            c.fixed = new ArrayList<Entry<Integer, Integer>>(fixed);
            c.excluded = new ArrayList<Entry<Integer, Integer>>(excluded);
            c.unspecified = new ArrayList<Entry<Integer, Integer>>(unspecified);
            return c;
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}