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
 * A unique identifier sufficient to retrieve a particular word from the Wordnet
 * database. Consists of a synset id, sense number, and lemma.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IWordID extends IHasPOS, IItemID<IWord> {

	/**
	 * Returns the synset id object associated with this word.
	 * 
	 * @return the synset id for this word; never <code>null</code>
	 * @since JWI 1.0
	 */
	public ISynsetID getSynsetID();

	/**
	 * Returns the word number, which is a number from 1 to 255 that indicates
	 * the order this word is listed in the Wordnet data files.
	 * 
	 * @return an integer between 1 and 255, inclusive.
	 * @since JWI 1.0
	 */
	public int getWordNumber();

	/**
	 * Returns the lemma (word root) associated with this word.
	 * 
	 * @return the lemma (word root) associated with this word.
	 * @since JWI 1.0
	 */
	public String getLemma();
}
