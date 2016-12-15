package org.deri.tarql.functions;

import org.deri.tarql.TarqlQuery;
import org.deri.tarql.tarql;

import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.shared.PrefixMapping;
import com.hp.hpl.jena.sparql.expr.ExprEvalException;
import com.hp.hpl.jena.sparql.expr.NodeValue;
import com.hp.hpl.jena.sparql.function.FunctionBase1;

/**
 * The function tarql:expandPrefixedName(?name). Expands a prefixed
 * name, such as dc:title, using any prefixes defined in the
 * query. The result is an IRI.
 */
public class ExpandPrefixedNameFunction extends FunctionBase1 {

	public static String IRI = tarql.NS + "expandPrefixedName";
	public static String Name = "tarql:expandPrefixedName";

	static {
		TarqlQuery.registerFunctions();
	}
	
	public ExpandPrefixedNameFunction() {
		super();
	}
	
	@Override
	public NodeValue exec(NodeValue name) {
		if (name == null) return null;
		if (!name.isString()) throw new ExprEvalException("Not a string: " + name);
		PrefixMapping prefixes = (PrefixMapping) getContext().get(ExpandPrefixFunction.PREFIX_MAPPING);
		if (prefixes == null) throw new ExprEvalException("No prefix mapping registered");
		String pname = name.asString();
		int idx = pname.indexOf(':');
		if (idx == -1) throw new ExprEvalException("Not a prefixed name: " + name);
		String prefix = pname.substring(0, idx);
		String iri = prefixes.getNsPrefixURI(prefix);
		if (iri == null) throw new ExprEvalException("Prefix not defined: " + prefix);
		return NodeValue.makeNode(NodeFactory.createURI(iri + pname.substring(idx + 1)));
	}
}
