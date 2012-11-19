package no.roek.nlpgraphs;

public class App {

	public static void main(String[] args) {
		PlagiarismSearch ps = new PlagiarismSearch();
		
		if(ps.shouldPreprocess()) {
			ps.preprocess();
		}else if(ps.shouldCreateIndex()) {
			ps.createIndex();
		}else {
			ps.startPlagiarismSearch();
		}
	}
}
