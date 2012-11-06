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

import edu.mit.jwi.data.IHasLifecycle;
import edu.mit.jwi.item.IItem;
import edu.mit.jwi.item.IItemID;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.IWord;

/**
 * Provides a governing interface for dictionaries that cache their results.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.2.0
 */
public interface ICachingDictionary extends IDictionary {

	/**
	 * Returns the cache used by this dictionary, so that it may be configured
	 * or manipulated directly.
	 * 
	 * @return the cache for this dictionary
	 * @since JWI 2.2.0
	 */
	public IItemCache getCache();

	/**
	 * The cache used by a caching dictionary.
	 * 
	 * @author Mark A. Finlayson
	 * @version 2.2.3
	 * @since JWI 2.2.0
	 */
	public interface IItemCache extends IHasLifecycle {

		/**
		 * Returns <code>true</code> if this cache is enabled;
		 * <code>false</code> otherwise. If a cache is enabled, it will cache an
		 * item passed to its <code>cache</code> methods.
		 * 
		 * @return <code>true</code> if this cache is enabled;
		 *         <code>false</code> otherwise
		 * @since JWI 2.2.0
		 */
		public boolean isEnabled();
		
		/**
		 * Enables (<code>true</code>) or disables (<code>false</code>) caching.
		 * 
		 * @param isEnabled
		 *            if <code>true</code>, caching is enabled; if
		 *            <code>false</code>, caching is disabled.
		 * @since JWI 2.2.0
		 */
		public void setEnabled(boolean isEnabled);
		
		/**
		 * Sets the maximum capacity of the cache
		 * 
		 * @param capacity
		 *            the maximum capacity
		 * @since JWI 2.2.0
		 */
		public void setMaximumCapacity(int capacity);
		
		/** 
		 * Returns the maximum capacity of this cache.
		 *
		 * @return the maximum capacity of this cache.
		 * @since JWI 2.2.0
		 */
		public int getMaximumCapacity();
		

		/**
		 * Returns the number of items in the cache.
		 * 
		 * @return the number of items in the cache.
		 * @since JWI 2.2.0
		 */
		public int size();

		/**
		 * Caches the specified item, if this cache is enabled. Otherwise does
		 * nothing.
		 * 
		 * @param item
		 *            the item to be cached; may not be <code>null</code>
		 * @throws NullPointerException
		 *             if the specified item is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public void cacheItem(IItem<?> item);
		
		/**
		 * Caches the specified word, indexed by its sense key.
		 * 
		 * @param word
		 *            the word to be cached; may not be <code>null</code>
		 * @throws NullPointerException
		 *             if the specified word is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public void cacheWordByKey(IWord word);
		
		/**
		 * Caches the specified entry.
		 * 
		 * @param entry
		 *            the entry to be cached; may not be <code>null</code>
		 * @throws NullPointerException
		 *             if the specified entry is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public void cacheSenseEntry(ISenseEntry entry);
		
		/**
		 * Retrieves the item identified by the specified id.
		 * 
		 * @param <T>
		 *            the type of the item
		 * @param <D>
		 *            the type of the item id
		 * @param id
		 *            the id for the requested item
		 * @return the item for the specified id, or <code>null</code> if not
		 *         present in the cache
		 * @throws NullPointerException
		 *             if the specified id is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public <T extends IItem<D>, D extends IItemID<T>> T retrieveItem(D id);
		
		/**
		 * Retrieves the word identified by the specified sense key.
		 * 
		 * @param key
		 *            the sense key for the requested word
		 * @return the word for the specified key, or <code>null</code> if not
		 *         present in the cache
		 * @throws NullPointerException
		 *             if the specified key is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public IWord retrieveWord(ISenseKey key);
		
		/**
		 * Retrieves the sense entry identified by the specified sense key.
		 * 
		 * @param key
		 *            the sense key for the requested sense entry
		 * @return the sense entry for the specified key, or <code>null</code> if not
		 *         present in the cache
		 * @throws NullPointerException
		 *             if the specified key is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public ISenseEntry retrieveSenseEntry(ISenseKey key);

		/**
		 * Removes all entries from the cache.
		 * 
		 * @since JWI 2.2.0
		 */
		public void clear();

	}

}
