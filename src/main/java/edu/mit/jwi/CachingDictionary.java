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

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.mit.jwi.item.ExceptionEntryID;
import edu.mit.jwi.item.IExceptionEntry;
import edu.mit.jwi.item.IExceptionEntryID;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IIndexWordID;
import edu.mit.jwi.item.IItem;
import edu.mit.jwi.item.IItemID;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IVersion;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.IndexWordID;
import edu.mit.jwi.item.POS;


/** 
 * A dictionary that caches the results of another dictionary
 *
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.2.0
 */
public class CachingDictionary implements ICachingDictionary {
	
	// final instance fields 
	private final IDictionary backing;
	private final IItemCache cache;

	/**
	 * Constructs a new caching dictionary that caches the results of the
	 * specified backing dictionary
	 * 
	 * @param backing
	 *            the dictionary whose results should be cached
	 * @since JWI 2.2.0
	 */
	public CachingDictionary(IDictionary backing){
		if(backing == null)
			throw new NullPointerException();
		this.cache = createCache();
		this.backing = backing;
	}
	
	/**
	 * Returns the dictionary that is wrapped by this dictionary; will never
	 * return <code>null</code>
	 * 
	 * @return the dictionary that is wrapped by this dictionary
	 * @since JWI 2.2.0
	 */
	public IDictionary getBackingDictionary(){
		return backing;
	}

	/**
	 * This operation creates the cache that is used by the dictionary. It is
	 * set inside it's own method for ease of subclassing. It is called only
	 * when an instance of this class is created. It is marked protected for
	 * ease of subclassing.
	 * 
	 * @return the item cache to be used by this dictionary
	 * @since JWI 2.2.0
	 */
	protected IItemCache createCache(){
		return new ItemCache();
	}
	
