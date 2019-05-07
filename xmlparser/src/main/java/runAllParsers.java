import java.util.concurrent.TimeUnit;

public class runAllParsers {
	public static void main(String[] args) {
		long startTime = System.currentTimeMillis();
		
		starxmlparser sxmlp = new starxmlparser();
		sxmlp.runParser();
		
		mainsxmlparser mxmlp = new mainsxmlparser();
		mxmlp.runParser();
		
		castsxmlparser cxmlp = new castsxmlparser();
		cxmlp.runParser();
		
		long endTime = System.currentTimeMillis();
		long totalMs = endTime - startTime;
		System.out.println("Total time to execute: " + totalMs + " ms");
		System.out.println("Total time to execute: " + String.format("%02dH:%02dM:%02dS", 
				TimeUnit.MILLISECONDS.toHours(totalMs),
				TimeUnit.MILLISECONDS.toMinutes(totalMs) -  
				TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(totalMs)), // The change is in this line
				TimeUnit.MILLISECONDS.toSeconds(totalMs) - 
				TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(totalMs))));
	}
}
