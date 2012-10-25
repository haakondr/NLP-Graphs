
package net.sourceforge.classifier4J;   
   
import net.sourceforge.classifier4J.bayesian.WordsDataSourceException;
   
   
public abstract class AbstractCategorizedTrainableClassifier extends AbstractClassifier implements ITrainableClassifier {   
   
    /**  
     * @see net.sourceforge.classifier4J.IClassifier#classify(java.lang.String)  
     */   
    public double classify(String input) throws WordsDataSourceException, ClassifierException {   
        return classify(ICategorisedClassifier.DEFAULT_CATEGORY, input);   
    }   
   
    public void teachMatch(String input) throws WordsDataSourceException, ClassifierException {   
        teachMatch(ICategorisedClassifier.DEFAULT_CATEGORY, input);   
    }   
   
    public void teachNonMatch(String input) throws WordsDataSourceException, ClassifierException {   
        teachNonMatch(ICategorisedClassifier.DEFAULT_CATEGORY, input);   
    }   
   
}