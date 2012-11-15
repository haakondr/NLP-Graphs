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
 * Represents an entry in an exception file (e.g., verb.exc or exc.vrb). Most of
 * the functionality of this interface is inherited from
 * <code>IExceptionEntryProxy</code>.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public interface IExceptionEntry extends IExceptionEntryProxy, IHasPOS, IItem<IExceptionEntryID> {

}
