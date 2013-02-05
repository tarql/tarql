package org.deri.tarql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.mozilla.intl.chardet.nsDetector;
import org.mozilla.intl.chardet.nsICharsetDetectionObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A {@link Reader} that wraps an {@link InputStream} and automatically takes
 * care of guessing the input stream's encoding, using the jchardet library.
 * Guessing is done on the fly without rewinding.
 */
public class CharsetDetectingReader extends Reader {
	private final static Logger log = LoggerFactory.getLogger(CharsetDetectingReader.class);
	
	private final static int DEFAULT_BUFFER_SIZE = 1024;
	private final static int EOF = -1;
	
	private final InputStream in;
	private final byte[] buffer;
	private final nsDetector detector = new nsDetector();
	private Reader reader = null;
	private boolean encodingDetected = false;
	private String detectedEncoding = null;
	private String guessedEncoding = null;
	
	public CharsetDetectingReader(InputStream in) {
		this(in, DEFAULT_BUFFER_SIZE);
	}

	public CharsetDetectingReader(InputStream in, int bufferSize) {
		if (in == null) throw new NullPointerException();
		this.in = in;
		buffer = new byte[bufferSize];
		detector.Init(new nsICharsetDetectionObserver() {
			public void Notify(String encoding) {
				log.debug("Encoding detected: {}", encoding);
				detectedEncoding = encoding;
			}
		});
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		if (len == 0 || cbuf.length == 0) return 0;
		int charsReadTotal = 0;
		while (len > 0) {
			if (reader == null && !fillBuffer()) {
				return charsReadTotal > 0 ? charsReadTotal : EOF;
			}
			int charsRead = reader.read(cbuf, off, len);
			if (charsRead == -1) {
				reader = null;
				continue;
			}
			off += charsRead;
			len -= charsRead;
			charsReadTotal += charsRead;
		}
		return charsReadTotal;
	}
	
	@Override
	public void close() throws IOException {
		in.close();
	}
	
	private boolean fillBuffer() throws IOException {
		int bytesRead = in.read(buffer);
		if (bytesRead == EOF) {
			detector.DataEnd();
			return false;
		}
		if (!encodingDetected) {
			// If it's all ASCII, then just proceed
			if (!detector.isAscii(buffer, bytesRead)) {
				encodingDetected = detector.DoIt(buffer, bytesRead, false);
				if (!encodingDetected) {
					// Best guess up to here. Might be revised next block.
					String[] guesses = detector.getProbableCharsets();
					guessedEncoding = guesses.length > 0 ? guesses[0] : null;
					if (guessedEncoding != null) {
						log.debug("Temporary encoding guess: {}", guessedEncoding);
					}
				}
			}
		}
		InputStream in = new ByteArrayInputStream(buffer, 0, bytesRead);
		try {
			reader = new InputStreamReader(in, 
					detectedEncoding == null
							? (guessedEncoding == null ? "US-ASCII" : guessedEncoding)
							: detectedEncoding);
		} catch (UnsupportedEncodingException ex) {
			// Fall back to US-ASCII
			detectedEncoding = "US-ASCII";
			reader = new InputStreamReader(in, detectedEncoding);
		}
		return true;
	}
}
