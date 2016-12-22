package org.deri.tarql.functions;

import static org.junit.Assert.fail;

import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.VariableNotBoundException;
import org.apache.jena.vocabulary.DC;
import org.deri.tarql.tarql;
import org.junit.Test;


public class ExpandPrefixTest extends FunctionTest {
	
	@Test
	public void testSuccess() {
		assertEval(stringNode(tarql.NS), "tarql:expandPrefix('tarql')");
	}
	
	@Test
	public void testSuccess2() {
		prefixes.setNsPrefix("dc", DC.NS);
		assertEval(stringNode(DC.NS), "tarql:expandPrefix('dc')");
	}

	@Test
	public void testUnboundArg() {
		try {
			eval("tarql:expandPrefix(?unbound)");
			fail();
		} catch (VariableNotBoundException ex) {}
	}

	@Test
	public void testNonStringArg() {
		try {
			eval("tarql:expandPrefix(true)");
			fail();
		} catch (ExprEvalException ex) {}
	}
	
	@Test
	public void testUndefinedPrefix() {
		try {
			eval("tarql:expandPrefix('dc')");
			fail();
		} catch (ExprEvalException ex) {}
	}
}
