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

import java.util.List;

/**
 * A Wordnet index word object, represented in the Wordnet files as a line in an
 * index file.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IIndexWord extends IHasPOS, IItem<IIndexWordID> {

	/**
	 * @return the lemma (word root) associated with this index word.
	 * @return the lemma (word root) for this index word, never
	 *         <code>null</code>, empty, or all whitespace.
	 * @since JWI 1.0
	 */
	public String getLemma();

	/**
	 * Returns an immutable list of word id objects, that point to the words for
	 * this root form and part of speech combination. The list will neither be
	 * <code>null</code> or empty, or contain <code>null</code>.
	 * 
	 * @return an immutable list of word id objects, that point to the words for
	 *         this root form and part of speech combination.
	 * @since JWI 2.0
	 */
	public List<IWordID> getWordIDs();
	
	/**
	 * Returns the number of senses of lemma that are ranked according to their
	 * frequency of occurrence in semantic concordance texts. This will be a
	 * non-negative number.
	 * 
	 * @return the number of senses of lemma that are ranked according to their
	 *         frequency of occurrence in semantic concordance texts.
	 * @since JWI 2.1.2
	 */
	public int getTagSenseCount();

}
