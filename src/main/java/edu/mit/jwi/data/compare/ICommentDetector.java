/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data.compare;

import java.util.Comparator;

/**
 * A detector for comment lines in data resources. Objects that implement this
 * interface also serve as comparators that say how comment lines are ordered,
 * if at all.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface ICommentDetector extends Comparator<String> {

	/**
	 * Returns <code>true</code> if the specified string is a comment line,
	 * <code>false</code> otherwise.
	 * 
	 * @param line
	 *            the line to be analyzed
	 * @return <code>true</code> if the specified string is a comment line,
	 *         <code>false</code> otherwise.
	 * @throws NullPointerException
	 *             if the specified line is <code>null</code>
	 * @since JWI 1.0
	 */
	public boolean isCommentLine(String line);

}