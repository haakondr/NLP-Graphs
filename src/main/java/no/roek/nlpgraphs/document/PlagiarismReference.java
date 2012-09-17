package no.roek.nlpgraphs.document;

public class PlagiarismReference {
	private String type, obfuscation,language, sourceReference, sourceLanguage;
	private int offset, length, sourceOffset, sourceLength;
		
	public PlagiarismReference(String type, String obfuscation, String language, int offset, int length, String sourceReference, String sourceLanguage, int sourceOffset, int sourceLength) {
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

	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	public int getSourceOffset() {
		return sourceOffset;
	}

	public int getSourceLength() {
		return sourceLength;
	}
}
