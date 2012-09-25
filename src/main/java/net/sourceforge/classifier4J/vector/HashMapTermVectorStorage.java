package net.sourceforge.classifier4J.vector;   
   
import java.util.HashMap;
import java.util.Map;
   
   
public class HashMapTermVectorStorage implements TermVectorStorage {   
    private Map storage = new HashMap();   
       
       
    /**  
     * @see net.sourceforge.classifier4J.vector.TermVectorStorage#addTermVector(java.lang.String, net.sourceforge.classifier4J.vector.TermVector)  
     */   
    public void addTermVector(String category, TermVector termVector) {   
        storage.put(category, termVector);           
    }   
   
    /**  
     * @see net.sourceforge.classifier4J.vector.TermVectorStorage#getTermVector(java.lang.String)  
     */   
    public TermVector getTermVector(String category) {   
        return (TermVector) storage.get(category);   
    }   
   
}