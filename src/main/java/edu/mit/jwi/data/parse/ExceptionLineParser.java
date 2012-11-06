/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data.parse;

import java.util.regex.Pattern;

import edu.mit.jwi.item.ExceptionEntryProxy;
import edu.mit.jwi.item.IExceptionEntryProxy;

/**
 * <p>
 * Parser for Wordnet exception files (e.g., <code>exc.adv</code> or
 * <code>adv.exc</code>). This parser produces {@code IExceptionEntryProxy}
 * objects instead of {@code IExceptionEntry} objects directly because the
 * exception files do not contain information about part of speech. This needs
 * to be added by the governing object to create a full-fledged
 * {@code IExceptionEntry} object.
 * </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * </p>
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class ExceptionLineParser implements ILineParser<IExceptionEntryProxy> {

	// singleton instance
	private static ExceptionLineParser instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.0.0
	 */
	public static ExceptionLineParser getInstance() {
		if (instance == null) 
			instance = new ExceptionLineParser();
		return instance;
	}
	
	// static fields
	private final static Pattern spacePattern = Pattern.compile(" ");

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.0.0
	 */
	protected ExceptionLineParser() {}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.parse.ILineParser#parseLine(java.lang.String)
	 */
	public IExceptionEntryProxy parseLine(String line) {
		if(line == null) 
			throw new NullPointerException();

		String[] forms = spacePattern.split(line);
		if (forms.length < 2) 
			throw new MisformattedLineException(line);

		String surface = forms[0].trim();

		String[] trimmed = new String[forms.length - 1];
		for (int i = 1; i < forms.length; i++)
			trimmed[i - 1] = forms[i].trim();

		return new ExceptionEntryProxy(surface, trimmed);
	}
}
