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
import java.nio.ByteBuffer;
import java.util.Comparator;

/**
 * Concrete implementation of a wordnet file data source. This particular
 * implementation is for files on disk, and uses a binary search algorithm to
 * find requested lines. It is appropriate for alphabetically-ordered Wordnet
 * files.
 * 
 * @param <T>
 *            the type of object represented in this data resource
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public class BinarySearchWordnetFile<T> extends WordnetFile<T> {
	
	// the comparator
	protected final Comparator<String> fComparator;

	/**
	 * Constructs a new binary search wordnet file, on the specified file with
	 * the specified content type.
	 * 
	 * @param file
	 *            the file which backs this wordnet file; may not be
	 *            <code>null</code>
	 * @param contentType
	 *            the content type for this file; may not be <code>null</code>
	 * @throws {@link NullPointerException} if either the file or content type
	 *         is <code>null</code>
	 * @since JWI 2.0.0
	 */
	public BinarySearchWordnetFile(File file, IContentType<T> contentType) {
		super(file, contentType);
		fComparator = getContentType().getLineComparator();
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IDataSource#getLine(java.lang.String)
	 */
	public String getLine(String key) {
		
		ByteBuffer buffer = getBuffer();
		
		synchronized(buffer) {
			int start = 0;
			int midpoint = -1;
			int stop = buffer.limit();
			int cmp;
			String line;
			while(stop - start > 1) {

				// find the middle of the buffer
				midpoint = (start + stop) / 2;
				buffer.position(midpoint);
				
				// back up to the beginning of the line
				rewindToLineStart(buffer);
				line = getLine(buffer);
				
				// if we get a null, we've reached the end of the file
				cmp = (line == null) ? 1 : fComparator.compare(line, key);
				
				// found our line
				if(cmp == 0)
					return line;
				
				if(cmp > 0){
					// too far forward
					stop = midpoint;
				} else {
					// too far back
					start = midpoint;
				}
			}
		}
		return null;
	}
	

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.WordnetFile#makeIterator(java.nio.ByteBuffer, java.lang.String)
	 */
	public LineIterator makeIterator(ByteBuffer buffer, String key) {
		return new BinarySearchLineIterator(buffer, key);
	}

	/**
	 * Used to iterate over lines in a file. It is a look-ahead iterator. Does
	 * not support the {@link #remove()} method; if that method is called, it
	 * will throw an {@link UnsupportedOperationException}.
	 * 
	 * @author Mark A. Finlayson
	 * @version 2.2.3
	 * @since JWI 2.0.0
	 */
	public class BinarySearchLineIterator extends LineIterator {

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
		 * @since JWI 2.0.0
		 */
		public BinarySearchLineIterator(ByteBuffer buffer, String key) {
			super(buffer, key);
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.data.WordnetFile.LineIterator#findFirstLine(java.lang.String)
		 */
		protected void findFirstLine(String key) {
			synchronized (itrBuffer) {
				int lastOffset = -1;
				int start = 0;
				int stop = itrBuffer.limit();
				int offset, midpoint = -1;
				int compare;
				String line;
				while (start + 1 < stop) {
					midpoint = (start + stop) / 2;
					itrBuffer.position(midpoint);
					line = getLine(itrBuffer);
					offset = itrBuffer.position();
					line = getLine(itrBuffer);
					
					// Fix for Bug009: If the line is null, we've reached
					// the end of the file, so just advance to the first line
					if(line == null){
						itrBuffer.position(itrBuffer.limit());
						return;
					}
					
					compare = fComparator.compare(line, key);
					// if the key matches exactly, we know we have found
					// the start of this pattern in the file
					if (compare == 0) {
						next = line;
						return;
					}
					else if (compare > 0) {
						stop = midpoint;
					}
					else {
						start = midpoint;
					}
					// if the key starts a line, remember it, because
					// it may be the first occurrence
					if (line.startsWith(key))
						lastOffset = offset;
				}

				// Getting here means that we didn't find an exact match
				// to the key, so we take the last line that started
				// with the pattern
				if (lastOffset > -1) {
					itrBuffer.position(lastOffset);
					next = getLine(itrBuffer);
					return;
				}

				// If we didn't have any lines that matched the pattern
				// then just advance to the first non-comment
				itrBuffer.position(itrBuffer.limit());
			}
		}

	}
}