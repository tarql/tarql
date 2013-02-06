package org.deri.tarql;

import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.algebra.table.TableData;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.util.FileManager;

public class CSVQueryExecutionFactory {

	private final static Model EMPTY_MODEL = ModelFactory.createDefaultModel();
	// FIXME: Oh god, what a hack! But adding this to each create() method is worse...
	private static Model previousResults = EMPTY_MODEL;
	public static void setPreviousResults(Model previousResults) {
		CSVQueryExecutionFactory.previousResults = previousResults;
	}
	public static void resetPreviousResults() {
		previousResults = EMPTY_MODEL;
	}
	
	public static QueryExecution create(Query query) {
		return create(query, FileManager.get());
	}

	public static QueryExecution create(String query) {
		return create(QueryFactory.create(query));
	}
	
	public static QueryExecution create(Query query, FileManager fm) {
		return makeExecution(query, fm);
	}
	
	public static QueryExecution create(String query, FileManager fm) {
		return create(QueryFactory.create(query), fm);
	}
	
	public static QueryExecution create(String filenameOrURL, Query query) {
		return create(FileManager.get().open(filenameOrURL), query);
	}

	public static QueryExecution create(String filenameOrURL, String query) {
		return create(filenameOrURL, QueryFactory.create(query));
	}

	public static QueryExecution create(InputStream input, Query query) {
		return create(createReader(input), query);
	}

	public static QueryExecution create(InputStream input, String query) {
		return create(input, QueryFactory.create(query));
	}

	public static QueryExecution create(Reader input, Query query) {
		return makeExecution(input, query);
	}

	public static QueryExecution create(Reader input, String query) {
		return create(input, QueryFactory.create(query));
	}

	public static QueryExecution create(TableData table, Query query) {
		return makeExecution(table, query);
	}

	public static QueryExecution create(TableData table, String query) {
		return create(table, QueryFactory.create(query));
	}

	public static Reader createReader(InputStream inputStream) {
		return new CharsetDetectingReader(inputStream);
	}
	
	public static Reader createReader(String filenameOrURL, FileManager fm) {
		InputStream in = fm.open(filenameOrURL);
		if (in == null) {
			throw new NotFoundException(filenameOrURL);
		}
		return createReader(in);
	}

	private static QueryExecution makeExecution(TableData table, Query query) {
		modifyQuery(query, table);
		return QueryExecutionFactory.create(query, previousResults);
	}
	
	private static QueryExecution makeExecution(Reader reader, Query query) {
		boolean useColumnHeadersAsVars = modifyQueryForColumnHeaders(query);
		return makeExecution(new CSVToValues(reader, useColumnHeadersAsVars).read(), query);
	}
	
	private static QueryExecution makeExecution(Query query, FileManager fm) {
		String filenameOrURL = getSingleFromClause(query, fm);
		return makeExecution(createReader(filenameOrURL, fm), query);
	}
	
	private static String getSingleFromClause(Query query, FileManager fm) {
		if (query.getGraphURIs() == null || query.getGraphURIs().isEmpty()) {
			throw new JenaException("No input file provided");
		}
		if (query.getGraphURIs().size() > 1) {
			throw new JenaException("Too many input files: " + query.getGraphURIs());
		}
		return query.getGraphURIs().get(0);
	}
	
	/**
	 * Detects whether column headers should be used as variable names
	 * (indicated in the query by use of OFFSET 1), and modify the query
	 * to make it work (setting OFFSET to 0, because the CSV reader will
	 * already remove the header row from the data)
	 * @param query Query to be analyzed and modified
	 * @return True if header row is to be used for variable names
	 */
	private static boolean modifyQueryForColumnHeaders(Query query) {
		if (query.getOffset() != 1) return false;
		query.setOffset(0);
		return true;
	}
	
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
