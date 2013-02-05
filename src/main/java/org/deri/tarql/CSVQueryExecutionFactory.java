package org.deri.tarql;

import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.Syntax;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.table.TableData;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.util.FileManager;

public class CSVQueryExecutionFactory {

	public static QueryExecution create(String filenameOrURL, Query query) {
		return create(FileManager.get().open(filenameOrURL), query);
	}

	public static QueryExecution create(String filenameOrURL, String query) {
		return create(filenameOrURL, QueryFactory.create(query));
	}

	public static QueryExecution create(String filenameOrURL, String query, Syntax syntax) {
		return create(filenameOrURL, QueryFactory.create(query, syntax));
	}

	public static QueryExecution create(InputStream input, Query query) {
		return create(makeReader(input), query);
	}

	public static QueryExecution create(InputStream input, String query) {
		return create(input, QueryFactory.create(query));
	}

	public static QueryExecution create(InputStream input, String query, Syntax syntax) {
		return create(input, QueryFactory.create(query, syntax));
	}

	public static QueryExecution create(Reader input, Query query) {
		return makeExecution(input, query);
	}

	public static QueryExecution create(Reader input, String query) {
		return create(input, QueryFactory.create(query));
	}

	public static QueryExecution create(Reader input, String query, Syntax syntax) {
		return create(input, QueryFactory.create(query, syntax));
	}

	private static Reader makeReader(InputStream inputStream) {
		return new CharsetDetectingReader(inputStream);
	}
	
	private static QueryExecution makeExecution(Reader reader, Query query) {
		modifyQuery(query, new CSVToValues(reader).read());
		return QueryExecutionFactory.create(query, EMPTY_MODEL);
	}
	private final static Model EMPTY_MODEL = ModelFactory.createDefaultModel();
	
	/**
	 * Modifies a query so that it operates onto a table. This is achieved
	 * by appending the table as a VALUES block to the end of the main
	 * query pattern.
	 * 
	 * @param query Original query; will be modified in place
	 * @param table Data table to be added into the query
	 */
	private static void modifyQuery(Query query, TableData table) {
		ElementData tableElement = new ElementData();
		for (Var var: table.getVars()) {
			tableElement.add(var);
		}
		for (Binding binding: table.getRows()) {
			tableElement.add(binding);
		}
		ElementGroup groupElement = new ElementGroup();
		groupElement.addElement(tableElement);
		if (query.getQueryPattern() instanceof ElementGroup) {
			for (Element element: ((ElementGroup) query.getQueryPattern()).getElements()) {
				groupElement.addElement(element);
			}
		} else {
			groupElement.addElement(query.getQueryPattern());
		}
		query.setQueryPattern(groupElement);
	}
}
