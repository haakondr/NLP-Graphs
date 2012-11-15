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

import java.util.StringTokenizer;

import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.IndexWord;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.SynsetID;
import edu.mit.jwi.item.WordID;

/**
 * <p>
 * Parser parser for Wordnet index files (e.g., <code>idx.adv</code> or
 * <code>adv.idx</code>). It produces an {@code IIndexWord} object.
 * </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class IndexLineParser implements ILineParser<IIndexWord> {

	// singleton instance
	private static IndexLineParser instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.0.0
	 */
	public static IndexLineParser getInstance() {
		if (instance == null) 
			instance = new IndexLineParser();
		return instance;
	}

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.0.0
	 */
	protected IndexLineParser() {}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.parse.ILineParser#parseLine(java.lang.String)
	 */
	public IIndexWord parseLine(String line) {
		if (line == null) 
			throw new NullPointerException();

		try {
			StringTokenizer tokenizer = new StringTokenizer(line, " ");

			// get lemma
			String lemma = tokenizer.nextToken();

			// get pos
			String posSym = tokenizer.nextToken();
			POS pos = POS.getPartOfSpeech(posSym.charAt(0));
			
			// consume synset_cnt
			tokenizer.nextToken();

			// consume ptr_symbols
			int p_cnt = Integer.parseInt(tokenizer.nextToken());
			for(int i = 0; i < p_cnt; ++i) 
				tokenizer.nextToken();

			// get sense_cnt
			int senseCount = Integer.parseInt(tokenizer.nextToken());

			// get tagged sense count
			int tagSenseCnt = Integer.parseInt(tokenizer.nextToken());

			// get words
			IWordID[] words = new IWordID[senseCount];
			int offset;
			for (int i = 0; i < senseCount; i++) {
				offset = Integer.parseInt(tokenizer.nextToken());
				words[i] = new WordID(new SynsetID(offset, pos), lemma);
			}

			return new IndexWord(lemma, pos, tagSenseCnt, words);
		} catch (Exception e) {
			throw new MisformattedLineException(line, e);
		} 
	}
}