	/**
	 * An internal method for assuring compliance with the dictionary interface
	 * that says that methods will throw {@code ObjectClosedException}s if
	 * the dictionary has not yet been opened.
	 * 
	 * @throws ObjectClosedException
	 *             if the dictionary is closed.
	 * @since JWI 2.2.0
	 */
	protected void checkOpen() {
		if(isOpen()){
			if(!getCache().isOpen()){
				try {
					getCache().open();
				} catch (IOException e) {
					throw new ObjectClosedException(e);
				}
			}
		} else {
			if(getCache().isOpen())
				getCache().close();
			throw new ObjectClosedException();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.ICachingDictionary#getCache()
	 */
	public IItemCache getCache() {
		return cache;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IHasLifecycle#open()
	 */
	public boolean open() throws IOException {
		if(isOpen())
			return true;
		cache.open();
		return backing.open();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IHasLifecycle#isOpen()
	 */
	public boolean isOpen() {
		return backing.isOpen();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IClosable#close()
	 */
	public void close() {
		if(!isOpen())
			return;
		getCache().close();
		backing.close();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IHasVersion#getVersion()
	 */
	public IVersion getVersion() {
		return backing.getVersion();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getIndexWord(java.lang.String, edu.mit.jwi.item.POS)
	 */
	public IIndexWord getIndexWord(String lemma, POS pos) {
		checkOpen();
		IIndexWordID id = new IndexWordID(lemma, pos);
		IIndexWord item = getCache().retrieveItem(id);
		if(item == null){
			item = backing.getIndexWord(id);
			if(item != null)
				getCache().cacheItem(item);
		}
		return item;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getIndexWord(edu.mit.jwi.item.IIndexWordID)
	 */
	public IIndexWord getIndexWord(IIndexWordID id) {
		checkOpen();
		IIndexWord item = getCache().retrieveItem(id);
		if(item == null){
			item = backing.getIndexWord(id);
			if(item != null)
				getCache().cacheItem(item);
		}
		return item;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getIndexWordIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<IIndexWord> getIndexWordIterator(POS pos) {
		return backing.getIndexWordIterator(pos);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getWord(edu.mit.jwi.item.IWordID)
	 */
	public IWord getWord(IWordID id) {
		checkOpen();
		IWord item = getCache().retrieveItem(id);
		if(item == null){
			item = backing.getWord(id);
			if(item != null)
				cacheSynset(item.getSynset());
		}
		return item;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getWord(edu.mit.jwi.item.ISenseKey)
	 */
	public IWord getWord(ISenseKey key) {
		checkOpen();
		IWord item = getCache().retrieveWord(key);
		if(item == null){
			item = backing.getWord(key);
			if(item != null)
				cacheSynset(item.getSynset());
		}
		return item;
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSynset(edu.mit.jwi.item.ISynsetID)
	 */
	public ISynset getSynset(ISynsetID id) {
		checkOpen();
		ISynset item = getCache().retrieveItem(id);
		if(item == null){
			item = backing.getSynset(id);
			if(item != null)
				cacheSynset(item);
		}
		return item;
	}

	/**
	 * Caches the specified synset and its words.
	 * 
	 * @param synset
	 *            the synset to be cached; may not be <code>null</code>
	 * @throws NullPointerException
	 *             if the specified synset is <code>null</code>
	 * @since JWI 2.2.0
	 */
	protected void cacheSynset(ISynset synset){
		IItemCache cache = getCache();
		cache.cacheItem(synset);
		for(IWord word : synset.getWords()){
			cache.cacheItem(word);
			cache.cacheWordByKey(word);
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSynsetIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<ISynset> getSynsetIterator(POS pos) {
		return backing.getSynsetIterator(pos);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSenseEntry(edu.mit.jwi.item.ISenseKey)
	 */
	public ISenseEntry getSenseEntry(ISenseKey key) {
		checkOpen();
		ISenseEntry entry = getCache().retrieveSenseEntry(key);
		if(entry == null){
			entry = backing.getSenseEntry(key);
			if(entry != null)
				getCache().cacheSenseEntry(entry);
		}
		return entry;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSenseEntryIterator()
	 */
	public Iterator<ISenseEntry> getSenseEntryIterator() {
		return backing.getSenseEntryIterator();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getExceptionEntry(java.lang.String, edu.mit.jwi.item.POS)
	 */
	public IExceptionEntry getExceptionEntry(String surfaceForm, POS pos) {
		checkOpen();
		IExceptionEntryID id = new ExceptionEntryID(surfaceForm, pos);
		IExceptionEntry item = getCache().retrieveItem(id);
		if(item == null){
			item = backing.getExceptionEntry(id);
			if(item != null)
				getCache().cacheItem(item);
		}
		return item;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getExceptionEntry(edu.mit.jwi.item.IExceptionEntryID)
	 */
	public IExceptionEntry getExceptionEntry(IExceptionEntryID id) {
		checkOpen();
		IExceptionEntry item = getCache().retrieveItem(id);
		if(item == null){
			item = backing.getExceptionEntry(id);
			if(item != null) getCache().cacheItem(item);
		}
		return item;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getExceptionEntryIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<IExceptionEntry> getExceptionEntryIterator(POS pos) {
		return backing.getExceptionEntryIterator(pos);
	}
	
	/**
	 * An LRU cache for objects in JWI.
	 * 
	 * @author Mark A. Finlayson
	 * @version 2.2.3
	 * @since JWI 2.2.0
	 */
	public static class ItemCache implements IItemCache {
		
		// default configuration
		public static final int DEFAULT_INITIAL_CAPACITY = 16;
		public static final int DEFAULT_MAXIMUM_CAPACITY = 512;
		public static final float DEFAULT_LOAD_FACTOR = 0.75f;
		
		protected Lock lifecycleLock = new ReentrantLock();

		/**
		 * Flag that records whether caching is enabled for this dictionary. Default
		 * starting state is <code>true</code>.
		 */
		private boolean isEnabled = true;
		
		/**
		 * Initial capacity of the caches. If this is set to less than one, then
		 * the system default is used.
		 */
		private int initialCapacity;

		/**
		 * Maximum capacity of the caches. If this is set to less than one, then
		 * the cache size is unlimited.
		 */
		private int maximumCapacity;
		
		// The caches themselves
		protected Map<IItemID<?>, IItem<?>> itemCache;
		protected Map<ISenseKey, IWord> keyCache;
		protected Map<ISenseKey, ISenseEntry> senseCache;

		/**
		 * Default constructor that initializes the dictionary with caching enabled.
		 * 
		 * @since JWI 2.2.0
		 */
		public ItemCache() {
			this(DEFAULT_INITIAL_CAPACITY, DEFAULT_MAXIMUM_CAPACITY, true);
		}

		/**
		 * Caller can specify both the initial size, maximum size, and the
		 * initial state of caching.
		 * 
		 * @param initialCapacity
		 *            the initial capacity of the cache
		 * @param maxCapacity
		 *            the maximum capacity of the cache
		 * @param enabled
		 *            whether the cache starts out enabled
		 * @since JWI 2.2.0
		 */
		public ItemCache(int initialCapacity, int maxCapacity, boolean enabled) {
			setInitialCapacity(initialCapacity);
			setMaximumCapacity(maxCapacity);
			setEnabled(enabled);
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.data.IHasLifecycle#open()
		 */
		public boolean open() throws IOException {
			if(isOpen()) return true;
			try{
				lifecycleLock.lock();
				int capacity = (initialCapacity < 1) ? 
						DEFAULT_INITIAL_CAPACITY : 
							initialCapacity;
				itemCache = this.<IItemID<?>,IItem<?>>makeCache(capacity);
				keyCache = this.<ISenseKey,IWord>makeCache(capacity);
				senseCache = this.<ISenseKey,ISenseEntry>makeCache(capacity);
			} finally {
				lifecycleLock.unlock();
			}
			return true;
		}

		/**
		 * Creates the map that backs this cache.
		 * 
		 * @param <K>
		 *            the key type
		 * @param <V>
		 *            the value type
		 * @param initialCapacity
		 *            the initial capacity
		 * @return the new map
		 * @since JWI 2.2.0
		 */
		protected <K,V> Map<K,V> makeCache(int initialCapacity){
			return new LinkedHashMap<K,V>(initialCapacity, DEFAULT_LOAD_FACTOR, true);
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.data.IHasLifecycle#isOpen()
		 */
		public boolean isOpen() {
			return senseCache != null;
		}
		
		/**
		 * An internal method for assuring compliance with the dictionary
		 * interface that says that methods will throw
		 * {@code ObjectClosedException}s if the dictionary has not yet been
		 * opened.
		 * 
		 * @throws ObjectClosedException
		 *             if the dictionary is closed.
		 * @since JWI 2.2.0
		 */
		protected void checkOpen() {
			if(!isOpen())
				throw new ObjectClosedException();
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.data.IClosable#close()
		 */
		public void close() {
			if(!isOpen())
				return;
			try{
				lifecycleLock.lock();
				itemCache = null;
				keyCache = null;
				senseCache = null;
			} finally {
				lifecycleLock.unlock();
			}
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#clear()
		 */
		public void clear(){
			if(itemCache != null)
				itemCache.clear();
			if(keyCache != null)
				keyCache.clear();
			if(senseCache != null)
				senseCache.clear();
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#isEnabled()
		 */
		public boolean isEnabled() {
			return isEnabled;
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#setEnabled(boolean)
		 */
		public void setEnabled(boolean isEnabled) {
			this.isEnabled = isEnabled;
		}

		/** 
		 * Returns the initial capacity of this cache.
		 *
		 * @return the initial capacity of this cache.
		 * @since JWI 2.2.0
		 */
		public int getInitialCapacity() {
			return initialCapacity;
		}
		
		/**
		 * Sets the initial capacity of the cache
		 * 
		 * @param capacity
		 *            the initial capacity
		 * @since JWI 2.2.0
		 */
		public void setInitialCapacity(int capacity){
			initialCapacity = capacity < 1 ? 
					DEFAULT_INITIAL_CAPACITY : 
						capacity;
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#getMaximumCapacity()
		 */
		public int getMaximumCapacity() {
			return maximumCapacity;
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#setMaximumCapacity(int)
		 */
		public void setMaximumCapacity(int capacity) {
			int oldCapacity = maximumCapacity;
			maximumCapacity = capacity;
			if(maximumCapacity < 1 || oldCapacity <= maximumCapacity)
				return;
			reduceCacheSize(itemCache);
			reduceCacheSize(keyCache);
			reduceCacheSize(senseCache);
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#size()
		 */
		public int size(){
			checkOpen();
			return itemCache.size() + keyCache.size() + senseCache.size();
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#cacheItem(edu.mit.jwi.item.IItem)
		 */
		public void cacheItem(IItem<?> item){
			checkOpen();
			if(!isEnabled())
				return;
			itemCache.put(item.getID(), item);
			reduceCacheSize(itemCache);
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#cacheWordByKey(edu.mit.jwi.item.IWord)
		 */
		public void cacheWordByKey(IWord word){
			checkOpen();
			if(!isEnabled())
				return;
			keyCache.put(word.getSenseKey(), word);
			reduceCacheSize(keyCache);
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#cacheSenseEntry(edu.mit.jwi.item.ISenseEntry)
		 */
		public void cacheSenseEntry(ISenseEntry entry){
			checkOpen();
			if(!isEnabled())
				return;
			senseCache.put(entry.getSenseKey(), entry);
			reduceCacheSize(senseCache);
		}

		/**
		 * Brings the map size into line with the specified maximum capacity of
		 * this cache.
		 * 
		 * @param cache
		 *            the map to be trimmed
		 * @since JWI 2.2.0
		 */
		protected void reduceCacheSize(Map<?,?> cache){
			if(!isOpen() || maximumCapacity < 1 || cache.size() < maximumCapacity)
				return;
			synchronized(cache){
				int remove = cache.size() - maximumCapacity;
				Iterator<?> itr = cache.keySet().iterator();
				for(int i = 0; i <= remove; i++)
					if(itr.hasNext()){
						itr.next();
						itr.remove();
					}
			}
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#retrieveItem(edu.mit.jwi.item.IItemID)
		 */
		@SuppressWarnings("unchecked")
		public <T extends IItem<D>, D extends IItemID<T>> T retrieveItem(D id){
			checkOpen();
			return (T)itemCache.get(id);
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#retrieveWord(edu.mit.jwi.item.ISenseKey)
		 */
		public IWord retrieveWord(ISenseKey key){
			checkOpen();
			return keyCache.get(key);
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.ICachingDictionary.IItemCache#retrieveSenseEntry(edu.mit.jwi.item.ISenseKey)
		 */
		public ISenseEntry retrieveSenseEntry(ISenseKey key){
			checkOpen();
			return senseCache.get(key);
		}
	}

}
