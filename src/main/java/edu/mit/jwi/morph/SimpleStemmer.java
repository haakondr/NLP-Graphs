/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.morph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Pattern;

import edu.mit.jwi.item.POS;

/**
 * Provides simple a simple pattern-based stemming facility based on the "Rules
 * of Detachment" as described in the {@code morphy} man page in the Wordnet
 * distribution, which can be found at <a
 * href="http://wordnet.princeton.edu/man/morphy.7WN.html">
 * http://wordnet.princeton.edu/man/morphy.7WN.html</a> It also attempts to
 * strip "ful" endings. It does not search Wordnet to see if stems actually
 * exist. In particular, quoting from that man page:
 * <p>
 * <h3>Rules of Detachment</h3>
 * <p>
 * The following table shows the rules of detachment used by Morphy. If a word
 * ends with one of the suffixes, it is stripped from the word and the
 * corresponding ending is added. ... No rules are applicable to adverbs.
 * <p>
 * POS Suffix Ending<br>
 * <ul>
 * <li>NOUN "s" ""
 * <li>NOUN "ses" "s"
 * <li>NOUN "xes" "x"
 * <li>NOUN "zes" "z"
 * <li>NOUN "ches" "ch"
 * <li>NOUN "shes" "sh"
 * <li>NOUN "men" "man"
 * <li>NOUN "ies" "y"
 * <li>VERB "s" ""
 * <li>VERB "ies" "y"
 * <li>VERB "es" "e"
 * <li>VERB "es" ""
 * <li>VERB "ed" "e"
 * <li>VERB "ed" ""
 * <li>VERB "ing" "e"
 * <li>VERB "ing" ""
 * <li>ADJ "er" ""
 * <li>ADJ "est" ""
 * <li>ADJ "er" "e"
 * <li>ADJ "est" "e"
 * </ul>
 * <p>
 * <h3>Special Processing for nouns ending with 'ful'</h3>
 * <p>
 * Morphy contains code that searches for nouns ending with ful and performs a
 * transformation on the substring preceding it. It then appends 'ful' back
 * onto the resulting string and returns it. For example, if passed the nouns
 * "boxesful", it will return "boxful".
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class SimpleStemmer implements IStemmer {

	public static final String underscore = "_";
	Pattern whitespace = Pattern.compile("\\s+");

	public static final String SUFFIX_ches = "ches";
	public static final String SUFFIX_ed = "ed";
	public static final String SUFFIX_es = "es";
	public static final String SUFFIX_est = "est";
	public static final String SUFFIX_er = "er";
	public static final String SUFFIX_ful = "ful";
	public static final String SUFFIX_ies = "ies";
	public static final String SUFFIX_ing = "ing";
	public static final String SUFFIX_men = "men";
	public static final String SUFFIX_s = "s";
	public static final String SUFFIX_ses = "ses";
	public static final String SUFFIX_shes = "shes";
	public static final String SUFFIX_xes = "xes";
	public static final String SUFFIX_zes = "zes";

	public static final String ENDING_null = "";
	public static final String ENDING_ch = "ch";
	public static final String ENDING_e = "e";
	public static final String ENDING_man = "man";
	public static final String ENDING_s = SUFFIX_s;
	public static final String ENDING_sh = "sh";
	public static final String ENDING_x = "x";
	public static final String ENDING_y = "y";
	public static final String ENDING_z = "z";

	String[][] nounMappings = new String[][] { 
			new String[] { SUFFIX_s, ENDING_null },
			new String[] { SUFFIX_ses, ENDING_s }, 
			new String[] { SUFFIX_xes, ENDING_x },
			new String[] { SUFFIX_zes, ENDING_z }, 
			new String[] { SUFFIX_ches, ENDING_ch },
			new String[] { SUFFIX_shes, ENDING_sh }, 
			new String[] { SUFFIX_men, ENDING_man },
			new String[] { SUFFIX_ies, ENDING_y }, };

	String[][] verbMappings = new String[][] { 
			new String[] { SUFFIX_s, ENDING_null },
			new String[] { SUFFIX_ies, ENDING_y }, 
			new String[] { SUFFIX_es, ENDING_e },
			new String[] { SUFFIX_es, ENDING_null }, 
			new String[] { SUFFIX_ed, ENDING_e },
			new String[] { SUFFIX_ed, ENDING_null }, 
			new String[] { SUFFIX_ing, ENDING_e },
			new String[] { SUFFIX_ing, ENDING_null }, };

	String[][] adjMappings = new String[][] { 
			new String[] { SUFFIX_er, ENDING_e },
			new String[] { SUFFIX_er, ENDING_null }, 
			new String[] { SUFFIX_est, ENDING_e },
			new String[] { SUFFIX_est, ENDING_null }, };

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.morph.IStemmer#findStems(java.lang.String, edu.mit.jwi.item.POS)
	 */
	public List<String> findStems(String word, POS pos) {
		
		word = normalize(word);
		
		// if pos is null, do all
		if(pos == null){
			Set<String> result = new LinkedHashSet<String>();
			for(POS p : POS.values())
				result.addAll(findStems(word, p));
			if(result.isEmpty())
				return Collections.emptyList();
			return new ArrayList<String>(result);
		}
		
		boolean isCollocation = word.contains(underscore);
		
		switch(pos){
		case NOUN:
			return isCollocation ? 
					getNounCollocationRoots(word) : 
						stripNounSuffix(word);
		case VERB:
			// BUG006: here we check for composites
			return isCollocation ? 
					getVerbCollocationRoots(word) : 
						stripVerbSuffix(word);
		case ADJECTIVE:
			return stripAdjectiveSuffix(word);
		case ADVERB:
			// nothing for adverbs
			return Collections.<String>emptyList();
		}
		
		throw new IllegalArgumentException("This should not happen");
	}
	
	/**
	 * Converts all whitespace runs to single underscores. Tests first to see if
	 * there is any whitespace before converting.
	 * 
	 * @param word
	 *            the string to be normalized
	 * @return a normalized string
	 * @throws NullPointerException
	 *             if the specified string is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified string is empty or all whitespace
	 * @since JWI 2.1.1
	 */
	protected String normalize(String word) {
		
		word = word.trim();
		if(word.length() == 0)
			throw new IllegalArgumentException();
		
		for (int i = 0; i < word.length(); i++)
			if (Character.isWhitespace(word.charAt(i))){
				word = whitespace.matcher(word).replaceAll(underscore); 
				break;
			}
		return word.toLowerCase();
	}

	/**
	 * Strips suffixes from the specified word according to the noun rules.
	 * 
	 * @param noun
	 *            the word to be modified
	 * @return a list of modified forms that were constructed, or the empty list
	 *         if none
	 * @throws NullPointerException
	 *             if the specified word is <code>null</code>
	 * @since JWI 1.0
	 */
	protected List<String> stripNounSuffix(final String noun) {

		int len;
		String word = noun;
		
		// strip off "ful"
		boolean endsWithFUL = false;
		if(noun.endsWith(SUFFIX_ful)) {
			endsWithFUL = true;
			word = noun.substring(0, noun.length()-SUFFIX_ful.length());
		}

		// stem the word
		SortedSet<String> result = new TreeSet<String>();
		StringBuilder sb;
		for (String[] mapping : nounMappings) {
			if(!word.endsWith(mapping[0]))
				continue;
			
			sb = new StringBuilder();
			// we loop directly over characters here to avoid two loops
			len = word.length()-mapping[0].length();
			for (int i = 0; i < len; i++) 
				sb.append(word.charAt(i));
			sb.append(mapping[1]);
			
			// add back on "ful" if it was stripped
			if (endsWithFUL) 
				sb.append(SUFFIX_ful);
			result.add(sb.toString());
		}
		
		// remove any empties
		for(Iterator<String> i = result.iterator(); i.hasNext(); )
			if(i.next().length() == 0)
				i.remove();
		
		return result.isEmpty() ? 
				Collections.<String>emptyList() : 
					new ArrayList<String>(result);
	}
	
	/**
	 * Handles stemming noun collocations.
	 * 
	 * @param composite
	 *            the word to be modified
	 * @return a list of modified forms that were constructed, or the empty list
	 *         if none
	 * @throws NullPointerException
	 *             if the specified word is <code>null</code>
	 * @since JWI 1.1.1
	 */
	protected List<String> getNounCollocationRoots(String composite){
		
		// split into parts
		String[] parts = composite.split(underscore);
		if(parts.length < 2) 
			return Collections.emptyList();
		
		// stem each part
		List<List<String>> rootSets = new ArrayList<List<String>>(parts.length);
		for(int i = 0; i < parts.length; i++)
			rootSets.add(findStems(parts[i], POS.NOUN));
		
		// reassemble all combinations
		Set<StringBuffer> poss = new HashSet<StringBuffer>();
		
		// seed the set
		List<String> rootSet = rootSets.get(0);
		if(rootSet == null){
			poss.add(new StringBuffer(parts[0]));
		} else {
			for(Object root : rootSet) 
				poss.add(new StringBuffer((String)root));
		}
		
		// make all combinations
		StringBuffer newBuf;
		Set<StringBuffer> replace;
		for(int i = 1; i < rootSets.size(); i++){
			rootSet = rootSets.get(i);
			if(rootSet.isEmpty()){
				for(StringBuffer p : poss){
					p.append("_");
					p.append(parts[i]);
				}
			} else {
				replace = new HashSet<StringBuffer>();
				for(StringBuffer p : poss){
					for(Object root : rootSet){
						newBuf = new StringBuffer();
						newBuf.append(p.toString());
						newBuf.append("_");
						newBuf.append(root);
						replace.add(newBuf);
					}
				}
				poss.clear();
				poss.addAll(replace);
			}
			
		}
		
		if(poss.isEmpty()) 
			return Collections.<String>emptyList();
		
		// make sure to remove empties
		SortedSet<String> result = new TreeSet<String>();
		String root;
		for(StringBuffer p : poss){
			root = p.toString().trim();
			if(root.length() != 0)
				result.add(root);
		}
		
		return new ArrayList<String>(result);
	}

	/**
	 * Strips suffixes from the specified word according to the verb rules.
	 * 
	 * @param verb
	 *            the word to be modified
	 * @return a list of modified forms that were constructed, or the empty list
	 *         if none
	 * @throws NullPointerException
	 *             if the specified word is <code>null</code>
	 * @since JWI 1.0
	 */
	protected List<String> stripVerbSuffix(final String verb) {
		
		SortedSet<String> result = new TreeSet<String>();
		int len;
		StringBuffer sb;
		for (String[] mapping : verbMappings) {
			if(!verb.endsWith(mapping[0]))
				continue;
			sb = new StringBuffer();
			// we loop directly over characters here to avoid two loops
			len = verb.length()-mapping[0].length();
			for (int i = 0; i < len; i++) 
				sb.append(verb.charAt(i));
			sb.append(mapping[1]);
			result.add(sb.toString());
		}
		
		// remove any empties
		for(Iterator<String> i = result.iterator(); i.hasNext(); )
			if(i.next().length() == 0)
				i.remove();
		
		return result.isEmpty() ? 
				Collections.<String>emptyList() : 
					new ArrayList<String>(result);
	}
	
	/**
	 * Handles stemming verb collocations.
	 * 
	 * @param composite
	 *            the word to be modified
	 * @return a list of modified forms that were constructed, or an empty list
	 *         if none
	 * @throws NullPointerException
	 *             if the specified word is <code>null</code>
	 * @since JWI 1.1.1
	 */
	protected List<String> getVerbCollocationRoots(String composite){
		
		// split into parts
		String[] parts = composite.split(underscore);
		if(parts.length < 2) 
			return Collections.emptyList();
		
		// find the stems of each parts
		List<List<String>> rootSets = new ArrayList<List<String>>(parts.length);
		for(int i = 0; i < parts.length; i++)
			rootSets.add(findStems(parts[i], POS.VERB));
		
		SortedSet<String> result = new TreeSet<String>();
		
		// form all combinations
		StringBuffer rootBuffer = new StringBuffer();
		for(int i = 0; i < parts.length; i++){
			if(rootSets.get(i) == null) 
				continue;
			for(Object partRoot : rootSets.get(i)){
				rootBuffer.replace(0, rootBuffer.length(), "");
				
				for(int j = 0; j < parts.length; j++){
					if(j == i){
						rootBuffer.append((String)partRoot);
					} else {
						rootBuffer.append(parts[j]);
					}
					if(j < parts.length-1) 
						rootBuffer.append(underscore);
				}
				result.add(rootBuffer.toString());
			}
		}
		
		// remove any empties
		for(Iterator<String> i = result.iterator(); i.hasNext(); )
			if(i.next().length() == 0)
				i.remove();
		
		return result.isEmpty() ? 
				Collections.<String>emptyList() : 
					new ArrayList<String>(result);
	}

	/**
	 * Strips suffixes from the specified word according to the adjective rules.
	 * 
	 * @param adj
	 *            the word to be modified
	 * @return a list of modified forms that were constructed, or an empty list
	 *         if none
	 * @throws NullPointerException
	 *             if the specified word is <code>null</code>
	 * @since JWI 1.0
	 */
	protected List<String> stripAdjectiveSuffix(final String adj) {

		SortedSet<String> result = new TreeSet<String>();
		int len;
		StringBuffer sb;
		for (String[] mapping : adjMappings) {
			if(!adj.endsWith(mapping[0]))
				continue;
			sb = new StringBuffer();
			// we loop directly over characters here to avoid two loops
			len = adj.length()-mapping[0].length();
			for (int i = 0; i < len; i++) 
				sb.append(adj.charAt(i));
			sb.append(mapping[1]);
			result.add(sb.toString());
		}
		
		// remove any empties
		for(Iterator<String> i = result.iterator(); i.hasNext(); )
			if(i.next().length() == 0)
				i.remove();
		
		return result.isEmpty() ? 
				Collections.<String>emptyList() : 
					new ArrayList<String>(result);
	}
}