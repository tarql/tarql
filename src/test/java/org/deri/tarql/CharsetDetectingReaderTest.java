package org.deri.tarql;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.junit.Test;

public class CharsetDetectingReaderTest {

	@Test
	public void testSimple() throws IOException {
		assertEquals("Hello", read("Hello".getBytes("US-ASCII"), 100, 100));
	}
	
	@Test
	public void testReadInBlocks() throws IOException {
		assertEquals("Hello", read("Hello".getBytes("US-ASCII"), 2, 100));
	}
	
	@Test
	public void testReadSmallDetectorBlocks() throws IOException {
		assertEquals("Hello", read("Hello".getBytes("US-ASCII"), 100, 2));
	}
	
	@Test
	public void testDetectUTF8() throws IOException {
		assertEquals("Sp\u00E4tzle", read("Sp\u00E4tzle".getBytes("UTF-8"), 100, 100));
	}
	
	@Test
	public void testDetectISO_8859_1() throws IOException {
		assertEquals("Sp\u00E4tzle", read("Sp\u00E4tzle".getBytes("ISO-8859-1"), 100, 100));
	}
	
	public void testUTF8CharacterOnBlockBoundary() throws IOException {
		assertEquals("Sp\u00E4tzle", read("Sp\u00E4tzle".getBytes("UTF-8"), 3, 100));
		assertEquals("Sp\u00E4tzle", read("Sp\u00E4tzle".getBytes("UTF-8"), 100, 3));
	}

	private String read(byte[] s, int readBlockSize, int detectorBlockSize) 
			throws IOException {
		try {
			Reader r = new CharsetDetectingReader(
					new ByteArrayInputStream(s), detectorBlockSize);
			StringBuffer result = new StringBuffer();
			char[] buffer = new char[readBlockSize];
			int i;
			try {
				while ((i = r.read(buffer)) != -1) {
					result.append(buffer, 0, i);
				}
			} finally {
				r.close();
			}
			return result.toString();
		} catch (UnsupportedEncodingException ex) {
			// Can't happen
			return null;
		}
	}
}
