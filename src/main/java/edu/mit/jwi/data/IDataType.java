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

import java.util.Set;

import edu.mit.jwi.data.compare.ILineComparator;
import edu.mit.jwi.data.parse.ILineParser;

/**
 * <p>
 * Objects that implement this interface represent possible types of data that
 * occur in the dictionary data directory.
 * </p>
 * <p>
 * In the standard Wordnet distributions, data types would include, but would
 * not be limited to, <i>Index</i> files, <i>Data</i> files, and
 * <i>Exception</i> files. The objects implementing this interface are then
 * paired with an {@link edu.mit.jwi.item.POS} instance and
 * {@link ILineComparator} instance to form an instance of an
 * {@link IContentType} class, which identifies the specific data contained in
 * the file. Note that here, 'data' refers not to an actual file, but to an
 * instance of the {@code IDataSource} interface that provides access to the
 * data, be it a file in the file system, a socket connection to a database, or
 * something else.
 * 
 * @param <T>
 *            the type of the object returned by the parser for this data type
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public interface IDataType<T> {

	/**
	 * Returns the line parser that can be used to process lines of data
	 * retrieved from an {@code IDataSource} file with this type.
	 * 
	 * @return the line parser that can be used to process lines of data
	 *         retrieved from an {@code IDataSource} file with this type.
	 * @since JWI 2.0.0
	 */
	public ILineParser<T> getParser();
	
	/**
	 * Indicates whether this content type usually has wordnet version
	 * information encoded in its header.
	 * 
	 * @return <code>true</code> if the content file that underlies this content
	 *         usually has wordnet version information in its comment header;
	 *         <code>false</code> otherwise.
	 * @since JWI 2.1.0
	 */
	public boolean hasVersion();

	/**
	 * Returns an immutable set of strings that can be used as keywords to
	 * identify resources that are of this type.
	 * 
	 * @return a set of resource name fragments
	 * @since JWI 2.0.0
	 */
	public Set<String> getResourceNameHints();
}