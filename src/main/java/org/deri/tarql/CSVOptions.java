package org.deri.tarql;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;

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
				} else if ("absent".equals(value)) {
					result.setColumnNamesInFirstRow(false);
					continue;
				}
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
	 * Creates a new {@link CSVParser} for a given {@link InputStreamSource}
	 * with the options of this instance.
	 */
	public CSVParser openParserFor(InputStreamSource source) throws IOException {
		return new CSVParser(openReaderFor(source), 
				columnNamesInFirstRow == null ? false : columnNamesInFirstRow);
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
