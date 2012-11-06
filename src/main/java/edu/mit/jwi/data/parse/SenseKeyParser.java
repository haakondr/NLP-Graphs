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

import edu.mit.jwi.item.ILexFile;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.LexFile;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.SenseKey;
import edu.mit.jwi.item.UnknownLexFile;

/**
 * <p>
 * A parser that takes a sense key string and produces an {@code ISenseKey}
 * object.
 * </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * </p>
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.4
 */
public class SenseKeyParser implements ILineParser<ISenseKey>{
	
	// singleton instance
	private static SenseKeyParser instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.1.4
	 */
	public static SenseKeyParser getInstance() {
		if (instance == null) 
			instance = new SenseKeyParser();
		return instance;
	}

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.1.4
	 */
	protected SenseKeyParser() {}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.parse.ILineParser#parseLine(java.lang.String)
	 */
	public ISenseKey parseLine(String key) {
		if(key == null) 
			throw new NullPointerException();
		
		try{
			int begin = 0, end = 0;
			
			// get lemma
			end = key.indexOf('%');
			String lemma = key.substring(begin, end);
			
			// get ss_type
			begin = end+1;
			end = key.indexOf(':', begin);
			int ss_type = Integer.parseInt(key.substring(begin, end));
			POS pos = POS.getPartOfSpeech(ss_type);
			boolean isAdjSat = POS.isAdjectiveSatellite(ss_type);
			
			// get lex_filenum
			begin = end+1;
			end = key.indexOf(':', begin);
			int lex_filenum = Integer.parseInt(key.substring(begin, end));
			ILexFile lexFile = resolveLexicalFile(lex_filenum);
			
			// get lex_id
			begin = end+1;
			end = key.indexOf(':', begin);
			int lex_id = Integer.parseInt(key.substring(begin, end));
			
			// if it's not an adjective satellite, we're done
			if(!isAdjSat) 
				return new SenseKey(lemma, lex_id, pos, lexFile, null, -1, key);

			// get head_word
			begin = end+1;
			end = key.indexOf(':', begin);
			String head_word = key.substring(begin, end);
			
			// get head_id
			begin = end+1;
			int head_id = Integer.parseInt(key.substring(begin));
				
			return new SenseKey(lemma, lex_id, pos, lexFile, head_word, head_id, key);
		} catch(Exception e){
			throw new MisformattedLineException(e);
		}
	}
	
	/**
	 * <p>
	 * Retrieves the lexical file objects for the {@link #parseLine(String)}
	 * method. If the lexical file number does correspond to a known lexical
	 * file, the method returns a singleton placeholder 'unknown' lexical file
	 * object.
	 * </p>
	 * <p>
	 * This is implemented in its own method for ease of subclassing.
	 * </p>
	 * 
	 * @param lexFileNum
	 *            the number of the lexical file to return
	 * @return the lexical file corresponding to the specified frame number
	 * @since JWI 2.1.0
	 */
	protected ILexFile resolveLexicalFile(int lexFileNum){
		ILexFile lexFile = LexFile.getLexicalFile(lexFileNum);
		if(lexFile == null)
			lexFile = UnknownLexFile.getUnknownLexicalFile(lexFileNum);
		return lexFile;
	}

}
