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

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.mit.jwi.data.compare.DataLineComparator;
import edu.mit.jwi.data.compare.ExceptionLineComparator;
import edu.mit.jwi.data.compare.ILineComparator;
import edu.mit.jwi.data.compare.IndexLineComparator;
import edu.mit.jwi.data.compare.SenseKeyLineComparator;
import edu.mit.jwi.item.IExceptionEntryProxy;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISenseEntry;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.POS;

/**
 * A concrete implementation of the {@code IContentType} interface. This class
 * provides the content types necessary for Wordnet in the form of static
 * fields. It is not implemented as an {@code Enum} so that clients may add
 * their own content types by instantiating this class.
 * 
 * @param <T>
 *            the type of object for the content type
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.0.0
 */
public class ContentType<T> implements IContentType<T> {

	public static final ContentType<IIndexWord> 			INDEX_NOUN 				= new ContentType<IIndexWord>(				DataType.INDEX, 		POS.NOUN, 		IndexLineComparator.getInstance());
	public static final ContentType<IIndexWord> 			INDEX_VERB 				= new ContentType<IIndexWord>(				DataType.INDEX, 		POS.VERB, 		IndexLineComparator.getInstance());
	public static final ContentType<IIndexWord> 			INDEX_ADVERB 			= new ContentType<IIndexWord>(				DataType.INDEX, 		POS.ADVERB, 	IndexLineComparator.getInstance());
	public static final ContentType<IIndexWord> 			INDEX_ADJECTIVE 		= new ContentType<IIndexWord>(				DataType.INDEX,			POS.ADJECTIVE, 	IndexLineComparator.getInstance());
	public static final ContentType<ISynset> 				DATA_NOUN 				= new ContentType<ISynset>(					DataType.DATA, 			POS.NOUN, 		DataLineComparator.getInstance());
	public static final ContentType<ISynset> 				DATA_VERB 				= new ContentType<ISynset>(					DataType.DATA, 			POS.VERB, 		DataLineComparator.getInstance());
	public static final ContentType<ISynset> 				DATA_ADVERB 			= new ContentType<ISynset>(					DataType.DATA, 			POS.ADVERB, 	DataLineComparator.getInstance());
	public static final ContentType<ISynset> 				DATA_ADJECTIVE 			= new ContentType<ISynset>(					DataType.DATA, 			POS.ADJECTIVE, 	DataLineComparator.getInstance());
	public static final ContentType<IExceptionEntryProxy> 	EXCEPTION_NOUN 			= new ContentType<IExceptionEntryProxy>(	DataType.EXCEPTION, 	POS.NOUN, 		ExceptionLineComparator.getInstance());
	public static final ContentType<IExceptionEntryProxy> 	EXCEPTION_VERB 			= new ContentType<IExceptionEntryProxy>(	DataType.EXCEPTION, 	POS.VERB, 		ExceptionLineComparator.getInstance());
	public static final ContentType<IExceptionEntryProxy> 	EXCEPTION_ADVERB 		= new ContentType<IExceptionEntryProxy>(	DataType.EXCEPTION, 	POS.ADVERB, 	ExceptionLineComparator.getInstance());
	public static final ContentType<IExceptionEntryProxy> 	EXCEPTION_ADJECTIVE 	= new ContentType<IExceptionEntryProxy>(	DataType.EXCEPTION, 	POS.ADJECTIVE,	ExceptionLineComparator.getInstance());
	public static final ContentType<ISenseEntry> 			SENSE 					= new ContentType<ISenseEntry>(				DataType.SENSE, 		null,			SenseKeyLineComparator.getInstance());

	// fields set on construction
	private final IDataType<T> fType;
	private final POS fPOS;
	private final ILineComparator fComparator;
	private final String fString;

