package org.deri.tarql.functions;

import static org.junit.Assert.assertEquals;

import org.deri.tarql.tarql;
import org.junit.Before;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.shared.impl.PrefixMappingImpl;
import com.hp.hpl.jena.sparql.engine.binding.BindingFactory;
import com.hp.hpl.jena.sparql.expr.Expr;
import com.hp.hpl.jena.sparql.function.FunctionEnv;
import com.hp.hpl.jena.sparql.function.FunctionEnvBase;
import com.hp.hpl.jena.sparql.util.ExprUtils;

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
