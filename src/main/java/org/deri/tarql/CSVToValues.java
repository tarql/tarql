package org.deri.tarql;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
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
	private final boolean varsFromHeader;
	private final List<Var> vars = new ArrayList<Var>();
	private int rownum;
	
	/**
	 * @param reader Reader over the contents of a CSV file
	 * @param varsFromHeader If true, use values of first row as column names
	 */
	public CSVToValues(Reader reader, boolean varsFromHeader) {
		this.reader = reader;
		this.varsFromHeader = varsFromHeader;
	}
	
	public TableData read() {
		List<Binding> bindings = new ArrayList<Binding>();
		try {
			CSVReader csv = new CSVReader(reader);
			String[] row;
			try {
				if (varsFromHeader) {
					while ((row = csv.readNext()) != null) {
						boolean foundValidColumnName = false;
						for (int i = 0; i < row.length; i++) {
							if (toVar(row[i]) == null) continue;
							foundValidColumnName = true;
						}
						// If row was empty or didn't contain anything usable
						// as column name, then try next row
						if (!foundValidColumnName) continue;
						for (int i = 0; i < row.length; i++) {
							Var var = toVar(row[i]);
							if (var == null || vars.contains(var) || var.equals(TarqlQuery.ROWNUM)) {
								getVar(i);
							} else {
								vars.add(var);
							}
						}
						break;
					}
				}
				rownum = 1;
				while ((row = csv.readNext()) != null) {
					// Skip rows without data
					if (isEmpty(row)) continue;
					bindings.add(toBinding(row));
					rownum++;
				}
				vars.add(TarqlQuery.ROWNUM);
				return new TableData(vars, bindings);
			} finally {
				csv.close();
			}
		} catch (IOException ex) {
			throw new JenaException(ex);
		}
	}
	
	private Var toVar(String s) {
		if (s == null) return null;
		s = s.trim().replace(" ", "_");
		if ("".equals(s)) return null;
		// FIXME: Handle other characters not allowed in Vars
		return Var.alloc(s);
	}

	private boolean isEmpty(String[] row) {
		for (int i = 0; i < row.length; i++) {
			if (!isUnboundValue(row[i])) return false;
		}
		return true;
	}

	/**
	 * Checks whether a string taken from a CSV cell is considered an unbound SPARQL value
	 */
	private boolean isUnboundValue(String value) {
		return value == null || "".equals(value);
	}
	
	private Binding toBinding(String[] row) {
		BindingHashMap result = new BindingHashMap();
		for (int i = 0; i < row.length; i++) {
			if (isUnboundValue(row[i])) continue;
			result.add(getVar(i), Node.createLiteral(sanitizeString(row[i])));
		}
		// Add current row number as ?ROWNUM
		result.add(TarqlQuery.ROWNUM, Node.createLiteral(Integer.toString(rownum), XSDDatatype.XSDinteger));
		return result;
	}
	
	/**
	 * Remove/replace weird characters known to cause problems in RDF toolkits.
	 */
	private String sanitizeString(String s) {
		// ASCII 10h, "Data Link Escape", causes parse failure in Turtle
        // in Virtuoso 7.0.0
		return s.replace((char) 0x10, (char) 0xFFFD);
	}
	
	private Var getVar(int column) {
		while (vars.size() <= column) {
			vars.add(null);
		}
		if (vars.get(column) == null) {
			Var var = Var.alloc(getColumnName(column));
			while (vars.contains(var)) {
				var = Var.alloc("_" + var.getName());
			}
			vars.set(column, var);
		}
		return vars.get(column);
	}
}
