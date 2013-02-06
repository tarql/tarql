package org.deri.tarql;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;
import com.hp.hpl.jena.sparql.util.NodeFactory;

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
			result.add(header.get(i), NodeFactory.parseNode(values[i]));
		}
		return result;
	}

	private Helpers() {} // Only static methods
}
