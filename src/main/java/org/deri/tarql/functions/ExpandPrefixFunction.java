package org.deri.tarql.functions;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.query.QueryBuildException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.sparql.ARQInternalErrorException;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.ExprEvalException;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.function.Function;
import org.apache.jena.sparql.function.FunctionEnv;
import org.apache.jena.sparql.util.Context;
import org.apache.jena.sparql.util.Symbol;
import org.deri.tarql.TarqlQuery;
import org.deri.tarql.tarql;


/**
 * The function tarql:expandPrefix(?prefix). Expands a prefix
 * defined in the query to its associated namespace URI.
 */
public class ExpandPrefixFunction implements Function {

	public static String IRI = tarql.NS + "expandPrefix";
	public static String NAME = "tarql:expandPrefix";
	
	public static final Symbol PREFIX_MAPPING = Symbol.create("prefixMapping");
	
	static {
		TarqlQuery.registerFunctions();
	}
	
	public ExpandPrefixFunction() {
		super();
	}

	public NodeValue exec(NodeValue prefix, Context context) {
		if (prefix == null) {
			return null;
		}
		if (!prefix.isString()) {
			throw new ExprEvalException(NAME + ": not a string: " + prefix);
		}
		PrefixMapping prefixes = context.get(PREFIX_MAPPING);
		if (prefixes == null) {
			throw new ExprEvalException(NAME + ": no prefix mapping registered");
		}
		String iri = prefixes.getNsPrefixURI(prefix.asString());
		if (iri == null) {
			throw new ExprEvalException(NAME + ": prefix not defined: " + prefix);
		}
		return NodeValue.makeString(iri);
	}

	@Override
	public NodeValue exec(Binding binding, ExprList args, String uri, FunctionEnv env) {
		if (args == null) {
			throw new ARQInternalErrorException("ExpandPrefixFunction: Null args list");
		}
		if (args.size() != 1) {
			throw new ExprEvalException("ExpandPrefixFunction: Wrong number of arguments: Wanted 1, got " + args.size());
		}
        return exec(args.get(0).eval(binding, env), env.getContext());
	}

	@Override
	public void build(String uri, ExprList args) {
		if (args.size() != 1) {
			throw new QueryBuildException("Function '" + Lib.className(this) + "' takes one argument");
		}
	}
}
