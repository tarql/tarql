package org.deri.tarql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.Test;

public class CSVFragmentTest {
	private final static String absoluteNoFragment = "http://example.com/file.csv";

	protected URLOptionsParser parser;
	protected String remainingURL;
	protected CSVOptions options;
	
	protected void parse(String url) {
		parser = new URLOptionsParser(url);
		remainingURL = parser.getRemainingURL();
		options = parser.getOptions();
	}
	
	protected CSVOptions options(String url) {
		return new URLOptionsParser(url).getOptions();
	}
	
	@Test
	public void testNoFragment() {
		parse(absoluteNoFragment);
		assertEquals(absoluteNoFragment, remainingURL);
		assertNull(options.getEncoding());
		assertNull(options.hasColumnNamesInFirstRow());
	}

	@Test
	public void testEmptyFragment() {
		parse(absoluteNoFragment + "#");
		assertEquals(absoluteNoFragment + "#", remainingURL);
		assertNull(options.getEncoding());
		assertNull(options.hasColumnNamesInFirstRow());
	}

	@Test
	public void testEmptyRelativeFragment() {
		parse("#");
		assertEquals("#", remainingURL);
		assertNull(options.getEncoding());
		assertNull(options.hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testRetainNonTarqlFragment() {
		parse("#foo");
		assertEquals("#foo", remainingURL);
		assertNull(options.getEncoding());
		assertNull(options.hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testExtractEncoding() {
		parse(absoluteNoFragment + "#encoding=utf-8");
		assertEquals(absoluteNoFragment, remainingURL);
		assertEquals("utf-8", options.getEncoding());
	}
	
	@Test
	public void testExtractCharset() {
		parse(absoluteNoFragment + "#charset=utf-8");
		assertEquals(absoluteNoFragment, remainingURL);
		assertEquals("utf-8", options.getEncoding());
	}
	
	@Test
	public void testExtractPresentHeader() {
		parse(absoluteNoFragment + "#header=present");
		assertEquals(absoluteNoFragment, remainingURL);
		assertEquals(true, options.hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testExtractAbsentHeader() {
		parse(absoluteNoFragment + "#header=absent");
		assertEquals(absoluteNoFragment, remainingURL);
		assertEquals(false, options.hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testIgnoreUnrecognizedHeaderValue() {
		parse(absoluteNoFragment + "#header=foo");
		assertEquals(absoluteNoFragment + "#header=foo", remainingURL);
		assertNull(options.hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testExtractMultipleKeys() {
		parse(absoluteNoFragment + "#encoding=utf-8;header=absent");
		assertEquals(absoluteNoFragment, remainingURL);
		assertEquals("utf-8", options.getEncoding());
		assertEquals(false, options.hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testRetainUnknownKeys() {
		parse(absoluteNoFragment + "#encoding=utf-8;foo=bar;header=absent");
		assertEquals(absoluteNoFragment + "#foo=bar", remainingURL);
		assertEquals("utf-8", options.getEncoding());
		assertEquals(false, options.hasColumnNamesInFirstRow());
	}
	
	@Test
	public void testExtractDelimiter() {
		assertEquals(',', options("file.csv").getDelimiter().charValue());
		assertEquals('\t', options("file.tsv").getDelimiter().charValue());
		assertEquals(',', options("file.csv#delimiter=,").getDelimiter().charValue());
		assertEquals(',', options("file.csv#delimiter=comma").getDelimiter().charValue());
		assertEquals(';', options("file.csv#delimiter=semicolon").getDelimiter().charValue());
		assertEquals('\t', options("file.csv#delimiter=tab").getDelimiter().charValue());
		assertEquals(' ', options("file.csv#delimiter=%20").getDelimiter().charValue());
		assertEquals(';', options("file.csv#delimiter=%3B").getDelimiter().charValue());
		assertEquals(',', options("file.csv#delimiter=%2C").getDelimiter().charValue());
		assertEquals('\t', options("file.csv#delimiter=%09").getDelimiter().charValue());
		assertEquals(',', options("file.csv#delimiter=foo").getDelimiter().charValue());
		assertEquals(',', options("file.csv#delimiter=none").getDelimiter().charValue());
		assertEquals(',', options("file.csv#delimiter=").getDelimiter().charValue());
	}
	
	@Test
	public void testExtractQuoteChar() {
		assertEquals('"', options("file.csv").getQuoteChar().charValue());
		assertEquals('"', options("file.foo").getQuoteChar().charValue());
		assertNull(options("file.tsv").getQuoteChar());
		assertNull(options("file.foo#delimiter=%09").getQuoteChar());
		assertEquals('"', options("file.csv#quotechar=%22").getQuoteChar().charValue());
		assertEquals('"', options("file.csv#quotechar=doublequote").getQuoteChar().charValue());
		assertEquals('\'', options("file.csv#quotechar=%27").getQuoteChar().charValue());
		assertEquals('\'', options("file.csv#quotechar=singlequote").getQuoteChar().charValue());
		assertEquals('"', options("file.csv#quotechar=foo").getQuoteChar().charValue());
		assertNull(options("file.csv#quotechar=none").getQuoteChar());
		assertNull(options("file.csv#quotechar=").getQuoteChar());
	}
	
	@Test
	public void testExtractEscapeChar() {
		assertNull(options("file.csv").getEscapeChar());
		assertEquals('\\', options("file.csv#escapechar=%5C").getEscapeChar().charValue());
		assertEquals('\\', options("file.csv#escapechar=backslash").getEscapeChar().charValue());
		assertNull(options("file.csv#escapechar=foo").getEscapeChar());
	}
}
