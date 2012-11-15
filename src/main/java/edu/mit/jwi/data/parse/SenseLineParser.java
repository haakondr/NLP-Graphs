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

import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.SenseEntry;

/**
 * Parser for Wordnet sense index files (e.g., <code>index.sense</code> or
 * <code>sense.index</code>). It produces an {@code ISenseEntry} object. </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * <p>
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public class SenseLineParser implements ILineParser<ISenseEntry> {

	// singleton instance
	private static SenseLineParser instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.1.0
	 */
	public static SenseLineParser getInstance() {
		if (instance == null) 
			instance = new SenseLineParser();
		return instance;
	}
	
	// instance fields
	protected final ILineParser<ISenseKey> keyParser;
	
	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.1.0
	 */
	protected SenseLineParser() {
		this(SenseKeyParser.getInstance());
	}

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @param keyParser
	 *            the sense key parser this sense line parser should use
	 * @throws NullPointerException
	 *             if the specified key parser is <code>null</code>
	 * @since JWI 2.2.0
	 */
	protected SenseLineParser(ILineParser<ISenseKey> keyParser) {
		if(keyParser == null) 
			throw new NullPointerException();
		this.keyParser = keyParser;
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.parse.ILineParser#parseLine(java.lang.String)
	 */
	public ISenseEntry parseLine(String line) {
		if(line == null) 
			throw new NullPointerException();

		try {
			int begin = 0, end = 0;
			
			// get sense key
			end = line.indexOf(' ', begin);
			String keyStr = line.substring(begin, end);
			ISenseKey sense_key = keyParser.parseLine(keyStr);
			
			// get offset
			begin = end+1;
			end = line.indexOf(' ', begin);
			int synset_offset = Integer.parseInt(line.substring(begin, end));
			
			// get sense number
			begin = end+1;
			end = line.indexOf(' ', begin);
			int sense_number = Integer.parseInt(line.substring(begin, end));
			
			// get tag cnt
			begin = end+1;
			int tag_cnt = Integer.parseInt(line.substring(begin));
			
			return new SenseEntry(sense_key, synset_offset, sense_number, tag_cnt);
		} catch (Exception e) {
			throw new MisformattedLineException(line, e);
		}
	}

}