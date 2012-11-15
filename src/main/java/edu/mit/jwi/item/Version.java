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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.mit.jwi.data.DataType;
import edu.mit.jwi.data.IContentType;
import edu.mit.jwi.data.WordnetFile;
import edu.mit.jwi.data.compare.ICommentDetector;

/**
 * Default, concrete implementation of the {@link IVersion} interface. This
 * class, much like the {@link Integer} class, caches instances, which should be
 * created via the {@code createVersion} methods.
 * <p>
 * This version object takes an optional bugfix version number and string qualifier.
 * The qualifier may only contain characters are that are valid Java 
 * 
 * @author Mark A. Finlayson
 * @version 2.2.3
 * @since JWI 2.1.0
 */
public class Version implements IVersion {
	
	// only create one instance of any version
	private static final Map<Integer, Version> versionCache = new HashMap<Integer, Version>();
	
	// Princeton Wordnet versions
	public static final Version ver16  = getVersion(1,6,0); 
	public static final Version ver17  = getVersion(1,7,0); 
	public static final Version ver171 = getVersion(1,7,1); 
	public static final Version ver20  = getVersion(2,0,0); 
	public static final Version ver21  = getVersion(2,1,0); 
	public static final Version ver30  = getVersion(3,0,0);
	
	// Stanford Augmented Wordnet versions
	public static final Version ver21swn_10k          = getVersion(2,1,0,"swn_10k");
	public static final Version ver21swn_20k          = getVersion(2,1,0,"swn_20k");
	public static final Version ver21swn_30k          = getVersion(2,1,0,"swn_30k");
	public static final Version ver21swn_40k          = getVersion(2,1,0,"swn_40k");
	public static final Version ver21swn_400k_cropped = getVersion(2,1,0,"swn_400k_cropped");
	public static final Version ver21swn_400k_full    = getVersion(2,1,0,"swn_400k_full");
	
	/** 
	 * The byte offset of the version indicator in the standard Wordnet file headers.
	 *
	 * @since JWI 2.1.0
	 */
	public static final int versionOffset = 803;

	// final instance fields
	private final int major;
	private final int minor;
	private final int bugfix;
	private final String qualifier;
	private transient String toString;

	/**
	 * Creates a new version object with the specified version numbers.
	 * <p>
	 * Clients should normally obtain instances of this class via the static
	 * {@code getVersion} methods.
	 * 
	 * @param major
	 *            the major version number, i.e., the '1' in 1.2.3
	 * @param minor
	 *            the minor version number, i.e., the '2' in 1.2.3
	 * @param bugfix
	 *            the bugfix version number, i.e., the '3' in 1.2.3
	 * @throws IllegalArgumentException
	 *             if any of the version numbers are negative
	 * @since JWI 2.1.0
	 */
	public Version(int major, int minor, int bugfix){
		this(major, minor, bugfix, null);
	}
	
