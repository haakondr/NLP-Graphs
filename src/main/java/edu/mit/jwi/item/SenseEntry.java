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


/**
 * Concrete implementation of the <code>ISenseEntry</code> interface.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public class SenseEntry implements ISenseEntry {
	
	// final instance fields
	private final int offset;
	private final int num;
	private final int count;
	private final ISenseKey key;
	
	/**
	 * Constructs a new sense entry object.
	 * 
	 * @param key
	 *            the sense key of the entry
	 * @param offset
	 *            the synset offset of the entry
	 * @param num
	 *            the sense number of the entry
	 * @param count
	 *            the tag count of the entry
	 * @since JWI 2.1.0
	 */
	public SenseEntry(ISenseKey key, int offset, int num, int count){
		if(key == null)
			throw new NullPointerException();
		Synset.checkOffset(offset);
		
		this.key = key;
		this.offset = offset;
		this.num = num;
		this.count = count;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.ISenseEntry#getOffset()
	 */
	public int getOffset() {
		return offset;
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IHasPOS#getPOS()
	 */
	public POS getPOS() {
		return key.getPOS();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.ISenseEntry#getSenseNumber()
	 */
	public int getSenseNumber() {
		return num;
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.ISenseEntry#getSenseKey()
	 */
	public ISenseKey getSenseKey() {
		return key;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.ISenseEntry#getTagCount()
	 */
	public int getTagCount() {
		return count;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + count;
		result = prime * result + key.hashCode();
		result = prime * result + num;
		result = prime * result + offset;
		return result;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ISenseEntry))
			return false;
		final ISenseEntry other = (ISenseEntry) obj;
		if (count != other.getTagCount())
			return false;
		if (num != other.getSenseNumber())
			return false;
		if (offset != other.getOffset())
			return false;
		if(!key.equals(other.getSenseKey()))
			return false;
		return true;
	}

}
