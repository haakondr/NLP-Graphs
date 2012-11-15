/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi;

import java.io.File;
import java.net.URL;

import edu.mit.jwi.data.FileProvider;

/**
 * Basic {@code IDictionary} implementation that mounts files on disk and has
 * caching. A file URL to the directory containing the Wordnet dictionary files
 * must be provided.  This implementation has adjustable caching.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class Dictionary extends CachingDictionary {

	/**
	 * Constructs a new dictionary that uses the Wordnet files located in a
	 * directory pointed to by the specified url
	 * 
	 * @param wordnetDir
	 *            a url pointing to a directory containing the wordnet data
	 *            files on the filesystem
	 * @throws NullPointerException
	 *             if the specified url is <code>null</code>
	 * @since JWI 1.0
	 */
	public Dictionary(URL wordnetDir) {
		super(new DataSourceDictionary(new FileProvider(wordnetDir)));
	}

	/**
	 * Constructs a new dictionary that uses the Wordnet files located in a
	 * directory pointed to by the specified file
	 * 
	 * @param wordnetDir
	 *            a file pointing to a directory containing the wordnet data files on the filesystem
	 * @throws NullPointerException
	 *             if the specified file is <code>null</code>
	 * @since JWI 1.0
	 */
	public Dictionary(File wordnetDir) {
		super(new DataSourceDictionary(new FileProvider(wordnetDir)));
	}
	
}
