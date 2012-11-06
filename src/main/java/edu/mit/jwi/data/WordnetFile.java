/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.mit.jwi.data.compare.ICommentDetector;
import edu.mit.jwi.item.IVersion;
import edu.mit.jwi.item.Version;

/**
 * <p>
 * Abstract superclass of wordnet data file objects. Provides all the
 * infrastructure required to access the files, except for the construction of
 * iterators and the actual implementation of the {@link #getLine(String)}
 * method.
 * </p>
 * <p>
 * While this object is implemented to provider load/unload capabilities (i.e.,
 * it allows the whole wordnet file to be loaded into memory, rather than read
 * from disk), this does not provide much of a performance boost. In tests, the
 * time to parsing a line of data into a data object dominates the time required
 * to read the data from disk (for a reasonable modern harddrive).
 * </p>
 * 
 * @param <T>
 *            the type of the objects represented in this file
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public abstract class WordnetFile<T> implements ILoadableDataSource<T> {
	
	// fields set on construction
	private final String name;
	private final IContentType<T> type;
	private final ICommentDetector detector;
	private final File file;

	// loading locks and status flag
	// the flag is marked transient to avoid different values in different threads 
	private transient boolean isLoaded = false;
	private final Lock lifecycleLock = new ReentrantLock();
	private final Lock loadingLock = new ReentrantLock();
	
	// fields generated dynamically on demand
	private FileChannel channel;
	private ByteBuffer buffer;
	private IVersion version;

	/**
	 * Constructs an instance of this class backed by the specified java
	 * {@code File} object, with the specified content type. No effort is made
	 * to ensure that the data in the specified file is actually formatted in
	 * the proper manner for the line parser associated with the content type's
	 * data type. If these are mismatched, this will result in
	 * {@code MisformattedLineExceptions} in later calls.
	 * 
	 * @param file
	 *            the file which backs this wordnet file; may not be
	 *            <code>null</code>
	 * @param contentType
	 *            the content type for this file; may not be <code>null</code>
	 * @throws NullPointerException
	 *             if the specified file or content type is <code>null</code>
	 * @since JWI 1.0
	 */
	public WordnetFile(File file, IContentType<T> contentType) {
		if(contentType == null)
			throw new NullPointerException();
		this.name = file.getName();
		this.file = file;
		this.type = contentType;
		this.detector = type.getLineComparator().getCommentDetector();
	}
		
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IDataSource#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Returns the file which backs this object.
	 * 
	 * @return the file which backs this object, should never return
	 *         <code>null</code>
	 * @since JWI 2.2.0
	 */
	public File getFile(){
		return file;
	}

	/**
	 * Returns the buffer which backs this object.
	 * 
	 * @return the buffer which backs this object
	 * @throws ObjectClosedException
	 *             if the object is closed
	 * @since JWI 2.2.0
	 */
	public ByteBuffer getBuffer(){
		if(!isOpen()) 
			throw new ObjectClosedException();
		return buffer;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IDataSource#getContentType()
	 */
	public IContentType<T> getContentType() {
		return type;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IHasLifecycle#open()
	 */
	public boolean open() throws IOException { 
		try {
			lifecycleLock.lock();
			if(isOpen()) 
				return true;
			RandomAccessFile raFile = new RandomAccessFile(file, "r");
			channel = raFile.getChannel();
			buffer = channel.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
			return true;
		} finally {
			lifecycleLock.unlock();
		}
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IHasLifecycle#isOpen()
	 */
	public boolean isOpen(){
		try{
			lifecycleLock.lock();
			return buffer != null;
		} finally {
			lifecycleLock.unlock();
		}
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IClosable#close()
	 */
	public void close(){
		try{
			lifecycleLock.lock();
			version = null;
			buffer = null;
			isLoaded = false;
			if(channel != null){
				try {
					channel.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			channel = null;
		} finally {
			lifecycleLock.unlock();
		}
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#isLoaded()
	 */
	public boolean isLoaded() {
		return isLoaded;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#load()
	 */
	public void load() {
		load(false);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#load(boolean)
	 */
	public void load(boolean block) {
		try {
			loadingLock.lock();
			int len = (int)file.length();
			ByteBuffer buf = buffer.asReadOnlyBuffer();
			buf.clear();
			byte[] data = new byte[len];
			buf.get(data, 0, len);
			
			try{
				lifecycleLock.lock();
				if(channel != null){
					try {
						channel.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
					channel = null;
				}
				if(buffer != null){
					buffer = ByteBuffer.wrap(data);
					isLoaded = true;
				}
			} finally {
				lifecycleLock.unlock();
			}
			
		} finally {
			loadingLock.unlock();
		}
	}

	/**
	 * Returns the wordnet version associated with this object, or null if the
	 * version cannot be determined.
	 * 
	 * @throws ObjectClosedException
	 *             if the object is closed when this method is called
	 * @return the wordnet version associated with this object, or null if the
	 *         version cannot be determined
	 * @see edu.mit.jwi.item.IHasVersion#getVersion()
	 */
	public IVersion getVersion() {
		if(!isOpen()) 
			throw new ObjectClosedException();
		if(version == null){
			version = Version.extractVersion(type, buffer.asReadOnlyBuffer());
			if(version == null) 
				version = IVersion.NO_VERSION;
		}
		return (version == IVersion.NO_VERSION) ? null : version;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.lang.Iterable#iterator()
	 */
	public LineIterator iterator() {
		if(!isOpen())
			throw new ObjectClosedException();
		return makeIterator(getBuffer(), null);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IDataSource#iterator(java.lang.String)
	 */
	public LineIterator iterator(String key) {
		if(!isOpen())
			throw new ObjectClosedException();
		return makeIterator(getBuffer(), key);
	}

	/**
	 * Constructs an iterator that can be used to iterate over the specified
	 * {@link ByteBuffer}, starting from the specified key.
	 * 
	 * @param buffer
	 *            the buffer over which the iterator will iterate, should not be
	 *            <code>null</code>
	 * @param key
	 *            the key at which the iterator should begin, should not be
	 *            <code>null</code>
	 * @return an iterator that can be used to iterate over the lines of the
	 *         {@link ByteBuffer}
	 * @since JWI 2.2.0
	 */
	public abstract LineIterator makeIterator(ByteBuffer buffer, String key);

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + type.hashCode();
		result = PRIME * result + file.hashCode();
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (this == obj) 
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass()) 
			return false;
		final WordnetFile<?> other = (WordnetFile<?>) obj;
		if (!type.equals(other.type)) 
			return false;
		if (!file.equals(other.file)) 
			return false;
		return true;
	}

	/**
	 * Returns the String from the current position up to, but not including,
	 * the next newline. The buffer's position is set to either directly after
	 * the next newline, or the end of the buffer. If the buffer is at its
	 * limit, the method returns null. If the buffer's position is directly
	 * before a valid newline marker (either \n, \r, or \r\n), then the method
	 * returns an empty string.
	 * 
	 * @param buf
	 *            the buffer from which the line should be extracted
	 * @throws NullPointerException
	 *             if the specified buffer is <code>null</code>
	 * @return the remainder of line in the specified buffer, starting from the
	 *         buffer's current position
	 * @since JWI 2.1.0
	 */
	public static String getLine(ByteBuffer buf){
		StringBuilder input = new StringBuilder();
		char c;
		boolean eol = false;
		int limit = buf.limit();
		
		// we are at end of buffer, return null
		if(buf.position() == limit)
			return null;
		
		while(!eol && buf.position() < limit) {
			c = (char)buf.get();
		    switch (c) {
			    case '\n':
					eol = true;
					break;
			    case '\r':
					eol = true;
					int cur = buf.position();
					c = (char)buf.get();
					if(c != '\n')
						buf.position(cur);
					break;
			    default:
					input.append(c);
					break;
		    }
		}
	
		return input.toString();
	}
	
	/**
	 * Rewinds the specified buffer to the beginning of the current line.
	 * 
	 * @param buf
	 *            the buffer to be rewound; may not be <code>null</code>
	 * @throws NullPointerException
	 *             if the specified buffer is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public static void rewindToLineStart(ByteBuffer buf){
		int i = buf.position();
		
		// check if the buffer is set in the middle of two-char
		// newline marker; if so, back up before it begins
		if(buf.get(i-1) == '\r' && buf.get(i) == '\n')
			i--;
		
		// start looking at the character just before
		// the one at which the buffer is set
		if(i > 0)
			i--;
		
		// walk backwards until we find a newline;
		// if we find a carriage return (CR) or a 
		// linefeed (LF), this must be the end of the 
		// previous line (either \n, \r, or \r\n)
		char c;
		for(; i > 0; i--){
			c = (char)buf.get(i);
			if(c == '\n' || c == '\r'){
				i++;
				break;
			}
		}
		
		// set the buffer to the beginning of the line
		buf.position(i);
	}

	/**
	 * Used to iterate over lines in a file. It is a look-ahead iterator. This
	 * iterator does not support the remove method; if that method is called, it
	 * throws an {@link UnsupportedOperationException}.
	 * 
	 * @author Mark A. Finlayson
	 * @version 2.2.3
	 * @since JWI 1.0
	 */
	protected abstract class LineIterator implements Iterator<String> {
	
		// fields set on construction
		protected final ByteBuffer parentBuffer;
		protected ByteBuffer itrBuffer;
		protected String next;
	
		/**
		 * Constructs a new line iterator over this buffer, starting at the
		 * specified key.
		 * 
		 * @param buffer
		 *            the buffer over which the iterator should iterator; may
		 *            not be <code>null</code>
		 * @param key
		 *            the key of the line to start at; may be <code>null</code>
		 * @throws NullPointerException
		 *             if the specified buffer is <code>null</code>
		 * @since JWI 1.0
		 */
		public LineIterator(ByteBuffer buffer, String key) {
			parentBuffer = buffer;
			itrBuffer = buffer.asReadOnlyBuffer();
			itrBuffer.clear();
			key = (key == null) ? null : key.trim();
			if(key == null || key.length() == 0) {
				advance();
			} else {
				findFirstLine(key);
			}
		}
		
		/**
		 * Returns the line currently stored as the 'next' line, if any. Is a
		 * pure getter; does not increment the iterator.
		 * 
		 * @return the next line that will be parsed and returned by this
		 *         iterator, or <code>null</code> if none
		 * @since JWI 2.2.0
		 */
		public String getNextLine(){
			return next;
		}
	
		/**
		 * Advances the iterator the first line the iterator should return,
		 * based on the specified key. If the key is not found in the file, it
		 * will advance the iterator past all lines.
		 * 
		 * @param key
		 *            the key indexed the first line to be returned by the
		 *            iterator
		 * @since JWI 1.0
		 */
		protected abstract void findFirstLine(String key);

		/* 
		 * (non-Javadoc) 
		 *
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext() {
			return next != null;
		}
	
		/**
		 * Skips over comment lines to find the next line that would be returned
		 * by the iterator in a call to {@link #next()}.
		 * 
		 * @since JWI 1.0
		 */
		protected void advance() {
			next = null;
			
			// check for buffer swap
			if(parentBuffer != buffer){
				int pos = itrBuffer.position();
				ByteBuffer newBuf = buffer.asReadOnlyBuffer();
				newBuf.clear();
				newBuf.position(pos);
				itrBuffer = newBuf;
			}
			
			String line;
			do {
				line = getLine(itrBuffer);
			} while (line != null && isComment(line));
			next = line;
		}

		/**
		 * Returns <code>true</code> if the specified line is a comment;
		 * <code>false</code> otherwise
		 * 
		 * @param line
		 *            the line to be tested
		 * @return <code>true</code> if the specified line is a comment;
		 *         <code>false</code> otherwise
		 * @since JWI 1.0
		 */
		protected boolean isComment(String line){
			if(detector == null)
				return false;
			return detector.isCommentLine(line);
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#next()
		 */
		public String next() {
			if(next == null) 
				throw new NoSuchElementException();
			String result = next;
			advance();
			return result;
		}
	
		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Iterator#remove()
		 */
		public final void remove() {
			throw new UnsupportedOperationException();
		}
	}

}
