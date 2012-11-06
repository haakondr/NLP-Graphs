/********************************************************************************
 * MIT Java Wordnet Interface Library (JWI) v2.2.3
 * Copyright (c) 2007-2012 Massachusetts Institute of Technology
 *
 * JWI is distributed under the terms of the Creative Commons Attribution 3.0 
 * Unported License, which means it may be freely used for all purposes, as long 
 * as proper acknowledgment is made.  See the license file included with this
 * distribution for more details.
 *******************************************************************************/

package edu.mit.jwi.data.parse;

/**
 * A parser that transforms lines of data from a data source into data objects.
 * 
 * @param <T>
 *            the type of the object into which this parser transforms lines
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface ILineParser<T> {

	/**
	 * Given the line of data, this method produces an object of class
	 * <code>T</code>.
	 * 
	 * @param line
	 *            the line to be parsed
	 * @return the object resulting from the parse
	 * @throws NullPointerException
	 *             if the specified line is <code>null</code>
	 * @throws MisformattedLineException
	 *             if the line is malformed in some way
	 * @since JWI 1.0
	 */
	public T parseLine(String line);
	
	/**
	 * Thrown when a line from a data resource does not match expected formatting
	 * conventions.
	 * 
	 * @author Mark A. Finlayson
	 * @version 2.2.3
	 * @since JWI 1.0
	 */
	public class MisformattedLineException extends RuntimeException {

		// serial version id
		private static final long serialVersionUID = -4402988991209648616L;

		/**
		 * Constructs a new exception with {@code null} as its detail message. The
		 * cause is not initialized, and may subsequently be initialized by a call
		 * to {@link #initCause}.
		 * 
		 * @since JWI 2.0.0
		 */
		public MisformattedLineException() {
			super();
		}

		/**
		 * Constructs a new exception with the specified detail message. The cause
		 * is not initialized, and may subsequently be initialized by a call to
		 * {@link #initCause}.
		 * 
		 * @param message
		 *            the detail message. The detail message is saved for later
		 *            retrieval by the {@link #getMessage()} method.
		 * @since JWI 1.0
		 */
		public MisformattedLineException(String message) {
			super(message);
		}

		/**
		 * <p>
		 * Constructs a new exception with the specified detail message and cause.
		 * </p>
		 * <p>
		 * Note that the detail message associated with <code>cause</code> is
		 * <i>not</i> automatically incorporated in this runtime exception's detail
		 * message.
		 * </p>
		 * 
		 * @param message
		 *            the detail message (which is saved for later retrieval by the
		 *            {@link #getMessage()} method).
		 * @param cause
		 *            the cause (which is saved for later retrieval by the
		 *            {@link #getCause()} method). (A {@code null} value is
		 *            permitted, and indicates that the cause is nonexistent or
		 *            unknown.)
		 * @since JWI 1.0
		 */
		public MisformattedLineException(String message, Throwable cause) {
			super(message, cause);
		}

		/**
		 * Constructs a new exception with the specified cause and a detail message
		 * of {@code (cause==null ? null : cause.toString())} (which typically
		 * contains the class and detail message of {@code cause}). This constructor
		 * is useful for runtime exceptions that are little more than wrappers for
		 * other throwables.
		 * 
		 * @param cause
		 *            the cause (which is saved for later retrieval by the
		 *            {@link #getCause()} method). (A {@code null} value is
		 *            permitted, and indicates that the cause is nonexistent or
		 *            unknown.)
		 * @since JWI 2.0.0
		 */
		public MisformattedLineException(Throwable cause) {
			super(cause);
		}
	}

}