	/**
	 * Creates a new version object with the specified version numbers.
	 * <p>
	 * Clients should normally obtain instances of this class via the static
	 * {@code getVersion} methods.
	 * 
	 * @param major
	 *            the major version number, i.e., the '1' in 1.2.3.q
	 * @param minor
	 *            the minor version number, i.e., the '2' in 1.2.3.q
	 * @param bugfix
	 *            the bugfix version number, i.e., the '3' in 1.2.3.q
	 * @param qualifier
	 *            the version qualifier, i.e., the 'q' in 1.2.3.q
	 * @throws IllegalArgumentException
	 *             if any of the version numbers are negative, or the qualifier
	 *             is not a legal qualifier
	 * @since JWI 2.2.0
	 */
	public Version(int major, int minor, int bugfix, String qualifier){
		qualifier = checkVersion(major, minor, bugfix, qualifier);
		
		// field assignments
		this.major = major;
		this.minor = minor;
		this.bugfix = bugfix;
		this.qualifier = qualifier;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IVersion#getMajorVersion()
	 */
	public int getMajorVersion() {
		return major;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IVersion#getMinorVersion()
	 */
	public int getMinorVersion() {
		return minor;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IVersion#getBugfixVersion()
	 */
	public int getBugfixVersion() {
		return bugfix;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see edu.mit.jwi.item.IVersion#getQualifier()
	 */
	public String getQualifier() {
		return qualifier;
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		return hashCode(major, minor, bugfix, qualifier);
	}

	/* 
	 * (non-Javadoc) 
	 *
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if(this == obj) 
			return true;
		if(obj == null) 
			return false;
		if(!(obj instanceof Version)) 
			return false;
		final Version other = (Version)obj;
		if(major != other.major) 
			return false;
		if(minor != other.minor) 
			return false;
		if(bugfix != other.bugfix) 
			return false;
		if(!qualifier.equals(other.qualifier))
			return false;
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		if (toString == null)
			toString = makeVersionString(major, minor, bugfix, qualifier);
		return toString;
	}

	/**
	 * Checks the supplied version numbers. Throws an
	 * {@link IllegalArgumentException} if they do not define a legal version.,
	 * 
	 * @param major
	 *            the major version number
	 * @param minor
	 *            the minor version number
	 * @param bugfix
	 *            the bugfix version number
	 * @param qualifier
	 *            the qualifier to check
	 * @return the <code>null</code>-masked qualifier
	 * @throws IllegalArgumentException
	 *             if the supplied arguments do not identify a legal version
	 * @since JWI 2.2.0
	 */
	public static String checkVersion(int major, int minor, int bugfix, String qualifier){
		checkVersionNumber(major, minor, bugfix);
		return checkQualifier(qualifier);
	}

	/**
	 * Checks the supplied version numbers. Throws an
	 * {@link IllegalArgumentException} if the version numbers are not valid
	 * (that is, any are below zero).
	 * 
	 * @param major
	 *            the major version number
	 * @param minor
	 *            the minor version number
	 * @param bugfix
	 *            the bugfix version number
	 * @throws IllegalArgumentException
	 *             if any of the supplied numbers are negative
	 * @since JWI 2.1.0
	 */
	public static void checkVersionNumber(int major, int minor, int bugfix){
		if(isIllegalVersionNumber(major, minor, bugfix)) 
			throw new IllegalArgumentException("Illegal version number: " + makeVersionString(major, minor, bugfix, null));
	}
	
	/**
	 * Checks the specified qualifier for legality. Throws an
	 * {@link IllegalArgumentException} if it is not a legal qualifier.
	 * 
	 * @param qualifier
	 *            the qualifier to check
	 * @return the <code>null</code>-masked qualifier
	 * @see #isIllegalQualifier(String)
	 * @since JWI 2.1.0
	 */
	public static String checkQualifier(String qualifier) {
		if(qualifier == null)
			return "";
		if(isIllegalQualifier(qualifier)) 
			throw new IllegalArgumentException("Illegal version qualifier: " + qualifier);
		return qualifier;
	}

	/**
	 * Returns <code>true</code> if the arguments identify a legal version;
	 * <code>false</code> otherwise.
	 * 
	 * @param major
	 *            the major version number
	 * @param minor
	 *            the minor version number
	 * @param bugfix
	 *            the bugfix version number
	 * @param qualifier
	 *            the version qualifier
	 * @return <code>true</code> if the arguments identify a legal version;
	 *         <code>false</code> otherwise.
	 * @since JWI 2.1.0
	 */
	public static boolean isIllegalVersion(int major, int minor, int bugfix, String qualifier){
		if(isIllegalVersionNumber(major, minor, bugfix))
			return true;
		if(isIllegalQualifier(qualifier))
			return true;
		return false;
	}
	
	/**
	 * Returns true if any of three numbers are negative
	 * 
	 * @param major
	 *            the major version number
	 * @param minor
	 *            the minor version number
	 * @param bugfix
	 *            the bugfix version number
	 * @return <code>true</code> if all the numbers are non-negative;
	 *         <code>false</code> otherwise
	 * @since JWI 2.1.0
	 */
	public static boolean isIllegalVersionNumber(int major, int minor, int bugfix){
		if(major < 0)
			return true;
		if(minor < 0)
			return true;
		if(bugfix < 0)
			return true;
		return false;
	}

	/**
	 * Returns <code>false</code>if the specified qualifier is legal, namely, if
	 * the string is either the empty string, or contains only characters that
	 * are found in valid java identifiers.
	 * 
	 * @see Character#isJavaIdentifierPart(char)
	 * @param qualifier
	 *            the qualifier to check
	 * @return <code>true</code> if not a legal qualifier; <code>false</code>
	 *         otherwise
	 * @throws NullPointerException
	 *             if the specified string is <code>null</code>
	 * @since JWI 2.2.0
	 */
	public static boolean isIllegalQualifier(String qualifier){
		for(int i = 0; i < qualifier.length(); i++)
			if(!Character.isJavaIdentifierPart(qualifier.charAt(i)))
				return true;
		return false;
	}
	
	/**
	 * Creates and caches, or retrieves from the cache, a version object
	 * corresponding to the specified numbers.
	 * 
	 * @param major
	 *            the major version number
	 * @param minor
	 *            the minor version number
	 * @param bugfix
	 *            the bugfix version number
	 * @return the cached version object corresponding to these numbers
	 * @since JWI 2.1.0
	 */
	public static Version getVersion(int major, int minor, int bugfix){
		return getVersion(major, minor, bugfix, null);
	}

	/**
	 * Creates and caches, or retrieves from the cache, a version object
	 * corresponding to the specified numbers.
	 * 
	 * @param major
	 *            the major version number
	 * @param minor
	 *            the minor version number
	 * @param bugfix
	 *            the bugfix version number
	 * @param qualifier
	 *            the version qualifier
	 * @return the cached version object corresponding to these numbers
	 * @throws IllegalArgumentException
	 *             if the version numbers and qualifier are not legal
	 * @since JWI 2.2.0
	 */
	public static Version getVersion(int major, int minor, int bugfix, String qualifier){
		qualifier = checkVersion(major, minor, bugfix, qualifier);
		int hash = hashCode(major, minor, bugfix, qualifier);
		Version version = versionCache.get(hash);
		if(version == null){
			version = new Version(major, minor, bugfix, qualifier);
			versionCache.put(version.hashCode(), version);
		}
		return version;
	}

	// fields for version parsing
	private static final Pattern periodPattern = Pattern.compile("\\Q.\\E");
	private static final Pattern digitPattern = Pattern.compile("\\d+");
	private static final String wordnetStr = "WordNet";
	private static final String copyrightStr = "Copyright";
	private static final Pattern versionPattern = Pattern.compile("WordNet\\s+\\d+\\Q.\\E\\d+(\\Q.\\E\\d+)?\\s+Copyright");

	/**
	 * Creates a version string for the specified version numbers.  If a version's
	 * bugfix number is 0, and if the qualifier is null or empty, the string produced is of the form "x.y".  I
	 * 
	 * @param major
	 *            the major version number, i.e., the '1' in 1.2.3.q
	 * @param minor
	 *            the minor version number, i.e., the '2' in 1.2.3.q
	 * @param bugfix
	 *            the bugfix version number, i.e., the '3' in 1.2.3.q
	 * @param qualifier
	 *            the version qualifier, i.e., the 'q' in 1.2.3.q
	 * @return a string representing the specified version
	 * @throws IllegalArgumentException
	 * @since JWI 2.2.0
	 */
	public static String makeVersionString(int major, int minor, int bugfix, String qualifier){
		qualifier = checkQualifier(qualifier);
		boolean hasQualifier = qualifier != null && qualifier.length() > 0;
		StringBuilder sb = new StringBuilder();
		sb.append(Integer.toString(major));
		sb.append('.');
		sb.append(Integer.toString(minor));
		if(bugfix > 0 || hasQualifier){
			sb.append('.');
			sb.append(Integer.toString(bugfix));
		}
		if(hasQualifier){
			sb.append('.');
			sb.append(qualifier);
		}
		return sb.toString();
	}
	
	/**
	 * Calculates the hash code for a version object with the specified version
	 * numbers.
	 * 
	 * @param major
	 *            the major version number, i.e., the '1' in 1.2.3.q
	 * @param minor
	 *            the minor version number, i.e., the '2' in 1.2.3.q
	 * @param bugfix
	 *            the bugfix version number, i.e., the '3' in 1.2.3.q
	 * @param qualifier
	 *            the version qualifier, i.e., the 'q' in 1.2.3.q
	 * @throws IllegalArgumentException
	 *             if the specified parameters do not identify a legal version
	 * @return the hash code for the specified version
	 */
	public static int hashCode(int major, int minor, int bugfix, String qualifier){
		qualifier = checkVersion(major, minor, bugfix, qualifier);
		final int prime = 31;
		int result = 1;
		result = prime * result + major;
		result = prime * result + minor;
		result = prime * result + bugfix;
		result = prime * result + qualifier.hashCode();
		return result;
	}

	/**
	 * Extracts a version object from a byte buffer that contains data with the
	 * specified content type. If no version can be extracted, returns
	 * <code>null</code>.
	 * 
	 * @param type
	 *            the content type of the data in the buffer
	 * @param buffer
	 *            the buffer containing the data
	 * @return the Version that was extracted, or <code>null</code> if none
	 * @since JWI 2.1.0
	 */
	public static Version extractVersion(IContentType<?> type, ByteBuffer buffer){
		if(!type.getDataType().hasVersion()) 
			return null;
		
		// first try direct access
		char c;
		StringBuilder sb = new StringBuilder();
		for(int i = versionOffset; i < buffer.limit(); i++){
			c = (char)buffer.get(i);
			if(Character.isWhitespace(c))
				break;
			sb.append(c);
		}
		Version version = parseVersionProtected(sb);
		if(version != null) 
			return version;
		
		// if direct access doesn't work, try walking forward in file
		// until we find a string that looks like "WordNet 2.1 Copyright"
		ICommentDetector cd = type.getLineComparator().getCommentDetector();
		if(cd == null)
			return null;
		
		int origPos = buffer.position();

		String line = null;
		Matcher m;
		while(buffer.position() < buffer.limit()){
			line = WordnetFile.getLine(buffer);
			if(line == null || !cd.isCommentLine(line)){
				line = null;
				break;
			}
			m = versionPattern.matcher(line);
			if(m.find()){
				line = m.group();
				int start = wordnetStr.length();
				int end = line.length()-copyrightStr.length();
				line = line.substring(start, end);
				break;
			}
		}
		buffer.position(origPos);
		return parseVersionProtected(line);
	}
	
	/**
	 * Tries to transform the specified character sequence into a version
	 * object. If it cannot, returns <code>null</code>
	 * 
	 * @param verStr
	 *            the sequence of characters to be transformed
	 * @return the version, or <code>null</code> if the character sequence is
	 *         not a valid version
	 * @since JWI 2.1.0
	 */
	public static Version parseVersionProtected(CharSequence verStr){
		if(verStr == null) 
			return null;
		String[] parts = periodPattern.split(verStr);
		
		if(parts.length < 2 || parts.length > 4)
			return null;
		
		String majorStr = parts[0].trim();
		if(!digitPattern.matcher(majorStr).matches())
			return null;
		int major = Integer.parseInt(majorStr);
		
		String minorStr = parts[1].trim();
		if(!digitPattern.matcher(minorStr).matches())
			return null;
		int minor = Integer.parseInt(minorStr);
		
		int bugfix = 0;
		if(parts.length >= 3){
			String bugfixStr = parts[2].trim();
			if(!digitPattern.matcher(bugfixStr).matches())
				return null;
			bugfix = Integer.parseInt(bugfixStr);
		}
		
		if(isIllegalVersionNumber(major, minor, bugfix))
			return null;
		
		String qualifier = null;
		if(parts.length == 4){
			qualifier = parts[3].trim();
			if(isIllegalQualifier(qualifier))
				return null;
		}
		
		return getVersion(major, minor, bugfix, qualifier);
	}
	
	/**
	 * Tries to transform the specified character sequence into a version
	 * object.
	 * 
	 * @param verStr
	 *            the sequence of characters to be transformed
	 * @return the version
	 * @throws NullPointerException
	 *             if the character sequence is <code>null</code>
	 * @throws IllegalArgumentException
	 *             if the character sequence does not correspond to a legal
	 *             version
	 * @since JWI 2.1.0
	 */
	public static Version parseVersion(CharSequence verStr){
		if(verStr == null) 
			throw new NullPointerException();
		
		String[] parts = periodPattern.split(verStr);
		if(parts.length < 2 || parts.length > 4)
			throw new IllegalArgumentException();
		
		// parts
		int major = Integer.parseInt(parts[0].trim());
		int minor = Integer.parseInt(parts[1].trim());
		int bugfix = (parts.length < 3) ? 
				0 : 
					Integer.parseInt(parts[2].trim());
		String qualifier = (parts.length < 4) ? 
				null : 
					parts[3].trim();
		
		return getVersion(major, minor, bugfix, qualifier);
	}
	
	// internal cache of declared version
	private static List<Version> versions;
	
	/**
	 * Emulates the Enum.values() function.
	 * 
	 * @return all the static data type instances listed in the class, in the
	 *         order they are declared.
	 * @since JWI 2.0.0
	 */
	public static List<Version> values(){
		if(versions == null){
			
			// get all the fields containing ContentType
			Field[] fields = DataType.class.getFields();
			List<Field> instanceFields = new ArrayList<Field>(); 
			for(Field field : fields)
				if(field.getGenericType() == Version.class)
					instanceFields.add(field);
			
			// this is the backing set
			List<Version> hidden = new ArrayList<Version>(instanceFields.size());
			
			// fill in the backing set
			Version dataType;
			for(Field field : instanceFields){
				try{
					dataType = (Version)field.get(null);
					if(dataType != null) 
						hidden.add(dataType);
				} catch(IllegalAccessException e){
					// Ignore
				}
			}
			
			// make the value set unmodifiable
			versions = Collections.unmodifiableList(hidden);
		}
			
		return versions;
	}
}
