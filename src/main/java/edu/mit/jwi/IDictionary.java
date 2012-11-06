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

import java.util.Iterator;

import edu.mit.jwi.data.IHasLifecycle;
import edu.mit.jwi.item.IExceptionEntry;
import edu.mit.jwi.item.IExceptionEntryID;
import edu.mit.jwi.item.IHasVersion;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IIndexWordID;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.morph.IStemmer;

/**
 * Objects that implement this interface are intended as the main entry point to
 * accessing Wordnet data. The dictionary must be opened by calling
 * {@code open()} before it is used.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IDictionary extends IHasVersion, IHasLifecycle {

	/**
	 * This method is identical to <code>getIndexWord(IIndexWordID)</code> and
	 * is provided as a convenience.
	 * 
	 * @param lemma
	 *            the lemma for the index word requested; may not be
	 *            <code>null</code>, empty, or all whitespace
	 * @param pos
	 *            the part of speech; may not be <code>null</code>
	 * @return the index word corresponding to the specified lemma and part of
	 *         speech, or <code>null</code> if none is found
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified lemma is empty or all whitespace
	 * @since JWI 1.0
	 */
	public IIndexWord getIndexWord(String lemma, POS pos);

	/**
	 * Retrieves the specified index word object from the database. If the
	 * specified lemma/part of speech combination is not found, returns
	 * {@code null}.
	 * <p>
	 * <i>Note:</i> This call does no stemming on the specified lemma, it is
	 * taken as specified. That is, if you submit the word "dogs", it will
	 * search for "dogs", not "dog"; in the standard Wordnet distribution, there
	 * is no entry for "dogs" and therefore the call will return
	 * <code>null</code>. This is in contrast to the Wordnet API provided by
	 * Princeton. If you want your searches to capture morphological variation,
	 * use the descendants of the {@link IStemmer} class.
	 * 
	 * @param id
	 *            the id of the index word to search for; may not be
	 *            <code>null</code>
	 * @return the index word, if found; <code>null</code> otherwise
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public IIndexWord getIndexWord(IIndexWordID id);

	/**
	 * Returns an iterator that will iterate over all index words of the
	 * specified part of speech.
	 * 
	 * @param pos
	 *            the part of speech over which to iterate; may not be
	 *            <code>null</code>
	 * @return an iterator that will iterate over all index words of the
	 *         specified part of speech
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public Iterator<IIndexWord> getIndexWordIterator(POS pos);

	/**
	 * Retrieves the word with the specified id from the database. If the
	 * specified word is not found, returns {@code null}
	 * 
	 * @param id
	 *            the id of the word to search for; may not be <code>null</code>
	 * @return the word, if found; <code>null</code> otherwise
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public IWord getWord(IWordID id);

	/**
	 * Retrieves the word with the specified sense key from the database. If the
	 * specified word is not found, returns {@code null}
	 * 
	 * @param key
	 *            the sense key of the word to search for; may not be
	 *            <code>null</code>
	 * @return the word, if found; <code>null</code> otherwise
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public IWord getWord(ISenseKey key);

	/**
	 * Retrieves the synset with the specified id from the database. If the
	 * specified synset is not found, returns {@code null}
	 * 
	 * @param id
	 *            the id of the synset to search for; may not be
	 *            <code>null</code>
	 * @return the synset, if found; <code>null</code> otherwise
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public ISynset getSynset(ISynsetID id);

	/**
	 * Returns an iterator that will iterate over all synsets of the specified
	 * part of speech.
	 * 
	 * @param pos
	 *            the part of speech over which to iterate; may not be
	 *            <code>null</code>
	 * @return an iterator that will iterate over all synsets of the specified
	 *         part of speech
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public Iterator<ISynset> getSynsetIterator(POS pos);

	/**
	 * Retrieves the sense entry for the specified sense key from the database.
	 * If the specified sense key has no associated sense entry, returns
	 * {@code null}
	 * 
	 * @param key
	 *            the sense key of the entry to search for; may not be
	 *            <code>null</code>
	 * @return the entry, if found; <code>null</code> otherwise
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public ISenseEntry getSenseEntry(ISenseKey key);

	/**
	 * Returns an iterator that will iterate over all sense entries in the
	 * dictionary.
	 * 
	 * @return an iterator that will iterate over all sense entries
	 * @since JWI 1.0
	 */
	public Iterator<ISenseEntry> getSenseEntryIterator();

	/**
	 * Retrieves the exception entry for the specified surface form and part of
	 * speech from the database. If the specified surface form/ part of speech
	 * pair has no associated exception entry, returns {@code null}
	 * 
	 * @param surfaceForm
	 *            the surface form to be looked up; may not be <code>null</code>
	 *            , empty, or all whitespace
	 * @param pos
	 *            the part of speech; may not be <code>null</code>
	 * @return the entry, if found; <code>null</code> otherwise
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified surface form is empty or all whitespace
	 * @since JWI 1.0
	 */
	public IExceptionEntry getExceptionEntry(String surfaceForm, POS pos);

	/**
	 * Retrieves the exception entry for the specified id from the database. If
	 * the specified id is not found, returns <code>null</code>
	 * 
	 * @param id
	 *            the exception entry id of the entry to search for; may not be
	 *            <code>null</code>
	 * @return the exception entry for the specified id
	 * @since JWI 1.1
	 */
	public IExceptionEntry getExceptionEntry(IExceptionEntryID id);

	/**
	 * Returns an iterator that will iterate over all exception entries of the
	 * specified part of speech.
	 * 
	 * @param pos
	 *            the part of speech over which to iterate; may not be
	 *            <code>null</code>
	 * @return an iterator that will iterate over all exception entries of the
	 *         specified part of speech
	 * @throws NullPointerException
	 *             if the argument is <code>null</code>
	 * @since JWI 1.0
	 */
	public Iterator<IExceptionEntry> getExceptionEntryIterator(POS pos);

}