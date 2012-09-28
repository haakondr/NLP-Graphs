package no.roek.nlpgraphs.document;

public class PlagiarismReference {
	private String filename, type, obfuscation,language, sourceReference, sourceLanguage;
	private String offset, length, sourceOffset, sourceLength;
	private double similarity;
		
	public PlagiarismReference(String filename, String type, String obfuscation, String language, String offset, String length, String sourceReference, String sourceLanguage, String sourceOffset, String sourceLength) {
		this.filename = filename;
		this.type = type;
		this.obfuscation = obfuscation;
		this.language = language;
		this.sourceReference = sourceReference;
		this.sourceLanguage = sourceLanguage;
		this.offset = offset;
		this.length = length;
		this.sourceOffset = sourceOffset;
		this.sourceLength = sourceLength;
	}
	
	public PlagiarismReference(String filename, String offset, String length, String sourceReference, String sourceOffset, String sourceLength) {
		this.filename = filename;
		this.sourceReference = sourceReference;
		this.offset = offset;
		this.length = length;
		this.sourceOffset = sourceOffset;
		this.sourceLength = sourceLength;
	}
	
	public PlagiarismReference(String filename, String offset, String length, String sourceReference, String sourceOffset, String sourceLength, double similarity) {
		this(filename, offset, length, sourceReference, sourceOffset, sourceLength);
		this.similarity = similarity;
	}
	
	public PlagiarismReference(String filename, int offset, int length, String sourceReference, int sourceOffset, int sourceLength) {
		this(filename, Integer.toString(offset), Integer.toString(length), sourceReference, Integer.toString(sourceOffset), Integer.toString(sourceLength));
	}

	public String getType() {
		return type;
	}

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

	public String getLength() {
		return length;
	}

	public String getSourceOffset() {
		return sourceOffset;
	}

	public String getSourceLength() {
		return sourceLength;
	}

	public double getSimilarity() {
		return similarity;
	}

	public String getFilename() {
		return filename;
	}
}
