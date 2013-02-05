package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.vars;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.List;

import org.junit.Test;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.core.Var;

public class TarqlTest {

	@Test
	public void testSimpleSelect() {
		String csv = "Alice,Smith\nBob,Cook";
		String query = "SELECT * {}";
		ResultSet rs = CSVQueryExecutionFactory.create(
				new StringReader(csv), query).execSelect();
		List<Var> vars = vars("a", "b");
		assertTrue(rs.hasNext());
		assertEquals(binding(vars, "\"Alice\"", "\"Smith\""), rs.nextBinding());
		assertTrue(rs.hasNext());
		assertEquals(binding(vars, "\"Bob\"", "\"Cook\""), rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testSkipFirstRow() {
		String csv = "First,Last\nAlice,Smith\nBob,Cook";
		String query = "SELECT * {} OFFSET 1";
		ResultSet rs = CSVQueryExecutionFactory.create(
				new StringReader(csv), query).execSelect();
		List<Var> vars = vars("a", "b");
		assertTrue(rs.hasNext());
		assertEquals(binding(vars, "\"Alice\"", "\"Smith\""), rs.nextBinding());
		assertTrue(rs.hasNext());
		assertEquals(binding(vars, "\"Bob\"", "\"Cook\""), rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testSelectWithFilter() {
		String csv = "Alice,Smith\nBob,Cook";
		String query = "SELECT * { FILTER(?b=\"Smith\") }";
		ResultSet rs = CSVQueryExecutionFactory.create(
				new StringReader(csv), query).execSelect();
		List<Var> vars = vars("a", "b");
		assertTrue(rs.hasNext());
		assertEquals(binding(vars, "\"Alice\"", "\"Smith\""), rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testConstruct() {
		String csv = "Alice,Smith";
		String query = "PREFIX ex: <http://example.com/> CONSTRUCT { _:x ex:first ?a; ex:last ?b } {}";
		String ttl = "@prefix ex: <http://example.com/>. _:x ex:first \"Alice\"; ex:last \"Smith\".";
		Model actual = CSVQueryExecutionFactory.create(new StringReader(csv), query).execConstruct();
		Model expected = ModelFactory.createDefaultModel().read(new StringReader(ttl), null, "TURTLE");
		assertTrue(actual.isIsomorphicWith(expected));
	}
	
	@Test
	public void testBindConstant() {
		String csv = "x";
		String query = "SELECT * { BIND (\"y\" AS ?b) }";
		ResultSet rs = CSVQueryExecutionFactory.create(
				new StringReader(csv), query).execSelect();
		List<Var> vars = vars("a", "b");
		assertTrue(rs.hasNext());
		assertEquals(binding(vars, "\"x\"", "\"y\""), rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testBindData() {
		String csv = "x";
		String query = "SELECT * { BIND (?a AS ?b) }";
		ResultSet rs = CSVQueryExecutionFactory.create(
				new StringReader(csv), query).execSelect();
		List<Var> vars = vars("a", "b");
		assertTrue(rs.hasNext());
		assertEquals(binding(vars, "\"x\"", "\"x\""), rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testFromClause() {
		String query = "SELECT * FROM <src/test/resources/simple.csv> {}";
		ResultSet rs = CSVQueryExecutionFactory.create(query).execSelect();
		assertTrue(rs.hasNext());
		assertEquals(binding(vars("a"), "\"x\""), rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testNoInputFile() {
		String query = "SELECT * {}";
		try {
			CSVQueryExecutionFactory.create(query);
			fail("Expected exception due to lacking input file in query");
		} catch (JenaException ex) {
			// Expected
		}
	}
	
	@Test
	public void testMultipleInputFiles() {
		String query = "SELECT * FROM <x> <y> {}";
		try {
			CSVQueryExecutionFactory.create(query);
			fail("Expected exception due to multiple input files in query");
		} catch (JenaException ex) {
			// Expected
		}
	}
	
	@Test
	public void testFromClauseStaysIntact() {
		String query = "SELECT * FROM <src/test/resources/simple.csv> {}";
		Query q = QueryFactory.create(query);
		String original = q.getGraphURIs().get(0);
		CSVQueryExecutionFactory.create(q).execSelect();
		assertEquals(1, q.getGraphURIs().size());
		String afterwards = q.getGraphURIs().get(0);
		assertEquals(original, afterwards);
	}
}
