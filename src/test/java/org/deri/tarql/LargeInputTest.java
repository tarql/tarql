package org.deri.tarql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.Iterator;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.ResultSet;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.ARQException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Test cases on large input. Not run as part of the normal
 * test suite, but can be enabled manually. The main purpose
 * is to ensure that processing is done in a streaming fashion.
 * Run with low memory settings, e.g., -Xmx2M.
 */
public class LargeInputTest {

	private final static String DUMMY_CONTENT = 
			"Lorem ipsum dolor sit amet. Consectetur adipiscing elit. Ut nec eros vel odio viverra fusce.";

	private final static String EX = "http://example.com/ns";

	private final static String CONSTRUCT_2TRIPLES =
			"PREFIX ex: <http://example.com/ns#>\n" +
			"CONSTRUCT { ?iri ex:line ?line; ex:content ?content. }\n" +
			"{ BIND (IRI(CONCAT(STR(ex:line), STR(?ROWNUM))) AS ?iri) }";

	protected PrefixMapping prefixes;

	@Before
	public void setUp() {
		PrefixMapping prefixes = new PrefixMappingImpl();
		prefixes.setNsPrefix("ex", EX + "#");
	}

	protected int consume(Iterator<?> it) {
		System.out.println("Consuming results...");
		int i = 0;
		while (it.hasNext()) {
			i++;
//			System.out.println(it.next());
			it.next();
			if (i % 1000 == 0) System.out.println("  Result # " + i);
		}
		System.out.println("Done: " + i + " results");
		return i;
	}
	
	protected TarqlQueryExecution prepare(String query, CSVOptions options, 
			InputStreamSource input) {
		TarqlQuery tq = new TarqlParser(new StringReader(query)).getResult();
		return TarqlQueryExecutionFactory.create(tq, input, options);
	}

	protected TarqlQueryExecution prepare(String query, InputStreamSource input) {
		return prepare(query, null, input);
	}
	
	protected class DummyContentSource extends InputStreamSource {
		private final int totalLines;
		public DummyContentSource(int totalLines) {
			this.totalLines = totalLines;
		}
		@Override public InputStream open() throws IOException {
			return new ContentProducer(totalLines) {
				@Override public String generateLine(int lineNumber) {
					if (lineNumber % 1000 == 0) System.out.println("  Input  # " + lineNumber);
					if (lineNumber == 1) {
						return "line,content";
					}
					return generateNonHeaderLine(lineNumber);
				}
			}.getInputStream();
		}
		public String generateNonHeaderLine(int lineNumber) {
			return "Line " + lineNumber + "," + DUMMY_CONTENT;
		}
	}
	
	protected class DummyOutputStream extends OutputStream {
		private long bytesWritten = 0;
		@Override
		public void write(int b) throws IOException {
			bytesWritten++;
			if (bytesWritten % 1000000 == 0) {
				System.out.println("  Output " + bytesWritten / 1000000 + "M");
			}
		}
		public long getBytesWritten() {
			return bytesWritten;
		}
	}
	
	@Ignore @Test public void testInput5GB() {
		System.out.println("testInput5GB");
		final int lines = 50000000;
		String query = "SELECT * {}";
		ResultSet rs = prepare(query, new DummyContentSource(lines)).execSelect();
		int results = consume(rs);
		assertEquals(lines - 1, results);
	}

	@Ignore @Test public void testOutput100Mt() throws IOException {
		System.out.println("testOutput100Mt");
		final int lines = 50000000;
		Iterator<Triple> triples = prepare(CONSTRUCT_2TRIPLES, new DummyContentSource(lines)).execTriples();
		int results = consume(triples);
		assertEquals((lines - 1) * 2, results);
	}

	@Ignore @Test public void testOutput100MtTurtle() throws IOException {
		System.out.println("testOutput100MtTurtle");
		final int lines = 50000000;
		Iterator<Triple> triples = prepare(CONSTRUCT_2TRIPLES, new DummyContentSource(lines)).execTriples();
		DummyOutputStream out = new DummyOutputStream();
		new StreamingRDFWriter(out, triples).writeTurtle(EX, prefixes);
		System.out.println("Done: " + out.getBytesWritten() + " bytes written");
		assertTrue(out.getBytesWritten() > lines * 100);
	}

	@Ignore @Test public void testOutput100MtNTriples() throws IOException {
		System.out.println("testOutput100MtNTriples");
		final int lines = 50000000;
		Iterator<Triple> triples = prepare(CONSTRUCT_2TRIPLES, new DummyContentSource(lines)).execTriples();
		DummyOutputStream out = new DummyOutputStream();
		new StreamingRDFWriter(out, triples).writeNTriples();
		System.out.println("Done: " + out.getBytesWritten() + " bytes written");
		assertTrue(out.getBytesWritten() > lines * 100);
	}
	
	@Ignore("This doesn't work with OpenCSV 3.8, but works with cygri's fork on GitHub")
	@Test public void testSmallInputWithRunawayQuote() {
		System.out.println("testInput5GBWithRunawayQuote");
		final int lines = 20000;
		String query = "SELECT * {}";
		ResultSet rs = prepare(query, new DummyContentSource(lines) {
			@Override
			public String generateNonHeaderLine(int lineNumber) {
				if (lineNumber == 11111) {
					return super.generateNonHeaderLine(lineNumber) + "\"";
				}
				return super.generateNonHeaderLine(lineNumber);
			}
		}).execSelect();
		try {
			consume(rs);
			fail("Should have thrown ARQException due to runaway quote");
		} catch (ARQException ex) {
			if (!ex.getMessage().contains("stray quote")) {
				throw ex;
			}
		}
	}
	
	@Ignore("This doesn't work with OpenCSV 3.8, but works with cygri's fork on GitHub")
	@Test public void testInput5GBWithRunawayQuote() {
		System.out.println("testInput5GBWithRunawayQuote");
		final int lines = 50000000;
		String query = "SELECT * {}";
		ResultSet rs = prepare(query, new DummyContentSource(lines) {
			@Override
			public String generateNonHeaderLine(int lineNumber) {
				if (lineNumber == 11111) {
					return super.generateNonHeaderLine(lineNumber) + "\"";
				}
				return super.generateNonHeaderLine(lineNumber);
			}
		}).execSelect();
		try {
			consume(rs);
			fail("Should have thrown ARQException due to runaway quote");
		} catch (ARQException ex) {
			if (!ex.getMessage().contains("stray quote")) {
				throw ex;
			}
		}
	}
	
	@Test public void testInput5GBWithRunawayQuoteButNoQuoteChar() {
		System.out.println("testInput5GBWithRunawayQuoteButNoQuoteChar");
		final int lines = 50000000;
		String query = "SELECT * {}";
		CSVOptions options = new CSVOptions();
		options.setQuoteChar(null);
		ResultSet rs = prepare(query, options, new DummyContentSource(lines) {
			@Override
			public String generateNonHeaderLine(int lineNumber) {
				if (lineNumber == 11111) {
					return super.generateNonHeaderLine(lineNumber) + "\"";
				}
				return super.generateNonHeaderLine(lineNumber);
			}
		}).execSelect();
		int results = consume(rs);
		assertEquals(lines - 1, results);
	}

	@Ignore("This breaks streaming as of v1.1-SNAPSHOT")
	@Test public void testDodgyVALUES() {
		System.out.println("testInput5GB");
		final int lines = 50000000;
		String query = "SELECT * { VALUES ?undef { UNDEF } }";
		ResultSet rs = prepare(query, new DummyContentSource(lines)).execSelect();
		int results = consume(rs);
		assertEquals(lines - 1, results);
	}
}
