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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.data.parse.DataLineParser;
import edu.mit.jwi.data.parse.ExceptionLineParser;
import edu.mit.jwi.data.parse.ILineParser;
import edu.mit.jwi.data.parse.IndexLineParser;
import edu.mit.jwi.data.parse.SenseLineParser;
import edu.mit.jwi.item.IExceptionEntryProxy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.POS;

/**
 * A concrete implementation of the {@code IDataType} interface. This class
 * provides the data types necessary for Wordnet in the form of static
 * fields. It is not implemented as an {@code Enum} so that clients may add
 * their own content types by instantiating this class.
 * 
 * @param <T>
 *            the type of object for the content type
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public class DataType<T> implements IDataType<T> {
	
	public static final DataType<IIndexWord> 			INDEX 		= new DataType<IIndexWord>(				"Index", 	 true,	IndexLineParser.getInstance(),		"index", "idx");
	public static final DataType<ISynset> 				DATA 		= new DataType<ISynset>(			 	"Data", 	 true, 	DataLineParser.getInstance(), 		"data", "dat");
	public static final DataType<IExceptionEntryProxy> 	EXCEPTION 	= new DataType<IExceptionEntryProxy>( 	"Exception", false,	ExceptionLineParser.getInstance(),	"exception", "exc");
	public static final DataType<ISenseEntry> 			SENSE 		= new DataType<ISenseEntry>( 			"Sense", 	 false,	SenseLineParser.getInstance(),		"sense");

	// fields set on construction
    private final String name;
    private final Set<String> hints;
    private final boolean hasVersion;
    private final ILineParser<T> parser;

	/**
	 * Constructs a new data type. This constructor takes the hints as an
	 * varargs array.
	 * 
	 * @param userFriendlyName
	 *            a user-friendly name, for easy identification of this data
	 *            type; may be <code>null</code>
	 * @param hasVersion
	 *            <code>true</code> if the comment header for this data type
	 *            usually contains a version number
	 * @param parser
	 *            the line parser for transforming lines from this data type
	 *            into objects; may not be <code>null</code>
	 * @param hints
	 *            a varargs array of resource name hints for identifying the
	 *            resource that contains the data. may be <code>null</code>, but
	 *            may not contain <code>null</code>
	 * @throws NullPointerException
	 *             if the specified parser is <code>null</code>
	 * @since JWI 2.0.0
	 */
    public DataType(String userFriendlyName, boolean hasVersion, ILineParser<T> parser, String... hints) {
    	this(userFriendlyName, hasVersion, parser, (hints == null) ? null : Arrays.asList(hints));
    }
    
	/**
	 * Constructs a new data type. This constructor takes the hints as an
	 * collection.
	 * 
	 * @param userFriendlyName
	 *            a user-friendly name, for easy identification of this data
	 *            type; may be <code>null</code>
	 * @param hasVersion
	 *            <code>true</code> if the comment header for this data type
	 *            usually contains a version number
	 * @param parser
	 *            the line parser for transforming lines from this data type
	 *            into objects; may not be <code>null</code>
	 * @param hints
	 *            a collection of resource name hints for identifying the
	 *            resource that contains the data. May be <code>null</code>, but
	 *            may not contain <code>null</code>
	 * @throws NullPointerException
	 *             if the specified parser is <code>null</code>
	 * @since JWI 2.0.0
	 */
    public DataType(String userFriendlyName, boolean hasVersion, ILineParser<T> parser, Collection<String> hints) {
    	if(parser == null) 
    		throw new NullPointerException();
        this.name = userFriendlyName;
        this.parser = parser;
        this.hasVersion = hasVersion;
        this.hints = (hints == null || hints.isEmpty()) ? Collections.<String>emptySet() : Collections.unmodifiableSet(new HashSet<String>(hints));
    }
    
    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.data.IDataType#hasVersion()
     */
    public boolean hasVersion(){
    	return hasVersion;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.data.IDataType#getResourceNameHints()
     */
    public Set<String> getResourceNameHints() {
        return hints;
    }
    
    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.data.IDataType#getParser()
     */
    public ILineParser<T> getParser(){
    	return parser;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return name;
    }
    
	// set of all data types implemented in this class
	private static Set<DataType<?>> dataTypes = null;
	
	/**
	 * Emulates the Enum.values() function.
	 * 
	 * @return all the static data type instances listed in the class, in the
	 *         order they are declared.
	 * @since JWI 2.0.0
	 */
	public static Collection<DataType<?>> values(){
		if(dataTypes == null){
			
			// get all the fields containing ContentType
			Field[] fields = DataType.class.getFields();
			List<Field> instanceFields = new ArrayList<Field>(); 
			for(Field field : fields)
				if(field.getGenericType() == DataType.class)
					instanceFields.add(field);
			
			// this is the backing set
			Set<DataType<?>> hidden = new LinkedHashSet<DataType<?>>(instanceFields.size());
			
			// fill in the backing set
			DataType<?> dataType;
			for(Field field : instanceFields){
				try{
					dataType = (DataType<?>)field.get(null);
					if(dataType != null) 
						hidden.add(dataType);
				} catch(IllegalAccessException e){
					// Ignore
				}
			}
			
			// make the value set unmodifiable
			dataTypes = Collections.unmodifiableSet(hidden);
		}
			
		return dataTypes;
	}

	/**
	 * Finds the first file that satisfies the naming constraints of both
	 * the data type and part of speech.
	 * 
	 * @param type
	 *            the data type whose resource name hints should be used, may
	 *            not be <code>null</code>
	 * @param pos
	 *            the part of speech whose resource name hints should be used,
	 *            may be <code>null</code>
	 * @param files
	 *            the files to be search, may be empty but not <code>null</code>
	 * @return the file that matches both the pos and type naming conventions,
	 *         or <code>null</code> if none is found.
	 * @throws NullPointerException
	 *             if the data type or file collection is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public static File find(IDataType<?> type, POS pos, Collection<? extends File> files){

		Set<String> typePatterns = type.getResourceNameHints();
		Set<String> posPatterns = (pos == null) ? Collections.<String>emptySet() : pos.getResourceNameHints();
		
		String name;
		for (File file : files) {
			name = file.getName();
			if(containsOneOf(name, typePatterns) && containsOneOf(name, posPatterns)) 
				return file;
		}
		
		return null;
	}

	/**
	 * Checks to see if one of the string patterns specified in the set of
	 * strings is found in the specified target string. If the pattern set is
	 * empty or null, returns <code>true</code>. If a pattern is found in the
	 * target string, returns <code>true</code>. Otherwise, returns
	 * <code>false</code>.
	 * 
	 * @param target
	 *            the string to be searched
	 * @param patterns
	 *            the patterns to search for
	 * @return <code>true</code> if the target contains one of the patterns;
	 *         <code>false</code> otherwise
	 * @since JWI 2.2.0
	 */
	public static boolean containsOneOf(String target, Set<String> patterns) {
		if (patterns == null || patterns.size() == 0) 
			return true;
		for (String pattern : patterns)
			if (target.indexOf(pattern) > -1) 
				return true;
		return false;
	}

}