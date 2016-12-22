package org.deri.tarql;

import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.apache.commons.io.input.ReaderInputStream;

/**
 * Abstract helper class for generating large test files.
 * Concrete implementations must provide a method for
 * generating a line of content.
 */
public abstract class ContentProducer {
	private final int totalLines;
	protected int deliveredLines = 0;

	/**
	 * @param totalLines The number of lines to generate
	 */
	public ContentProducer(int totalLines) {
		this.totalLines = totalLines;
	}

	/**
	 * Generates one line of content. The contract is to
	 * not include any line breaks. Will be called once
	 * for each line to be produced, starting at line 1.
	 */
	public abstract String generateLine(int lineNumber);
	
	private String nextLine() {
		if (deliveredLines >= totalLines) return null;
		deliveredLines++;
		return generateLine(deliveredLines) + "\n";
	}
	
	public Reader getReader() {
		return new Reader() {
			private String buffer = null;
			private int offset = 0;
			@Override
			public int read(char[] cbuf, int off, int len) {
				if (len == 0) return 0;
				if (buffer == null || offset >= buffer.length()) {
					buffer = nextLine();
					offset = 0;
					if (buffer == null) {
						return -1;	// End of input reached
					}
				}
				int charsToRead = Math.min(buffer.length(), len);
				buffer.getChars(offset, offset + charsToRead, cbuf, off);
				offset += charsToRead;
				return charsToRead;
			}
			@Override
			public void close() {
				buffer = null;
			}
		};
	}
	
	public InputStream getInputStream() {
		return new ReaderInputStream(getReader(), StandardCharsets.UTF_8);
	}
	
	public InputStream getInputStream(Charset encoding) {
		return new ReaderInputStream(getReader(), encoding);
	}
}
