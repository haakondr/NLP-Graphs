package no.roek.nlpgraphs.document;

public class PlagiarismReference {
	private String type, obfuscation,language, sourceReference, sourceLanguage;
	private String offset, length, sourceOffset, sourceLength;
		
	public PlagiarismReference(String type, String obfuscation, String language, String offset, String length, String sourceReference, String sourceLanguage, String sourceOffset, String sourceLength) {
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
	
	public PlagiarismReference(String offset, String length, String sourceReference, String sourceOffset, String sourceLength) {
		this.sourceReference = sourceReference;
		this.offset = offset;
		this.length = length;
		this.sourceOffset = sourceOffset;
		this.sourceLength = sourceLength;
	}
	
	public PlagiarismReference(int offset, int length, String sourceReference, int sourceOffset, int sourceLength) {
		this(Integer.toString(offset), Integer.toString(length), sourceReference, Integer.toString(sourceOffset), Integer.toString(sourceLength));
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
}
