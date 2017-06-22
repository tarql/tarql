package org.deri.tarql;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Triple;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingHashMap;
import org.apache.jena.sparql.util.NodeFactoryExtra;


public class Helpers {

	public static List<Var> vars(String... names) {
		List<Var> result = new ArrayList<Var>(names.length);
		for (String name: names) {
			result.add(Var.alloc(name));
		}
		return result;
	}
	
	public static List<Binding> bindings(Binding... bindings) {
		return Arrays.asList(bindings);
	}

	public static List<Var> vars(List<String> header) {
		List<Var> vars = new ArrayList<Var>(header.size());
		for (String var: header) {
			vars.add(Var.alloc(var));
		}
		return vars;
	}
	
	public static Binding binding(List<Var> header, String... values) {
		if (header.size() != values.length) {
			throw new IllegalArgumentException(
					"header and values must have same length: " + 
							header + ", " + Arrays.toString(values));
		}
		BindingHashMap result = new BindingHashMap();
		for (int i = 0; i < header.size(); i++) {
			result.add(header.get(i), NodeFactoryExtra.parseNode(values[i]));
		}
		return result;
	}

	private Helpers() {} // Only static methods
	
	public static Binding removePseudoVars(Binding binding) {
		BindingHashMap result = new BindingHashMap();
		Iterator<Var> it = binding.vars();
		while (it.hasNext()) {
			Var var = it.next();
			if (var.equals(TarqlQuery.ROWNUM)) continue;
			result.add(var, binding.get(var));
		}
		return result;
	}
	
	public static Triple triple(String tripleAsTurtle) {
		Model m = ModelFactory.createDefaultModel();
		m.read(new StringReader(tripleAsTurtle), "urn:x-base:", "TURTLE");
		return m.listStatements().next().asTriple();
	}
	
	public static List<Triple> triples(String... triples) {
		List<Triple> results = new ArrayList<>();
		for (String triple: triples) {
			results.add(triple(triple));
		}
		return results;
	}
}
