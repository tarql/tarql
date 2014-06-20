package org.deri.tarql;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.graph.NodeFactory;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.binding.BindingHashMap;

public class CSVToValues implements Iterator<Binding> {

	public static String getColumnName(int i) {
		String var = "";
		do {
			var = alphabet.charAt(i % alphabet.length()) + var;
			i = i / alphabet.length() - 1;
		} while (i >= 0);
		return var;
	}

	private final static String alphabet = "abcdefghijklmnopqrstuvwxyz";

	private Reader reader;
	private boolean varsFromHeader;
	private final List<Var> vars = new ArrayList<Var>();
	private int rownum;

	private Binding binding;
	private CSVReader csv;

	/**
	 * @param reader
	 *            Reader over the contents of a CSV file
	 * @param varsFromHeader
	 *            If true, use values of first row as column names
	 * @throws IOException
	 */
	public CSVToValues(Reader reader, boolean varsFromHeader)
			throws IOException {
		this.varsFromHeader = varsFromHeader;
		this.reader = reader;
		init();
	}

	private Var toVar(String s) {
		if (s == null)
			return null;
		s = s.trim().replace(" ", "_");
		if ("".equals(s))
			return null;
		// FIXME: Handle other characters not allowed in Vars
		return Var.alloc(s);
	}

	private boolean isEmpty(String[] row) {
		for (int i = 0; i < row.length; i++) {
			if (!isUnboundValue(row[i]))
				return false;
		}
		return true;
	}

	/**
	 * Checks whether a string taken from a CSV cell is considered an unbound
	 * SPARQL value
	 */
	private boolean isUnboundValue(String value) {
		return value == null || "".equals(value);
	}

	private Binding toBinding(String[] row) {
		BindingHashMap result = new BindingHashMap();
		for (int i = 0; i < row.length; i++) {
			if (isUnboundValue(row[i]))
				continue;
			result.add(getVar(i), NodeFactory.createLiteral(sanitizeString(row[i])));
		}
		// Add current row number as ?ROWNUM
		result.add(TarqlQuery.ROWNUM, NodeFactory.createLiteral(
				Integer.toString(rownum), XSDDatatype.XSDinteger));
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

	@Override
	public boolean hasNext() {
		return binding != null;
	}

	@Override
	public Binding next() {
		Binding current = binding;
		binding = null;
		String[] row;
		try {
			while ((row = csv.readNext()) != null) {
				// Skip rows without data
				if (isEmpty(row))
					continue;
				binding = toBinding(row);
				rownum++;
				break;
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return current;
	}

	@Override
	public void remove() {
		throw new UnsupportedOperationException(
				"Remove is not supported. It is a read-only iterator");
	}

	public void close() throws IOException {
		csv.close();
	}
	
	public List<Var> getVars() {
		List<Var> varsWithRowNum = new ArrayList<Var>(vars);
		varsWithRowNum.add(TarqlQuery.ROWNUM);
		return varsWithRowNum;
	}
	
	public void reset() throws IOException{
		reader.reset();
		init();
	}
	
	private void init() throws IOException{
		String[] row;
		csv = new CSVReader(reader);
		if (varsFromHeader) {
			while ((row = csv.readNext()) != null) {
				boolean foundValidColumnName = false;
				for (int i = 0; i < row.length; i++) {
					if (toVar(row[i]) == null)
						continue;
					foundValidColumnName = true;
				}
				// If row was empty or didn't contain anything usable
				// as column name, then try next row
				if (!foundValidColumnName)
					continue;
				for (int i = 0; i < row.length; i++) {
					Var var = toVar(row[i]);
					if (var == null || vars.contains(var)
							|| var.equals(TarqlQuery.ROWNUM)) {
						getVar(i);
					} else {
						vars.add(var);
					}
				}
				break;
			}
		}
		rownum = 1;
		next();
	}
}
