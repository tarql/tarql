package org.deri.tarql;

import java.io.InputStream;
import java.io.Reader;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.algebra.table.TableData;
import com.hp.hpl.jena.util.FileManager;

public class CSVQueryExecutionFactory {

	public static TarqlQueryExecution create(TarqlQuery query) {
		return create(query, FileManager.get());
	}

	public static TarqlQueryExecution create(TarqlQuery query, FileManager fm) {
		return makeExecution(query, fm);
	}
	
	public static TarqlQueryExecution create(String filenameOrURL, TarqlQuery query) {
		return create(FileManager.get().open(filenameOrURL), query);
	}

	public static TarqlQueryExecution create(InputStream input, TarqlQuery query) {
		return create(createReader(input), query);
	}

	public static TarqlQueryExecution create(Reader input, TarqlQuery query) {
		return makeExecution(input, query);
	}

	public static TarqlQueryExecution create(TableData table, TarqlQuery query) {
		return makeExecution(table, query);
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

	private static TarqlQueryExecution makeExecution(TableData table, TarqlQuery query) {
		return new TarqlQueryExecution(table, query);
	}
	
	private static TarqlQueryExecution makeExecution(Reader reader, TarqlQuery query) {
		return new TarqlQueryExecution(reader, query);
	}
	
	private static TarqlQueryExecution makeExecution(TarqlQuery query, FileManager fm) {
		String filenameOrURL = getSingleFromClause(query.getQueries().get(0), fm);
		return new TarqlQueryExecution(createReader(filenameOrURL, fm), query);
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
}

