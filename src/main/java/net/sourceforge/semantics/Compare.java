package net.sourceforge.semantics;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.StringTokenizer;

import net.sourceforge.classifier4J.vector.HashMapTermVectorStorage;
import net.sourceforge.classifier4J.vector.TermVectorStorage;
import net.sourceforge.classifier4J.vector.VectorClassifier;




public class Compare {

String a = "";
String b ="";

public Compare(String a, String b){
this.a  = a.toLowerCase();	
this.b = b.toLowerCase();
}

public double getResult(){

StringTokenizer ta = new StringTokenizer(a,"~!@#$%^&*()_-=+`[]{}:\"\\|;'<>?,./ ");	
StringTokenizer tb = new StringTokenizer(b,"~!@#$%^&*()_-=+`[]{}:\"\\|;'<>?,./ ");	

if (ta.countTokens()==1){
	File f = new File("src/main/resources/WORDS/"+Character.toString(a.charAt(0))+"/"+a+"/"+a+".txt");
	if(!f.exists()) {return -1;}
}
if (tb.countTokens()==1){
	File f = new File("src/main/resources/WORDS/"+Character.toString(b.charAt(0))+"/"+b+"/"+b+".txt");
	if(!f.exists()) {return -1;}	
}
	
String aa="";
String bb="";

while(ta.hasMoreTokens()){
	String g = ta.nextToken();
	String add = read(g);
	if (add.equals("")){aa=aa+" "+g;}
	else{aa = aa+" "+read(g);}
}


while(tb.hasMoreTokens()){
	String g = tb.nextToken();
	String add = read(g);
	if (add.equals("")){bb=bb+" "+g;}
	else{bb = bb+" "+read(g);}
}

try{
	
	TermVectorStorage storage = new HashMapTermVectorStorage();
    VectorClassifier vc = new VectorClassifier(storage);



    vc.teachMatch("category", aa);
    double result = vc.classify("category", bb);

        if (a.length()!=b.length()) result = result/Math.abs((a.length()-b.length())); //System.out.println(res);
        

    return result;
	
}catch(Exception e){return -1;}


}


private String read(String g){
	try {
	String filename = "src/main/resources/WORDS/"+Character.toString(g.charAt(0))+"/"+g+"/"+g+".txt";
	File f = new File(filename);
	if(f.exists()) {
		
		BufferedReader bread = new BufferedReader(new FileReader(filename));
		String rt = bread.readLine();
		bread.close();
		return rt;
		
	}else{return "";}
	} catch (Exception e) {
		return "";
	}

}


	
}
