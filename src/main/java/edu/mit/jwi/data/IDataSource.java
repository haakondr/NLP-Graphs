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

import java.util.Iterator;

import edu.mit.jwi.IDataSourceDictionary;
import edu.mit.jwi.item.IHasVersion;

/**
 * <p>
 * An object that mediate between an {@link IDataSourceDictionary} and the data
 * that is contained in the dictionary data resources. Data resources are
 * assigned a name (e.g., <i>verb.data</i>, for the data resource pertaining to
 * verbs) and a content type. Data resources are assumed to be indexed by keys
 * that can be passed into the {@link #getLine(String)} method to find a
 * particular piece of data in the resource. The <code>String</code> return can
 * be parsed by the parser associated with the content type to produce a data
 * object (e.g., an {@code ISynset} or {@code IIndexWord} object).
 * </p>
 * <p>
 * The iterator produced by this class should not support the
 * {@link Iterator#remove()} operation; if that method is called, the iterator
 * will throw an {@link UnsupportedOperationException}.
 * </p>
 * 
 * @param <T>
 *            the type of object represented in this data resource
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public interface IDataSource<T> extends IHasVersion, Iterable<String>, IHasLifecycle {

	/**
	 * Returns a string representation of the name of this resource. For
	 * file-based resources, this will usually be the filename.
	 * 
	 * @return the name of this resource, neither <code>null</code>, empty, or
	 *         all whitespace
	 * @since JWI 2.0.0
	 */
	public String getName();

	/**
	 * Returns the assigned content type of the resource that backs this object.
	 * 
	 * @return the assigned content type for this data source. Will not return
	 *         <code>null</code>.
	 * @since JWI 2.0.0
	 */
	public IContentType<T> getContentType();

	/**
	 * Returns the line in the resource contains the data indexed by the
	 * specified key. If the file cannot find the key in its data resource, it
	 * returns <code>null</code>
	 * 
	 * @param key
	 *            the key which indexes the desired data
	 * @throws NullPointerException
	 *             if the specified key is <code>null</code>
	 * @return the line indexed by the specified key in the resource
	 * @since JWI 2.0.0
	 */
	public String getLine(String key);

	/**
	 * Returns an iterator that will iterator over lines in the data resource,
	 * starting at the line specified by the given key. If the key is
	 * <code>null</code>, this is the same as calling the plain
	 * {@link #iterator()} method. If no line starts with the pattern, the
	 * iterator's {@link Iterator#hasNext()} will return <code>false</code>. The
	 * iterator does not support the {@link Iterator#remove()} operation; if
	 * that method is called, the iterator will throw an
	 * {@link UnsupportedOperationException}.
	 * 
	 * @param key
	 *            the key at which the iterator should begin
	 * @return an iterator that will iterate over the file starting at the line
	 *         indexed by the specified key
	 * @since JWI 2.0.0
	 */
	public Iterator<String> iterator(String key);

}