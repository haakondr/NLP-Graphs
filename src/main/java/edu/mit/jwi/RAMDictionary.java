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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.mit.jwi.data.FileProvider;
import edu.mit.jwi.data.ILoadPolicy;
import edu.mit.jwi.item.ExceptionEntryID;
import edu.mit.jwi.item.IExceptionEntry;
import edu.mit.jwi.item.IExceptionEntryID;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IIndexWordID;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IVerbFrame;
import edu.mit.jwi.item.IVersion;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.IndexWord;
import edu.mit.jwi.item.IndexWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.SenseEntry;
import edu.mit.jwi.item.Synset;
import edu.mit.jwi.item.Synset.IWordBuilder;
import edu.mit.jwi.item.Word;

/**
 * Default implementation of the <code>IRAMDictionary</code> interface. This
 * implementation is designed to wrap an arbitrary dictionary object; however,
 * convenience constructors are provided for the most common use cases (i.e.,
 * Wordnet located on the local filesystem, pointed to by a URL or File).
 * <p>
 * <b>Note:</b> If you receive an {@link OutOfMemoryError} while using this
 * object, try increasing the heap size of your JVM, for example, by using the
 * <code>-Xmx</code> switch.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.2.0
 */
public class RAMDictionary implements IRAMDictionary {
	
	// immutable fields
	protected final IDictionary backing;
	protected final Lock lifecycleLock = new ReentrantLock(); 
	protected final Lock loadLock = new ReentrantLock(); 
	
	// instance fields
	protected transient Thread loader;
	protected int loadPolicy;
	protected IVersion version;
	protected DictionaryData data;

	/**
	 * Constructs a new wrapper RAM dictionary that will load the contents of
	 * the wrapped dictionary into memory, with the specified load policy
	 * 
	 * @see ILoadPolicy
	 * @param dict
	 *            the dictionary to be wrapped
	 * @param loadPolicy
	 *            the load policy of the dictionary; see constants in
	 *            {@link ILoadPolicy}.
	 * @since JWI 2.2.0
	 */
	public RAMDictionary(IDictionary dict, int loadPolicy){
		if(dict == null)
			throw new NullPointerException();
		this.backing = dict;
		this.loadPolicy = loadPolicy;
	}

