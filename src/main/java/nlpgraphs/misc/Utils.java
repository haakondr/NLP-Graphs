package nlpgraphs.misc;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Utils {

	public static <T> int listDiff(List<T> list1, List<T> list2) {
		//TODO: rewrite to something that doesn't require overriding hashCode in edge/node
		Set<T> intersect = new HashSet<>(list1);
		intersect.retainAll(list2);
		
		Set<T> temp = new HashSet<>();
		temp.addAll(list1);
		temp.addAll(list2);
		
		temp.removeAll(intersect);
		
		return temp.size();
	}
}
