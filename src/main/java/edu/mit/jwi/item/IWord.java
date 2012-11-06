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
 * A word, which in Wordnet is an index word paired with a synset.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IWord extends IHasPOS, IItem<IWordID> {

	/**
	 * Returns the root form of this word, never <code>null</code> or empty.
	 * 
	 * @return the non-<code>null</code>, non-empty root form of this word
	 * @since JWI 1.0
	 */
	public String getLemma();

	/**
	 * Returns the synset uniquely identified by this word. The returned synset
	 * will never be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> synset identified by this word.
	 * @since JWI 2.1.0
	 */
	public ISynset getSynset();

	/**
	 * Returns the sense key for this word. Will never return <code>null</code>;
	 * however, the sense key that is returned <em>may</em> not yet have it's
	 * head lemma and head lexical id set yet, and so may throw an exception on
	 * some calls.
	 * 
	 * @see ISenseKey#needsHeadSet()
	 * @see ISenseKey#setHead(String, int)
	 * @return the sense key for this word, never <code>null</code>
	 * @since JWI 2.1.0
	 */
	public ISenseKey getSenseKey();

	/**
	 * A integer in the closed range [0,15] that, when appended onto lemma,
	 * uniquely identifies a sense within a lexicographer file. Lexical id
	 * numbers usually start with 0, and are incremented as additional senses of
	 * the word are added to the same file, although there is no requirement
	 * that the numbers be consecutive or begin with 0. Note that a value of 0
	 * is the default, and therefore is not present in lexicographer files. In
	 * the wordnet data files the lexical id is represented as a one digit
	 * hexadecimal integer.
	 * 
	 * @return the lexical id of the word, an integer between 0 and 15,
	 *         inclusive
	 * @since JWI 1.0
	 */
	public int getLexicalID();

	/**
	 * Returns an immutable map of from pointers to immutable maps. Note that
	 * this only returns IWords related by lexical pointers (i.e., not semantic
	 * pointers). To retrieve items related by semantic pointers, call
	 * {@link ISynset#getRelatedMap()} on the appropriate object.
	 * 
	 * @return an immutable map from lexical pointers to words
	 * @since JWI 2.0.0
	 */
	public Map<IPointer, List<IWordID>> getRelatedMap();

	/**
	 * Returns an immutable list of all word ids related to this word by the
	 * specified pointer type. Note that this only returns words related by
	 * lexical pointers (i.e., not semantic pointers). To retrieve items related
	 * by semantic pointers, call {@link ISynset#getRelatedSynsets()}. If this
	 * word has no targets for the for the specified pointer, this method
	 * returns an empty list. This method never returns {@code null}.
	 * 
	 * @param ptr
	 *            the pointer for which related words are requested
	 * @return the list of words related by the specified pointer, or an empty
	 *         list if none.
	 * @since JWI 2.0.0
	 */
	public List<IWordID> getRelatedWords(IPointer ptr);

	/**
	 * Returns an immutable list of all word ids related to this word by
	 * pointers in the database. Note this only returns words related to this
	 * word by lexical pointers, i.e., not semantic pointers. To retrieve
	 * synsets related to the synset for this word by semantic pointers, call
	 * {@link ISynset#getRelatedSynsets()} on the <code>ISynset</code> for this
	 * word.
	 * 
	 * @return an immutable list of all lexically-related words
	 * @since JWI 2.0.0
	 */
	public List<IWordID> getRelatedWords();

	/**
	 * Returns an immutable list of all verb frames associated with this word.
	 * If there are no such frames, returns the empty list.
	 * 
	 * @return an immutable list of all the verb frames associated with this
	 *         word, or the empty list if none.
	 * @since JWI 2.0.0
	 */
	public List<IVerbFrame> getVerbFrames();

	/**
	 * Returns the adjective marker of this word. If this word has no adjective
	 * marker, returns {@code null}
	 * 
	 * @return the adjective marker for this word, or <code>null</code> if none.
	 * @since JWI 2.1.0
	 */
	public AdjMarker getAdjectiveMarker();

}
