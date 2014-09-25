package org.deri.tarql;

import java.io.IOException;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.util.FileManager;

/**
 * Static convenience methods for setting up TARQL query executions.
 */
public class TarqlQueryExecutionFactory {

	public static TarqlQueryExecution create(TarqlQuery query) throws IOException {
		return create(query, FileManager.get(), new CSVOptions());
	}

	public static TarqlQueryExecution create(TarqlQuery query, CSVOptions options) throws IOException {
		return create(query, FileManager.get(), options);
	}

	public static TarqlQueryExecution create(TarqlQuery query, FileManager fm) throws IOException {
		String filenameOrURL = getSingleFromClause(query.getQueries().get(0), fm);
		return create(query, InputStreamSource.fromFilenameOrIRI(filenameOrURL, fm), new CSVOptions());
	}

	public static TarqlQueryExecution create(TarqlQuery query, FileManager fm, CSVOptions options) throws IOException {
		String filenameOrURL = getSingleFromClause(query.getQueries().get(0), fm);
		return create(query, InputStreamSource.fromFilenameOrIRI(filenameOrURL, fm), options);
	}

	public static TarqlQueryExecution create(TarqlQuery query, String filenameOrURL) throws IOException {
		return create(query, InputStreamSource.fromFilenameOrIRI(filenameOrURL), new CSVOptions());
	}

	public static TarqlQueryExecution create(TarqlQuery query, String filenameOrURL, CSVOptions options) throws IOException {
		return create(query, InputStreamSource.fromFilenameOrIRI(filenameOrURL), options);
	}
	
	public static TarqlQueryExecution create(TarqlQuery query, InputStreamSource input) {
		return new TarqlQueryExecution(input, new CSVOptions(), query);
	}

	public static TarqlQueryExecution create(TarqlQuery query, InputStreamSource input, CSVOptions options) {
		return new TarqlQueryExecution(input, options, query);
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

