package org.deri.tarql;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

/**
 * Extracts a CSVOptions object from the fragment part of a
 * CSV file URL, e.g.,
 * <code>http://example.com/file.csv#encoding=utf-8;header=absent</code>
 * The remainder of the URL, with anything interpretable
 * removed, can also be obtained from the returned
 * parse result.
 */
public class URLOptionsParser {
	private final static String encodingKey = "encoding=";
	private final static String charsetKey = "charset=";
	private final static String headerKey = "header=";
	private final static String delimiterKey = "delimiter=";
	private final static String quoteCharKey = "quotechar=";
	private final static String escapeCharKey = "escapechar=";

	private final StringBuilder remainingURL = new StringBuilder();
	private final CSVOptions options = new CSVOptions();
	private Boolean isTSV = null;

	public URLOptionsParser(String url) {
		parseURL(url);
	}

	public String getRemainingURL() {
		return remainingURL.toString();
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

	public void parseURL(String url) {
		int hash = url.indexOf("#");
		if (hash == -1) {
			parseFileName(url);
			setDefaults();
		} else {
			parseFileName(url.substring(0, hash));
			String[] fragmentParts = url.substring(hash + 1).split(";");
			checkForTSV(fragmentParts);
			setDefaults();
			parseFragmentParts(fragmentParts);
		}
	}	

	private void parseFileName(String fileName) {
		if (fileName.toLowerCase().endsWith(".csv")) {
			isTSV = false;
		} else if (fileName.toLowerCase().endsWith(".tsv")) {
			isTSV = true;
		}
		remainingURL.append(fileName);
	}
	
	private void checkForTSV(String[] fragmentParts) {
		for (String part: fragmentParts) {
			if (!part.startsWith(delimiterKey)) continue;
			try {
				Character delim = parseChar(part.substring(delimiterKey.length()));
				if (delim != null && delim == '\t') {
					isTSV = true;
				}
			} catch (IllegalArgumentException ex) {
				// Bad delimiter -- will be handled properly elsewhere
			}
		}
	}
	
	private void setDefaults() {
		if (isTSV != null && isTSV == true) {
			options.setDefaultsForTSV();
		} else {
			options.setDefaultsForCSV();
		}
	}
	
	private void parseFragmentParts(String[] parts) {
		boolean hasHash = false;
		for (String part: parts) {
			if (parseFragmentPart(part)) {
				// Special part -- remove from URL
				continue;
			}
			remainingURL.append(hasHash ? ";" : "#");
			hasHash = true;
			remainingURL.append(part);
		}
	}
	
	private boolean parseFragmentPart(String part) {
		if (part.startsWith(encodingKey)) {
			options.setEncoding(part.substring(encodingKey.length()));
			return true;
		}
		if (part.startsWith(charsetKey)) {
			options.setEncoding(part.substring(charsetKey.length()));
			return true;
		}
		if (part.startsWith(headerKey)) {
			String value = part.substring(headerKey.length());
			if ("present".equals(value)) {
				options.setColumnNamesInFirstRow(true);
				return true;
			}
			if ("absent".equals(value)) {
				options.setColumnNamesInFirstRow(false);
				return true;
			}
		}
		try {
			if (part.startsWith(delimiterKey)) {
				Character c = parseChar(part.substring(delimiterKey.length()));
				if (c != null) {
					options.setDelimiter(c);
					return true;
				}
			}
			if (part.startsWith(quoteCharKey)) {
				Character c = parseChar(part.substring(quoteCharKey.length()));
				options.setQuoteChar(c);
				return true;
			}
			if (part.startsWith(escapeCharKey)) {
				Character c = parseChar(part.substring(escapeCharKey.length()));
				options.setEscapeChar(c);
				return true;
			}
		} catch (IllegalArgumentException ex) {
			// Not interpretable -- treat as part of remaining URL
		}
		return false;
	}
	
	/**
	 * Interprets argument as a character. Can be a literal single-character
	 * string, or a %-encoded character (e.g., %09 for tab), or one of the
	 * pre-defined named characters such as "tab", "backslash", etc.
	 * 
	 * @throw IllegalArgumentException on unknown multi-char string
	 */
	private static Character parseChar(String value) {
		if (CSVOptions.charNames.containsKey(value.toLowerCase())) {
			return CSVOptions.charNames.get(value.toLowerCase());
		}
		try {
			value = URLDecoder.decode(value, "utf-8");
			if (value.length() > 1) {
				throw new IllegalArgumentException("Must be single character: " + value);
			}
			return value.charAt(0);
		} catch (UnsupportedEncodingException ex) {
			// Can't happen, UTF-8 always supported
			return null;
		}
	}
}
