package org.deri.tarql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.deri.tarql.CSVOptions.ParseResult;
import org.junit.Test;

public class CSVFragmentTest {
	private final static String absoluteNoFragment = "http://example.com/file.csv";

	@Test
	public void testNoFragment() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment);
		assertEquals(absoluteNoFragment, parsed.getRemainingIRI());
		assertNull(parsed.getOptions().getEncoding());
		assertNull(parsed.getOptions().hasColumnNamesInFirstRow());
	}

	@Test
	public void testEmptyFragment() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#");
		assertEquals(absoluteNoFragment + "#", parsed.getRemainingIRI());
		assertNull(parsed.getOptions().getEncoding());
		assertNull(parsed.getOptions().hasColumnNamesInFirstRow());
	}

	@Test
	public void testEmptyRelativeFragment() {
		ParseResult parsed = CSVOptions.parseIRI("#");
		assertEquals("#", parsed.getRemainingIRI());
		assertNull(parsed.getOptions().getEncoding());
		assertNull(parsed.getOptions().hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testRetainNonTarqlFragment() {
		ParseResult parsed = CSVOptions.parseIRI("#foo");
		assertEquals("#foo", parsed.getRemainingIRI());
		assertNull(parsed.getOptions().getEncoding());
		assertNull(parsed.getOptions().hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testExtractEncoding() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#encoding=utf-8");
		assertEquals(absoluteNoFragment, parsed.getRemainingIRI());
		assertEquals("utf-8", parsed.getOptions().getEncoding());
	}
	
	@Test
	public void testExtractCharset() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#charset=utf-8");
		assertEquals(absoluteNoFragment, parsed.getRemainingIRI());
		assertEquals("utf-8", parsed.getOptions().getEncoding());
	}
	
	@Test
	public void testExtractPresentHeader() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#header=present");
		assertEquals(absoluteNoFragment, parsed.getRemainingIRI());
		assertEquals(true, parsed.getOptions().hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testExtractAbsentHeader() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#header=absent");
		assertEquals(absoluteNoFragment, parsed.getRemainingIRI());
		assertEquals(false, parsed.getOptions().hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testIgnoreUnrecognizedHeaderValue() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#header=foo");
		assertEquals(absoluteNoFragment + "#header=foo", parsed.getRemainingIRI());
		assertNull(parsed.getOptions().hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testExtractMultipleKeys() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#encoding=utf-8;header=absent");
		assertEquals(absoluteNoFragment, parsed.getRemainingIRI());
		assertEquals("utf-8", parsed.getOptions().getEncoding());
		assertEquals(false, parsed.getOptions().hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testRetainUnknownKeys() {
		ParseResult parsed = CSVOptions.parseIRI(absoluteNoFragment + "#encoding=utf-8;foo=bar;header=absent");
		assertEquals(absoluteNoFragment + "#foo=bar", parsed.getRemainingIRI());
		assertEquals("utf-8", parsed.getOptions().getEncoding());
		assertEquals(false, parsed.getOptions().hasColumnNamesInFirstRow());
	}
}
