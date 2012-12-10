package no.roek.nlpgraphs.application;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import no.roek.nlpgraphs.misc.Fileutils;
import no.roek.nlpgraphs.preprocessing.DependencyParser;
import no.roek.nlpgraphs.preprocessing.POSTagParser;

import com.mongodb.BasicDBObject;

public class DependencyGraphCreator {

	public static void main(String[] args) throws FileNotFoundException {
		
		POSTagParser postagger = new POSTagParser();
		DependencyParser depParser = new DependencyParser();
		
		boolean running = true;
		while(running==true) {
			running = preprocess(postagger, depParser);
		}
	}
	
	public static boolean preprocess(POSTagParser postagger, DependencyParser depParser) {
		String text = getInput("Please enter the text, finish with enter: ");
		String outFile = getInput("Please enter output filename: ");
		parseToFile(text, outFile, postagger, depParser);
		String temp = getInput("Dependency parsing done, written to "+outFile+". Continue? [y/n]");
		
		return temp.equalsIgnoreCase("y");
	}
	
	public static String getInput(String text) {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		String input = "";
		try {
			System.out.println(text);
			input = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return input;
	}
	
	public static void parseToFile(String text, String outFile, POSTagParser postagger, DependencyParser depParser) {
		BasicDBObject json = depParser.parseSentence(postagger.postagSentence(text), outFile, 1,0, text.length());
		Fileutils.writeToFile(outFile, json.toString());
	}
}
