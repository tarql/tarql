package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.bindings;
import static org.deri.tarql.Helpers.vars;

import java.util.Arrays;
import java.util.List;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.junit.Test;

import junit.framework.TestCase;


public class ARQTest extends TestCase {

	@Test
	public void testSPARQLFilterIsAppliedToValues() {
		Query q = QueryFactory.create(
				"SELECT * { FILTER(?a=1) VALUES (?a) { (1) (2) } }");
		ResultSet rs = QueryExecutionFactory.create(q, 
				ModelFactory.createDefaultModel()).execSelect();
		assertTrue(rs.hasNext());
		assertEquals(binding(vars("a"), "1"), rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testQuerySetValuesDataBlock() {
		List<Var> header = vars("a", "b");
		Binding b1 = binding(header, "1", "2");
		Binding b2 = binding(header, "3", "4");

		Query q = QueryFactory.create("SELECT * {}");
		q.setValuesDataBlock(header, bindings(b1, b2));
		ResultSet rs = QueryExecutionFactory.create(q, 
				ModelFactory.createDefaultModel()).execSelect();

		assertEquals(Arrays.asList(new String[]{"a","b"}), rs.getResultVars());
		assertTrue(rs.hasNext());
		assertEquals(b1, rs.nextBinding());
		assertTrue(rs.hasNext());
		assertEquals(b2, rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testQueryAddValuesInQueryPattern() {
		List<Var> header = vars("a", "b");
		Binding b1 = binding(header, "1", "2");
		Binding b2 = binding(header, "3", "4");

		Query q = QueryFactory.create("SELECT * {}");
		ElementGroup group = new ElementGroup();
		ElementData table = new ElementData();
		table.add(Var.alloc("a"));
		table.add(Var.alloc("b"));
		table.add(b1);
		table.add(b2);
		group.addElement(q.getQueryPattern());
		group.addElement(table);
		q.setQueryPattern(group);
		ResultSet rs = QueryExecutionFactory.create(q, 
				ModelFactory.createDefaultModel()).execSelect();

		assertEquals(Arrays.asList(new String[]{"a","b"}), rs.getResultVars());
		assertTrue(rs.hasNext());
		assertEquals(b1, rs.nextBinding());
		assertTrue(rs.hasNext());
		assertEquals(b2, rs.nextBinding());
		assertFalse(rs.hasNext());
	}
	
	@Test
	public void testDetectSelectStar() {
		Query selectStar = QueryFactory.create("SELECT * { ?s ?p ?o }");
		assertTrue(selectStar.isQueryResultStar());
		Query selectVars = QueryFactory.create("SELECT ?s ?p ?o { ?s ?p ?o }");
		assertFalse(selectVars.isQueryResultStar());
	}
}
