package org.deri.tarql;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration options for describing a CSV file. Also provides
 * convenience functions for creating {@link Reader}s and
 * {@link CSVParser}s based on these options.
 */
public class CSVOptions {

	/**
	 * Extracts a CSVOptions object from the fragment part of a
	 * CSV file URL, e.g.,
	 * <code>http://example.com/file.csv#encoding=utf-8;header=absent</code>
	 * The remainder of the URL, with anything interpretable
	 * removed, can also be obtained from the returned
	 * parse result.
	 */
	public static ParseResult parseIRI(String iri) {
		CSVOptions result = new CSVOptions();
		int hash = iri.indexOf("#");
		if (hash == -1 || hash == iri.length() - 1) {
			return new ParseResult(iri, result);
		}
		String fragment = iri.substring(hash + 1);
		StringBuffer remainingIRI = new StringBuffer(iri.substring(0, hash));
		boolean hasHash = false;
		for (String part: fragment.split(";")) {
			if (part.startsWith(encodingKey)) {
				result.setEncoding(part.substring(encodingKey.length()));
				continue;
			}
			if (part.startsWith(charsetKey)) {
				result.setEncoding(part.substring(charsetKey.length()));
				continue;
			}
			if (part.startsWith(headerKey)) {
				String value = part.substring(headerKey.length());
				if ("present".equals(value)) {
					result.setColumnNamesInFirstRow(true);
					continue;
				}
				if ("absent".equals(value)) {
					result.setColumnNamesInFirstRow(false);
					continue;
				}
			}
			if (part.startsWith(delimiterKey)) {
				Character c = parseChar(part.substring(delimiterKey.length()));
				if (c == null) continue;
				result.setDelimiter(c);
			}
			if (part.startsWith(quoteCharKey)) {
				Character c = parseChar(part.substring(quoteCharKey.length()));
				if (c == null) continue;
				result.setQuoteChar(c);
			}
			if (part.startsWith(escapeCharKey)) {
				Character c = parseChar(part.substring(escapeCharKey.length()));
				if (c == null) continue;
				result.setEscapeChar(c);
			}
			if (hasHash) {
				remainingIRI.append(";");
			} else {
				remainingIRI.append('#');
				hasHash = true;
			}
			remainingIRI.append(part);
		}
		return new ParseResult(remainingIRI.toString(), result);
	}	
	private final static String encodingKey = "encoding=";
	private final static String charsetKey = "charset=";
	private final static String headerKey = "header=";
	private final static String delimiterKey = "delimiter=";
	private final static String quoteCharKey = "quotechar=";
	private final static String escapeCharKey = "escapechar=";
	@SuppressWarnings("serial")
	private final static Map<String, Character> charNames = new HashMap<String, Character>() {{
		put("tab", '\t');
		put("comma", ',');
		put("semicolon", ';');
		put("singlequote", '\'');
		put("doublequote", '"');
		put("backslash", '\\');
	}};

	/**
	 * Interprets argument as a character. Can be a literal single-character
	 * string, or a %-encoded character (e.g., %09 for tab), or one of the
	 * pre-defined named characters such as "tab", "backslash", etc.
	 */
	private static Character parseChar(String value) {
		if (charNames.containsKey(value.toLowerCase())) {
			return charNames.get(value.toLowerCase());
		}
		try {
			value = URLDecoder.decode(value, "utf-8");
			if (value.length() == 1) {
				return value.charAt(0);
			}
		} catch (UnsupportedEncodingException ex) {
			// Can't happen, UTF-8 always supported
		}
		return null;
	}
	
	/**
	 * Helper class for the result of parseIRI(iri)
	 */
	public static class ParseResult {
		private final String iri;
		private final CSVOptions options;
		public ParseResult(String iri, CSVOptions options) {
			this.iri = iri;
			this.options = options;
		}
		public String getRemainingIRI() {
			return iri;
		}
		public CSVOptions getOptions() {
			return options;
		}
		public CSVOptions getOptions(CSVOptions defaults) {
			CSVOptions result = new CSVOptions();
			result.overrideWith(defaults);
			result.overrideWith(options);
			return result;
		}
	}
	
