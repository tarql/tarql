package org.deri.tarql.functions;

import static com.hp.hpl.jena.graph.NodeFactory.createURI;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.VariableNotBoundException;
import com.hp.hpl.jena.vocabulary.DC;

public class ExpandPrefixedNameTest extends FunctionTest {
	
	@Test
	public void testSuccess() {
		assertEval(createURI(ExpandPrefixedNameFunction.IRI), "tarql:expandPrefixedName('tarql:expandPrefixedName')");
	}
	
	@Test
	public void testSuccess2() {
		prefixes.setNsPrefix("dc", DC.NS);
		assertEval(createURI(DC.NS + "title"), "tarql:expandPrefixedName('dc:title')");
	}

	@Test
	public void testUnboundArg() {
		try {
			eval("tarql:expandPrefixedName(?unbound)");
			fail();
		} catch (VariableNotBoundException ex) {}
	}

	@Test
	public void testNonStringArg() {
		try {
			eval("tarql:expandPrefixedName(true)");
			fail();
		} catch (ExprEvalException ex) {}
	}
	
	@Test
	public void testUndefinedPrefix() {
		try {
			eval("tarql:expandPrefixedName('dc')");
			fail();
		} catch (ExprEvalException ex) {}
	}
	
	@Test
	public void testNotAPrefixedName1() {
		try {
			eval("tarql:expandPrefixedName('')");
			fail();
		} catch (ExprEvalException ex) {}
	}
	
	@Test
	public void testNotAPrefixedName2() {
		try {
			eval("tarql:expandPrefixedName(':')");
			fail();
		} catch (ExprEvalException ex) {}
	}
	
	@Test
	public void testNotAPrefixedName3() {
		try {
			eval("tarql:expandPrefixedName(':a a')");
			fail();
		} catch (ExprEvalException ex) {}
	}
}
