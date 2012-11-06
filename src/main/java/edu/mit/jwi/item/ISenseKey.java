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

import edu.mit.jwi.data.parse.SenseKeyParser;

/**
 * A sense key is a unique string that identifies a Wordnet word (an
 * {@link IWord}). The canonical string representation is:
 * <pre>
 * lemma%ss_type:lex_filenum:lex_id:head_word:head_id
 * </pre>
 * To transform a {@link String} representation of a sense key into an actual
 * sense key, use the {@link SenseKeyParser} class.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public interface ISenseKey extends IHasPOS, Comparable<ISenseKey> {

	/**
	 * The lemma (root form) of the word indicated by this key. The returned
	 * lemma will not be <code>null</code>, empty, or all whitespace.
	 * 
	 * @return the lemma for this key
	 * @since JWI 2.1.0
	 */
	public String getLemma();

	/**
	 * Returns the synset type for the key. The synset type is a one digit
	 * decimal integer representing the synset type for the sense.
	 * 
	 * <pre>
	 * 1=NOUN
	 * 2=VERB
	 * 3=ADJECTIVE
	 * 4=ADVERB
	 * 5=ADJECTIVE SATELLITE
	 * </pre>
	 * 
	 * @return the synset type, an integer between 1 and 5, inclusive
	 * @since JWI 2.1.0
	 */
	public int getSynsetType();

	/**
	 * Returns <code>true</code> if this sense key points to an adjective
	 * satellite; <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this sense key points to an adjective
	 *         satellite; <code>false</code> otherwise
	 * @since JWI 2.1.0
	 */
	public boolean isAdjectiveSatellite();

	/**
	 * Returns the lexical file associated with this sense key. This method
	 * should not return <code>null</code>. If the lexical file is unknown, an
	 * appropriate object to return is an {@link UnknownLexFile} object obtained
	 * via the {@link UnknownLexFile#getLexicalFile(int)} method.
	 * 
	 * @return the lexical file associated with this sense key
	 * @since JWI 2.1.0
	 */
	public ILexFile getLexicalFile();

	/**
	 * Returns the lexical id for this sense key, which is a non-negative
	 * integer.
	 * 
	 * @return the non-negative lexical id for this sense key
	 * @since JWI 2.1.0
	 */
	public int getLexicalID();

	/**
	 * Returns the head word for this sense key. The head word is only present
	 * if the sense is an adjective satellite synset, and it is the lemma of the
	 * first word of the satellite's head synset. If this sense key is not for
	 * an adjective synset, this method returns <code>null</code>. If non-
	 * <code>null</code>, the head word will be neither empty or nor all
	 * whitespace.
	 * 
	 * @return the head word for this adjective satellite synset, or
	 *         <code>null</code> if the indicated sense is not an adjective
	 *         satellite
	 * @since JWI 2.1.0
	 */
	public String getHeadWord();

	/**
	 * Returns the head id for this sense key. The head id is only present if
	 * the sense is an adjective satellite synset, and is a two digit decimal
	 * integer that, when appended onto the head word, uniquely identifies the
	 * sense within a lexicographer file. If this sense key is not for an
	 * adjective synset, this method returns <code>-1</code>.
	 * 
	 * @return the head id for this adjective satellite synset, or
	 *         <code>-1</code> if the indicated sense is not an adjective
	 *         satellite
	 * @since JWI 2.1.0
	 */
	public int getHeadID();

	/**
	 * This method is used to set the head for sense keys for adjective
	 * satellites, and it can only be called once, directly after the relevant
	 * word is created. If this method is called on a sense key that has had its
	 * head set already, or is not an adjective satellite, it will throw an
	 * exception.
	 * 
	 * @param headLemma
	 *            the head lemma to be set
	 * @param headLexID
	 * @throws IllegalStateException
	 *             if this method has already been called, if the headLemma is
	 *             empty or all whitespace or if the headLexID is illegal.
	 * @throws NullPointerException
	 *             if the headLemma is <code>null</code>
	 * @since JWI 2.1.0
	 */
	public void setHead(String headLemma, int headLexID);

	/**
	 * This method will always return <code>false</code> if the
	 * {@link #isAdjectiveSatellite()} returns <code>false</code>. If that
	 * method returns <code>true</code>, this method will only return
	 * <code>true</code> if {@link #setHead(String, int)} has not yet been
	 * called.
	 * 
	 * @return <code>true</code> if the head lemma and lexical id need to be
	 *         set; <code>false</code> otherwise.
	 * @since JWI 2.1.0
	 */
	public boolean needsHeadSet();
}
