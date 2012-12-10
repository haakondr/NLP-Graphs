package no.roek.nlpgraphs.misc;

public class ProgressPrinter {
	
	private final int total;
	private volatile int current;
	
	public ProgressPrinter(int total) {
		this.total = total;
	}
	
	
	private int getPercent() {
		return (current * 100) / total;
	}
	
	public synchronized void printProgressbar(String text){
		current++;
		int percent = getPercent();
		
	    StringBuilder bar = new StringBuilder("[");

	    for(int i = 0; i < 50; i++){
	        if( i < (percent/2)){
	            bar.append("=");
	        }else if( i == (percent/2)){
	            bar.append(">");
	        }else{
	            bar.append(" ");
	        }
	    }
	    bar.append("]  " + percent + "% files done: "+current+"/"+total);
	    System.out.print("\r" + bar.toString()+" | "+text);
	}
	
	public synchronized boolean isDone() {
		return current >= total;
	}
}
