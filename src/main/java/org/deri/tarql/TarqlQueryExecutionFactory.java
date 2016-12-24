package org.deri.tarql;

import java.io.IOException;

import org.apache.jena.query.Query;
import org.apache.jena.util.FileManager;


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
		URLOptionsParser parseResult = new URLOptionsParser(filenameOrURL);
		return create(query, InputStreamSource.fromFilenameOrIRI(parseResult.getRemainingURL(), fm), parseResult.getOptions());
	}

	public static TarqlQueryExecution create(TarqlQuery query, FileManager fm, CSVOptions options) throws IOException {
		String filenameOrURL = getSingleFromClause(query.getQueries().get(0), fm);
		URLOptionsParser parseResult = new URLOptionsParser(filenameOrURL);
		CSVOptions newOptions = new CSVOptions(parseResult.getOptions());
		newOptions.overrideWith(options);
		return create(query, InputStreamSource.fromFilenameOrIRI(parseResult.getRemainingURL(), fm), newOptions);
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
			throw new TarqlException("No input file provided");
		}
		if (query.getGraphURIs().size() > 1) {
			throw new TarqlException("Too many input files: " + query.getGraphURIs());
		}
		return query.getGraphURIs().get(0);
	}
}

