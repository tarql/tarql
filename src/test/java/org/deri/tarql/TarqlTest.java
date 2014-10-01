package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.vars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;

public class TarqlTest {

	//fixture
	String csv;
	
	@Before
	public void setUp() {
		csv = null;
	}
	
	private void assertSelect(TarqlQuery tq, Binding... bindings) throws IOException{
		TarqlQueryExecution ex;
		if (csv == null) {
			ex = TarqlQueryExecutionFactory.create(tq);
		} else {
			ex = TarqlQueryExecutionFactory.create(tq, InputStreamSource.fromBytes(csv.getBytes("utf-8")), null);
		}
		ResultSet rs = ex.execSelect();
		int counter = 0;
		while (rs.hasNext()) {
			assertEquals(bindings[counter], rs.nextBinding());
			counter += 1;
		}
		assertEquals(bindings.length, counter);
	}
	
	private void assertConstruct(TarqlQuery tq, String expectedTTL) throws IOException {
		Model expected = ModelFactory.createDefaultModel().read(new StringReader(expectedTTL), null, "TURTLE");
		TarqlQueryExecution ex = TarqlQueryExecutionFactory.create(tq, InputStreamSource.fromBytes(csv.getBytes("utf-8")), null);
		Model actual = ModelFactory.createDefaultModel();
		ex.exec(actual);
		assertTrue(actual.isIsomorphicWith(expected));
	}
	
	@Test
	public void testSimpleSelect() throws IOException {
		csv = "Alice,Smith\nBob,Cook";
		String query = "SELECT * {}";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""), binding(vars, "\"Bob\"", "\"Cook\""));
	}
	
	@Test
	public void testSkipFirstRows() throws IOException {
		csv = "First,Last\nAlice,Smith\nBob,Cook";
		String query = "SELECT * {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("First", "Last");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""), binding(vars, "\"Bob\"", "\"Cook\""));
	}
	
	@Test
	public void testSelectWithFilter() throws IOException {
		csv = "Alice,Smith\nBob,Cook";
		String query = "SELECT * { FILTER(?b=\"Smith\") }";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""));
	}
	
	@Test
	public void testConstruct() throws IOException {
		csv = "Alice,Smith";
		String query = "PREFIX ex: <http://example.com/> CONSTRUCT { _:x ex:first ?a; ex:last ?b } {}";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		String ttl = "@prefix ex: <http://example.com/>. _:x ex:first \"Alice\"; ex:last \"Smith\".";
		assertConstruct(tq, ttl);
	}
	
	@Test
	public void testBindConstant() throws IOException {
		csv = "x";
		String query = "SELECT * { BIND (\"y\" AS ?b) }";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"x\"", "\"y\""));
	}
	
	@Test
	public void testBindData() throws IOException {
		csv = "x";
		String query = "SELECT * { BIND (?a AS ?b) }";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"x\"", "\"x\""));
	}
	
	@Test
	public void testFromClause() throws IOException {
		String query = "SELECT * FROM <src/test/resources/simple.csv> {}";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		assertSelect(tq, binding(vars("a"), "\"x\""));
	}
	
	@Test
	public void testNoInputFile() throws IOException {
		String query = "SELECT * {}";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		try {
			assertSelect(tq);
			fail("Expected exception due to lacking input file in query");
		} catch (JenaException ex) {
			// Expected
		}
	}
	
	@Test
	public void testMultipleInputFiles() throws IOException {
		String query = "SELECT * FROM <x> <y> {}";
		try {
			TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
			assertSelect(tq);
			fail("Expected exception due to multiple input files in query");
		} catch (JenaException ex) {
			// Expected
		}
	}
	
	@Test
	public void testVarNamesFromHeaders() throws IOException {
		csv = "First,Last\nAlice,Smith";
		String query = "SELECT * {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("First", "Last");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""));
	}
	
	@Test
	public void testMultipleQueries() throws IOException {
		csv = "Alice,Smith";
		String query = 
				"PREFIX ex: <http://example.com/>\n" +
				"CONSTRUCT { _:x ex:first ?a } {}\n" +
				"CONSTRUCT { _:x ex:last ?b } {}\n";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		String ttl = "@prefix ex: <http://example.com/>. _:x ex:first \"Alice\". _:y ex:last \"Smith\".";
		assertConstruct(tq, ttl);
	}
	
	@Test
	public void testFROMisRelativeToMappingLocation1() throws IOException {
		String file = "src/test/resources/mappings/simple-with-from.sparql";
		TarqlQuery tq = new TarqlParser(file).getResult();
		assertSelect(tq, binding(vars("a"), "\"x\""));
	}
	
	/**
	 * This isn't quite legal. An IRI of the form "file:relative/path/to/file" is
	 * not valid, even though it appears to work here. Jena correctly prints out
	 * a warning. We leave the test here for now, but this is not really a good
	 * idea. ("file:///absolute/path/to/file" is fine, as is "relative/path/to/file".) 
	 */
	@Test
	public void testFROMisRelativeToMappingLocation2() throws IOException {
		String file = "src/test/resources/mappings/simple-with-base.sparql";
		TarqlQuery tq = new TarqlParser(file).getResult();
		assertSelect(tq, binding(vars("a", "base"), "\"x\"", "<http://example.com/>"));
	}
	
	@Test
	public void testROWNUM() throws IOException {
		csv = "First,Last\nAlice,Smith\nBob,Miller";
		String query = "SELECT ?ROWNUM ?First ?Last {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("ROWNUM", "First", "Last");
		assertSelect(tq, binding(vars, "1", "\"Alice\"", "\"Smith\""), binding(vars, "2", "\"Bob\"", "\"Miller\""));
	}
	
	@Test
	public void testAvoidNameClashWithROWNUM() throws IOException {
		csv = "ROWNUM\nfoo";
		String query = "SELECT ?ROWNUM ?a {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		assertSelect(tq, binding(vars("ROWNUM", "a"), "1", "\"foo\""));
	}
	
	@Test
	public void testOptionsInURLFragmentInFROMClause() throws IOException {
		String query1 = "SELECT * FROM <src/test/resources/simple.csv#header=absent> {}";
		TarqlQuery tq1 = new TarqlParser(new StringReader(query1)).getResult();
		assertSelect(tq1, binding(vars("a"), "\"x\""));
		String query2 = "SELECT * FROM <src/test/resources/simple.csv#header=present> {}";
		TarqlQuery tq2 = new TarqlParser(new StringReader(query2)).getResult();
		assertSelect(tq2);
	}
}
