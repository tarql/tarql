package org.deri.tarql;

import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.sparql.syntax.ElementFilter;
import org.apache.jena.sparql.syntax.ElementBind;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementData;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.deri.tarql.functions.ExpandPrefixFunction;


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
		
		if (query.getQueryPattern() instanceof ElementGroup) {
			for (Element element: ((ElementGroup) query.getQueryPattern()).getElements()) {
				groupElement.addElement(element);
			}
		} else {
			groupElement.addElement(query.getQueryPattern());
		}
		// Put VALUES before the CSV table when it is safe to do so.
		boolean valuesFirst = false;

		if (query.getQueryPattern() instanceof ElementData) {
			valuesFirst = true;
		} else if (query.getQueryPattern() instanceof ElementGroup) {
			java.util.List<Element> elements =
					((ElementGroup) query.getQueryPattern()).getElements();

			valuesFirst = elements.size() == 1
					&& elements.get(0) instanceof ElementData;

			if (elements.size() == 2
					&& elements.get(0) instanceof ElementData
					&& (elements.get(1) instanceof ElementBind
					|| elements.get(1) instanceof ElementFilter)) {
				valuesFirst = true;
			}
		}

		if (valuesFirst) {
			if (groupElement.getElements().size() > 1
					&& groupElement.getElements().get(0) instanceof ElementData) {
				groupElement.getElements().add(1, tableElement);
			} else {
				groupElement.addElement(tableElement);
			}
		} else {
			groupElement.getElements().add(0, tableElement);
		}

		query.setQueryPattern(groupElement);
		
		// For SELECT * queries, we don't want to include pseudo
		// columns such as ?ROWNUM that may exist in the table.
		// That's why we skipped ?ROWNUM further up.
		if (query.isSelectType() && query.isQueryResultStar()) {
			// Force expansion of "SELECT *" to actual projection list
			query.ensureResultVars();
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

	private QueryExecution createQueryExecution(Query query, Model model) {
		QueryExecution result = QueryExecutionFactory.create(query, model);
		PrefixMappingImpl prefixes = new PrefixMappingImpl();
		prefixes.setNsPrefixes(tq.getPrologue().getPrefixMapping());
		prefixes.setNsPrefix("tarql", tarql.NS);
		result.getContext().set(ExpandPrefixFunction.PREFIX_MAPPING, prefixes);
		return result;
	}
	
	public void exec(Model model) throws IOException {
		for (Query q: tq.getQueries()) {
			modifyQuery(q, table);
			QueryExecution ex = createQueryExecution(q, model);
			ex.execConstruct(model);
		}
		if (tarql.NS.equals(model.getNsPrefixURI("tarql"))) {
			model.removeNsPrefix("tarql");
		}
	}

	public Iterator<Triple> execTriples() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		ExtendedIterator<Triple> result = new NullIterator<Triple>();
		for (Query q: tq.getQueries()) {
			modifyQuery(q, table);
			QueryExecution ex = createQueryExecution(q, model);
			result = result.andThen(ex.execConstructTriples());
		}
		return result;
	}
	public Iterator<Quad> execQuads() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		java.util.List<Iterator<Quad>> iterators = new java.util.ArrayList<>();

		for (Query q: tq.getQueries()) {
			modifyQuery(q, table);
			QueryExecution ex = createQueryExecution(q, model);
			iterators.add(ex.execConstructQuads());
		}

		return iterators.stream()
				.flatMap(it -> java.util.stream.StreamSupport.stream(
						java.util.Spliterators.spliteratorUnknownSize(it, 0), false))
				.iterator();
	}
	
	public ResultSet execSelect() {
		//TODO check only first query. right?
		Query q = getFirstQuery();
		modifyQuery(q, table);
		QueryExecution ex = createQueryExecution(q, ModelFactory.createDefaultModel());
		return ex.execSelect();
	}

	public Query getFirstQuery() {
		return tq.getQueries().get(0);
	}
	
	public void close() {
		table.close();
	}
}