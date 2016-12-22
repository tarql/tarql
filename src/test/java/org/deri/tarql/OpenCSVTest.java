package org.deri.tarql;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.StringReader;

import org.junit.Test;

import com.opencsv.CSVReader;


public class OpenCSVTest {

	@Test
	public void test() throws IOException {
		CSVReader r = new CSVReader(new StringReader("a,b\n1,2\n3,4"));
		try {
			assertArrayEquals(new String[]{"a","b"}, r.readNext());
			assertArrayEquals(new String[]{"1","2"}, r.readNext());
			assertArrayEquals(new String[]{"3","4"}, r.readNext());
			assertNull(r.readNext());
		} finally {
			r.close();
		}
	}
}
