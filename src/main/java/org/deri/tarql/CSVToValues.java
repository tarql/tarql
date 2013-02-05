package org.deri.tarql;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.algebra.table.TableData;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;

public class CSVToValues {

	public static String getColumnName(int i) {
		String var = "";
		do {
			var = alphabet.charAt(i % alphabet.length()) + var;
			i = i / alphabet.length() - 1;
		} while (i >= 0);
		return var;
	}
	
	private final static String alphabet = "abcdefghijklmnopqrstuvwxyz";
	
	private final Reader reader;
	
	public CSVToValues(Reader reader) {
		this.reader = reader;
	}
	
	public TableData read() {
		List<Binding> bindings = new ArrayList<Binding>();
		try {
			CSVReader csv = new CSVReader(reader);
			int maxCols = 0;
			String[] row;
			try {
				while ((row = csv.readNext()) != null) {
					if (row.length > maxCols) maxCols = row.length;
					Binding binding = toBinding(row);
					if (binding.size() > 0) {
						// Skip rows without data
						bindings.add(binding);
					}
				}
				List<Var> variables = new ArrayList<Var>(maxCols);
				for (int i = 0; i < maxCols; i++) {
					variables.add(Var.alloc(getColumnName(i)));
				}
				return new TableData(variables, bindings);
			} finally {
				csv.close();
			}
		} catch (IOException ex) {
			throw new JenaException(ex);
		}
		
	}
	
	private static Binding toBinding(String[] row) {
		BindingHashMap result = new BindingHashMap();
		for (int i = 0; i < row.length; i++) {
			if (row[i] == null || "".equals(row[i])) continue;
			result.add(Var.alloc(getColumnName(i)), Node.createLiteral(row[i]));
		}
		return result;
	}
}
