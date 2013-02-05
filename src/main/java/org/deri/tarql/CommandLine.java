package org.deri.tarql;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.util.FileManager;

public class CommandLine {

	public static void main(String... args) {
		String csvFile = args[0];
		String sparqlFile = args[1];
		Query q = QueryFactory.create(FileManager.get().readWholeFileAsUTF8(sparqlFile));
		QueryExecution ex = CSVQueryExecutionFactory.create(csvFile, q);
		if (q.isSelectType()) {
			System.out.println(ResultSetFormatter.asText(ex.execSelect()));
		} else if (q.isConstructType()) {
			ex.execConstruct().write(System.out, "TURTLE");
		}
	}
}
