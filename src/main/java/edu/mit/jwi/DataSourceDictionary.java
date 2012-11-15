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

import edu.mit.jwi.data.ContentType;
import edu.mit.jwi.data.IContentType;
import edu.mit.jwi.data.IDataProvider;
import edu.mit.jwi.data.IDataSource;
import edu.mit.jwi.data.parse.ILineParser;
import edu.mit.jwi.item.ExceptionEntry;
import edu.mit.jwi.item.ExceptionEntryID;
import edu.mit.jwi.item.IExceptionEntry;
import edu.mit.jwi.item.IExceptionEntryID;
import edu.mit.jwi.item.IExceptionEntryProxy;
import edu.mit.jwi.item.IHasPOS;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IIndexWordID;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISenseKey;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IVersion;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.IndexWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.Synset;
import edu.mit.jwi.item.SynsetID;

/**
 * Basic implementation of the {@code IDictionary} interface. A path to the
 * Wordnet dictionary files must be provided. If no {@code IDataProvider} is
 * specified, it uses the default implementation provided with the distribution.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.2.0
 */
public class DataSourceDictionary implements IDataSourceDictionary {
	
	private final IDataProvider provider;

	/**
	 * Constructs a dictionary with a caller-specified {@code IDataProvider}.
	 * 
	 * @throws NullPointerException
	 *             if the specified data provider is <code>null</code>
	 */
	public DataSourceDictionary(IDataProvider provider) {
		if(provider == null) throw new NullPointerException();
		this.provider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDataSourceDictionary#getDataProvider()
	 */
	public IDataProvider getDataProvider() {
		return provider;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.item.IHasVersion#getVersion()
	 */
	public IVersion getVersion() {
		checkOpen();
		return provider.getVersion();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#open()
	 */
	public boolean open() throws IOException {
		return provider.open();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#close()
	 */
	public void close() {
		provider.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#isOpen()
	 */
	public boolean isOpen() {
		return provider.isOpen();
	}

	/**
	 * An internal method for assuring compliance with the dictionary interface
	 * that says that methods will throw {@code ObjectClosedException}s if
	 * the dictionary has not yet been opened.
	 * 
	 * @throws ObjectClosedException
	 *             if the dictionary is closed.
	 */
	protected void checkOpen() {
		if(!isOpen()) throw new ObjectClosedException();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getIndexWord(java.lang.String,
	 *      edu.mit.jwi.item.POS)
	 */
	public IIndexWord getIndexWord(String lemma, POS pos) {
		checkOpen();
		return getIndexWord(new IndexWordID(lemma, pos));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getIndexWord(edu.mit.jwi.item.IIndexWordID)
	 */
	public IIndexWord getIndexWord(IIndexWordID id) {
		checkOpen();
		IContentType<IIndexWord> content = resolveIndexContentType(id.getPOS());
		IDataSource<?> file = provider.getSource(content);
		String line = file.getLine(id.getLemma());
		if (line == null) return null;
		return content.getDataType().getParser().parseLine(line);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getWord(edu.mit.jwi.item.IWordID)
	 */
	public IWord getWord(IWordID id) {
		checkOpen();
		
		ISynset synset = getSynset(id.getSynsetID());
		if(synset == null) return null;
		
		// One or the other of the WordID number or lemma may not exist,
		// depending on whence the word id came, so we have to check 
		// them before trying.
		if (id.getWordNumber() > 0) {
			return synset.getWords().get(id.getWordNumber() - 1);
		} else if (id.getLemma() != null) {
			for(IWord word : synset.getWords()) {
				if (word.getLemma().equalsIgnoreCase(id.getLemma())) return word;
			}
			return null;
		} else {
			throw new IllegalArgumentException("Not enough information in IWordID instance to retrieve word.");
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getWord(edu.mit.jwi.item.ISenseKey)
	 */
	public IWord getWord(ISenseKey key) {
		checkOpen();
		
		// no need to cache result from the following calls as this will have been
		// done in the call to getSynset()
		ISenseEntry entry = getSenseEntry(key);
		if(entry != null){
			ISynset synset = getSynset(new SynsetID(entry.getOffset(), entry.getPOS()));
			if(synset != null)
				for(IWord synonym : synset.getWords())
					if(synonym.getSenseKey().equals(key)) 
						return synonym;
		}
			
		IWord word = null;
		
		// sometimes the sense.index file doesn't have the sense key entry
		// so try an alternate method of retrieving words by sense keys
		// We have to search the synonyms of the words returned from the
		// index word search because some synsets have lemmas that differ only in case
		// e.g., {earth, Earth} or {south, South}, and so separate entries
		// are not found in the index file
		IIndexWord indexWord = getIndexWord(key.getLemma(), key.getPOS());
		if(indexWord != null){
			IWord possibleWord;
			for(IWordID wordID : indexWord.getWordIDs()){
				possibleWord = getWord(wordID);
				if(possibleWord != null)
					for(IWord synonym : possibleWord.getSynset().getWords()){
						if(synonym.getSenseKey().equals(key)){
							word = synonym;
							if(synonym.getLemma().equals(key.getLemma())) return synonym;
						}
					}
			}
		}

		return word;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getSenseEntry(edu.mit.jwi.item.ISenseKey)
	 */
	public ISenseEntry getSenseEntry(ISenseKey key) {
		checkOpen();
		IContentType<ISenseEntry> content = resolveSenseContentType();
		IDataSource<ISenseEntry> file = provider.getSource(content);
		String line = file.getLine(key.toString());
		if (line == null) return null;
		return content.getDataType().getParser().parseLine(line);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.wordnet.core.dict.IDictionary#getSynset(edu.mit.wordnet.core.data.ISynsetID)
	 */
	public ISynset getSynset(ISynsetID id) {
		checkOpen();
		IContentType<ISynset> content = resolveDataContentType(id.getPOS());
		IDataSource<ISynset> file = provider.getSource(content);
		String zeroFilledOffset = Synset.zeroFillOffset(id.getOffset());
		String line = file.getLine(zeroFilledOffset);
		if(line == null) 
			return null;
		ISynset result = content.getDataType().getParser().parseLine(line);
		if(result != null) 
			setHeadWord(result);
		return result;
	}
	
	/**
	 * This method sets the head word on the specified synset by searching in
	 * the dictionary to find the head of its cluster. We will assume the head
	 * is the first adjective head synset related by an '&' pointer (SIMILAR_TO)
	 * to this synset.
	 */
	protected void setHeadWord(ISynset synset){

		// head words are only needed for adjective satellites
		if(!synset.isAdjectiveSatellite()) return;
		
		// go find the head word
		ISynset headSynset;
		IWord headWord = null;
		for(ISynsetID simID : synset.getRelatedSynsets(Pointer.SIMILAR_TO)){
			headSynset = getSynset(simID);
			// assume first 'similar' adjective head is the right one
			if(headSynset.isAdjectiveHead()){
				headWord = headSynset.getWords().get(0);
				break;
			}
		}
		if(headWord == null) return;
		
		// set head word, if we found it
		String headLemma = headWord.getLemma();
		
		// version 1.6 of Wordnet adds the adjective marker symbol 
		// on the end of the head word lemma
		IVersion ver = getVersion();
		boolean isVer16 = (ver == null) ? false :  ver.getMajorVersion() == 1 && ver.getMinorVersion() == 6;
		if(isVer16 && headWord.getAdjectiveMarker() != null) headLemma += headWord.getAdjectiveMarker().getSymbol(); 
		
		// set the head word for each word
		for(IWord word : synset.getWords()){
			if(word.getSenseKey().needsHeadSet()) word.getSenseKey().setHead(headLemma, headWord.getLexicalID());
		}
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getExceptionEntry(java.lang.String,
	 *      edu.mit.jwi.item.POS)
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
		checkOpen();
		IContentType<IExceptionEntryProxy> content = resolveExceptionContentType(id.getPOS());
		IDataSource<IExceptionEntryProxy> file = provider.getSource(content);
		// fix for bug 010
		if(file == null) 
			return null; 
		String line = file.getLine(id.getSurfaceForm());
		if (line == null) 
			return null;
		IExceptionEntryProxy proxy = content.getDataType().getParser().parseLine(line);
		if(proxy == null)
			return null;
		return new ExceptionEntry(proxy, id.getPOS());
	}
	
	/** 
	 * This method retrieves the appropriate content type for exception entries,
	 * and is marked protected for ease of subclassing.
	 */
	protected IContentType<IIndexWord> resolveIndexContentType(POS pos){
		return ContentType.getIndexContentType(pos);
	}
	
	/** 
	 * This method retrieves the appropriate content type for exception entries,
	 * and is marked protected for ease of subclassing.
	 */
	protected IContentType<ISynset> resolveDataContentType(POS pos){
		return ContentType.getDataContentType(pos);
	}
	
	/** 
	 * This method retrieves the appropriate content type for exception entries,
	 * and is marked protected for ease of subclassing.
	 */
	protected IContentType<IExceptionEntryProxy> resolveExceptionContentType(POS pos){
		return ContentType.getExceptionContentType(pos);
	}
	
	/** 
	 * This method retrieves the appropriate content type for sense entries,
	 * and is marked protected for ease of subclassing.
	 */
	protected IContentType<ISenseEntry> resolveSenseContentType(){
		return ContentType.SENSE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getIndexWordIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<IIndexWord> getIndexWordIterator(POS pos) {
		checkOpen();
		return new IndexFileIterator(pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getSynsetIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<ISynset> getSynsetIterator(POS pos) {
		checkOpen();
		return new DataFileIterator(pos);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getExceptionEntryIterator(edu.mit.jwi.item.POS)
	 */
	public Iterator<IExceptionEntry> getExceptionEntryIterator(POS pos) {
		checkOpen();
		return new ExceptionFileIterator(pos);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.IDictionary#getSenseEntryIterator()
	 */
	public Iterator<ISenseEntry> getSenseEntryIterator() {
		checkOpen();
		return new SenseEntryFileIterator();
	}

	/**
	 * Abstract class used for iterating over line-based files.
	 */
	public abstract class FileIterator<T, N> implements Iterator<N>, IHasPOS {

		protected final IDataSource<T> fFile;
		protected final Iterator<String> iterator;
		protected final ILineParser<T> fParser;
		protected String currentLine;

		public FileIterator(IContentType<T> content) {
			this(content, null);
		}
		
		public FileIterator(IContentType<T> content, String startKey) {
			this.fFile = provider.getSource(content);
			this.fParser = content.getDataType().getParser();
			this.iterator = fFile.iterator(startKey);
		}
		
		/** 
		 * Returns the current line.
		 *
		 * @return the current line
		 * @since JWI 2.2.0
		 */
		public String getCurrentLine(){
			return currentLine;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.mit.wordnet.data.IHasPartOfSpeech#getPartOfSpeech()
		 */
		public POS getPOS() {
			return fFile.getContentType().getPOS();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return iterator.hasNext();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		public N next() {
			currentLine = iterator.next();
			return parseLine(currentLine);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public void remove() {
			iterator.remove();
		}

		/** Parses the line using a parser provided at construction time */
		public abstract N parseLine(String line);
	}
	
	/** 
	 * A file iterator where the data type returned by the iterator is the same
	 * as that returned by the backing data source. 
	 *
	 * @author Mark A. Finlayson
	 * @since JWI 2.1.5
	 */
	public abstract class FileIterator2<T> extends FileIterator<T, T> {

		/** 
		 * Constructs a new file iterator with the specified content type.
		 * 
		 * @since JWI 2.1.5
		 */
		public FileIterator2(IContentType<T> content) {
			super(content);
		}
		
		/** 
		 * Constructs a new file iterator with the specified content type and start key.
		 *
		 * @since JWI 2.1.5
		 */
		public FileIterator2(IContentType<T> content, String startKey) {
			super(content, startKey);
		}
		
	}

	/**
	 * Iterates over index files.
	 */
	public class IndexFileIterator extends FileIterator2<IIndexWord> {

		public IndexFileIterator(POS pos) {
			this(pos, "");
		}

		public IndexFileIterator(POS pos, String pattern) {
			super(resolveIndexContentType(pos), pattern);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.mit.wordnet.core.base.dict.Dictionary.FileIterator#parseLine(java.lang.String)
		 */
		public IIndexWord parseLine(String line) {
			return fParser.parseLine(line);
		}

	}

	/**
	 * Iterates over the sense file.
	 */
	public class SenseEntryFileIterator extends FileIterator2<ISenseEntry> {

		public SenseEntryFileIterator() {
			super(resolveSenseContentType());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.mit.wordnet.core.base.dict.Dictionary.FileIterator#parseLine(java.lang.String)
		 */
		public ISenseEntry parseLine(String line) {
			return fParser.parseLine(line);
		}

	}

	/**
	 * Iterates over data files.
	 */
	public class DataFileIterator extends FileIterator2<ISynset> {

		public DataFileIterator(POS pos) {
			super(resolveDataContentType(pos));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.mit.wordnet.core.base.dict.Dictionary.FileIterator#parseLine(java.lang.String)
		 */
		public ISynset parseLine(String line) {
			if(getPOS() == POS.ADJECTIVE){
				ISynset synset = fParser.parseLine(line);
				setHeadWord(synset);
				return synset;
			} else {
				return fParser.parseLine(line);
			}
		}

	}

	/**
	 * Iterates over exception files.
	 */
	public class ExceptionFileIterator extends FileIterator<IExceptionEntryProxy, IExceptionEntry> {

		public ExceptionFileIterator(POS pos) {
			super(resolveExceptionContentType(pos));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see edu.mit.wordnet.dict.Dictionary.FileIterator#parseLine(java.lang.String)
		 */
		public IExceptionEntry parseLine(String line) {
			IExceptionEntryProxy proxy = fParser.parseLine(line);
			return (proxy == null) ? null : new ExceptionEntry(proxy, getPOS());
		}
	}
}
