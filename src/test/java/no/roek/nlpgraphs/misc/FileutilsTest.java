package no.roek.nlpgraphs.misc;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.sun.media.sound.InvalidFormatException;

public class FileutilsTest {

	@Test
	public void shouldChangeFileExtention() {
		String xml = Fileutils.replaceFileExtention("test.txt", "xml");
		assertEquals("test.xml", xml);
		
		
	}
	
	@Test
	public void shouldNotChangeFileExtention() {
		String xml = Fileutils.replaceFileExtention("test.omg/txt.txt", "xml");
		assertEquals("test.omg/txt.xml", xml);
	}
}
