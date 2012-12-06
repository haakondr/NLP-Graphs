package no.roek.nlpgraphs.detailed.analysis;

public class PlagiarismReference {
	private String filename, obfuscation,language, sourceReference, sourceLanguage;
	private String offset, length, sourceOffset, sourceLength, name;
	private double similarity;
		
	public PlagiarismReference(String filename, String name, String type, String obfuscation, String language, String offset, String length, String sourceReference, String sourceLanguage, String sourceOffset, String sourceLength) {
		this.filename = filename;
		this.name = name;
//		this.type = type;
		this.obfuscation = obfuscation;
		this.language = language;
		this.sourceReference = sourceReference;
		this.sourceLanguage = sourceLanguage;
		this.offset = offset;
		this.length = length;
		this.sourceOffset = sourceOffset;
		this.sourceLength = sourceLength;
	}
	
	public PlagiarismReference(String filename, String name, String offset, String length, String sourceReference, String sourceOffset, String sourceLength) {
		this.filename = filename;
		this.name = name;
		this.sourceReference = sourceReference;
		this.offset = offset;
		this.length = length;
		this.sourceOffset = sourceOffset;
		this.sourceLength = sourceLength;
	}
	
	public PlagiarismReference(String filename, String name, String offset, String length, String sourceReference, String sourceOffset, String sourceLength, double similarity) {
		this(filename, name, offset, length, sourceReference, sourceOffset, sourceLength);
		this.similarity = similarity;
	}
	
	public PlagiarismReference(String filename, String name, int offset, int length, String sourceReference, int sourceOffset, int sourceLength) {
		this(filename, name, Integer.toString(offset), Integer.toString(length), sourceReference, Integer.toString(sourceOffset), Integer.toString(sourceLength));
	}

//	public String getType() {
//		return type;
//	}

	public String getObfuscation() {
		return obfuscation;
	}

	public String getLanguage() {
		return language;
	}

	public String getSourceReference() {
		return sourceReference;
	}

	public String getSourceLanguage() {
		return sourceLanguage;
	}

	public String getOffset() {
		return offset;
	}
	
	public int getOffsetInt() {
		return Integer.parseInt(offset);
	}

	public String getLength() {
		return length;
	}
	
	public int getLengthInt() {
		return Integer.parseInt(length);
	}

	public String getSourceOffset() {
		return sourceOffset;
	}
	
	public int getSourceOffsetInt() {
		return Integer.parseInt(sourceOffset);
	}

	public String getSourceLength() {
		return sourceLength;
	}

	public int getSourceLengthInt() {
		return Integer.parseInt(sourceLength);
	}
	
	public double getSimilarity() {
		return similarity;
	}
	
	public void setSimilarity(double sim) {
		this.similarity = sim;
	}

	public String getFilename() {
		return filename;
	}

	public String getName() {
		return name;
	}
	
	public void setOffset(String offset) {
		this.offset = offset;
	}
	
	public void setOffset(int offset) {
		this.offset = Integer.toString(offset);
	}
	
	public void setLength(String length) {
		this.length = length;
	}
	
	public void setLength(int length) {
		this.length = Integer.toString(length);
	}
	
	public void setSourceOffset(String offset) {
		this.sourceOffset = offset;
	}
	
	public void setSourceOffset(int offset) {
		this.sourceOffset = Integer.toString(offset);
	}
	
	public void setSourceLength(String length) {
		this.sourceLength = length;
	}
	
	public void setSourceLength(int length) {
		this.sourceLength = Integer.toString(length);
	}
	
	public boolean equalFiles(PlagiarismReference other) {
		return sourceReference.equals(other.getSourceReference()) && filename.equals(other.getFilename());
	}
	
	public int getEndInt() {
		return getOffsetInt() + getLengthInt();
	}
	
	public int getSourceEndInt() {
		return getSourceOffsetInt() + getSourceLengthInt();
	}
}
