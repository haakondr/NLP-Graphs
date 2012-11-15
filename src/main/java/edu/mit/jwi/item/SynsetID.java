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

/**
 * Default implementation of the {@code ISynsetID} interface
 *
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class SynsetID implements ISynsetID {

	/** 
	 * String prefix for the {@link #toString()} method.
	 *
	 * @since JWI 2.0.0
	 */
	public static final String synsetIDPrefix = "SID-";
	
	// final instance fields
    private final int offset;
    private final POS pos;

	/**
	 * Constructs a new synset id with the specified offset and part of speech.
	 * 
	 * @param offset
	 *            the offset
	 * @param pos
	 *            the part of speech; may not be <code>null</code>
	 * @throws NullPointerException
	 *             if the specified part of speech is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified offset is not a legal offset
	 * @since JWI 1.0
	 */
    public SynsetID(int offset, POS pos) {
        if (pos == null)
        	throw new NullPointerException();
        Synset.checkOffset(offset);
        
        this.offset = offset;
        this.pos = pos;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.ISynsetID#getOffset()
     */
    public int getOffset() {
        return offset;
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
        result = PRIME * result + offset;
        result = PRIME * result + pos.hashCode();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj)
        	return true;
        if (obj == null)
        	return false;
        if (!(obj instanceof ISynsetID))
        	return false;
        final ISynsetID other = (ISynsetID) obj;
        if (offset != other.getOffset())
        	return false;
        if (!pos.equals(other.getPOS()))
        	return false;
        return true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
    	StringBuilder sb = new StringBuilder(14);
    	sb.append(synsetIDPrefix);
    	sb.append(Synset.zeroFillOffset(offset));
    	sb.append('-');
    	sb.append(Character.toUpperCase(pos.getTag()));
        return sb.toString();
    }

	/**
	 * Convenience method for transforming the result of the {@link #toString()}
	 * method back into an {@code ISynsetID}. Synset IDs are always 14
	 * characters long and have the following format: SID-########-C, where
	 * ######## is the zero-filled eight decimal digit offset of the synset, and
	 * C is the upper-case character code indicating the part of speech.
	 * 
	 * @param value
	 *            the string representation of the id; may include leading or
	 *            trailing whitespace
	 * @return a synset id object corresponding to the specified string
	 *         representation
	 * @throws NullPointerException
	 *             if the specified string is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified string is not a properly formatted synset id
	 * @since JWI 1.0
	 */
    public static SynsetID parseSynsetID(String value) {
    	if(value == null)
    		throw new NullPointerException();
    	
    	value = value.trim();
        if(value.length() != 14)
        	throw new IllegalArgumentException();

        if(!value.startsWith("SID-"))
        	throw new IllegalArgumentException();

        // get offset
        int offset = Integer.parseInt(value.substring(4, 12));
        
        // get pos
        char tag = Character.toLowerCase(value.charAt(13));
        POS pos = POS.getPartOfSpeech(tag);
        if(pos == null)
        	throw new IllegalArgumentException("unknown part of speech tag: " + tag);

        return new SynsetID(offset, pos);
    }
}
