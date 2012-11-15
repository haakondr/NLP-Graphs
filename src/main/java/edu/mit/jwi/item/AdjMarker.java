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
 * The three different possible syntactic markers indicating limitations on the
 * syntactic position an adjective may have in relation to the noun it modifies.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public enum AdjMarker {

	PREDICATE  ("(p)", "predicate position"), 
	PRENOMINAL ("(a)", "prenominal (attributive) position"), 
	POSTNOMINAL("(ip)","immediately postnominal position");

	// unmodifiable fields
	private final String symbol;
	private final String description;

	/**
	 * Constructs a new adjective marker with the specified symbol and
	 * description
	 * 
	 * @param symbol
	 *            the symbol, may neither be <code>null</code> nor empty
	 * @param description
	 *            the description, may neither be <code>null</code> nor empty
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if either argument is empty or all whitespace
	 * @since JWI 2.1.0
	 */
	private AdjMarker(String symbol, String description) {
		if(symbol == null)
			throw new NullPointerException();
		if(description == null)
			throw new NullPointerException();
		symbol = symbol.trim();
		description = description.trim();
		if(symbol.length() == 0)
			throw new IllegalArgumentException();
		if(description.length() == 0)
			throw new IllegalArgumentException();
		this.symbol = symbol;
		this.description = description;
	}

	/**
	 * Returns the adjective marker symbol, as found appended to the ends of
	 * adjective words in the data files, parenthesis included.
	 * 
	 * @return the symbol for this adjective marker
	 * @since JWI 2.1.0
	 */
	public String getSymbol() {
		return symbol;
	}

	/**
	 * Returns a user-readable description of the type of marker, drawn from the
	 * Wordnet specification.
	 * 
	 * @return a user-readable description of the marker
	 * @since JWI 2.1.0
	 */
	public String getDescription() {
		return description;
	}

}
