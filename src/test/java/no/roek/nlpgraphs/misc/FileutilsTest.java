package no.roek.nlpgraphs.misc;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import no.roek.nlpgraphs.document.DocumentFile;

import org.junit.Before;
import org.junit.Test;


public class FileutilsTest {

	private Path dir;
	
	@Before
	public void setup() {
		dir = Paths.get("src/test/resources/documents");
	}
	
	@Test
	public void shouldSetRelativePath() {
		Path lol = Paths.get("src/test/resources/documents/lol.txt");
		assertEquals(lol.getFileName().toString(), "lol.txt");
		assertEquals(dir.relativize(lol), lol.getFileName());
		
		DocumentFile file = new DocumentFile(Paths.get("src/test/resources/documents/omg.txt"), dir);
		assertEquals("omg.txt", file.getRelPath().toString());
	}
	
	@Test
	public void shouldGetFiles() {
		File[] files = Fileutils.getFiles(dir);
		assertEquals(18, files.length);
	}
}
