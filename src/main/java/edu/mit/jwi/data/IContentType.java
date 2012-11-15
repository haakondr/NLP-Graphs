/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data;

import edu.mit.jwi.data.compare.ILineComparator;
import edu.mit.jwi.item.IHasPOS;

/**
 * <p>
 * Objects that implement this interface represent all possible types of content
 * that are contained in the dictionary data resources. Each unique object of
 * this type will correspond to a particular resource or file.
 * </p>
 * <p>
 * In the standard Wordnet distributions, examples of content types would
 * include, but would not be limited to, <em>Index</em>, <em>Data</em>, and
 * <em>Exception</em> files for each part of speech.
 * </p>
 * 
 * @param <T>
 *            the parameterization of the data type for this content type
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IContentType<T> extends IHasPOS {
	
	/**
	 * Returns the assigned resource type of this object. This method may not
	 * return <code>null</code>
	 * 
	 * @return the data type object representing the resource type for this
	 *         content type
	 * @since JWI 1.0
	 */
	public IDataType<T> getDataType();

	/**
	 * Returns a comparator that can be used to determine ordering between
	 * different lines of data in the resource. This is used for searching. If
	 * the data in the resource is not ordered, then this method returns
	 * <code>null</code>.
	 * 
	 * @return a comparator that imposes an ordering on the lines in the data
	 *         file; or <code>null</code> if there is no comparator
	 * @since JWI 1.0
	 */
	public ILineComparator getLineComparator();

}