	/**
	 * Constructs a new wrapper RAM dictionary that will load the contents the
	 * specified local Wordnet data, with the specified load policy
	 * 
	 * @see ILoadPolicy
	 * @param url
	 *            a url pointing to a local copy of wordnet; may not be
	 *            <code>null</code>
	 * @param loadPolicy
	 *            the load policy of the dictionary; see constants in
	 *            {@link ILoadPolicy}.
	 * @throws NullPointerException
	 *             if the specified url is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public RAMDictionary(URL url, int loadPolicy){
		this(new DataSourceDictionary(new FileProvider(url)), loadPolicy);
	}
	
	/**
	 * Constructs a new wrapper RAM dictionary that will load the contents the
	 * specified local Wordnet data, with the specified load policy
	 * 
	 * @see ILoadPolicy
	 * @param file
	 *            a file pointing to a local copy of wordnet; may not be
	 *            <code>null</code>
	 * @param loadPolicy
	 *            the load policy of the dictionary; see constants in
	 *            {@link ILoadPolicy}.
	 * @throws NullPointerException
	 *             if the specified file is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public RAMDictionary(File file, int loadPolicy){
		this(new DataSourceDictionary(new FileProvider(file)), loadPolicy);

	}

	/**
	 * Returns the dictionary that backs this instance.
	 * 
	 * @return the dictionary that backs this instance.
	 * @since JWI 2.2.0
	 */
	public IDictionary getBackingDictionary(){
		return backing;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadPolicy#getLoadPolicy()
	 */
	public int getLoadPolicy() {
		return loadPolicy;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadPolicy#setLoadPolicy(int)
	 */
	public void setLoadPolicy(int policy) {
		if(isOpen()) throw new IllegalStateException("dictionary is currently open");
		this.loadPolicy = policy;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#isLoaded()
	 */
	public boolean isLoaded() {
		return data != null;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#load()
	 */
	public void load() {
		try {
			load(false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#load(boolean)
	 */
	public void load(boolean block) throws InterruptedException {
		if(loader != null || !isOpen()) return;
		try{
			loadLock.lock();
			if(loader != null) return;
			loader = new Thread(new JWIBackgroundDataLoader());
			loader.setName(JWIBackgroundDataLoader.class.getSimpleName());
			loader.setDaemon(true);
			loader.start();
			if(block) loader.join();
		} finally {
			loadLock.unlock();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IHasLifecycle#open()
	 */
	public boolean open() throws IOException {
		try {
			lifecycleLock.lock();
			if(isOpen()) return true;
			boolean result = backing.open();
			if(result){
				version = backing.getVersion();
				try {
					switch(loadPolicy){
					case IMMEDIATE_LOAD:
						load(true);
						break;
					case BACKGROUND_LOAD:
						load(false);
						break;
					}
				} catch(InterruptedException e){
					e.printStackTrace();
				}
			}
			return result;
		} finally {
			lifecycleLock.unlock();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IHasLifecycle#isOpen()
	 */
	public boolean isOpen() {
		return data != null || backing.isOpen();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IClosable#close()
	 */
	public void close() {
		try {
			lifecycleLock.lock();
			
			// stop loading
			if(loader != null){
				loader.interrupt();
				try {
					loader.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}

			loader = null;
			version = null;
			backing.close();
			data = null;
		} finally {
			lifecycleLock.unlock();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IHasVersion#getVersion()
	 */
	public IVersion getVersion() {
		return version;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getIndexWord(java.lang.String, edu.mit.jwi.item.POS)
	 */
	public IIndexWord getIndexWord(String lemma, POS pos) {
		return getIndexWord(new IndexWordID(lemma, pos));
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getIndexWord(edu.mit.jwi.item.IIndexWordID)
	 */
	public IIndexWord getIndexWord(IIndexWordID id) {
		if(data != null){
			return data.idxWords.get(id.getPOS()).get(id);
		} else {
			return backing.getIndexWord(id);
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getIndexWordIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<IIndexWord> getIndexWordIterator(POS pos) {
		return new HotSwappableIndexWordIterator(pos);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getWord(edu.mit.jwi.item.IWordID)
	 */
	public IWord getWord(IWordID id) {
		if(data != null){
			ISynset synset = data.synsets.get(id.getPOS()).get(id.getSynsetID());
			return (synset == null) ? null : synset.getWord(id.getWordNumber());
		} else {
			return backing.getWord(id);
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getWord(edu.mit.jwi.item.ISenseKey)
	 */
	public IWord getWord(ISenseKey key) {
		if(data != null){
			return data.words.get(key);
		} else {
			return backing.getWord(key);
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSynset(edu.mit.jwi.item.ISynsetID)
	 */
	public ISynset getSynset(ISynsetID id) {
		if(data != null){
			return data.synsets.get(id.getPOS()).get(id);
		} else {
			return backing.getSynset(id);
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSynsetIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<ISynset> getSynsetIterator(POS pos) {
		return new HotSwappableSynsetIterator(pos);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSenseEntry(edu.mit.jwi.item.ISenseKey)
	 */
	public ISenseEntry getSenseEntry(ISenseKey key) {
		if(data != null){
			return data.senses.get(key);
		} else {
			return backing.getSenseEntry(key);
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getSenseEntryIterator()
	 */
	public Iterator<ISenseEntry> getSenseEntryIterator() {
		return new HotSwappableSenseEntryIterator();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getExceptionEntry(java.lang.String, edu.mit.jwi.item.POS)
	 */
	public IExceptionEntry getExceptionEntry(String surfaceForm, POS pos) {
		return getExceptionEntry(new ExceptionEntryID(surfaceForm, pos));
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getExceptionEntry(edu.mit.jwi.item.IExceptionEntryID)
	 */
	public IExceptionEntry getExceptionEntry(IExceptionEntryID id) {
		if(data != null){
			return data.exceptions.get(id.getPOS()).get(id);
		} else {
			return backing.getExceptionEntry(id);
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.IDictionary#getExceptionEntryIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<IExceptionEntry> getExceptionEntryIterator(POS pos) {
		return new HotSwappableExceptionEntryIterator(pos);
	}

	/**
	 * An iterator that allows the dictionary to be loaded into memory while it
	 * is iterating.
	 * 
	 * @param <E>
	 *            the element type of the iterator
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	protected abstract class HotSwappableIterator<E> implements Iterator<E> {
		
		private Iterator<E> itr;
		private boolean checkForLoad;
		private E last = null;

		/**
		 * Constructs a new hot swappable iterator.
		 * 
		 * @param itr the wrapped iterator
		 * @param checkForLoad
		 *            if <code>true</code>, on each call the iterator checks to
		 *            see if the dictionary has been loaded into memory,
		 *            switching data sources if so
		 * @throws NullPointerException
		 *             if the specified iterator is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public HotSwappableIterator(Iterator<E> itr, boolean checkForLoad){
			if(itr == null)
				throw new NullPointerException();
			this.itr = itr;
			this.checkForLoad = checkForLoad;
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			if(checkForLoad)
				checkForLoad();
			return itr.hasNext();
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see java.util.Iterator#next()
		 */
		public E next() {
			if(checkForLoad){
				checkForLoad();
				last = itr.next();
				return last;
			} else {
				return itr.next();
			}
		}
		
		/**
		 * Checks to see if the data has been loaded into memory; is so,
		 * replaces the original iterator with one that iterates over the
		 * in-memory data structures.
		 * 
		 * @since JWI 2.2.0
		 */
		protected void checkForLoad(){
			if(data == null)
				return;
			checkForLoad = false;
			itr = makeIterator();
			if(last != null){
				E consume;
				int i = 0;
				while(itr.hasNext()){
					i++;
					consume = itr.next();
					if(last.equals(consume))
						return;
				}
				throw new IllegalStateException();
			}
		}
		
		/**
		 * Constructs the iterator that will iterate over the loaded data.
		 * 
		 * @return the new iterator to be swapped in when loading is done
		 * @since JWI 2.2.0
		 */
		protected abstract Iterator<E> makeIterator();

		/* 
		 * (non-Javadoc) 
		 *
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			throw new UnsupportedOperationException();
		}
	}
	
	/** 
	 * A hot swappable iterator for index words.
	 *
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	protected class HotSwappableIndexWordIterator extends HotSwappableIterator<IIndexWord> {

		// the part of speech for this iterator
		private final POS pos;
		
		/**
		 * Constructs a new hot swappable iterator for index words.
		 * 
		 * @param pos
		 *            the part of speech for the iterator
		 * @since JWI 2.2.0
		 */
		public HotSwappableIndexWordIterator(POS pos){
			super((data == null) ? 
					backing.getIndexWordIterator(pos) : 
						data.idxWords.get(pos).values().iterator(), data == null);
			this.pos = pos;
		}
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.RAMDictionary.HotSwappableIterator#makeIterator()
		 */
		@Override
		protected Iterator<IIndexWord> makeIterator() {
			return data.idxWords.get(pos).values().iterator();
		}
		
	}
	
	/** 
	 * A hot swappable iterator for synsets.
	 *
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	protected class HotSwappableSynsetIterator extends HotSwappableIterator<ISynset> {
		
		// the part of speech for this iterator
		private final POS pos;

		/**
		 * Constructs a new hot swappable iterator for synsets.
		 * 
		 * @param pos
		 *            the part of speech for the iterator
		 * @since JWI 2.2.0
		 */
		public HotSwappableSynsetIterator(POS pos){
			super((data == null) ? 
					backing.getSynsetIterator(pos) : 
						data.synsets.get(pos).values().iterator(), data == null);
			this.pos = pos;
		}
		
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.RAMDictionary.HotSwappableIterator#makeIterator()
		 */
		@Override
		protected Iterator<ISynset> makeIterator() {
			return data.synsets.get(pos).values().iterator();
		}
		
	}

	/**
	 * A hot swappable iterator that iterates over exceptions entries for a
	 * particular part of speech.
	 * 
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	protected class HotSwappableExceptionEntryIterator extends HotSwappableIterator<IExceptionEntry> {

		// the part of speech for this iterator
		private final POS pos;

		/**
		 * Constructs a new hot swappable iterator that iterates over exception
		 * entries for the specified part of speech.
		 * 
		 * @param pos
		 *            the part of speech for this iterator, may not be
		 *            <code>null</code>
		 * @throws NullPointerException
		 *             if the specified part of speech is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public HotSwappableExceptionEntryIterator(POS pos){
			super((data == null) ? backing.getExceptionEntryIterator(pos) : data.exceptions.get(pos).values().iterator(), data == null);
			this.pos = pos;
		}
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.RAMDictionary.HotSwappableIterator#makeIterator()
		 */
		@Override
		protected Iterator<IExceptionEntry> makeIterator() {
			return data.exceptions.get(pos).values().iterator();
		}
		
	}

	/**
	 * A hot swappable iterator that iterates over sense entries.
	 * 
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	protected class HotSwappableSenseEntryIterator extends HotSwappableIterator<ISenseEntry> {

		/**
		 * Constructs a new hot swappable iterator that iterates over sense
		 * entries.
		 * 
		 * @throws NullPointerException
		 *             if the specified part of speech is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public HotSwappableSenseEntryIterator(){
			super((data == null) ? backing.getSenseEntryIterator() : data.senses.values().iterator(), data == null);
		}
		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.RAMDictionary.HotSwappableIterator#makeIterator()
		 */
		@Override
		protected Iterator<ISenseEntry> makeIterator() {
			return data.senses.values().iterator();
		}
		
	}

	/**
	 * This runnable loads the dictionary data into memory and sets the
	 * appropriate variable in the parent dictionary.
	 * 
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	protected class JWIBackgroundDataLoader implements Runnable {
	
		/* 
		 * (non-Javadoc) 
		 *
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				DataLoader loader = new DataLoader(backing);
				RAMDictionary.this.data = loader.call();
				backing.close();
			} catch(Throwable t) {
				if(!Thread.currentThread().isInterrupted()){
					t.printStackTrace();
					System.err.println("Unable to load dictionary data into memory");	
				}
			}
		}
	}

	/**
	 * A <code>Callable</code> that creates a dictionary data from a specified
	 * dictionary. The data loader does not change the open state of the
	 * dictionary; the dictionary for the loader must be open for the loader to
	 * function without throwing an exception. The loader may be called multiple
	 * times (in a thread-safe manner) as long as the dictionary is open.
	 * 
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	public static class DataLoader implements Callable<DictionaryData> {
		
		// the source of the dictionary data
		private final IDictionary source;
	
		/**
		 * Constructs a new data loader object, that uses the specified
		 * dictionary to load its data.
		 * 
		 * @throws NullPointerException
		 *             if the specified dictionary is <code>null</code>
		 * @since JWI 2.2.0
		 */
		public DataLoader(IDictionary source){
			if(source == null) throw new NullPointerException();
			this.source = source;
		}
	
		/* 
		 * (non-Javadoc) 
		 *
		 * @see java.util.concurrent.Callable#call()
		 */
		public DictionaryData call() throws Exception {
			
			DictionaryData result = new DictionaryData();
			
			Map<IIndexWordID, IIndexWord> idxWords;
			Map<ISynsetID, ISynset> synsets;
			Map<IExceptionEntryID, IExceptionEntry> exceptions;
			
			IIndexWord idxWord;
			ISynset synset;
			IExceptionEntry exception;
			
			Thread t = Thread.currentThread();
			
			for(POS pos : POS.values()){
				
				// index words
				idxWords = result.idxWords.get(pos);
				for(Iterator<IIndexWord> i = source.getIndexWordIterator(pos); i.hasNext(); ){
					idxWord = i.next();
					idxWords.put(idxWord.getID(), idxWord);
				}
				if(t.isInterrupted()) return null;
				
				// synsets and words
				synsets = result.synsets.get(pos);
				for(Iterator<ISynset> i = source.getSynsetIterator(pos); i.hasNext(); ){
					synset = i.next();
					synsets.put(synset.getID(), synset);
					for(IWord word : synset.getWords())	result.words.put(word.getSenseKey(), word);
				}
				if(t.isInterrupted()) return null;
				
				// exceptions
				exceptions = result.exceptions.get(pos);
				for(Iterator<IExceptionEntry> i = source.getExceptionEntryIterator(pos); i.hasNext(); ){
					exception = i.next();
					exceptions.put(exception.getID(), exception);
				}
				if(t.isInterrupted()) return null;
			}
			
			// sense entries
			ISenseEntry entry;
			IWord word;
			for(Iterator<ISenseEntry> i = source.getSenseEntryIterator(); i.hasNext(); ){
				entry = i.next();
				word = result.words.get(entry.getSenseKey());
				result.senses.put(word.getSenseKey(), makeSenseEntry(word.getSenseKey(), entry));
			}
			if(t.isInterrupted()) return null;
			
			result.compactSize();
			if(t.isInterrupted()) return null;
	
			result.compactObjects();
			if(t.isInterrupted()) return null;
			
			return result;
		}
	
		/**
		 * Creates a new sense entry that replicates the specified sense entry.
		 * The new sense entry replaces it's internal sense key with the
		 * specified sense key thus removing a redundant object.
		 * 
		 * @param key
		 *            the sense key to be used
		 * @param old
		 *            the sense entry to be replicated
		 * @return the new sense entry object
		 * @throws NullPointerException
		 *             if either argument is <code>null</code>
		 * @since JWI 2.2.0
		 */
		protected ISenseEntry makeSenseEntry(ISenseKey key, ISenseEntry old){
			return new SenseEntry(key, old.getOffset(), old.getSenseNumber(), old.getTagCount());
		}
	}

	/**
	 * Object that holds all the dictionary data loaded from the Wordnet files.
	 * 
	 * @author Mark A. Finlayson
	 * @since JWI 2.2.0
	 */
	public static class DictionaryData {
		
		// data maps
		protected Map<POS, Map<IIndexWordID, IIndexWord>> idxWords;
		protected Map<POS, Map<ISynsetID, ISynset>> synsets;
		protected Map<POS, Map<IExceptionEntryID, IExceptionEntry>> exceptions;
		protected Map<ISenseKey, IWord> words;
		protected Map<ISenseKey, ISenseEntry> senses;
		
		/** 
		 * Constructs an empty dictionary data object.
		 *
		 * @since JWI 2.2.0
		 */
		public DictionaryData(){
			idxWords = makePOSMap();
			synsets = makePOSMap();
			exceptions = makePOSMap();
			words = makeMap(208000, null);
			senses = makeMap(208000, null);
		}
	
		/**
		 * This method is used when constructing the dictionary data object.
		 * Constructs an map with an empty sub-map for every part of speech.
		 * Subclasses may override to change map character
		 * 
		 * @param <K>
		 *            the type of the keys for the sub-maps
		 * @param <V>
		 *            the type of the values for the sub-maps
		 * @return a map with an empty sub-map for every part of speech.
		 * @since JWI 2.2.0
		 */
		protected <K,V> Map<POS, Map<K,V>> makePOSMap(){
			Map<POS, Map<K,V>> result = new HashMap<POS, Map<K,V>>(POS.values().length);
			for(POS pos : POS.values()) result.put(pos, this.<K,V>makeMap(4096, null));
			return result;
		}
	
		/**
		 * Creates the actual sub-maps for the part-of-speech maps. This
		 * particular implementation creates <code>LinkedHashMap</code> maps.
		 * 
		 * @param <K>
		 *            the type of the keys for the sub-maps
		 * @param <V>
		 *            the type of the values for the sub-maps
		 * @param initialSize
		 *            the initial size of the map; this parameter is ignored if
		 *            the <code>contents</code> parameter is non-
		 *            <code>null</code>.
		 * @param contents
		 *            the items to be inserted in the map, may be
		 *            <code>null</code>. If non-<code>null</code>, the
		 *            <code>initialSize</code> parameter is ignored
		 * @return an empty map with either the specified initial size, or
		 *         contained the specified contents
		 * @throws IllegalArgumentException
		 *             if the initial size is invalid (less than 1) and the
		 *             specified contents are <code>null</code>
		 * @since JWI 2.2.0
		 */
		protected <K,V> Map<K,V> makeMap(int initialSize, Map<K,V> contents){
			return (contents == null) ? new LinkedHashMap<K,V>(initialSize) : new LinkedHashMap<K, V>(contents);
		}
	
		/**
		 * Compacts this dictionary data object by resizing the internal maps,
		 * and removing redundant objects where possible.
		 * 
		 * @since JWI 2.2.0
		 */
		public void compact(){
			compactSize();
			compactObjects();
		}
	
		/**
		 * Resizes the internal data maps to be the exact size to contain their
		 * data.
		 * 
		 * @since JWI 2.2.0
		 */
		public void compactSize(){
			compactPOSMap(idxWords);
			compactPOSMap(synsets);
			compactPOSMap(exceptions);
			words = compactMap(words);
			senses = compactMap(senses);
		}
	
		/**
		 * Compacts a part-of-speech map
		 * 
		 * @param map
		 *            the part-of-speech keyed map to be compacted
		 * @since JWI 2.2.0
		 */
		protected <K,V> void compactPOSMap(Map<POS, Map<K,V>> map){
			for(Entry<POS, Map<K,V>> entry : map.entrySet()){
				entry.setValue(compactMap(entry.getValue()));
			}
		}
	
		/**
		 * Compacts a regular map.
		 * 
		 * @param map
		 *            the map to be compacted, may not be <code>null</code>.
		 * @return the new, compacted map
		 * @throws NullPointerException if the specified map is <code>null</code>
		 * @since JWI 2.2.0
		 */
		protected <K,V> Map<K,V> compactMap(Map<K,V> map){
			if(map == null) throw new NullPointerException();
			return makeMap(-1, map);
		}
		
		/** 
		 * Replaces redundant objects where possible
		 *
		 * @since JWI 2.2.0
		 */
		public void compactObjects(){
			for(POS pos : POS.values()){
				for(Entry<ISynsetID, ISynset> entry : synsets.get(pos).entrySet())
					entry.setValue(makeSynset(entry.getValue()));
				for(Entry<IIndexWordID, IIndexWord> entry : idxWords.get(pos).entrySet())
					entry.setValue(makeIndexWord(entry.getValue()));
			}
		}
	
		/**
		 * Creates a new synset object that replaces all the old internal
		 * <code>ISynsetID</code> objects with those from the denoted synsets,
		 * thus throwing away redundant sysnet ids.
		 * 
		 * @param old
		 *            the synset to be replicated
		 * @return the new synset, a copy of the first
		 * @throws NullPointerException
		 *             if the specified synset is <code>null</code>
		 * @since JWI 2.2.0
		 */
		protected ISynset makeSynset(ISynset old){
			
			Map<IPointer, List<ISynsetID>> oldIDs = old.getRelatedMap();
			Map<IPointer, List<ISynsetID>> newIDs = new HashMap<IPointer, List<ISynsetID>>(oldIDs.size());
			
			List<ISynsetID> newList;
			ISynset otherSynset;
			for(Entry<IPointer, List<ISynsetID>> entry : oldIDs.entrySet()){
				newList = new ArrayList<ISynsetID>(entry.getValue().size());
				for(ISynsetID otherID : entry.getValue()){
					otherSynset = synsets.get(otherID.getPOS()).get(otherID);
					newList.add(otherSynset.getID());
				}
				newIDs.put(entry.getKey(), newList);
			}
			
			// words
			List<IWord> oldWords = old.getWords();
			List<IWordBuilder> newWords = new ArrayList<IWordBuilder>(oldWords.size());
			for(IWord oldWord : old.getWords()) 
				newWords.add(new WordBuilder(old, oldWord));
			
			return new Synset(old.getID(), old.getLexicalFile(), old.isAdjectiveSatellite(), old.isAdjectiveHead(), old.getGloss(), newWords, newIDs);
		}
	
		/**
		 * Creates a new word object that replaces all the old internal
		 * <code>IWordID</code> objects with those from the denoted words, thus
		 * throwing away redundant word ids.
		 * 
		 * @param newSynset
		 *            the synset for which the word is being made
		 * @param oldSynset
		 *            the old synset from which the word should be made
		 * @param old
		 *            the word to be replicated
		 * @return the new synset, a copy of the first
		 * @throws NullPointerException
		 *             if any argument is <code>null</code>
		 * @since JWI 2.2.0
		 */
		protected IWord makeWord(ISynset newSynset, ISynset oldSynset, IWord old){
			
			Map<IPointer, List<IWordID>> oldPtrs = old.getRelatedMap();
			Map<IPointer, List<IWordID>> newPtrs = new HashMap<IPointer, List<IWordID>>(oldPtrs.size());
			List<IWordID> newList;
			ISynset otherSynset;
			for(Entry<IPointer, List<IWordID>> entry : oldPtrs.entrySet()){
				newList = new ArrayList<IWordID>(entry.getValue().size());
				for(IWordID otherID : entry.getValue()){
					otherSynset = synsets.get(otherID.getPOS()).get(otherID.getSynsetID());
					newList.add(otherSynset.getWord(otherID.getWordNumber()).getID());
				}
				newPtrs.put(entry.getKey(), newList);
			}
			
			IWord word = new Word(newSynset, old.getID(), old.getLexicalID(), old.getAdjectiveMarker(), old.getVerbFrames(), newPtrs);
			ISenseKey key = word.getSenseKey();
			if(key.needsHeadSet()){
				ISenseKey oldKey = old.getSenseKey();
				key.setHead(oldKey.getHeadWord(), oldKey.getHeadID());
			}
			return word;
		}
	
		/**
		 * Creates a new index word that replicates the specified index word.
		 * The new index word replaces it's internal synset ids with synset ids
		 * from the denoted synsets, thus removing redundant ids.
		 * 
		 * @param old
		 *            the index word to be replicated
		 * @return the new index word object
		 * @throws NullPointerException
		 *             if the specified index word is <code>null</code>
		 * @since JWI 2.2.0
		 */
		protected IIndexWord makeIndexWord(IIndexWord old){
			List<IWordID> oldIDs = old.getWordIDs();
			IWordID[] newIDs = new IWordID[oldIDs.size()];
			IWordID oldID;
			ISynset synset;
			for(int i = 0; i < oldIDs.size(); i++){
				oldID = oldIDs.get(i);
				synset = synsets.get(oldID.getPOS()).get(oldID.getSynsetID());
				for(IWord newWord : synset.getWords()){
					if(!newWord.getID().equals(oldID)) continue;
					newIDs[i] = newWord.getID();
					break;
				}
				if(newIDs[i] == null) throw new IllegalStateException();
			}
			return new IndexWord(old.getID(), old.getTagSenseCount(), newIDs);
		}
		
		/** 
		 * A utility class that allows us to build word objects
		 *
		 * @author Mark A. Finlayson
		 * @version 2.2.3
		 * @since JWI 2.2.0
		 */
		public class WordBuilder implements IWordBuilder {
			
			// final instance fields
			private final ISynset oldSynset;
			private final IWord oldWord;

			/**
			 * Constructs a new word builder object out of the specified old
			 * synset and word.
			 * 
			 * @param oldSynset
			 *            the old synset that backs this builder; may not be
			 *            <code>null</code>
			 * @param oldWord
			 *            the old word that backs this builder; may not be
			 *            <code>null</code>
			 * @throws NullPointerException
			 *             if either argument is <code>null</code>
			 * @since 2.2.0
			 */
			public WordBuilder(ISynset oldSynset, IWord oldWord){
				if(oldSynset == null)
					throw new NullPointerException();
				if(oldWord == null)
					throw new NullPointerException();
				this.oldSynset = oldSynset;
				this.oldWord = oldWord;
			}

			/* 
			 * (non-Javadoc) 
			 *
			 * @see edu.mit.jwi.item.Synset.IWordBuilder#toWord(edu.mit.jwi.item.ISynset)
			 */
			public IWord toWord(ISynset synset) {
				return makeWord(synset, oldSynset, oldWord);
			}

			/* 
			 * (non-Javadoc) 
			 *
			 * @see edu.mit.jwi.item.Synset.IWordBuilder#addVerbFrame(edu.mit.jwi.item.IVerbFrame)
			 */
			public void addVerbFrame(IVerbFrame frame) {
				throw new UnsupportedOperationException();
			}

			/* 
			 * (non-Javadoc) 
			 *
			 * @see edu.mit.jwi.item.Synset.IWordBuilder#addRelatedWord(edu.mit.jwi.item.IPointer, edu.mit.jwi.item.IWordID)
			 */
			public void addRelatedWord(IPointer type, IWordID id) {
				throw new UnsupportedOperationException();
			}
			
		}
	}

}
