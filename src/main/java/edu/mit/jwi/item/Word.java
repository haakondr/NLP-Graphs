/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

/**
 * Default implementation of the {@code IWord} interface.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class Word implements IWord {
	
	// final instance fields
    private final IWordID id;
    private final ISynset synset;
    private final ISenseKey senseKey;
    private final AdjMarker adjMarker;
    private final int lexID;
    private final List<IVerbFrame> frames;
    private final List<IWordID> allWords;
    private final Map<IPointer, List<IWordID>> wordMap;
    
	/**
	 * Constructs a new word object.
	 * 
	 * @param synset
	 *            the synset for the word; may not be <code>null</code>
	 * @param number
	 *            the word number
	 * @param lemma
	 *            the word lemma; may not be empty or all whitespace
	 * @param lexID
	 *            the lexical id
	 * @param adjMarker
	 *            non-<code>null</code> only if this is an adjective
	 * @param frames
	 *            verb frames if this is a verb
	 * @param pointers
	 *            lexical pointers
	 * @throws NullPointerException
	 *             if the synset is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the adjective marker is non-<code>null</code> and this is
	 *             not an adjective
	 * @since JWI 1.0
	 */
    public Word(ISynset synset, int number, String lemma, int lexID, AdjMarker adjMarker,
    		List<IVerbFrame> frames, Map<IPointer, ? extends List<IWordID>> pointers){
    	this(synset, new WordID(synset.getID(), number, lemma), lexID, adjMarker, frames, pointers);
    }

	/**
	 * Constructs a new word object.
	 * 
	 * @param synset
	 *            the synset for the word; may not be <code>null</code> the word
	 *            lemma; may not be empty or all whitespace
	 * @param id
	 *            the word id; may not be <code>null</code>
	 * @param lexID
	 *            the lexical id
	 * @param adjMarker
	 *            non-<code>null</code> only if this is an adjective
	 * @param frames
	 *            verb frames if this is a verb
	 * @param pointers
	 *            lexical pointers
	 * @throws NullPointerException
	 *             if the synset or word ID is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the adjective marker is non-<code>null</code> and this is
	 *             not an adjective
	 * @since JWI 1.0
	 */
    public Word(ISynset synset, IWordID id, int lexID, AdjMarker adjMarker,
    		List<IVerbFrame> frames, Map<IPointer, ? extends List<IWordID>> pointers) {
    	
    	// check arguments
    	if(synset == null)
    		throw new NullPointerException();
    	if(id == null)
    		throw new NullPointerException();
    	checkLexicalID(lexID);
    	if(synset.getPOS() != POS.ADJECTIVE && adjMarker != null)
    		throw new IllegalArgumentException();
    	
        // fill synset map
        Set<IWordID> hiddenSet = null;
        Map<IPointer, List<IWordID>> hiddenMap = null;
        if(pointers != null){
        	hiddenSet = new LinkedHashSet<IWordID>();
        	hiddenMap = new HashMap<IPointer, List<IWordID>>(pointers.size());
        	for(Entry<IPointer, ? extends List<IWordID>> entry : pointers.entrySet())
        		if(entry.getValue() != null && !entry.getValue().isEmpty()){
	        		hiddenMap.put(entry.getKey(), Collections.unmodifiableList(new ArrayList<IWordID>(entry.getValue())));
	        		hiddenSet.addAll(entry.getValue());
        		}
        }
        
        // field assignments
    	this.synset = synset;
        this.id = id;
        this.lexID = lexID;
        this.adjMarker = adjMarker;
        this.senseKey = new SenseKey(id.getLemma(), lexID, synset);
        this.allWords = (hiddenSet != null && !hiddenSet.isEmpty()) ? 
        		Collections.unmodifiableList(new ArrayList<IWordID>(hiddenSet)) : 
        			Collections.<IWordID>emptyList();
        this.wordMap = (hiddenMap != null && !hiddenMap.isEmpty()) ? 
        		Collections.unmodifiableMap(hiddenMap) : 
        			Collections.<IPointer, List<IWordID>>emptyMap();
        this.frames = (frames == null || frames.isEmpty()) ? 
        		Collections.<IVerbFrame>emptyList() : 
        			Collections.unmodifiableList(new ArrayList<IVerbFrame>(frames));
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IItem#getID()
     */
    public IWordID getID() {
	    return id;
	}

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWord#getLemma()
     */
    public String getLemma() {
        return id.getLemma();
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IHasPOS#getPOS()
     */
    public POS getPOS() {
        return id.getSynsetID().getPOS();
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWord#getSynset()
     */
    public ISynset getSynset() {
		return synset;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IWord#getLexicalID()
	 */
	public int getLexicalID() {
		return lexID;
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IWord#getAdjectiveMarker()
	 */
	public AdjMarker getAdjectiveMarker() {
	    return adjMarker;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IWord#getSenseKey()
	 */
	public ISenseKey getSenseKey() {
		return senseKey;
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IWord#getRelatedMap()
	 */
	public Map<IPointer, List<IWordID>> getRelatedMap() {
		return wordMap;
	}
    
    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWord#getRelatedWords(edu.mit.jwi.item.IPointer)
     */
    public List<IWordID> getRelatedWords(IPointer type) {
        List<IWordID> result = wordMap.get(type);
        return (result == null) ? Collections.<IWordID>emptyList() : result;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWord#getRelatedWords()
     */
    public List<IWordID> getRelatedWords() {
    	return (wordMap == null) ?
    			Collections.<IWordID>emptyList() : 
    				allWords;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWord#getVerbFrames()
     */
    public List<IVerbFrame> getVerbFrames() {
        return frames;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        if (id.getWordNumber() == 0) {
            return "W-" + id.getSynsetID().toString().substring(4) + "-?-"
                    + id.getLemma();
        } else {
            return "W-" + id.getSynsetID().toString().substring(4) + "-"
                    + id.getWordNumber() + "-" + id.getLemma();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result;
        result = PRIME + frames.hashCode();
        result = PRIME * result + wordMap.hashCode();
        result = PRIME * result + id.hashCode();
        result = PRIME * result + lexID;
        result = PRIME * result + ((adjMarker == null) ? 0 : adjMarker.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
    	
    	// check nulls
        if (this == obj)
        	return true;
        if (obj == null)
        	return false;
        
        // check interface
        if (!(obj instanceof Word))
        	return false;
        final Word that = (Word)obj;
        
        // check id
        if (!this.id.equals(that.id))
        	return false;
        
        // check lexical id
        if(this.lexID != that.lexID)
        	return false;
        
        // check adjective marker
        if(this.adjMarker == null) {
            if(that.adjMarker != null)
            	return false;
        } else if (!adjMarker.equals(that.adjMarker))
        	return false;
        
        // check maps
        if (!frames.equals(that.frames))
        	return false;
        if (!wordMap.equals(that.wordMap))
        	return false;
        
        return true;
    }
    
	/**
	 * Checks the specified word number, and throws an
	 * {@link IllegalArgumentException} if it is not legal.
	 * 
	 * @param num
	 *            the number to check
	 * @throws IllegalArgumentException
	 *             if the specified lexical id is not in the closed range [0,15]
	 * @since JWI 2.1.0
	 */
	public static void checkWordNumber(int num) {
		if(isIllegalWordNumber(num))
			throw new IllegalArgumentException("'" + num + " is an illegal word number: word numbers are in the closed range [1,255]");
	}
    
	/**
	 * Checks the specified lexical id, and throws an
	 * {@link IllegalArgumentException} if it is not legal.
	 * 
	 * @param id
	 *            the id to check
	 * @throws IllegalArgumentException
	 *             if the specified lexical id is not in the closed range [0,15]
	 * @since JWI 2.1.0
	 */
	public static void checkLexicalID(int id) {
		if(isIllegalLexicalID(id))
			throw new IllegalArgumentException("'" + id + " is an illegal lexical id: lexical ids are in the closed range [0,15]");
	}
	
	/**
	 * Lexical id's are always an integer in the closed range [0,15]. In the
	 * wordnet data files, lexical ids are represented as a one digit
	 * hexidecimal integer.
	 * 
	 * @param id
	 *            the lexical id to check
	 * @return <code>true</code> if the specified integer is an invalid lexical
	 *         id; <code>false</code> otherwise.
	 * @since JWI 2.1.0
	 */
	public static boolean isIllegalLexicalID(int id) {
		if(id < 0)
			return true;
		if(id > 15)
			return true;
		return false;
	}
	
	/**
	 * Word numbers are always an integer in the closed range [1,255]. In the
	 * wordnet data files, the word number is determined by the order of the
	 * word listing.
	 * 
	 * @param num
	 *            the number to check
	 * @return <code>true</code> if the specified integer is an invalid lexical
	 *         id; <code>false</code> otherwise.
	 * @since JWI 2.1.0
	 */
	public static boolean isIllegalWordNumber(int num) {
		if(num < 1)
			return true;
		if(num > 255)
			return true;
		return false;
	}
	
	/**
	 * Returns a string form of the lexical id as they are written in data
	 * files, which is a single digit hex number.
	 * 
	 * @param lexID
	 *            the lexical id to convert
	 * @throws IllegalArgumentException
	 *             if the specified integer is not a valid lexical id.
	 * @return a string form of the lexical id as they are written in data
	 *         files, which is a single digit hex number.
	 * @since JWI 2.1.0
	 */
	public static String getLexicalIDForDataFile(int lexID) {
		checkLexicalID(lexID);
		return Integer.toHexString(lexID);
	}
	
	// static cache
	private static final String[] lexIDNumStrs = new String[]{"00", "01", "02", "03", "04", "05", "06", "07", "08", "09"};
	
	/**
	 * Returns a string form of the lexical id as they are written in sense
	 * keys, which is as a two-digit decimal number.
	 * 
	 * @param lexID
	 *            the lexical id to convert
	 * @return a string form of the lexical id as they are written in sense
	 *         keys, which is as a two-digit decimal number.
	 * @throws IllegalArgumentException
	 *             if the specified integer is not a valid lexical id.
	 * @since JWI 2.1.0
	 */
	public static String getLexicalIDForSenseKey(int lexID) {
		checkLexicalID(lexID);
		return (lexID < 10) ? 
				lexIDNumStrs[lexID] : 
					Integer.toString(lexID);
	}
	
	/**
	 * Returns a string representation of the specified integer as a two hex
	 * digit zero-filled string. E.g., "1" becomes "01", "10" becomes "0A", and
	 * so on. This is used for the generation of Word ID numbers.
	 * 
	 * @param num
	 *            the number to be converted
	 * @return a two hex digit zero-filled string representing the specified
	 *         number
	 * @throws IllegalArgumentException
	 *             if the specified number is not a legal word number
	 * @since JWI 2.1.0
	 */
    public static String zeroFillWordNumber(int num){
    	checkWordNumber(num);
    	StringBuilder sb = new StringBuilder(2);
    	String str = Integer.toHexString(num);
    	int numZeros = 2-str.length();
    	for(int i = 0; i < numZeros; i++)
    		sb.append('0');
    	for(int i = 0; i < str.length(); i++)
    		sb.append(Character.toUpperCase(str.charAt(i)));
    	return sb.toString();
    }



}