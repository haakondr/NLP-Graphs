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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import edu.mit.jwi.item.AdjMarker;
import edu.mit.jwi.item.ILexFile;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IVerbFrame;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.LexFile;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.Synset;
import edu.mit.jwi.item.Synset.IWordBuilder;
import edu.mit.jwi.item.Synset.WordBuilder;
import edu.mit.jwi.item.SynsetID;
import edu.mit.jwi.item.UnknownLexFile;
import edu.mit.jwi.item.VerbFrame;
import edu.mit.jwi.item.WordID;

/**
 * <p>
 * Parser for Wordnet data files (e.g., <code>data.adv</code> or
 * <code>adv.dat</code>). This parser produces an <code>ISynset</code> object.
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
public class DataLineParser implements ILineParser<ISynset> {

	// singleton instance
	private static DataLineParser instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.0.0
	 */
	public static DataLineParser getInstance() {
		if (instance == null) 
			instance = new DataLineParser();
		return instance;
	}

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.0.0
	 */
	protected DataLineParser() {}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.parse.ILineParser#parseLine(java.lang.String)
	 */
	public ISynset parseLine(String line) {
		if(line == null) 
			throw new NullPointerException();

		try {
			StringTokenizer tokenizer = new StringTokenizer(line, " ");

			// Get offset
			int offset = Integer.parseInt(tokenizer.nextToken());

			// Consume lex_filenum
			int lex_filenum = Integer.parseInt(tokenizer.nextToken());
			ILexFile lexFile = resolveLexicalFile(lex_filenum);
			
			// Get part of speech
			POS synset_pos;
			char synset_tag = tokenizer.nextToken().charAt(0);
			synset_pos = POS.getPartOfSpeech(synset_tag);

			ISynsetID synsetID = new SynsetID(offset, synset_pos);

			// Determine if it is an adjective satellite
			boolean isAdjSat = (synset_tag == 's');
			
			// A synset is an adjective head if it is the 00 lexical file, is
			// not a adjective satellite, and it has an antonym. The Wordnet
			// definition says head synsets have to have an antonym, but this is
			// actually violated (perhaps mistakenly) in a small number of
			// cases, e.g., in Wordnet 3.0:
			// 01380267 aerial (no antonyms), with satellite 01380571 free-flying
			// 01380721 marine (no antonyms), with satellite 01380926 deep-sea
			boolean isAdjHead = !isAdjSat && lex_filenum == 0;

			// Get word count
			int wordCount = Integer.parseInt(tokenizer.nextToken(), 16);

			// Get words
			String lemma;
			AdjMarker marker;
			int lexID;
			IWordBuilder[] wordProxies = new IWordBuilder[wordCount];
			for (int i = 0; i < wordCount; i++) {
				// consume next word
				lemma = tokenizer.nextToken();

				// if it is an adjective, it may be followed by a marker
				marker = null;
				if (synset_pos == POS.ADJECTIVE)
					for(AdjMarker adjMarker : AdjMarker.values()){
						if(lemma.endsWith(adjMarker.getSymbol())){
							marker = adjMarker;
							lemma = lemma.substring(0, lemma.length()-adjMarker.getSymbol().length());
						}
					}

				// parse lex_id
				lexID = Integer.parseInt(tokenizer.nextToken(), 16);

				wordProxies[i] = new WordBuilder(i + 1, lemma, lexID, marker);
			}

			// Get pointer count
			int pointerCount = Integer.parseInt(tokenizer.nextToken());

			Map<IPointer, ArrayList<ISynsetID>> synsetPointerMap = null;

			// Get pointers
			IPointer pointer_type;
			int target_offset;
			POS target_pos;
			int source_target_num, source_num, target_num;
			ArrayList<ISynsetID> pointerList;
			IWordID target_word_id;
			ISynsetID target_synset_id;
			for (int i = 0; i < pointerCount; i++) {
				// get pointer symbol
				pointer_type = resolvePointer(tokenizer.nextToken(), synset_pos);

				// get synset target offset
				target_offset = Integer.parseInt(tokenizer.nextToken());

				// get target synset part of speech
				target_pos = POS.getPartOfSpeech(tokenizer.nextToken().charAt(0));

				target_synset_id = new SynsetID(target_offset, target_pos);

				// get source/target numbers
				source_target_num = Integer.parseInt(tokenizer.nextToken(), 16);

				// this is a semantic pointer if the source/target numbers are
				// zero
				if (source_target_num == 0) {
					if (synsetPointerMap == null) 
						synsetPointerMap = new HashMap<IPointer, ArrayList<ISynsetID>>();
					pointerList = synsetPointerMap.get(pointer_type);
					if (pointerList == null) {
						pointerList = new ArrayList<ISynsetID>();
						synsetPointerMap.put(pointer_type, pointerList);
					}
					pointerList.add(target_synset_id);
				}
				else {
					// this is a lexical pointer
					source_num = source_target_num / 256;
					target_num = source_target_num & 255;
					target_word_id = new WordID(target_synset_id, target_num);
					wordProxies[source_num - 1].addRelatedWord(pointer_type, target_word_id);
				}
			}
			
			// trim pointer lists
			if(synsetPointerMap != null)
				for(ArrayList<ISynsetID> list : synsetPointerMap.values()) list.trimToSize();

			// parse verb frames
			if (synset_pos == POS.VERB) {
				int frame_num, word_num;
				int verbFrameCount = Integer.parseInt(tokenizer.nextToken());
				IVerbFrame frame;
				for (int i = 0; i < verbFrameCount; i++) {
					// Consume '+'
					tokenizer.nextToken();
					// Get frame number
					frame_num = Integer.parseInt(tokenizer.nextToken());
					frame = resolveVerbFrame(frame_num);
					// Get word number
					word_num = Integer.parseInt(tokenizer.nextToken(), 16);
					if (word_num > 0)
						wordProxies[word_num - 1].addVerbFrame(frame);
					else {
						for (IWordBuilder proxy : wordProxies)
							proxy.addVerbFrame(frame);
					}
				}
			}

			// Get gloss
			String gloss = "";
			int index = line.indexOf('|');
			if (index > 0)
				gloss = line.substring(index + 2).trim();
			
			// create synset and words
			List<IWordBuilder> words = Arrays.asList(wordProxies);
			return new Synset(synsetID, lexFile, isAdjSat, isAdjHead, gloss, words, synsetPointerMap);
		
		} catch (NumberFormatException e) {
			throw new MisformattedLineException(line, e);
		} catch (NoSuchElementException e) {
			throw new MisformattedLineException(line, e);
		}
	}

	/**
	 * <p>
	 * Retrieves the verb frames for the {@link #parseLine(String)} method.
	 * </p>
	 * <p>
	 * This is implemented in its own method for ease of subclassing.
	 * </p>
	 * 
	 * @param frameNum
	 *            the number of the frame to return
	 * @return the verb frame corresponding to the specified frame number, or
	 *         <code>null</code> if there is none
	 * @since JWI 2.1.0
	 */
	protected IVerbFrame resolveVerbFrame(int frameNum) {
		return VerbFrame.getFrame(frameNum);
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

	/**
	 * <p>
	 * Retrieves the pointer objects for the {@link #parseLine(String)} method.
	 * </p>
	 * <p>
	 * This is implemented in its own method for ease of subclassing.
	 * </p>
	 * 
	 * @param symbol
	 *            the symbol of the pointer to return
	 * @param pos
	 *            the part of speech of the pointer to return, can be
	 *            <code>null</code> unless the pointer symbol is ambiguous
	 * @return the pointer corresponding to the specified symbol and part of
	 *         speech combination
	 * @throws NullPointerException
	 *             if the symbol is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the symbol and part of speech combination does not
	 *             correspond to a known pointer
	 * @since JWI 2.1.0
	 */
	protected IPointer resolvePointer(String symbol, POS pos){
		return Pointer.getPointerType(symbol, pos);
	}


}