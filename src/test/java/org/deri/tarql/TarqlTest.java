package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.vars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.JenaException;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Before;
import org.junit.Test;



public class TarqlTest {

	//fixture
	private String csv;
	private CSVOptions options;
	
	@Before
	public void setUp() {
		csv = null;
		options = new CSVOptions();
		options.setColumnNamesInFirstRow(false);
	}
	
	private void assertSelect(TarqlQuery tq, Binding... bindings) throws IOException{
		TarqlQueryExecution ex;
		if (csv == null) {
			ex = TarqlQueryExecutionFactory.create(tq, options);
		} else {
			ex = TarqlQueryExecutionFactory.create(tq, InputStreamSource.fromBytes(csv.getBytes("utf-8")), options);
		}
		ResultSet rs = ex.execSelect();
		int counter = 0;
		while (rs.hasNext()) {
			if (counter >= bindings.length) {
				fail("Too many bindings in result; expected " + bindings.length);
			}
			assertEquals(bindings[counter], rs.nextBinding());
			counter += 1;
		}
		assertEquals(bindings.length, counter);
	}
	
	private void assertConstruct(TarqlQuery tq, String expectedTTL) throws IOException {
		Model expected = ModelFactory.createDefaultModel().read(new StringReader(expectedTTL), null, "TURTLE");
		TarqlQueryExecution ex = TarqlQueryExecutionFactory.create(tq, InputStreamSource.fromBytes(csv.getBytes("utf-8")), options);
		Model actual = ModelFactory.createDefaultModel();
		ex.exec(actual);
		if (!actual.isIsomorphicWith(expected)) {
			StringWriter out = new StringWriter();
			actual.write(out, "TURTLE");
			fail("Actual not isomorphic to input. Actual was:\n" + out.toString());
		}
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
		options = new CSVOptions();
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
		} catch (TarqlException ex) {
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
	public void testVarNamesFromHeadersViaOFFSET() throws IOException {
		options = new CSVOptions();
		csv = "First,Last\nAlice,Smith";
		String query = "SELECT * {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("First", "Last");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""));
	}
	
	@Test
	public void testVarNamesFromHeadersViaOFFSETCanBeOverridden() throws IOException {
		options = new CSVOptions();
		options.setColumnNamesInFirstRow(false);
		csv = "First,Last\nAlice,Smith";
		String query = "SELECT * {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
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
		options = new CSVOptions();
		csv = "First,Last\nAlice,Smith\nBob,Miller";
		String query = "SELECT ?ROWNUM ?First ?Last {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("ROWNUM", "First", "Last");
		assertSelect(tq, binding(vars, "1", "\"Alice\"", "\"Smith\""), binding(vars, "2", "\"Bob\"", "\"Miller\""));
	}
	
	@Test
	public void testConstructROWNUM() throws IOException {
		options = new CSVOptions();
		csv = "First,Last\nAlice,Smith\nBob,Miller";
		String query =
				"PREFIX ex: <http://example.com/>\n" +
				"CONSTRUCT { ?iri ex:first ?First; ex:last ?Last }\n" +
				"{ BIND (IRI(CONCAT(STR(ex:person), STR(?ROWNUM))) AS ?iri) }";
		TarqlQuery tq =  new TarqlParser(new StringReader(query)).getResult();
		String ttl =
				"@prefix ex: <http://example.com/>.\n" +
				"ex:person1 ex:first \"Alice\"; ex:last \"Smith\".\n" +
				"ex:person2 ex:first \"Bob\"; ex:last \"Miller\".\n";
		assertConstruct(tq, ttl);
	}
	
	@Test
	public void testAvoidNameClashWithROWNUM() throws IOException {
		options = new CSVOptions();
		csv = "ROWNUM\nfoo";
		String query = "SELECT ?ROWNUM ?a {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		assertSelect(tq, binding(vars("ROWNUM", "a"), "1", "\"foo\""));
	}
	
	@Test
	public void testOptionsInURLFragmentInFROMClause() throws IOException {
		options = new CSVOptions();
		String query1 = "SELECT * FROM <src/test/resources/simple.csv#header=absent> {}";
		TarqlQuery tq1 = new TarqlParser(new StringReader(query1)).getResult();
		assertSelect(tq1, binding(vars("a"), "\"x\""));
		String query2 = "SELECT * FROM <src/test/resources/simple.csv#header=present> {}";
		TarqlQuery tq2 = new TarqlParser(new StringReader(query2)).getResult();
		assertSelect(tq2);
	}
	
	@Test
	public void testExpandPrefixFunctionSimple() throws IOException {
		csv = "x";
		String query = 
				"SELECT ?ns { BIND (tarql:expandPrefix('tarql') AS ?ns) }";
		TarqlQuery tq = new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("ns");
		assertSelect(tq, binding(vars, "\"" + tarql.NS + "\""));
	}
	
	@Test
	public void testExpandPrefixFunction() throws IOException {
		csv = "ex:foo\naaa:bbb";
		String query = 
				"PREFIX ex: <http://example.com/>\n" +
				"PREFIX aaa: <http://aaa.example.com/>\n" +
				"SELECT ?uri { BIND (URI(CONCAT(tarql:expandPrefix(STRBEFORE(?a, ':')), STRAFTER(?a, ':'))) AS ?uri) }";
		TarqlQuery tq = new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("uri");
		assertSelect(tq, binding(vars, "<http://example.com/foo>"), binding(vars, "<http://aaa.example.com/bbb>"));
	}
	
	
	@Test
	public void testBuiltInPrefixes() throws IOException {
		csv = "x";
		String query = "SELECT ?prefix ?ns { " +
				"VALUES (?prefix ?ns) { ('tarql' tarql:) ('apf' apf:) } }";
		TarqlQuery tq = new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("prefix", "ns");
		assertSelect(tq, 
				binding(vars, "'tarql'", "<http://tarql.github.io/tarql#>"), 
				binding(vars, "'apf'", "<http://jena.apache.org/ARQ/property#>"));
	}
}
