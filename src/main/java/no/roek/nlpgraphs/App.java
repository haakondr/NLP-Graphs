package no.roek.nlpgraphs;

import no.roek.nlpgraphs.concurrency.ConcurrencyService;

public class App {

	public static void main(String[] args) throws InterruptedException {
		ConcurrencyService cs = new ConcurrencyService();
		
		if(cs.shouldPreprocess()) {
			cs.preprocess();
		}else if(cs.shouldCreateIndex()) {
			cs.createIndex();
		}else {
			cs.PlagiarismSearch();
		}
	}
}
