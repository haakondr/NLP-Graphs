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

import java.util.List;

import edu.mit.jwi.item.POS;

/**
 * A stemmer is an object that can transform surface forms of words into a stem,
 * also known as a root form, base form, or headword.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.2
 */
public interface IStemmer {

	/**
	 * Takes the surface form of a word, as it appears in the text, and the
	 * assigned Wordnet part of speech. The surface form may or may not contain
	 * whitespace or underscores, and may be in mixed case. The part of speech
	 * may be <code>null</code>, which means that all parts of speech should be
	 * considered. Returns a list of stems, in preferred order. No stem should
	 * be repeated in the list. If no stems are found, this call returns an
	 * empty list. It will never return <code>null</code>.
	 * 
	 * @param surfaceForm
	 *            the surface form of which to find the stem
	 * @param pos
	 *            the part of speech to find stems for; if <code>null</code>,
	 *            find stems for all parts of speech
	 * @return the set of stems found for the surface form and part of speech
	 *         combination
	 * @throws NullPointerException
	 *             if the specified surface form is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified surface form is empty or all whitespace
	 * @since JWI 2.1.2
	 */
	public List<String> findStems(String surfaceForm, POS pos);

}
