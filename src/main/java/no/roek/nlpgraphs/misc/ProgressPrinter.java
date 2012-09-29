package no.roek.nlpgraphs.misc;

public class ProgressPrinter {
	
	private int total, current;
	private long startTime;
	
	public ProgressPrinter(int total) {
		this.total = total;
		startTime = System.currentTimeMillis();
	}
	
	
	private synchronized int getPercent() {
		return (current * 100) / total;
	}
	
	public synchronized void printProgressbar(){
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
	    long timeSpent = System.currentTimeMillis() - startTime;
	    bar.append("]   " + percent + "%     files done: "+current+"/"+total+". Time spent: "+timeSpent);
	    System.out.print("\r" + bar.toString());
	}
}
