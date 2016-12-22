package org.deri.tarql.functions;

import static org.junit.Assert.assertEquals;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.function.FunctionEnvBase;
import org.apache.jena.sparql.util.ExprUtils;
import org.deri.tarql.tarql;
import org.junit.Before;


public abstract class FunctionTest {
	protected PrefixMapping prefixes;
	protected FunctionEnv env;
	
	@Before
	public void setUp() {
		prefixes = new PrefixMappingImpl();
		prefixes.setNsPrefix("tarql", tarql.NS);
		env = new FunctionEnvBase();
		env.getContext().set(ExpandPrefixFunction.PREFIX_MAPPING, prefixes);
	}

	protected void assertEval(Node expected, String expression) {
		assertEquals(expected, eval(expression));
	}
	
	protected Node eval(String expression) {
		Expr expr = ExprUtils.parse(expression, prefixes);
		return expr.eval(BindingFactory.root(), env).asNode();
	}

	protected Node stringNode(String s) {
		return NodeFactory.createLiteral(s);
	}
}
