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
	
	public static CSVOptions withCSVDefaults() {
		CSVOptions result = new CSVOptions();
		result.setDefaultsForCSV();
		return result;
	}
	
	public static CSVOptions withTSVDefaults() {
		CSVOptions result = new CSVOptions();
		result.setDefaultsForTSV();
		return result;
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
	 * 
	 * @param defaults Another instance to take default values from
	 */
	public CSVOptions(CSVOptions defaults) {
		overrideWith(defaults);
	}

	/**
	 * Override values in this object with those from the other. Anything
	 * that is <code>null</code> in the other object will be ignored.
	 * 
	 * @param other The instance whose values to take over
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
	
	public void setDefaultsForCSV() {
		setDelimiter(',');
		setQuoteChar('"');
	}

	public void setDefaultsForTSV() {
		setDelimiter('\t');
		setQuoteChar(null);
	}
	
	/**
	 * Specify the CSV file's character encoding. <code>null</code>
	 * signifies unknown encoding, that is, auto-detection.
	 * The default is <code>null</code>.
	 * 
	 * @param encoding The character encoding to set for this value
	 */
	public void setEncoding(String encoding) {
		this.encoding = encoding;
	}
	
	/**
	 * Returns the CSV file's character encoding, or <code>null</code>
	 * if unknown.
	 * 
	 * @return This instance's value for the character encoding, or null if unknown
	 */
	public String getEncoding() {
		return encoding;
	}
	
	/**
	 * Set whether the CSV file's first row contains column names.
	 * <code>null</code> means unknown.
	 * The default is <code>null</code>.
	 * 
	 * @param value The value to set
	 */
	public void setColumnNamesInFirstRow(Boolean value) {
		this.columnNamesInFirstRow = value;
	}
	
	/**
	 * Set whether the CSV file's first row contains column names.
	 * <code>null</code> means unknown.
	 * The default is <code>null</code>.
	 * 
	 * @return This instance's value, or null if unknown
	 */
	public Boolean hasColumnNamesInFirstRow() {
		return columnNamesInFirstRow;
	}
	
	/**
	 * Sets the delimiter between entries. <code>null</code> means unknown.
	 * 
	 * @param delimiter The delimiter character for this instance
	 */
	public void setDelimiter(Character delimiter) {
		this.delimiter = delimiter;
	}
	
	/**
	 * Gets the delimiter between entries. <code>null</code> means unknown.
	 * 
	 * @return The delimiter character for this instance, or null if unknown
	 */
	public Character getDelimiter() {
		return delimiter;
	}
	
	/**
	 * Sets the quote character used in the file. <code>null</code> means no
	 * quote character, and there is no way to escape the delimiter by quoting.
	 * 
	 * @param quote The quote character for this instance, or null
	 */
	public void setQuoteChar(Character quote) {
		this.quote = quote;
	}
	
	/**
	 * Gets the quote character used in the file. <code>null</code> means no
	 * quote character, and there is no way to escape the delimiter by quoting.
	 * 
	 * @return The configured quote character, or null if none
	 */
	public Character getQuoteChar() {
		return quote;
	}
	
	/**
	 * Sets the escape character used in the file to escape quotes.
	 * <code>null</code> means no escape character, and quotes inside quoted
	 * values are escaped as two quote characters.
	 * 
	 * @param escape The escape character for this instance, or null
	 */
	public void setEscapeChar(Character escape) {
		this.escape = escape;
	}
	
	/**
	 * Gets the escape character used in the file to escape quotes.
	 * <code>null</code> means no escape character, and quotes inside quoted
	 * values are escaped as two quote characters.
	 * 
	 * @return The configured escape character, or null if none
	 */
	public Character getEscapeChar() {
		return escape;
	}
	
	/**
	 * Creates a new {@link CSVParser} for a given {@link InputStreamSource}
	 * with the options of this instance.
	 * 
	 * @param source The input to read from
	 * @return a parser for the source, configured with the options from this instance
	 * @throws IOException if an I/O error occurs while opening the source
	 */
	public CSVParser openParserFor(InputStreamSource source) throws IOException {
		return new CSVParser(openReaderFor(source), 
				columnNamesInFirstRow == null ? true : columnNamesInFirstRow,
				delimiter, quote, escape);
	}
	
	/**
	 * Creates a new {@link Reader} for a given {@link InputStreamSource}
	 * with the options of this instance.
	 * 
	 * @param source The input to read from
	 * @return a Reader over the source, using this instance's character encoding
	 * @throws IOException if an I/O error occurs while opening the source
	 */
	public Reader openReaderFor(InputStreamSource source) throws IOException {
		if (encoding == null) {
			return new CharsetDetectingReader(source.open());
		}
		return new InputStreamReader(source.open(), encoding);
	}
}
