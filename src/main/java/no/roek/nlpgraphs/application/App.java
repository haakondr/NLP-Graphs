package no.roek.nlpgraphs.application;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;


public class App {

	public static void main(String[] args) {
		PlagiarismSearch ps = new PlagiarismSearch();

		int choice = getChoice();
		if(choice==1) {
			GED.main(args);
		}else if(choice==2) {
			ps.preprocess();			
		}else if(choice==3) {
			ps.createIndex();
		}else if(choice==4) {
			ps.startCandidateRetrieval();
		}else if(choice==5) {
			ps.startPlagiarismSearchWithoutCandret();
		}
	}
	
	public static int getChoice()  {
		InputStreamReader converter = new InputStreamReader(System.in);
		BufferedReader in = new BufferedReader(converter);
		int choice = 0;

		try {
			System.out.println("Welcome to Graph Edit Distance plagiarism search");
			System.out.println("For the program to work, some preferences must be specified in the file app.properties. Have a look at app.properties.example for an example.");
			System.out.println("Please select action..");
			System.out.println("type <number> then enter");
			System.out.println("1: Graph edit distance calculation of two sentences");
			System.out.println("2: preprocess the data specified in DATA_DIR in app.properties");
			System.out.println("3: build index required for the candidate retrieval phase");
			System.out.println("4: start candidate retrieval. The results will be saved to the data base.");
			System.out.println("5: start detailed analysis with candidate retrieval results written to file.");
			System.out.println("exit: exits the application");
			String action = in.readLine();
			if(action.equalsIgnoreCase("exit")) {
				System.out.println("Exiting..");
				System.exit(0);
			}
			try {
				choice = Integer.parseInt(action);
				if(choice > 0 && choice < 6) {
					return choice;
				}else {
					System.out.println("invalid choice, try again");
					return getChoice();
				}
			}catch(NumberFormatException e) {
				System.out.println("Invalid choice, try again..");
				return getChoice();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		return -1;
	}
}
