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
 * A description of a Wordnet lexical file. This interface does not give access
 * to the actual lexicographer's file, but rather is a description, giving the
 * name of the file, it's assigned number, and a brief description.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public interface ILexFile extends IHasPOS {

	/**
	 * Returns the number of the lexicographer file. This is used in sense keys
	 * and the data files. A lexical file number is always in the closed range
	 * [0, 99].
	 * 
	 * @return the lexicograph file number, between 0 and 99, inclusive.
	 * @since JWI 2.1.0
	 */
	public int getNumber();

	/**
	 * Returns the name of the lexicographer file. The string will not be
	 * <code>null</code>, empty, or all whitespace.
	 * 
	 * @return the lexicographer file name
	 * @since JWI 2.1.0
	 */
	public String getName();

	/**
	 * Returns a description of the lexicographer file contents. The string will
	 * not be <code>null</code>, empty, or all whitespace.
	 * 
	 * @return a description of the lexicographer file contents
	 * @since JWI 2.1.0
	 */
	public String getDescription();

}
