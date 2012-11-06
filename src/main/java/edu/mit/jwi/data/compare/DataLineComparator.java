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
 * A line comparator that captures the ordering of lines in Wordnet data files
 * (e.g., <code>data.adv</code> or <code>adv.dat</code> files). These files are
 * ordered by offset, which is an eight-digit zero-filled decimal number that is
 * assumed to start the line.
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
public class DataLineComparator implements ILineComparator {

	// singleton instance
	private static DataLineComparator instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.0.0
	 */
	public static DataLineComparator getInstance() {
		if (instance == null) 
			instance = new DataLineComparator(CommentComparator.getInstance());
		return instance;
	}

	// instance fields
	private final CommentComparator detector;

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @param detector
	 *            the comment detector for this line comparator, or
	 *            <code>null</code> if there is none
	 * @throws NullPointerException
	 *             if the specified comment comparator is <code>null</code>
	 * @since JWI 2.0.0
	 */
	protected DataLineComparator(CommentComparator detector) {
		if(detector == null)
			throw new NullPointerException();
		this.detector = detector;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(String s1, String s2) {

		boolean c1 = detector.isCommentLine(s1);
		boolean c2 = detector.isCommentLine(s2);

		if (c1 & c2) {
			// both lines are comments, defer to comment comparator
			return detector.compare(s1, s2);
		}
		else if (c1 & !c2) {
			// first line is a comment, should come before the other
			return -1;
		}
		else if (!c1 & c2) {
			// second line is a comment, should come before the other
			return 1;
		}

		// Neither strings are comments, so extract the offset from the
		// beginnings of both and compare them as two ints.
		int i1 = s1.indexOf(' ');
		int i2 = s2.indexOf(' ');
		
		if (i1 == -1) 
			i1 = s1.length();
		if (i2 == -1) 
			i2 = s2.length();

		String sub1 = s1.substring(0, i1);
		String sub2 = s2.substring(0, i2);

		int l1 = Integer.parseInt(sub1);
		int l2 = Integer.parseInt(sub2);
		
		if (l1 < l2) 
			return -1;
		else if (l1 > l2) 
			return 1;
		return 0;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.compare.ILineComparator#getCommentDetector()
	 */
	public ICommentDetector getCommentDetector() {
		return detector;
	}
}