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
 * The data that can be obtained from a line in an exception entry file. Because
 * each exception entry does not specify its associated part of speech, this object
 * is just a proxy and must be supplemented by the part of speech at some
 * point to make a full {@code IExceptionEntry} object.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IExceptionEntryProxy {

	/**
	 * Returns the surface form (i.e., not the root form) of the word for this
	 * exception entry. Because all surface forms in the exception files are
	 * lower case, the string returned by this call is also lower case.
	 * 
	 * @return the lowercase surface form of the exception entry
	 * @since JWI 1.0
	 */
	public String getSurfaceForm();

	/**
	 * Returns an unmodifiable list of cceptable root forms for the surface
	 * form.
	 * 
	 * @return A non-null, non-empty, unmodifiable list of root forms
	 * @since JWI 2.0.0
	 */
	public List<String> getRootForms();

}