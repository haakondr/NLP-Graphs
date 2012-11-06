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
import java.util.Map;

/**
 * Represents a synset.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3, Nov. 16, 2007
 * @since JWI 1.0
 */
public interface ISynset extends IHasPOS, IItem<ISynsetID> {

	/**
	 * Returns the data file byte offset of this synset.
	 * 
	 * @return int the offset in the associated data source
	 * @since JWI 1.0
	 */
	public int getOffset();

	/**
	 * Returns a description of the lexical file. In wordnet data files, the
	 * lexical file number is a two digit decimal integer representing the name
	 * of the lexicographer file containing the synset for the sense.
	 * 
	 * @return the lexical file object that describes the lexical file in which
	 *         this synset is stored
	 * @since JWI 2.1.0
	 */
	public ILexFile getLexicalFile();

	/**
	 * Returns the type of the synset, encoded as follows: 1=Noun, 2=Verb,
	 * 3=Adjective, 4=Adverb, 5=Adjective Satellite.
	 * 
	 * @return the type of the synset, an integer between 1 and 5, inclusive
	 * @since JWI 1.0
	 */
	public int getType();

	/**
	 * Returns the gloss (brief, plain-English description) of this synset.
	 * 
	 * @return String Returns the non-<code>null</code>, non-empty gloss.
	 * @since JWI 1.0
	 */
	public String getGloss();

	/**
	 * Returns an immutable list of the word objects (synset, index word pairs)
	 * associated with this synset.
	 * 
	 * @return a non-<code>null</code>, immutable list of words for this synset
	 * @since JWI 2.0.0
	 */
	public List<IWord> getWords();
	
	/**
	 * Returns the word with the specified word number. Words are numbered
	 * sequentially from 1 up to, and including, 255.
	 * 
	 * @param wordNumber
	 *            the number of the word to be retrieved
	 * @return the word with the specified word number
	 * @throws IndexOutOfBoundsException
	 *             if the word number is not an appropriate word number for this
	 *             synset.
	 * @since JWI 2.1.2
	 */
	public IWord getWord(int wordNumber);
	
	/**
	 * Returns <code>true</code> if this synset is an adjective head;
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this synset represents an adjective head;
	 *         <code>false</code> otherwise.
	 * @since JWI 1.0
	 */
	public boolean isAdjectiveHead();

	/**
	 * Returns <code>true</code> if this synset is an adjective satellite;
	 * <code>false</code> otherwise.
	 * 
	 * @return <code>true</code> if this synset represents an adjective satellite;
	 *         <code>false</code> otherwise.
	 * @since JWI 1.0
	 */
	public boolean isAdjectiveSatellite();

	/**
	 * Returns an immutable map from semantic pointers to immutable lists of
	 * synsets. Note that this only returns synsets related by semantic pointers
	 * (i.e., non-lexical pointers). To obtain lexical pointers, call
	 * {@link IWord#getRelatedMap()} on the appropriate word.
	 * 
	 * @return a non-<code>null</code>, unmodifiable map from pointers to
	 *         synsets
	 * @since JWI 2.0.0
	 */
	public Map<IPointer, List<ISynsetID>> getRelatedMap();

	/**
	 * Returns an immutable list of the ids of all synsets that are related to
	 * this synset by the specified pointer type. Note that this only returns a
	 * non-empty result for semantic pointers (i.e., non-lexical pointers). To
	 * obtain lexical pointers, call {@link IWord#getRelatedWords()()} on the
	 * appropriate object.
	 * <p>
	 * If there are no such synsets, this method returns the empty list.
	 * 
	 * @param ptr
	 *            the pointer for which related synsets are to be retrieved.
	 * @return the list of synsets related by the specified pointer; if there
	 *         are no such synsets, returns the empty list
	 * @since JWI 2.0.0
	 */
	public List<ISynsetID> getRelatedSynsets(IPointer ptr);

	/**
	 * Returns an immutable list of synset ids for all synsets that are
	 * connected by pointers to this synset. Note that the related synsets
	 * returned by this call are related by semantic pointers (as opposed to
	 * lexical pointers, which are relationships between
	 * {@link edu.mit.jwi.item.IWord} objects.
	 *
	 * @return a list of all synsets semantically related to the current synset 
	 * @since JWI 2.0.0
	 */
	public List<ISynsetID> getRelatedSynsets();

}
