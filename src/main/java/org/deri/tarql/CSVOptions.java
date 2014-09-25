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
		this.encoding = defaults.encoding;
		this.columnNamesInFirstRow = defaults.columnNamesInFirstRow;
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
