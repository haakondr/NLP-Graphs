package no.roek.nlpgraphs;

import no.roek.nlpgraphs.concurrency.ConcurrencyService;

public class App {

	public static void main(String[] args) throws InterruptedException {
		ConcurrencyService cs = new ConcurrencyService();
		cs.start();
	}
}
