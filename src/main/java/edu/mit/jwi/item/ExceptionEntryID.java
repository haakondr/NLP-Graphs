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
 * Default implementation of {@code IExceptionEntryID}.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class ExceptionEntryID implements IExceptionEntryID {

	// immutable instance fields
    private final String surfaceForm;
    private final POS pos;

	/**
	 * Creates a new exception entry id with the specified information.
	 * 
	 * @param surfaceForm
	 *            the surface form for the entry
	 * @param pos
	 *            the part of speech for the entry
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the surface form is empty or all whitespace
	 * @since JWI 1.0
	 */
    public ExceptionEntryID(String surfaceForm, POS pos) {
    	if(pos == null) 
    		throw new NullPointerException();
    	if(surfaceForm == null)
    		throw new NullPointerException();
    	surfaceForm = surfaceForm.trim();
    	if(surfaceForm.length() == 0)
    		throw new IllegalArgumentException();
    	// all exception entries are lower-case
    	// this call also checks for null
        this.surfaceForm = surfaceForm.toLowerCase(); 
        this.pos = pos;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IExceptionEntryID#getSurfaceForm()
     */
    public String getSurfaceForm() {
        return surfaceForm;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.IHasPOS#getPOS()
     */
    public POS getPOS() {
        return pos;
    }
    
    /* 
     * (non-Javadoc) 
     *
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return "EID-" + surfaceForm + "-" + pos.getTag();
    }

    /* 
     * (non-Javadoc) 
     *
     * @see java.lang.Object#hashCode()
     */
    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + surfaceForm.hashCode();
        result = PRIME * result + pos.hashCode();
        return result;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        if (this == obj) 
        	return true;
        if (obj == null) 
        	return false;
        if(!(obj instanceof IExceptionEntryID)) 
        	return false;
        final IExceptionEntryID other = (IExceptionEntryID) obj;
        if (!surfaceForm.equals(other.getSurfaceForm())) 
        	return false;
        if (!pos.equals(other.getPOS())) 
        	return false;
        return true;
    }
    
	/**
	 * Convenience method for transforming the result of the {@link #toString()}
	 * method back into an {@code IExceptionEntryID}.
	 * 
	 * @param value
	 *            the string to parse
	 * @return the derived exception entry id
	 * @throws NullPointerException
	 *             if the specified string is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the specified string does not conform to an exception
	 *             entry id
	 * @since JWI 2.2.0
	 */
    public static ExceptionEntryID parseExceptionEntryID(String value) {
        if(value == null)
        	throw new NullPointerException();

        if(!value.startsWith("EID-"))
        	throw new IllegalArgumentException();
        
        if(value.charAt(value.length()-2) != '-')
        	throw new IllegalArgumentException();
        
        POS pos = POS.getPartOfSpeech(value.charAt(value.length()-1));
        
        return new ExceptionEntryID(value.substring(4, value.length()-2), pos);
    }

}
