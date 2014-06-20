package org.deri.tarql;

import java.io.IOException;
import java.io.Reader;

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

public class TarqlQueryExecution {

	private CSVTable table;
	private TarqlQuery tq;
	
	public TarqlQueryExecution(CSVTable table, TarqlQuery query) {
		this.table = table;
		this.tq = query;
	}

	public TarqlQueryExecution(Reader reader, TarqlQuery query) throws IOException {
		boolean useColumnHeadersAsVars = modifyQueryForColumnHeaders(query.getQueries().get(0));
		this.table = new CSVTable(reader, useColumnHeadersAsVars);
		this.tq = query;
	}
	
	/**
	 * Detects whether column headers should be used as variable names
	 * (indicated in the query by use of OFFSET 1), and modify the query
	 * to make it work (setting OFFSET to 0, because the CSV reader will
	 * already remove the header row from the data)
	 * @param query Query to be analyzed and modified
	 * @return True if header row is to be used for variable names
	 */
	private boolean modifyQueryForColumnHeaders(Query query) {
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
	private void modifyQuery(Query query, Table table) {
		ElementData tableElement = new MyTableElement(table);
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

	public Model exec() throws IOException {
		Model model = ModelFactory.createDefaultModel();
		for(Query q:tq.getQueries()){
			modifyQuery(q, table);
			QueryExecution ex = QueryExecutionFactory.create(q, model);
			ex.execConstruct(model);
			table.reset();
		}
		return model;
	}

	public ResultSet execSelect() {
		//TODO check only first query. right?
		Model model = ModelFactory.createDefaultModel();
		Query q = tq.getQueries().get(0);
		modifyQuery(q, table);
		QueryExecution ex = QueryExecutionFactory.create(q, model);
		return ex.execSelect();
	}

	public Query getFirstQuery() {
		return tq.getQueries().get(0);
	}
	
	public void close(){
		table.close();
	}
}

class MyTableElement extends ElementData{

	private final Table table;
	
	public MyTableElement(Table t) {
		this.table = t;
	}
	@Override
	public Table getTable() {
		return table;
	}
	
}