	/**
	 * Constructs a new ContentType
	 * 
	 * @param type
	 *            the data type for this content type, may not be
	 *            <code>null</code>
	 * @param pos
	 *            the part of speech for this content type; may be null if the
	 *            content type has no natural part of speech
	 * @param comparator
	 *            the line comparator for this content type; may be
	 *            <code>null</code> if the lines are not ordered
	 * @since JWI 2.0.0
	 */
	public ContentType(IDataType<T> type, POS pos, ILineComparator comparator) {
		if(type == null)
			throw new NullPointerException();
		fType = type;
		fPOS = pos;
		fComparator = comparator;

		if (pos != null) {
			fString = "[ContentType: " + fType.toString() + "/" + fPOS.toString() + "]";
		} else {
			fString = "[ContentType: " + fType.toString() + "]";
		}
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IContentType#getDataType()
	 */
	public IDataType<T> getDataType() {
		return fType;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IHasPOS#getPOS()
	 */
	public POS getPOS() {
		return fPOS;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IContentType#getLineComparator()
	 */
	public ILineComparator getLineComparator() {
		return fComparator;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return fString;
	}
	
	// set of all content types implemented in this class
	private static final Set<ContentType<?>> contentTypes;
	
	// initialization for static content type set
	static {
		
		// get all the fields containing ContentType
		Field[] fields = ContentType.class.getFields();
		List<Field> instanceFields = new ArrayList<Field>(); 
		for(Field field : fields)
			if(field.getType() == ContentType.class)
				instanceFields.add(field);
		
		// this is the backing set
		Set<ContentType<?>> hidden = new LinkedHashSet<ContentType<?>>(instanceFields.size());
		
		// fill in the backing set
		ContentType<?> contentType;
		for(Field field : instanceFields){
			try{
				contentType = (ContentType<?>)field.get(null);
				if(contentType == null) 
					continue;
				hidden.add(contentType);
			} catch(IllegalAccessException e){
				// Ignore
			}
		}
		
		// make the value set unmodifiable
		contentTypes = Collections.unmodifiableSet(hidden);
	}
	
	/**
	 * Emulates the Enum.values() function.
	 * 
	 * @return all the static ContentType instances listed in the class, in the
	 *         order they are declared.
	 * @since JWI 2.0.0
	 */
	public static Collection<ContentType<?>> values(){
		return contentTypes;
	}

	/**
	 * Use this convenience method to retrieve the appropriate
	 * {@code IIndexWord} content type for the specified POS.
	 * 
	 * @param pos
	 *            the part of speech for the content type, may not be
	 *            <code>null</code>
	 * @return the index content type for the specified part of speech
	 * @throws NullPointerException
	 *             if the specified part of speech is <code>null</code>
	 * @since JWI 2.0.0
	 */
	public static IContentType<IIndexWord> getIndexContentType(POS pos) {
		if(pos == null)
			throw new NullPointerException();
		switch(pos){
		case NOUN:
			return INDEX_NOUN;
		case VERB:
			return INDEX_VERB;
		case ADVERB:
			return INDEX_ADVERB;
		case ADJECTIVE:
			return INDEX_ADJECTIVE;
		}
		throw new IllegalStateException("This should not happen.");
	}

	/**
	 * Use this convenience method to retrieve the appropriate
	 * {@code ISynset} content type for the specified POS.
	 * 
	 * @param pos
	 *            the part of speech for the content type, may not be
	 *            <code>null</code>
	 * @return the index content type for the specified part of speech
	 * @throws NullPointerException
	 *             if the specified part of speech is <code>null</code>
	 * @since JWI 2.0.0
	 */
	public static IContentType<ISynset> getDataContentType(POS pos) {
		if(pos == null)
			throw new NullPointerException();
		switch(pos){
		case NOUN:
			return DATA_NOUN;
		case VERB:
			return DATA_VERB;
		case ADVERB:
			return DATA_ADVERB;
		case ADJECTIVE:
			return DATA_ADJECTIVE;
		}
		throw new IllegalStateException("How in the world did we get here?");
	}

	/**
	 * Use this convenience method to retrieve the appropriate
	 * {@code IExceptionEntryProxy} content type for the specified POS.
	 * 
	 * @param pos
	 *            the part of speech for the content type, may not be
	 *            <code>null</code>
	 * @return the index content type for the specified part of speech
	 * @throws NullPointerException
	 *             if the specified part of speech is <code>null</code>
	 * @since JWI 2.0.0
	 */
	public static IContentType<IExceptionEntryProxy> getExceptionContentType(POS pos) {
		if(pos == null)
			throw new NullPointerException();
		switch(pos){
		case NOUN:
			return EXCEPTION_NOUN;
		case VERB:
			return EXCEPTION_VERB;
		case ADVERB:
			return EXCEPTION_ADVERB;
		case ADJECTIVE:
			return EXCEPTION_ADJECTIVE;
		}
		throw new IllegalStateException("Great Scott, there's been a rupture in the space-time continuum!");
	}


}