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
 * Default implementation of the {@code IWordID} interface.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class WordID implements IWordID {
	
	/** 
	 * String prefix for the {@link #toString()} method.
	 *
	 * @since JWI 2.0.0
	 */
	public static final String wordIDPrefix = "WID-";
	
	/** 
	 * Represents an unknown lemma for the {@link #toString()} method.
	 *
	 * @since JWI 2.0.0
	 */
	public static final String unknownLemma = "?";
	
	/** 
	 * Represents an unknown word number for the {@link #toString()} method.
	 *
	 * @since JWI 2.0.0
	 */
	public static final String unknownWordNumber = "??";
	
	// final instance fields
    private final ISynsetID id;
    private final String lemma;
    private final int num;

	/**
	 * Constructs a word id from the specified arguments. This constructor
	 * produces a word with an unknown lemma.
	 * 
	 * @param offset
	 *            the synset offset
	 * @param pos
	 *            the part of speech; may not be <code>null</code>
	 * @param num
	 *            the word number
	 * @throws IllegalArgumentException
	 *             if the offset or number are not legal
	 * @since JWI 1.0
	 */
    public WordID(int offset, POS pos, int num){
    	this(new SynsetID(offset, pos), num);
    }
    
	/**
	 * Constructs a word id from the specified arguments. This constructor
	 * produces a word with an unknown word number.
	 * 
	 * @param offset
	 *            the synset offset
	 * @param pos
	 *            the part of speech; may not be <code>null</code>
	 * @param lemma
	 *            the lemma; may not be <code>null</code>, empty, or all
	 *            whitespace
	 * @since JWI 1.0
	 */
    public WordID(int offset, POS pos, String lemma){
    	this(new SynsetID(offset, pos), lemma);
    }

	/**
	 * Constructs a word id from the specified arguments. This constructor
	 * produces a word with an unknown lemma.
	 * 
	 * @param id
	 *            the synset id; may not be <code>null</code>
	 * @param num
	 *            the word number
	 * @throws NullPointerException
	 *             if the synset id is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the lemma is empty or all whitespace
	 * @since JWI 1.0
	 */
    public WordID(ISynsetID id, int num) {
        if (id == null)
        	throw new NullPointerException();
        Word.checkWordNumber(num);
        this.id = id;
        this.num = num;
        this.lemma = unknownLemma;
    }

	/**
	 * Constructs a word id from the specified arguments. This constructor
	 * produces a word with an unknown word number.
	 * 
	 * @param id
	 *            the synset id; may not be <code>null</code>
	 * @param lemma
	 *            the lemma; may not be <code>null</code>, empty, or all
	 *            whitespace
	 * @throws NullPointerException
	 *             if the synset id is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the lemma is empty or all whitespace
	 * @since JWI 1.0
	 */
    public WordID(ISynsetID id, String lemma) {
        if(id == null)
        	throw new NullPointerException();
        if(lemma.trim().length() == 0)
        	throw new IllegalArgumentException();
        this.id = id;
        this.num = -1;
        this.lemma = lemma.toLowerCase();
    }
    
	/**
	 * Constructs a fully specified word id
	 * 
	 * @param id
	 *            the synset id; may not be <code>null</code>
	 * @param num
	 *            the word number
	 * @param lemma
	 *            the lemma; may not be <code>null</code>, empty, or all
	 *            whitespace
	 * @throws NullPointerException
	 *             if the synset id is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the lemma is empty or all whitespace, or the word number
	 *             is not legal
	 * @since JWI 1.0
	 */
    public WordID(ISynsetID id, int num, String lemma){
    	if(id == null)
    		throw new NullPointerException();
    	if(lemma.trim().length() == 0)
    		throw new IllegalArgumentException();
    	Word.checkWordNumber(num);
        this.id = id;
        this.num = num;
        this.lemma = lemma;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWordID#getSynsetID()
     */
    public ISynsetID getSynsetID() {
        return id;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWordID#getWordNumber()
     */
    public int getWordNumber() {
        return num;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IWordID#getLemma()
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
        return id.getPOS();
    }

    /* 
     * (non-Javadoc) 
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((id == null) ? 0 : id.hashCode());
        result = PRIME * result + num;
        if (lemma != null)
        	result = PRIME * result + lemma.hashCode();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
        	return true;
        if (obj == null)
        	return false;
        if (getClass() != obj.getClass())
        	return false;
        final WordID other = (WordID) obj;
        if (!id.equals(other.id))
        	return false;
        if (other.num != -1 && num != -1 && other.num != num)
        	return false;
        if (other.lemma != null && lemma != null)
            if (!other.lemma.equalsIgnoreCase(lemma))
            	return false;
        return true;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder(16 + lemma.length());
    	sb.append(wordIDPrefix);
    	sb.append(Synset.zeroFillOffset(id.getOffset()));
    	sb.append('-');
    	sb.append(Character.toUpperCase(id.getPOS().getTag()));
    	sb.append('-');
    	sb.append((num < 0) ? unknownWordNumber : Word.zeroFillWordNumber(num));
        sb.append('-');
        sb.append(lemma);
        return sb.toString();
    }

	/**
	 * Parses the result of the {@link #toString()} method back into an
	 * {@code WordID}. Word id's are always of the following format:
	 * WID-########-P-##-lemma where ######## is the eight decimal digit
	 * zero-filled offset of the associated synset, P is the upper case
	 * character representing the part of speech, ## is the two hexidecimal
	 * digit zero-filled word number (or ?? if unknown), and lemma is the lemma.
	 * 
	 * @param value
	 *            the string to be parsed
	 * @return WordID the parsed id
	 * @throws IllegalArgumentException
	 *             if the specified string does not represent a word id
	 * @throws NullPointerException
	 *             if the specified string is <code>null</code>
	 * @since JWI 1.0
	 */
    public static IWordID parseWordID(String value) {
    	if(value == null)
    		throw new NullPointerException();
        if (value.length() < 19)
        	throw new IllegalArgumentException();
        if (!value.startsWith("WID-"))
        	throw new IllegalArgumentException();
        
        // get synset id
        int offset = Integer.parseInt(value.substring(4,12));
        POS pos = POS.getPartOfSpeech(value.charAt(13));
        ISynsetID id = new SynsetID(offset, pos);
        
        // get word number
        String numStr = value.substring(15, 17);
        if(!numStr.equals(unknownWordNumber)){
            int num = Integer.parseInt(numStr, 16);
            return new WordID(id, num);
        }

        // get lemma
        String lemma = value.substring(18);
        if(lemma.equals(unknownLemma) || lemma.length() == 0)
        	throw new IllegalArgumentException();

        return new WordID(id, lemma);
    }
    
}