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

import java.util.regex.Pattern;

import edu.mit.jwi.data.parse.ILineParser.MisformattedLineException;

/**
 * <p>
 * A comparator that captures the ordering of lines in Wordnet exception files
 * (e.g., <code>exc.adv</code> or <code>adv.exc</code> files). These files are
 * ordered alphabetically.
 * </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * </p>
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class ExceptionLineComparator implements ILineComparator {

	// singleton instance
	private static ExceptionLineComparator instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.0.0
	 */
	public static ExceptionLineComparator getInstance() {
		if (instance == null) 
			instance = new ExceptionLineComparator();
		return instance;
	}
	
	// static fields
	private final static Pattern spacePattern = Pattern.compile(" ");

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.0.0
	 */
	protected ExceptionLineComparator() {}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(String line1, String line2) {

		String[] words1 = spacePattern.split(line1);
		String[] words2 = spacePattern.split(line2);

		if (words1.length < 1) 
			throw new MisformattedLineException(line1);
		if (words2.length < 1) 
			throw new MisformattedLineException(line2);

		return words1[0].compareTo(words2[0]);
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