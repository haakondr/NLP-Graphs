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

import java.util.Comparator;

/**
 * <p>
 * Default comment detector that is designed for comments found at the head of
 * Wordnet dictionary files. It assumes that each comment line starts with two
 * spaces, followed by a number that indicates the position of the comment line
 * relative to the rest of the comment lines in the file.
 * </p>
 * <p>
 * This class follows a singleton design pattern, and is not intended to be
 * instantiated directly; rather, call the {@link #getInstance()} method to get
 * the singleton instance.
 * <p>
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class CommentComparator implements Comparator<String>, ICommentDetector {
	
	// singleton instance
	private static CommentComparator instance;

	/**
	 * Returns the singleton instance of this class, instantiating it if
	 * necessary. The singleton instance will not be <code>null</code>.
	 * 
	 * @return the non-<code>null</code> singleton instance of this class,
	 *         instantiating it if necessary.
	 * @since JWI 2.0.0
	 */
	public static CommentComparator getInstance() {
		if (instance == null) 
			instance = new CommentComparator();
		return instance;
	}

	/**
	 * This constructor is marked protected so that the class may be
	 * sub-classed, but not directly instantiated. Obtain instances of this
	 * class via the static {@link #getInstance()} method.
	 * 
	 * @since JWI 2.0.0
	 */
	protected CommentComparator() {}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(String s1, String s2) {
		s1 = s1.trim();
		s2 = s2.trim();
		
		int idx1 = s1.indexOf(' ');
		int idx2 = s2.indexOf(' ');
		if(idx1 == -1) 
			idx1 = s1.length();
		if(idx2 == -1) 
			idx2 = s2.length();
		
		int num1 = Integer.parseInt(s1.substring(0, idx1));
		int num2 = Integer.parseInt(s2.substring(0, idx2));
		
		if(num1 < num2) 
			return -1;
		else if(num1 > num2) 
			return 1;
		return 0;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.compare.ICommentDetector#isCommentLine(java.lang.String)
	 */
	public boolean isCommentLine(String line) {
		return line.length() >= 2 && 
		       line.charAt(0) == ' ' && 
		       line.charAt(1) == ' ';
	}
}