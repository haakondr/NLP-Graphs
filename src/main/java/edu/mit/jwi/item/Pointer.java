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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Concrete implementation of the <code>IPointer</code> interface. This class
 * includes, as public fields, all pointers, lexical and semantic, defined in
 * the standard WordNet distribution.
 * <p>
 * This class in not implemented as an {@code Enum} so that clients may
 * instantiate their own pointers using this implementation.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public class Pointer implements IPointer {
    
	public static final Pointer ALSO_SEE 				= new Pointer("^", 	"Also See");
	public static final Pointer	ANTONYM 				= new Pointer("!", 	"Antonym");
	public static final Pointer	ATTRIBUTE 				= new Pointer("=", 	"Attribute");
	public static final Pointer	CAUSE 					= new Pointer(">", 	"Cause");
	public static final Pointer DERIVATIONALLY_RELATED 	= new Pointer("+", 	"Derivationally related form");
	public static final Pointer DERIVED_FROM_ADJ		= new Pointer("\\", "Derived from adjective");
	public static final Pointer ENTAILMENT 				= new Pointer("*", 	"Entailment");
	public static final Pointer HYPERNYM 				= new Pointer("@", 	"Hypernym");
	public static final Pointer HYPERNYM_INSTANCE 		= new Pointer("@i", "Instance hypernym");
	public static final Pointer HYPONYM 				= new Pointer("~", 	"Hyponym");
	public static final Pointer HYPONYM_INSTANCE 		= new Pointer("~i", "Instance hyponym");
	public static final Pointer HOLONYM_MEMBER 			= new Pointer("#m", "Member holonym");
	public static final Pointer HOLONYM_SUBSTANCE 		= new Pointer("#s", "Substance holonym");
	public static final Pointer HOLONYM_PART 			= new Pointer("#p", "Part holonym");
	public static final Pointer MERONYM_MEMBER 			= new Pointer("%m", "Member meronym");
	public static final Pointer MERONYM_SUBSTANCE 		= new Pointer("%s", "Substance meronym");
	public static final Pointer MERONYM_PART 			= new Pointer("%p", "Part meronym");
	public static final Pointer PARTICIPLE 				= new Pointer("<", 	"Participle");
	public static final Pointer PERTAINYM 				= new Pointer("\\", "Pertainym (pertains to nouns)");
	public static final Pointer REGION 					= new Pointer(";r", "Domain of synset - REGION");
	public static final Pointer REGION_MEMBER 			= new Pointer("-r", "Member of this domain - REGION");
	public static final Pointer SIMILAR_TO 				= new Pointer("&", 	"Similar To");
	public static final Pointer TOPIC 					= new Pointer(";c", "Domain of synset - TOPIC");
	public static final Pointer TOPIC_MEMBER 			= new Pointer("-c", "Member of this domain - TOPIC");
	public static final Pointer USAGE 					= new Pointer(";u", "Domain of synset - USAGE");
	public static final Pointer USAGE_MEMBER 			= new Pointer("-u", "Member of this domain - USAGE");
	public static final Pointer VERB_GROUP 				= new Pointer("$", 	"Verb Group");
	
	// final instance fields
	private final String symbol;
    private final String name;
    private final String toString;

	/**
	 * Constructs a new pointer object with the specified symbol and name.
	 * 
	 * @param symbol
	 *            the pointer symbol; may not be <code>null</code>, empty, or
	 *            all whitespace
	 * @param name
	 *            the pointer name; may not be <code>null</code>, empty, or all
	 *            whitespace
	 * @since JWI 2.1.0
	 */
    public Pointer(String symbol, String name) {
    	this.symbol = checkString(symbol);
        this.name = checkString(name);
        this.toString = name.toLowerCase().replace(' ', '_').replace(",", "");
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.item.IPointer#getSymbol()
	 */
    public String getSymbol() {
        return symbol;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.item.IPointer#getName()
	 */
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return toString;
    }
    
	/**
	 * Throws an exception if the specified string is <code>null</code>, empty,
	 * or all whitespace. Returns a trimmed form of the string.
	 * 
	 * @param str
	 *            the string to be checked
	 * @return a trimmed form of the string
	 * @throws NullPointerException
	 *             if the specified string is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified string is empty or all whitespace
	 * @since JWI 2.2.0
	 */
	protected static String checkString(String str){
		str = str.trim();
		if(str.length() == 0)
			throw new IllegalArgumentException();
		return str;
	}

    private static final Map<String, Pointer> pointerMap;
    private static final Set<Pointer> pointerSet;
    
    static {

		// get the instance fields
		Field[] fields = Pointer.class.getFields();
		List<Field> instanceFields = new ArrayList<Field>();
		for(Field field : fields)
			if(field.getGenericType() == Pointer.class)
				instanceFields.add(field);
		
		// these are our backing collections
		Set<Pointer> hiddenSet = new LinkedHashSet<Pointer>(instanceFields.size());
		Map<String, Pointer> hiddenMap = new LinkedHashMap<String, Pointer>(instanceFields.size()-1);
		
		Pointer ptr;
		for(Field field : instanceFields){
			try{
				ptr = (Pointer)field.get(null);
				if(ptr == null)
					continue;
				hiddenSet.add(ptr);
				if(ptr != DERIVED_FROM_ADJ)
					hiddenMap.put(ptr.getSymbol(), ptr);
			} catch(IllegalAccessException e){
				// Ignore
			}
		}
		
		// make the collections unmodifiable
		pointerSet = Collections.unmodifiableSet(hiddenSet);
		pointerMap = Collections.unmodifiableMap(hiddenMap);
    }
    
	/**
	 * Emulates the {@code Enum#values()} function. Returns an unmodifiable collection
	 * of all the pointers declared in this class, in the order they are
	 * declared.
	 * 
	 * @return returns an unmodifiable collection of the pointers declared in
	 *         this class
	 * @since JWI 2.1.0
	 */
	public static Collection<Pointer> values(){
		return pointerSet;
	}

	private static final String ambiguousSymbol = "\\";
	
	/**
	 * Returns the pointer type (static final instance) that matches the
	 * specified pointer symbol.
	 * 
	 * @param symbol
	 *            the symbol to look up
	 * @param pos
	 *            the part of speech for the symbol; may be <code>null</code>
	 *            except for ambiguous symbols
	 * @throws IllegalArgumentException
	 *             if the symbol does not correspond to a known pointer.
	 * @since JWI 2.1.0
	 */
    public static Pointer getPointerType(String symbol, POS pos) {
    	if(pos == POS.ADVERB && symbol.equals(ambiguousSymbol))
    		return DERIVED_FROM_ADJ;
        Pointer pointerType = pointerMap.get(symbol);
        if (pointerType == null)
        	throw new IllegalArgumentException("No pointer type corresponding to symbol '" + symbol + "'");
        return pointerType;
    }
}
