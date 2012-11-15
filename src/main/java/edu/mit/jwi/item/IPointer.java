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
 * A pointer is a marker object that represents different types of relationships
 * between items in a Wordnet dictionary.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public interface IPointer {

	/**
	 * The symbol in the Wordnet data files that is used to indicate this
	 * pointer type. Will not be <code>null</code>, empty, or all whitespace.
	 * 
	 * @return the symbol for this pointer
	 * @since JWI 2.0.0
	 */
	public String getSymbol();

	/**
	 * Returns a user-friendly name of this pointer type for identification
	 * purposes. Will not be <code>null</code>, empty, or all whitespace.
	 * 
	 * @return the user-friendly name of this pointer
	 * @since JWI 2.0.0
	 */
	public String getName();

}
