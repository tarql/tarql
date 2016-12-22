package org.deri.tarql.functions;

import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.NodeFactory;
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
import org.deri.tarql.TarqlQuery;
import org.deri.tarql.tarql;


/**
 * The function tarql:expandPrefixedName(?name). Expands a prefixed
 * name, such as dc:title, using any prefixes defined in the
 * query. The result is an IRI.
 */
public class ExpandPrefixedNameFunction implements Function {

	public static String IRI = tarql.NS + "expandPrefixedName";
	public static String Name = "tarql:expandPrefixedName";

	static {
		TarqlQuery.registerFunctions();
	}
	
	public ExpandPrefixedNameFunction() {
		super();
	}
	
	public NodeValue exec(NodeValue name, Context context) {
		if (name == null) return null;
		if (!name.isString()) throw new ExprEvalException("Not a string: " + name);
		PrefixMapping prefixes = context.get(ExpandPrefixFunction.PREFIX_MAPPING);
		if (prefixes == null) throw new ExprEvalException("No prefix mapping registered");
		String pname = name.asString();
		int idx = pname.indexOf(':');
		if (idx == -1) throw new ExprEvalException("Not a prefixed name: " + name);
		String prefix = pname.substring(0, idx);
		String iri = prefixes.getNsPrefixURI(prefix);
		if (iri == null) throw new ExprEvalException("Prefix not defined: " + prefix);
		return NodeValue.makeNode(NodeFactory.createURI(iri + pname.substring(idx + 1)));
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
