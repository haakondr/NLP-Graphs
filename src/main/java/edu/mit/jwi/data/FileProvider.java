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
import java.io.FileFilter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import edu.mit.jwi.RAMDictionary;
import edu.mit.jwi.data.parse.ILineParser;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IVersion;
import edu.mit.jwi.item.Synset;

/**
 * <p>
 * Implementation of a data provider for Wordnet that uses files in the file
 * system to back instances of its data sources. This implementation takes a
 * <code>URL</code> to a file system directory as its path argument, and uses
 * the resource hints from the data types and parts of speech for its content
 * types to examine the filenames in the that directory to determine which files
 * contain which data.
 * </p>
 * <p>
 * This implementation supports loading the wordnet files into memory,
 * but this is actually not that beneficial for speed. This is because the
 * implementation loads the file data into memory uninterpreted, and on modern
 * machines, the time to interpret a line of data (i.e., parse it into a Java
 * object) is much larger than the time it takes to load the line from disk.
 * Those wishing to achieve speed increases from loading Wordnet into memory
 * should rely on the implementation in {@link RAMDictionary}, or something
 * similar, which pre-processes the Wordnet data into objects before caching
 * them.
 * </p>
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 1.0
 */
public class FileProvider implements IDataProvider, ILoadable, ILoadPolicy {
	
	// final instance fields
	private final Lock lifecycleLock = new ReentrantLock();
	private final Lock loadingLock = new ReentrantLock();
	private final Set<IContentType<?>> types;

	// instance fields 
	private URL url = null;
	private IVersion version = null;
	private Map<IContentType<?>, ILoadableDataSource<?>> fileMap = null;
	private int loadPolicy = NO_LOAD;
	private transient JWIBackgroundLoader loader = null;
	
	/**
	 * Constructs the file provider pointing to the resource indicated by the
	 * path.  This file provider has an initial {@link ILoadPolicy#NO_LOAD} load policy.
	 * 
	 * @param file
	 *            A file pointing to the wordnet directory, may not be
	 *            <code>null</code>
	 * @throws NullPointerException
	 *             if the specified file is <code>null</code>
	 * @since JWI 1.0
	 */
	public FileProvider(File file) {
		this(toURL(file));
	}

	/**
	 * Constructs the file provider pointing to the resource indicated by the
	 * path, with the specified load policy.
	 * 
	 * @param file
	 *            A file pointing to the wordnet directory, may not be
	 *            <code>null</code>
	 * @param loadPolicy
	 *            the load policy for this provider; this provider supports the
	 *            three values defined in <code>ILoadPolicy</code>.
	 * @throws NullPointerException
	 *             if the specified file is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public FileProvider(File file, int loadPolicy) {
		this(toURL(file), loadPolicy, ContentType.values());
	}

	/**
	 * Constructs the file provider pointing to the resource indicated by the
	 * path, with the specified load policy, looking for the specified content
	 * type.s
	 * 
	 * @param file
	 *            A file pointing to the wordnet directory, may not be
	 *            <code>null</code>
	 * @param loadPolicy
	 *            the load policy for this provider; this provider supports the
	 *            three values defined in <code>ILoadPolicy</code>.
	 * @param types
	 *            the content types this provider will look for when it loads
	 *            its data; may not be <code>null</code> or empty
	 * @throws NullPointerException
	 *             if the file or content type collection is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the set of types is empty
	 * @since JWI 2.2.0
	 */
	public FileProvider(File file, int loadPolicy, Collection<? extends IContentType<?>> types) {
		this(toURL(file), loadPolicy, types);
	}

	/**
	 * Constructs the file provider pointing to the resource indicated by the
	 * path.  This file provider has an initial {@link ILoadPolicy#NO_LOAD} load policy.
	 * 
	 * @param url
	 *            A file URL in UTF-8 decodable format, may not be
	 *            <code>null</code>
	 * @throws NullPointerException
	 *             if the specified URL is <code>null</code>
	 * @since JWI 1.0
	 */
	public FileProvider(URL url) {
		this(url, NO_LOAD);
	}

