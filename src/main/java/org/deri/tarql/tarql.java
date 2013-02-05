package org.deri.tarql;

import java.util.ArrayList;
import java.util.List;

import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.FileManager;

public class tarql extends CmdGeneral {

	public static void main(String... args) {
		new tarql(args).mainRun();
	}

	private String queryFile;
	private List<String> csvFiles = new ArrayList<String>();
	
	public tarql(String[] args) {
		super(args);
		getUsage().startCategory("Main arguments");
		getUsage().addUsage("query.sparql", "File containing a SPARQL query to be applied to a CSV file");
		getUsage().addUsage("table.csv", "CSV file to be processed; can be omitted if specified in FROM clause");
	}
	
	@Override
    protected String getCommandName() {
		return Utils.className(this);
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " query.sparql [table.csv [...]]";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().isEmpty()) {
			doHelp();
		}
		queryFile = getPositionalArg(0);
		for (int i = 1; i < getPositional().size(); i++) {
			csvFiles.add(getPositionalArg(i));
		}
	}

	@Override
	protected void exec() {
		try {
			Query q = QueryFactory.create(FileManager.get().readWholeFileAsUTF8(queryFile));
			if (csvFiles.isEmpty()) {
				executeQuery(CSVQueryExecutionFactory.create(q));
			} else {
				for (String csvFile: csvFiles) {
					executeQuery(CSVQueryExecutionFactory.create(csvFile, q));
				}
			}
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		}
	}
		
	private void executeQuery(QueryExecution ex) {
		if (ex.getQuery().isSelectType()) {
			System.out.println(ResultSetFormatter.asText(ex.execSelect()));
		} else if (ex.getQuery().isAskType()) {
			System.out.println(ResultSetFormatter.asText(ex.execSelect()));
		} else if (ex.getQuery().isConstructType()) {
			ex.execConstruct().write(System.out, "TURTLE");
		} else {
			cmdError("Only query forms CONSTRUCT, SELECT and ASK are supported");
		}
	}
}
