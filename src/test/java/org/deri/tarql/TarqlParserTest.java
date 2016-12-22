package org.deri.tarql;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;

import org.apache.jena.query.QueryParseException;
import org.junit.Test;


public class TarqlParserTest {

	@Test
	public void testSimpleSELECT() throws Exception {
		String s = "SELECT * {}";
		TarqlParser x = new TarqlParser(new StringReader(s));
		assertEquals(1, x.getResult().getQueries().size());
		assertTrue(x.getResult().getQueries().get(0).isSelectType());
	}
	
	@Test
	public void testSimpleCONSTRUCT() throws Exception {
		String s = "CONSTRUCT { [] a [] } WHERE {}";
		TarqlParser x = new TarqlParser(new StringReader(s));
		assertEquals(1, x.getResult().getQueries().size());
		assertTrue(x.getResult().getQueries().get(0).isConstructType());
	}
	
	@Test
	public void testMultipleSELECT() throws Exception {
		try {
			String s = "SELECT * {} SELECT * {}";
			TarqlParser x = new TarqlParser(new StringReader(s));
			x.getResult().getQueries();
			fail("Expected exception due to multiple queries");
		} catch (QueryParseException ex) {
			// Expected
		}
	}

	@Test
	public void testSELECTAndCONSTRUCT() throws Exception {
		try {
			String s = "CONSTRUCT { [] a [] } WHERE {} SELECT * {}";
			TarqlParser x = new TarqlParser(new StringReader(s));
			x.getResult().getQueries();
			fail("Expected exception due to multiple queries");
		} catch (QueryParseException ex) {
			// Expected
		}
	}

	@Test
	public void testMultipleCONSTRUCT() throws Exception {
		String s = "CONSTRUCT { [] a [] } WHERE {} CONSTRUCT { [] a [] } WHERE {}";
		TarqlParser x = new TarqlParser(new StringReader(s));
		assertEquals(2, x.getResult().getQueries().size());
		assertTrue(x.getResult().getQueries().get(0).isConstructType());
		assertTrue(x.getResult().getQueries().get(1).isConstructType());
	}

	@Test
	public void testIncrementalPrologue() {
		String s =
				"PREFIX a: <http://example.com/a#> CONSTRUCT { [] a:a [] } WHERE {}\n" +
				"PREFIX b: <http://example.com/b#> CONSTRUCT { [] a:a [] } WHERE {}\n" +
				"PREFIX c: <http://example.com/c#> CONSTRUCT { [] b:b [] } WHERE {}\n";
		TarqlParser p = new TarqlParser(new StringReader(s));
		assertEquals(3, p.getResult().getQueries().size());
		assertEquals(3, p.getResult().getPrologue().getPrefixMapping().getNsPrefixMap().size());
	}
}