	private String encoding = null;
	private Boolean columnNamesInFirstRow = null;
	private Character delimiter = null;
	private Character quote = null;
	private Character escape = null;

	/**
	 * Creates a new instance with default values.
	 */
	public CSVOptions() {}
	
	/**
	 * Creates a new instance and initializes it with values
	 * from another instance.
	 */
	public CSVOptions(CSVOptions defaults) {
		overrideWith(defaults);
	}

	/**
	 * Override values in this object with those from the other. Anything
	 * that is <code>null</code> in the other object will be ignored.
	 */
	public void overrideWith(CSVOptions other) {
		if (other.encoding != null) {
			this.encoding = other.encoding;
		}
		if (other.columnNamesInFirstRow != null) {
			this.columnNamesInFirstRow = other.columnNamesInFirstRow;
		}
		if (other.delimiter != null) {
			this.delimiter = other.delimiter;
		}
		if (other.quote != null) {
			this.quote = other.quote;
		}
		if (other.escape != null) {
			this.escape = other.escape;
		}
	}
	
	/**
	 * Specify the CSV file's character encoding. <code>null</code>
	 * signifies unknown encoding, that is, auto-detection.
	 * The default is <code>null</code>.
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	/**
	 * Returns the CSV file's character encoding, or <code>null</code>
	 * if unknown.
	 */
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * Set whether the CSV file's first row contains column names.
	 * <code>null</code> means unknown.
	 * The default is <code>null</code>.
	 */
	public void setColumnNamesInFirstRow(Boolean value) {
		this.columnNamesInFirstRow = value;
	}
	
	/**
	 * Set whether the CSV file's first row contains column names.
	 * <code>null</code> means unknown.
	 * The default is <code>null</code>.
	 */
	public Boolean hasColumnNamesInFirstRow() {
		return columnNamesInFirstRow;
	}
	
	/**
	 * Sets the delimiter between entries. <code>null</code> means unknown.
	 */
	public void setDelimiter(Character delimiter) {
		this.delimiter = delimiter;
	}
	
	/**
	 * Gets the delimiter between entries. <code>null</code> means unknown.
	 */
	public Character getDelimiter() {
		return delimiter;
	}
	
	/**
	 * Sets the quote character used in the file. <code>null</code> means unknown.
	 */
	public void setQuoteChar(Character quote) {
		this.quote = quote;
	}
	
	/**
	 * Gets the quote character used in the file. <code>null</code> means unknown.
	 */
	public Character getQuoteChar() {
		return quote;
	}
	
	/**
	 * Sets the escape character used in the file to escape quotes.
	 * <code>null</code> means no escape character, and quotes inside quoted
	 * values are escaped as two quote characters.
	 */
	public void setEscapeChar(Character escape) {
		this.escape = escape;
	}
	
	/**
	 * Gets the escape character used in the file to escape quotes.
	 * <code>null</code> means no escape character, and quotes inside quoted
	 * values are escaped as two quote characters.
	 */
	public Character getEscapeChar() {
		return escape;
	}
	
	/**
	 * Creates a new {@link CSVParser} for a given {@link InputStreamSource}
	 * with the options of this instance.
	 */
	public CSVParser openParserFor(InputStreamSource source) throws IOException {
		return new CSVParser(openReaderFor(source), 
				columnNamesInFirstRow == null ? false : columnNamesInFirstRow,
				delimiter, quote, escape);
	}
	
	/**
	 * Creates a new {@link Reader} for a given {@link InputStreamSource}
	 * with the options of this instance.
	 */
	public Reader openReaderFor(InputStreamSource source) throws IOException {
		if (encoding == null) {
			return new CharsetDetectingReader(source.open());
		}
		return new InputStreamReader(source.open(), encoding);
	}
}
