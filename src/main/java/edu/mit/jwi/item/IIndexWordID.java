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
 * A unique identifier for an index word. An index word ID is sufficient to
 * retrieve a specific index word from the Wordnet database. It consists of both
 * a lemma (root form) and part of speech.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IIndexWordID extends IHasPOS, IItemID<IIndexWord> {

	/**
	 * Returns the lemma (root form) of the index word that this ID indicates.
	 * The lemma will never be <code>null</code>, empty, or all whitespace.
	 * 
	 * @return the lemma of the index word
	 * @since JWI 1.0
	 */
	public String getLemma();

}
