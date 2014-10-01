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
	
	@Test
	public void testExtractDelimiter() {
		assertNull(CSVOptions.parseIRI("file.csv").getOptions().getDelimiter());
		assertEquals(',', CSVOptions.parseIRI("file.csv#delimiter=,").getOptions().getDelimiter().charValue());
		assertEquals(',', CSVOptions.parseIRI("file.csv#delimiter=comma").getOptions().getDelimiter().charValue());
		assertEquals(';', CSVOptions.parseIRI("file.csv#delimiter=semicolon").getOptions().getDelimiter().charValue());
		assertEquals('\t', CSVOptions.parseIRI("file.csv#delimiter=tab").getOptions().getDelimiter().charValue());
		assertEquals(' ', CSVOptions.parseIRI("file.csv#delimiter=%20").getOptions().getDelimiter().charValue());
		assertEquals(';', CSVOptions.parseIRI("file.csv#delimiter=%3B").getOptions().getDelimiter().charValue());
		assertEquals(',', CSVOptions.parseIRI("file.csv#delimiter=%2C").getOptions().getDelimiter().charValue());
		assertEquals('\t', CSVOptions.parseIRI("file.csv#delimiter=%09").getOptions().getDelimiter().charValue());
		assertNull(CSVOptions.parseIRI("file.csv#delimiter=foo").getOptions().getDelimiter());
	}
	
	@Test
	public void testExtractQuoteChar() {
		assertNull(CSVOptions.parseIRI("file.csv").getOptions().getQuoteChar());
		assertEquals('"', CSVOptions.parseIRI("file.csv#quotechar=%22").getOptions().getQuoteChar().charValue());
		assertEquals('"', CSVOptions.parseIRI("file.csv#quotechar=doublequote").getOptions().getQuoteChar().charValue());
		assertEquals('\'', CSVOptions.parseIRI("file.csv#quotechar=%27").getOptions().getQuoteChar().charValue());
		assertEquals('\'', CSVOptions.parseIRI("file.csv#quotechar=singlequote").getOptions().getQuoteChar().charValue());
		assertNull(CSVOptions.parseIRI("file.csv#quotechar=foo").getOptions().getDelimiter());
	}
	
	@Test
	public void testExtractEscapeChar() {
		assertNull(CSVOptions.parseIRI("file.csv").getOptions().getEscapeChar());
		assertEquals('\\', CSVOptions.parseIRI("file.csv#escapechar=%5C").getOptions().getEscapeChar().charValue());
		assertEquals('\\', CSVOptions.parseIRI("file.csv#escapechar=backslash").getOptions().getEscapeChar().charValue());
		assertNull(CSVOptions.parseIRI("file.csv#escapechar=foo").getOptions().getEscapeChar());
	}
}