	/**
	 * Constructs the file provider pointing to the resource indicated by the
	 * path, with the specified load policy.
	 * 
	 * @param url
	 *            A file URL in UTF-8 decodable format, may not be
	 *            <code>null</code>
	 * @param loadPolicy
	 *            the load policy for this provider; this provider supports the
	 *            three values defined in <code>ILoadPolicy</code>.
	 * @throws NullPointerException
	 *             if the specified URL is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public FileProvider(URL url, int loadPolicy) {
		this(url, loadPolicy, ContentType.values());
	}

	/**
	 * Constructs the file provider pointing to the resource indicated by the
	 * path, with the specified load policy, looking for the specified content
	 * type.s
	 * 
	 * @param url
	 *            A file URL in UTF-8 decodable format, may not be
	 *            <code>null</code>
	 * @param loadPolicy
	 *            the load policy for this provider; this provider supports the
	 *            three values defined in <code>ILoadPolicy</code>.
	 * @param types
	 *            the content types this provider will look for when it loads
	 *            its data; may not be <code>null</code> or empty
	 * @throws NullPointerException
	 *             if the url or content type collection is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the set of types is empty
	 * @since JWI 2.2.0
	 */
	public FileProvider(URL url, int loadPolicy, Collection<? extends IContentType<?>> types) {
		if(url == null) 
			throw new NullPointerException();
		if(types.isEmpty()) 
			throw new IllegalArgumentException();
		this.url = url;
		this.loadPolicy = loadPolicy;
		this.types = Collections.unmodifiableSet(new HashSet<IContentType<?>>(types));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.data.IDataProvider#getSource()
	 */
	public URL getSource() {
		return url;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadPolicy#getLoadPolicy()
	 */
	public int getLoadPolicy() {
		return loadPolicy;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.data.IDataProvider#setSource(java.net.URL)
	 */
	public void setSource(URL url) {
		if(isOpen()) 
			throw new IllegalStateException("provider currently open");
		if(url == null) 
			throw new NullPointerException();
		this.url = url;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadPolicy#setLoadPolicy(int)
	 */
	public void setLoadPolicy(int policy) {
		try{
			loadingLock.lock();
			this.loadPolicy = policy; 	
		} finally {
			loadingLock.unlock();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see edu.mit.jwi.item.IHasVersion#getVersion()
	 */
	public IVersion getVersion() {
		checkOpen();
		if(version == null) 
			version = determineVersion(fileMap.values());
		if(version == IVersion.NO_VERSION)
			return null;
		return version;
	}

	/**
	 * Determines a version from the set of data sources, if possible, otherwise
	 * returns {@link IVersion#NO_VERSION}
	 * 
	 * @param srcs
	 *            the data sources to be used to determine the verison
	 * @return the single version that describes these data sources, or
	 *        {@link IVersion#NO_VERSION} if there is none
	 * @since JWI 2.1.0
	 */
	protected IVersion determineVersion(Collection<? extends IDataSource<?>> srcs){
		IVersion ver = IVersion.NO_VERSION;
		for(IDataSource<?> dataSrc : srcs){
			
			// if no version to set, ignore
			if(dataSrc.getVersion() == null)
				continue;
	
			// init version
			if(ver == IVersion.NO_VERSION){
				ver = dataSrc.getVersion();
				continue;
			} 
			
			// if version different from current
			if(!ver.equals(dataSrc.getVersion())) 
				return IVersion.NO_VERSION;
		}
		
		return ver;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IHasLifecycle#open()
	 */
	public boolean open() throws IOException {
		
		try {
			lifecycleLock.lock();
			loadingLock.lock();
			
			int policy = getLoadPolicy();
			
			// make sure directory exists
			File directory = toFile(url);
			if (!directory.exists()) 
				throw new IOException("Dictionary directory does not exist: " + directory);
	
			// get files in directory
			List<File> files = new ArrayList<File>(Arrays.asList(directory.listFiles(new FileFilter(){
				public boolean accept(File file) {
					return file.isFile();
				}
			})));
			if(files.isEmpty()) 
				throw new IOException("No files found in " + directory);
			
			// make the source map
			Map<IContentType<?>, ILoadableDataSource<?>> hiddenMap = createSourceMap(files, policy);
			if(hiddenMap.isEmpty()) 
				return false;
			
			// determine if it's already unmodifiable, wrap if not
			Map<?,?> map = Collections.unmodifiableMap(Collections.emptyMap());
			if(hiddenMap.getClass() != map.getClass())
				hiddenMap = Collections.unmodifiableMap(hiddenMap);
			this.fileMap = hiddenMap;
			
			// do load
			try {
				switch(loadPolicy){
				case BACKGROUND_LOAD:
					load(false);
					break;
				case IMMEDIATE_LOAD:
					load(true);
					break;
				default:
					// do nothing
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			return true;
		} finally {
			lifecycleLock.unlock();
			loadingLock.unlock();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#load()
	 */
	public void load() {
		try {
			load(false);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#load(boolean)
	 */
	public void load(boolean block) throws InterruptedException {
		try{
			loadingLock.lock();
			checkOpen();
			if(isLoaded()) 
				return;
			if(loader != null)
				return;
			loader = new JWIBackgroundLoader();
			loader.start();
			if(block) 
				loader.join();
		} finally {
			loadingLock.lock();
		}

	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.ILoadable#isLoaded()
	 */
	public boolean isLoaded() {
		if(!isOpen()) 
			throw new IllegalStateException("provider not open");
		try {
			loadingLock.lock();
			for(ILoadableDataSource<?> source : fileMap.values())
				if(!source.isLoaded()) 
					return false;
			return true;
		} finally{
			loadingLock.unlock();
		}
	}

	/**
	 * Creates the map that contains the content types mapped to the data
	 * sources. The method should return a non-null result, but it may be empty
	 * if no data sources can be created. Subclasses may override this method.
	 * 
	 * @param files
	 *            the files from which the data sources should be created, may
	 *            not be <code>null</code>
	 * @param policy
	 *            the load policy of the provider
	 * @return a map, possibly empty, but not <code>null</code>, of content
	 *         types mapped to data sources
	 * @throws NullPointerException
	 *             if the file list is <code>null</code>
	 * @throws IOException
	 *             if there is a problem creating the data source
	 * @since JWI 2.2.0
	 */
	protected Map<IContentType<?>, ILoadableDataSource<?>> createSourceMap(List<File> files, int policy) throws IOException {
		Map<IContentType<?>, ILoadableDataSource<?>> result = new HashMap<IContentType<?>, ILoadableDataSource<?>>();
		File file;
		for (IContentType<?> type : types) {
			file = DataType.find(type.getDataType(), type.getPOS(), files);
			if(file == null) continue;
			files.remove(file);
			result.put(type, createDataSource(file, type, policy));
		}
		return result;
	}

	/**
	 * Creates the actual data source implementations.
	 * 
	 * @param <T>
	 *            the content type of the data source
	 * @param file
	 *            the file from which the data source should be created, may not
	 *            be <code>null</code>
	 * @param type
	 *            the content type of the data source
	 * @param policy
	 *            the load policy to follow when creating the data source
	 * @return the created data source
	 * @throws NullPointerException
	 *             if any argument is <code>null</code>
	 * @throws IOException
	 *             if there is an IO problem when creating the data source
	 * @since JWI 2.2.0
	 */
	protected <T> ILoadableDataSource<T> createDataSource(File file, IContentType<T> type, int policy) throws IOException {
		
		ILoadableDataSource<T> src;
		if(type.getDataType() == DataType.DATA){
			
			src = createDirectAccess(file, type);
			src.open();
			if(policy == IMMEDIATE_LOAD) {
				try {
					src.load(true);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
			// check to see if direct access works with the file
			// often people will extract the files incorrectly on windows machines
			// and the binary files will be corrupted with extra CRs
			
			// get first line
			Iterator<String> itr = src.iterator();
			String firstLine = itr.next();
			if(firstLine == null) return src;
			
			// extract key
			ILineParser<T> parser = type.getDataType().getParser();
			ISynset s = (ISynset)parser.parseLine(firstLine);
			String key = Synset.zeroFillOffset(s.getOffset());
			
			// try to find line by direct access
			String soughtLine = src.getLine(key);
			if(soughtLine != null) return src;
			
			System.err.println(System.currentTimeMillis() + " - Error on direct access in " + type.getPOS().toString() + " data file: check CR/LF endings");
		}
		
		src = createBinarySearch(file, type);
		src.open();
		if(policy == IMMEDIATE_LOAD){
			try {
				src.load(true);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return src;
	}

	/**
	 * Creates a direct access data source for the specified type, using the
	 * specified file.
	 * 
	 * @param <T>
	 *            the parameter of the content type
	 * @param file
	 *            the file on which the data source is based; may not be
	 *            <code>null</code>
	 * @param type
	 *            the data type for the data source; may not be
	 *            <code>null</code>
	 * @return the data source
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @throws IOException
	 *             if there is an IO problem when creating the data source
	 *             object
	 * @since JWI 2.2.0
	 */
	protected <T> ILoadableDataSource<T> createDirectAccess(File file, IContentType<T> type) throws IOException {
		return new DirectAccessWordnetFile<T>(file, type);
	}

	/**
	 * Creates a binary search data source for the specified type, using the
	 * specified file.
	 * 
	 * @param <T>
	 *            the parameter of the content type
	 * @param file
	 *            the file on which the data source is based; may not be
	 *            <code>null</code>
	 * @param type
	 *            the data type for the data source; may not be
	 *            <code>null</code>
	 * @return the data source
	 * @throws NullPointerException
	 *             if either argument is <code>null</code>
	 * @throws IOException
	 *             if there is an IO problem when creating the data source
	 *             object
	 * @since JWI 2.2.0
	 */
	protected <T> ILoadableDataSource<T> createBinarySearch(File file, IContentType<T> type) throws IOException {
		return new BinarySearchWordnetFile<T>(file, type);
	}
	
	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IHasLifecycle#isOpen()
	 */
	public boolean isOpen() {
		try {
			lifecycleLock.lock();
			return fileMap != null;
		} finally {
			lifecycleLock.unlock();
		}
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IClosable#close()
	 */
	public void close() {
		try {
			lifecycleLock.lock();
			if(!isOpen()) 
				return;
			if(loader != null) 
				loader.cancel();
			for(IDataSource<?> source : fileMap.values()) 
				source.close();
			fileMap = null;
		} finally {
			lifecycleLock.unlock();
		}
	}

	/**
	 * Convenience method that throws an exception if the provider is closed.
	 * 
	 * @throws ObjectClosedException
	 *             if the provider is closed
	 * @since JWI 1.1
	 */
	protected void checkOpen() {
		if(!isOpen()) 
			throw new ObjectClosedException();
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IDataProvider#getSource(edu.mit.jwi.data.IContentType)
	 */
	// no way to safely cast; must rely on registerSource method to assure compliance
	@SuppressWarnings("unchecked") 
	public <T> ILoadableDataSource<T> getSource(IContentType<T> type) {
		checkOpen();
		return (ILoadableDataSource<T>)fileMap.get(type);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.data.IDataProvider#getTypes()
	 */
	public Set<? extends IContentType<?>> getTypes() {
		return types;
	}

	/**
	 * A thread class which tries to load each data source in this provider.
	 * 
	 * @author Mark A. Finlayson
	 * @version 2.2.3
	 * @since JWI 2.2.0
	 */
	protected class JWIBackgroundLoader extends Thread {
		
		// cancel flag
		private transient boolean cancel = false;
		
		/** 
		 * Constructs a new background loader that operates
		 * on the internal data structures of this provider.
		 *
		 * @since JWI 2.2.0
		 */
		public JWIBackgroundLoader(){
			setName(JWIBackgroundLoader.class.getSimpleName());
			setDaemon(true);
		}

		/* 
		 * (non-Javadoc) 
		 *
		 * @see java.lang.Thread#run()
		 */
		@Override
		public void run() {
			try {
				for(ILoadableDataSource<?> source : fileMap.values()){
					if(!cancel && !source.isLoaded()){
						try {
							source.load(true);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			} finally {
				loader = null;
			}
		}

		/** 
		 * Sets the cancel flag for this loader. 
		 *
		 * @since JWI 2.2.0
		 */
		public void cancel() {
			cancel = true;
			try {
				join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}

	/**
	 * Transforms a URL into a File. The URL must use the 'file' protocoal and
	 * must be in a UTF-8 compatible format as specified in
	 * {@link java.net.URLDecoder}.
	 * 
	 * @return a file pointing to the same place as the url
	 * @throws NullPointerException
	 *             if the url is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the url does not use the 'file' protocol
	 * @since JWI 1.0
	 */
	public static File toFile(URL url) throws IOException {
		if(!url.getProtocol().equals("file")) 
			throw new IllegalArgumentException("URL source must use 'file' protocol");
		try {
			return new File(URLDecoder.decode(url.getPath(), "UTF-8"));
		} catch(UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Transforms a file into a URL.
	 * 
	 * @param file
	 *            the file to be transformed
	 * @return a URL representing the file
	 * @throws NullPointerException
	 *             if the specified file is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public static URL toURL(File file) {
		if(file == null)
			throw new NullPointerException();
		try{
			URI uri = new URI("file", "//", file.toURL().getPath() , null);
			return new URL("file", null, uri.getRawPath());
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		} catch (URISyntaxException e) {
			e.printStackTrace();
			return null;
		}
	}

}