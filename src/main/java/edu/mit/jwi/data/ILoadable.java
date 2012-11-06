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

/**
 * An object that can be loaded. What 'loading' means may be implementation
 * dependent, but usually it will mean 'loaded into memory'.
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.2.0
 */
public interface ILoadable {

	/**
	 * Starts a simple, non-blocking load. If the object is already loaded, the
	 * method returns immediately and has no effect. If the object is in the
	 * process of loading, the method also returns immediately.
	 * 
	 * @since JWI 2.2.0
	 */
	public void load();

	/**
	 * Initiates the loading process. Depending on the flag, the method may
	 * return immediately (<code>block</code> is <code>false</code>), or return
	 * only when the loading process is complete. If the object is already
	 * loaded, the method returns immediately and has no effect. If the object
	 * is in the process of loading, and the method is called in blocking mode,
	 * the method blocks until loading is complete, even if that call of the
	 * method did not initiate the loading process. Some implementors of this
	 * interface may not support the immediate-return functionality.
	 * 
	 * @param block
	 *            if <code>true</code>, the method returns only when the loading
	 *            process is complete; if <code>false</code>, the method returns
	 *            immediately.
	 * @throws InterruptedException
	 *             if the method is blocking, and is interrupted while waiting
	 *             for loading to complete
	 * @since JWI 2.2.0
	 */
	public void load(boolean block) throws InterruptedException;

	/**
	 * Returns whether this object is loaded or not. This method should return
	 * <code>true</code> only if the loading process has completed and the
	 * object is actually loaded; if the object is still in the process of
	 * loading, or failed to load, the method should return <code>false</code>.
	 * 
	 * @return <code>true</code> if the method has completed loading;
	 *         <code>false</code> otherwise
	 * @since JWI 2.2.0
	 */
	public boolean isLoaded();

}
