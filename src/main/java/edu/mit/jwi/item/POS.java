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

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents part of speech objects.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public enum POS {
	
	/** 
	 * Object representing the Noun part of speech.
	 *
	 * @since JWI 2.0.0
	 */
	NOUN		("noun", 		'n', 1,	"noun"),
	
    /** 
     * Object representing the Verb part of speech.
     *
     * @since JWI 2.0.0
     */
    VERB		("verb", 		'v', 2, "verb"),
    
    /** 
     * Object representing the Adjective part of speech.
     *
     * @since JWI 2.0.0
     */
    ADJECTIVE	("adjective", 	'a', 3, "adj", "adjective"),
    
    /** 
     * Object representing the Adverb part of speech.
     *
     * @since JWI 2.0.0
     */
    ADVERB		("adverb", 		'r', 4, "adv", "adverb");
	
	// standard WordNet numbering scheme for parts of speech
	public static final int NUM_NOUN                = 1;
	public static final int NUM_VERB                = 2;
	public static final int NUM_ADJECTIVE           = 3;
	public static final int NUM_ADVERB              = 4;
	public static final int NUM_ADJECTIVE_SATELLITE = 5;
	
	// standard character tags for the parts of speech
	public static final char TAG_NOUN                = 'n';
	public static final char TAG_VERB                = 'v';
	public static final char TAG_ADJECTIVE           = 'a';
	public static final char TAG_ADVERB              = 'r';
	public static final char TAG_ADJECTIVE_SATELLITE = 's';

	// final instance fields
	private final String name;
	private final char tag;
	private final int num;
    private final Set<String> filenameHints;

    // private constructor
    private POS(String name, char tag, int type, String... patterns) {
    	this.name = name;
    	this.tag = tag;
    	this.num = type;
    	this.filenameHints = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(patterns)));
    }
    
	/**
	 * Returns a set of strings that can be used to identify resource
	 * corresponding to objects with this part of speech.
	 * 
	 * @return an immutable set of resource name hints
	 * @since JWI 2.2
	 */
    public Set<String> getResourceNameHints() {
        return filenameHints;
    }

	/**
	 * The tag that is used to indicate this part of speech in Wordnet data
	 * files
	 * 
	 * @return the character representing this part of speech
	 * @since JWI 2.0.0
	 */
    public char getTag() {
    	return tag;
    }
    
    /** 
     * Returns the standard WordNet number of this part of speech
     *
     * @return the standard WordNet number of this part of speech 
     * @since JWI 2.0.0
     */
    public int getNumber(){
    	return num;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
    
	/**
	 * Returns <code>true</code> if the specified number represents an adjective
	 * satellite, namely, if the number is 5; <code>false</code> otherwise
	 * 
	 * @param num
	 *            the number to be checked
	 * @return <code>true</code> if the specified number represents an adjective
	 *         satellite, namely, if the number is 5; <code>false</code> otherwise
	 * @since JWI 2.0.0
	 */
    public static boolean isAdjectiveSatellite(int num){
    	return num == 5;
    }
    
	/**
	 * Returns <code>true</code> if the specified character represents an
	 * adjective satellite, namely, if the number is 's' or 'S';
	 * <code>false</code> otherwise
	 * 
	 * @param tag
	 *            the character to be checked
	 * @return <code>true</code> if the specified number represents an adjective
	 *         satellite, namely, if the number is 's' or 'S';
	 *         <code>false</code> otherwise
	 * @since JWI 2.0.0
	 */
    public static boolean isAdjectiveSatellite(char tag){
    	return tag == 's' || tag == 'S';
    }
    
	/**
	 * Retrieves the part of speech object given the number.
	 * 
	 * @param num
	 *            the number for the part of speech
	 * @return POS the part of speech object corresponding to the specified tag,
	 *         or <code>null</code> if none is found
	 * @since JWI 2.0.0
	 */
    public static POS getPartOfSpeech(int num) {
    	switch(num){
    	case(1): return NOUN;
    	case(2): return VERB;
    	case(4): return ADVERB;
    	case(5): // special case, '5' for adjective satellite, fall through
    	case(3): return ADJECTIVE;
    	}
        return null;
    }

	/**
	 * Retrieves of the part of speech object given the tag. Accepts both lower
	 * and upper case characters.
	 * 
	 * @param tag
	 * @return POS the part of speech object corresponding to the specified tag,
	 *         or null if none is found
	 * @since JWI 2.0.0
	 */
    public static POS getPartOfSpeech(char tag) {
    	switch(tag){
    	case('N'): // capital, fall through
    	case('n'): return NOUN;
    	case('V'): // capital, fall through
    	case('v'): return VERB;
    	case('R'): // capital, fall through
    	case('r'): return ADVERB;
    	case('s'): // special case, 's' for adjective satellite, fall through
    	case('S'): // capital, fall through
    	case('A'): // capital, fall through
    	case('a'): return ADJECTIVE;
    	}
        return null;
    }
}
