package no.roek.nlpgraphs;

public class RunApp {

	public static void main(String[] args) {
		App app = new App();
		
		if(app.shouldPreprocess()) {
			app.preprocess();
		}else if(app.shouldCreateIndex()) {
			app.createIndex();
		}else {
			app.PlagiarismSearch();
		}
	}
}
