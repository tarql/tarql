package org.deri.tarql;

import java.io.IOException;
import java.util.Iterator;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.sparql.algebra.Table;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.Element;
import com.hp.hpl.jena.sparql.syntax.ElementData;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;

/**
 * The execution of a {@link TarqlQuery} over a particular CSV file.
 * Results can be delivered written into a {@link Model} or as an
 * iterator over triples.
 */
public class TarqlQueryExecution {
	private final CSVTable table;
	private final TarqlQuery tq;

	/**
	 * Sets up a new query execution.
	 * 
	 * @param source The input CSV file
	 * @param options Configuration options for the CSV file
	 * @param query The input query
	 */
	public TarqlQueryExecution(InputStreamSource source, CSVOptions options, TarqlQuery query) {
		if (options == null) {
			options = new CSVOptions();
		}
		if (options.hasColumnNamesInFirstRow() == null) {
			// Presence or absence of header row was not specified on command line or FROM clause.
			// So we fall back to the convention where OFFSET 1 in the query
			// indicates that a header is present. To make that work, we
			// set the OFFSET to 0 and tell the parser to gobble up the first
			// row for column names.
			options = new CSVOptions(options);
			Query firstQuery = query.getQueries().get(0);
			if (firstQuery.getOffset() == 1) {
				options.setColumnNamesInFirstRow(true);
				firstQuery.setOffset(0);
			}
		}
		table = new CSVTable(source, options);
		tq = query;
	}

	/**
	 * Modifies a query so that it operates onto a table. This is achieved
	 * by appending the table as a VALUES block to the end of the main
	 * query pattern.
	 * 
	 * @param query Original query; will be modified in place
	 * @param table Data table to be added into the query
	 */
	private void modifyQuery(Query query, final Table table) {
		ElementData tableElement = new ElementData() {
			@Override
			public Table getTable() {
				return table;
			}
		};
		for (Var var: table.getVars()) {
			// Skip ?ROWNUM for "SELECT *" queries -- see further below
			if (query.isSelectType() && query.isQueryResultStar() 
					&& var.equals(TarqlQuery.ROWNUM)) continue;
			tableElement.add(var);
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
		
		// For SELECT * queries, we don't want to include pseudo
		// columns such as ?ROWNUM that may exist in the table.
		// That's why we skipped ?ROWNUM further up.
		if (query.isSelectType() && query.isQueryResultStar()) {
			// Force expansion of "SELECT *" to actual projection list
			query.setResultVars();
			// Tell ARQ that it actually needs to pay attention to
			// the projection list
			query.setQueryResultStar(false);
			// And now we can add ?ROWNUM to the table, as the "*"
			// has already been expanded.
			tableElement.add(TarqlQuery.ROWNUM);
		}
		// Data can only be added to table after we've finished the
		// ?ROWNUM shenangians
		/*for (Binding binding: table.getRows()) {
			tableElement.add(binding);
		}*/
	}

	public void exec(Model model) throws IOException {
		for (Query q: tq.getQueries()) {
			modifyQuery(q, table);
			QueryExecution ex = QueryExecutionFactory.create(q, model);
			ex.execConstruct(model);
		}
	}

	public Iterator<Triple> execTriples() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		ExtendedIterator<Triple> result = new NullIterator<Triple>();
		for (Query q: tq.getQueries()) {
			modifyQuery(q, table);
			QueryExecution ex = QueryExecutionFactory.create(q, model);
			result = result.andThen(ex.execConstructTriples());
		}
		return result;
	}
	
	public ResultSet execSelect() {
		//TODO check only first query. right?
		Query q = getFirstQuery();
		modifyQuery(q, table);
		QueryExecution ex = QueryExecutionFactory.create(q, ModelFactory.createDefaultModel());
		return ex.execSelect();
	}

	public Query getFirstQuery() {
		return tq.getQueries().get(0);
	}
	
	public void close() {
		table.close();
	}
}