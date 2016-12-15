package org.deri.tarql.functions;

import org.deri.tarql.TarqlQuery;
import org.deri.tarql.tarql;

import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;
import com.hp.hpl.jena.sparql.util.Symbol;

/**
 * The function tarql:expandPrefix(?prefix). Expands a prefix
 * defined in the query to its associated namespace URI.
 */
public class ExpandPrefixFunction extends FunctionBase1 {

	public static String IRI = tarql.NS + "expandPrefix";
	public static String NAME = "tarql:expandPrefix";
	
	public static final Symbol PREFIX_MAPPING = Symbol.create("prefixMapping");
	
	static {
		TarqlQuery.registerFunctions();
	}
	
	public ExpandPrefixFunction() {
		super();
	}
	
	@Override
	public NodeValue exec(NodeValue prefix) {
		if (prefix == null) return null;
		if (!prefix.isString()) throw new ExprEvalException(NAME + ": not a string: " + prefix);
		PrefixMapping prefixes = (PrefixMapping) getContext().get(PREFIX_MAPPING);
		if (prefixes == null) throw new ExprEvalException(NAME + ": no prefix mapping registered");
		String iri = prefixes.getNsPrefixURI(prefix.asString());
		if (iri == null) throw new ExprEvalException(NAME + ": prefix not defined: " + prefix);
		return NodeValue.makeString(iri);
	}
}
