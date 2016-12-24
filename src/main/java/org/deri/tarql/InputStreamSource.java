package org.deri.tarql;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import org.apache.jena.shared.NotFoundException;
import org.apache.jena.util.FileManager;


/**
 * A source of {@link InputStream}s. Can be opened repeatedly
 * to provide input streams over some data.
 * <p>
 * Static methods are provided to create instances from various inputs.
 */
public abstract class InputStreamSource {

	public static InputStreamSource fromFilenameOrIRI(final String filenameOrIRI) {
		return fromFilenameOrIRI(filenameOrIRI, FileManager.get());
	}
	
	public static InputStreamSource fromFilenameOrIRI(final String filenameOrIRI, final FileManager fm) {
		return new InputStreamSource() {
			public InputStream open() throws IOException {
				InputStream in = fm.open(filenameOrIRI);
				if (in == null) {
					throw new NotFoundException(filenameOrIRI);
				}
				return in;
			}
		};
	}
	
	public static InputStreamSource fromBytes(final byte[] buffer) {
		return new InputStreamSource() {
			public InputStream open() throws IOException {
				return new ByteArrayInputStream(buffer);
			}
		};
	}

	public static InputStreamSource fromString(String data) {
		try {
			return fromBytes(data.getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Can't happen, UTF-8 is always supported");
		}
	}
	
	public static InputStreamSource fromStdin() {
		return new InputStreamSource() {
			boolean open = false;
			public InputStream open() throws IOException {
				if (open) {
					throw new TarqlException("Cannot use STDIN in mapping requiring multiple read passes");
				}
				open = true;
				return System.in;
			}
		};
	}
	
	/**
	 * Opens an input stream over the input data.
	 * 
	 * @return A fresh input stream over the input, set to the start of the input.
	 * @throws IOException if an I/O error occurs. 
	 */
	public abstract InputStream open() throws IOException; 
}
