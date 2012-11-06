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

import java.util.regex.Pattern;

/**
 * Default implementation of {@code IIndexWordID}.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class IndexWordID implements IIndexWordID {
	
	/** 
	 * Whitespace pattern for use in replacing whitespace with underscores
	 *
	 * @since JWI 2.1.2
	 */
	protected static final Pattern whitespace = Pattern.compile("\\s+");

	// immutable instance fields
    private final String lemma;
    private final POS pos;

	/**
	 * Constructs an index word id object with the specified lemma and part of
	 * speech. Since all index entries are in lower case, with whitespace
	 * converted to underscores, this constructor applies this conversion.
	 * 
	 * @param lemma
	 *            the lemma for the id
	 * @param pos
	 *            the part of speech for the id
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the lemma is empty or all whitespace
	 * @since JWI 1.0
	 */
    public IndexWordID(String lemma, POS pos) {
    	if(pos == null)
    		throw new NullPointerException();
    	lemma = lemma.toLowerCase().trim();
    	if(lemma.length() == 0)
    		throw new IllegalArgumentException();
    	this.lemma = whitespace.matcher(lemma).replaceAll("_");
        this.pos = pos;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IIndexWordID#getLemma()
     */
    public String getLemma() {
        return lemma;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IHasPOS#getPOS()
     */
    public POS getPOS() {
        return pos;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + lemma.hashCode();
        result = PRIME * result + pos.hashCode();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if(this == obj) 
        	return true;
        if(obj == null)
        	return false;
        if(!(obj instanceof IIndexWordID))
        	return false;
        final IIndexWordID other = (IIndexWordID) obj;
        if(!lemma.equals(other.getLemma()))
        	return false;
        if(!pos.equals(other.getPOS()))
        	return false;
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "XID-" + lemma + "-" + pos.getTag();
    }

	/**
	 * Convenience method for transforming the result of the {@link #toString()}
	 * method into an {@code IndexWordID}
	 * 
	 * @param value
	 *            the string to be parsed
	 * @return the index word id
	 * @throws NullPointerException
	 *             if the specified string is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified string does not conform to an index word id
	 *             string
	 * @since JWI 1.0
	 */
    public static IndexWordID parseIndexWordID(String value) {
        if(value == null)
        	throw new NullPointerException();
        
        if(!value.startsWith("XID-"))
        	throw new IllegalArgumentException();
        
        if(value.charAt(value.length()-2) != '-')
        	throw new IllegalArgumentException();
        
        POS pos = POS.getPartOfSpeech(value.charAt(value.length()-1));
        return new IndexWordID(value.substring(4, value.length()-2), pos);
    }
}