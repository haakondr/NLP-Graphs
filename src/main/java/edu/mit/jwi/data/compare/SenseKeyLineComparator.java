/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data.compare;

/**
 * <p>
 * A comparator that captures the ordering of lines in sense index files (e.g.,
 * the <code>sense.index</code> file). This files are ordered alphabetically by
 * sense key.
 * </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * <p>
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public class SenseKeyLineComparator implements ILineComparator {
	
	// singleton instance
	private static SenseKeyLineComparator instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.1.0
	 */
	public static SenseKeyLineComparator getInstance() {
		if (instance == null) 
			instance = new SenseKeyLineComparator();
		return instance;
	}

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.1.0
	 */
	protected SenseKeyLineComparator() {}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(String line1, String line2) {
		// get sense keys
		int i1 = line1.indexOf(' ');
		int i2 = line2.indexOf(' ');
		line1 = (i1 == -1) ? line1 : line1.substring(0, i1);
		line2 = (i2 == -1) ? line2 : line2.substring(0, i2);
		return line1.compareTo(line2);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.compare.ILineComparator#getCommentDetector()
	 */
	public ICommentDetector getCommentDetector() {
		return null;
	}
}
