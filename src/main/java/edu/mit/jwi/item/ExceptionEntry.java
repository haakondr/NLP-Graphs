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
 * Default implementation of {@code IExceptionEntry}
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class ExceptionEntry extends ExceptionEntryProxy implements
        IExceptionEntry {

	// immutable instance fields
    private final POS pos;
    private final IExceptionEntryID id;

	/**
	 * Creates a new exception entry for the specified part of speech using the
	 * information in the specified exception proxy object.
	 * 
	 * @param proxy
	 *            the proxy containing the information for the entry
	 * @param pos
	 *            the part of speech for the entry
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @since JWI 1.0
	 */
    public ExceptionEntry(IExceptionEntryProxy proxy, POS pos) {
        super(proxy);
        if (pos == null)
            throw new NullPointerException();
        this.pos = pos;
        this.id = new ExceptionEntryID(getSurfaceForm(), pos);
    }

	/**
	 * Creates a new exception entry for the specified part of speech using the
	 * specified surface and root forms.
	 * 
	 * @param surfaceForm
	 *            the surface form for the entry
	 * @param pos
	 *            the part of speech for the entry
	 * @param rootForms
	 *            the root forms for the entry
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @since JWI 1.0
	 */
    public ExceptionEntry(String surfaceForm, POS pos, String... rootForms) {
        super(surfaceForm, rootForms);
        if(pos == null)
            throw new NullPointerException();
        this.id = new ExceptionEntryID(getSurfaceForm(), pos);
        this.pos = pos;
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
     * @see edu.mit.jwi.item.IItem#getID()
     */
    public IExceptionEntryID getID() {
        return id;
    }

    /* 
     * (non-Javadoc) 
     *
     * @see edu.mit.jwi.item.ExceptionEntryProxy#toString()
     */
    public String toString() {
        return super.toString() + "-" + pos.toString();
    }
}
