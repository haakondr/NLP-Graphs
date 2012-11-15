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

/**
 * Concrete implementation of a wordnet file data source. This particular
 * implementation is for files on disk, and directly accesses the appropriate
 * byte offset in the file to find requested lines. It is appropriate for
 * Wordnet data files.
 * 
 * @param <T>
 *            the type of object represented in this data resource
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public class DirectAccessWordnetFile<T> extends WordnetFile<T> {

	/**
	 * Constructs a new direct access wordnet file, on the specified file with
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
	public DirectAccessWordnetFile(File file, IContentType<T> contentType) {
		super(file, contentType);
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IDataSource#getLine(java.lang.String)
	 */
	public String getLine(String key) {
		ByteBuffer buffer = getBuffer();
		synchronized(buffer){
			try{
				int byteOffset = Integer.parseInt(key);
				if(buffer.limit() <= byteOffset) 
					return null; 
				buffer.position(byteOffset);
				String line = getLine(buffer);
				return line.startsWith(key) ? line : null;
			} catch(NumberFormatException e){
				return null;
			}
		}
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.WordnetFile#makeIterator(java.nio.ByteBuffer, java.lang.String)
	 */
	public LineIterator makeIterator(ByteBuffer buffer, String key){
		return new DirectLineIterator(buffer, key);
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
	public class DirectLineIterator extends LineIterator {

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
		public DirectLineIterator(ByteBuffer buffer, String key) {
			super(buffer, key);
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see edu.mit.jwi.data.WordnetFile.LineIterator#findFirstLine(java.lang.String)
		 */
		protected void findFirstLine(String key){
			synchronized(itrBuffer){
				try{
					Integer byteOffset = Integer.parseInt(key);
					if(itrBuffer.limit() <= byteOffset) 
						return; 
					itrBuffer.position(byteOffset);
					next = getLine(itrBuffer);
				} catch(NumberFormatException e){
					// Ignore
				}
			}
		}
	}
}