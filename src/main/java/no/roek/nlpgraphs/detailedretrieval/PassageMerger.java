package no.roek.nlpgraphs.detailedretrieval;

import java.util.ArrayList;
import java.util.List;

public class PassageMerger {

	public static List<PlagiarismReference> mergePassages(List<PlagiarismReference> references) {
		List<PlagiarismReference> merged = new ArrayList<>();
		
		for (PlagiarismReference ref : references) {
			PlagiarismReference temp = addRef(ref, merged);
			if(temp!=null) {
				merged.remove(temp);
				merged.add(mergePassage(ref, temp));
			}else {
				merged.add(ref);
			}
		}
		
		return merged;
	}
	
	public static PlagiarismReference addRef(PlagiarismReference ref, List<PlagiarismReference> merged) {
		for (PlagiarismReference ref2 : merged) {
			if(shouldMergePassages(ref, ref2)) {
				return ref2;
			}
		}
		
		return null;
	}
	
	public static PlagiarismReference mergePassage(PlagiarismReference ref, PlagiarismReference other) {
		if(ref.getOffsetInt() == other.getOffsetInt()) {
			int len = Math.max(ref.getLengthInt(), other.getLengthInt());
			ref.setLength(len);
		}
		if(ref.getSourceOffsetInt() == other.getSourceOffsetInt()) {
			int len = Math.max(ref.getSourceLengthInt(), other.getSourceLengthInt());
			ref.setSourceLength(len);
		}
		
		if(ref.getOffsetInt() < other.getOffsetInt()) {
			ref.setLength(other.getOffsetInt() + other.getLengthInt() - ref.getOffsetInt());
		}else {
			ref.setLength(ref.getOffsetInt() + ref.getLengthInt() - other.getOffsetInt());
			ref.setOffset(other.getOffsetInt());
		}

		if(ref.getSourceOffsetInt() < other.getSourceOffsetInt()) {
			ref.setSourceLength(other.getSourceOffsetInt() + other.getSourceLengthInt() - ref.getSourceOffsetInt());
		}else {
			ref.setSourceLength(ref.getSourceOffsetInt() + ref.getSourceLengthInt() - other.getSourceOffsetInt());
			ref.setSourceOffset(other.getSourceOffsetInt());
		}
		
		return ref;
	}
	
	
	public static  boolean shouldMergePassages(PlagiarismReference ref1, PlagiarismReference ref2) {
		if(!equalFilenames(ref1, ref2)) {
			return false;
		}
		int suspiciousDiff = getPassageDiff(ref1.getOffsetInt(), ref1.getEndInt(), ref2.getOffsetInt(), ref2.getEndInt());
		int sourceDiff = getPassageDiff(ref1.getSourceOffsetInt(), ref1.getSourceEndInt(), ref2.getSourceOffsetInt(), ref2.getSourceEndInt());
		return (suspiciousDiff < 100) && (sourceDiff < 100);
	}
	
	private static  boolean equalFilenames(PlagiarismReference ref1, PlagiarismReference ref2) {
		return ref1.getFilename().equals(ref2.getFilename()) && ref1.getSourceReference().equals(ref2.getSourceReference());
	}
	
	private static  int getPassageDiff(int offset1, int end1, int offset2, int end2) {
		if(isOverlap(offset1, end1, offset2, end2)) {
			return 0;
		}
		int dist1 = Math.abs(end1 - offset2);
		int dist2 = Math.abs(end2 - offset1);
		
		return Math.min(dist1, dist2);
	}

	private static boolean isOverlap(int offset1, int end1, int offset2, int end2) {
		if(offset1 <= offset2 && offset2 <= end1) {
			return true;
		}else if(offset2 <= offset1 && offset1 <= end2) {
			return true;
		}else {
			return false;
		}
	}

}
