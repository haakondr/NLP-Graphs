import static org.junit.Assert.assertEquals;

import org.junit.Test;

import no.roek.nlpgraphs.concurrency.ParseJob;


public class ParseJobTest {

	@Test
	public void shouldSetParentDir() {
		ParseJob job = new ParseJob("/test/parent/file.txt");
		assertEquals(job.getParentDir(), "parent/");
	}
	
	@Test
	public void shouldGetFilename() {
		ParseJob job = new ParseJob("test/parent/file.txt");
		assertEquals(job.getFilename(), "file.txt");
	}
}
