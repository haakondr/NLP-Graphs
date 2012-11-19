package no.roek.nlpgraphs;

public class App {

	public static void main(String[] args) {
		PlagiarismSearch app = new PlagiarismSearch();
		
		if(app.shouldPreprocess()) {
			app.preprocess();
		}else if(app.shouldCreateIndex()) {
			app.createIndex();
		}else {
			app.startPlagiarismSearch();
		}
	}
}
