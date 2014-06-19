package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.vars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
	public void setUp(){
		csv = null;
	}
	
	private void assertSelect(TarqlQuery tq, Binding... bindings){
		
		TarqlQueryExecution ex;
		if(csv != null){
			ex = TarqlQueryExecutionFactory.create(new StringReader(csv), tq);
		} else{
			ex = TarqlQueryExecutionFactory.create(tq);
		}
		ResultSet rs = ex.execSelect();
		int counter = 0;
		while(rs.hasNext()){
			assertEquals(bindings[counter], rs.nextBinding());
			counter += 1;
		}
		assertEquals(counter, bindings.length);
	}
	
	private void assertConstruct(TarqlQuery tq, String expectedTTL){
		Model expected = ModelFactory.createDefaultModel().read(new StringReader(expectedTTL), null, "TURTLE");
		TarqlQueryExecution ex = TarqlQueryExecutionFactory.create(new StringReader(csv), tq);
		Model actual = ex.exec();
		assertTrue(actual.isIsomorphicWith(expected));
	}
	
	@Test
	public void testSimpleSelect() {
		csv = "Alice,Smith\nBob,Cook";
		String query = "SELECT * {}";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""), binding(vars, "\"Bob\"", "\"Cook\""));
	}
	
	@Test
	public void testSkipFirstRows() {
		csv = "First,Last\nAlice,Smith\nBob,Cook";
		String query = "SELECT * {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("First", "Last");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""), binding(vars, "\"Bob\"", "\"Cook\""));
	}
	
	@Test
	public void testSelectWithFilter() {
		csv = "Alice,Smith\nBob,Cook";
		String query = "SELECT * { FILTER(?b=\"Smith\") }";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""));
	}
	
	@Test
	public void testConstruct() {
		csv = "Alice,Smith";
		String query = "PREFIX ex: <http://example.com/> CONSTRUCT { _:x ex:first ?a; ex:last ?b } {}";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		String ttl = "@prefix ex: <http://example.com/>. _:x ex:first \"Alice\"; ex:last \"Smith\".";
		assertConstruct(tq, ttl);
	}
	
	@Test
	public void testBindConstant() {
		csv = "x";
		String query = "SELECT * { BIND (\"y\" AS ?b) }";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"x\"", "\"y\""));
	}
	
	@Test
	public void testBindData() {
		csv = "x";
		String query = "SELECT * { BIND (?a AS ?b) }";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("a", "b");
		assertSelect(tq, binding(vars, "\"x\"", "\"x\""));
	}
	
	@Test
	public void testFromClause() {
		String query = "SELECT * FROM <src/test/resources/simple.csv> {}";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		assertSelect(tq, binding(vars("a"), "\"x\""));
	}
	
	@Test
	public void testNoInputFile() {
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
	public void testMultipleInputFiles() {
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
	public void testVarNamesFromHeaders() {
		csv = "First,Last\nAlice,Smith";
		String query = "SELECT * {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("First", "Last");
		assertSelect(tq, binding(vars, "\"Alice\"", "\"Smith\""));
	}
	
	@Test
	public void testMultipleQueries() {
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
	public void testFROMisRelativeToMappingLocation1() {
		String file = "src/test/resources/mappings/simple-with-from.sparql";
		TarqlQuery tq = new TarqlParser(file).getResult();
		assertSelect(tq, binding(vars("a"), "\"x\""));
	}
	
	@Test
	public void testFROMisRelativeToMappingLocation2() {
		String file = "src/test/resources/mappings/simple-with-base.sparql";
		TarqlQuery tq = new TarqlParser(file).getResult();
		assertSelect(tq, binding(vars("a", "base"), "\"x\"", "<http://example.com/>"));
	}
	
	@Test
	public void testROWNUM() {
		csv = "First,Last\nAlice,Smith\nBob,Miller";
		String query = "SELECT ?ROWNUM ?First ?Last {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		List<Var> vars = vars("ROWNUM", "First", "Last");
		assertSelect(tq, binding(vars, "1", "\"Alice\"", "\"Smith\""), binding(vars, "2", "\"Bob\"", "\"Miller\""));
	}
	
	@Test
	public void testAvoidNameClashWithROWNUM() {
		csv = "ROWNUM\nfoo";
		String query = "SELECT ?ROWNUM ?a {} OFFSET 1";
		TarqlQuery tq =  new TarqlParser(new StringReader(query), null).getResult();
		assertSelect(tq, binding(vars("ROWNUM", "a"), "1", "\"foo\""));
	}
}
