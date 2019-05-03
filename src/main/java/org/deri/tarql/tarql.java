package org.deri.tarql;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.jena.atlas.io.IndentedWriter;
import org.apache.jena.atlas.lib.Lib;
import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.serializer.FmtTemplate;
import org.apache.jena.sparql.serializer.SerializationContext;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.util.iterator.NullIterator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import jena.cmd.ArgDecl;
import jena.cmd.CmdGeneral;



/**
 * The <code>tarql</code> CLI command.
 */
public class tarql extends CmdGeneral {

	// This will be displayed by --version
	public static final String VERSION;
	public static final String BUILD_DATE;
	
	public static final String NS = "http://tarql.github.io/tarql#";
	
	static {
		String version = "Unknown";
		String date = "Unknown";
		try {
			URL res = tarql.class.getResource(tarql.class.getSimpleName() + ".class");
			Manifest mf = ((JarURLConnection) res.openConnection()).getManifest();
			version = (String) mf.getMainAttributes().getValue("Implementation-Version");
			date = (String) mf.getMainAttributes().getValue("Built-Date")
					.replaceFirst("(\\d\\d\\d\\d)(\\d\\d)(\\d\\d)-(\\d\\d)(\\d\\d)", "$1-$2-$3T$4:$5:00Z");
	    } catch (Exception ex) {
		}
		VERSION = version;
		BUILD_DATE = date;
		
		TarqlQuery.registerFunctions();
	}
	
	public static void main(String... args) {
		new tarql(args).mainRun();
	}

	private final ArgDecl stdinArg = new ArgDecl(false, "stdin");
	private final ArgDecl testQueryArg = new ArgDecl(false, "test");
	private final ArgDecl withHeaderArg = new ArgDecl(false, "header-row", "header");
	private final ArgDecl withoutHeaderArg = new ArgDecl(false, "no-header-row", "no-header", "H");
	private final ArgDecl encodingArg = new ArgDecl(true, "encoding", "e");
	private final ArgDecl nTriplesArg = new ArgDecl(false, "ntriples");
	private final ArgDecl delimiterArg = new ArgDecl(true, "delimiter", "d");
	private final ArgDecl tabsArg = new ArgDecl(false, "tabs", "tab", "t");
	private final ArgDecl quoteArg = new ArgDecl(true, "quotechar");
	private final ArgDecl escapeArg = new ArgDecl(true, "escapechar", "p");
	private final ArgDecl baseArg = new ArgDecl(true, "base");
	private final ArgDecl writeBaseArg = new ArgDecl(false, "write-base");
	private final ArgDecl dedupArg = new ArgDecl(true, "dedup");
	
	private String queryFile;
	private List<String> csvFiles = new ArrayList<String>();
	private boolean stdin = false;
	private CSVOptions options = new CSVOptions();
	private boolean testQuery = false;
	private boolean writeNTriples = false;
	private String baseIRI = null;
	private boolean writeBase = false;
	private int dedupWindowSize = 0;
	
	private ExtendedIterator<Triple> resultTripleIterator = NullIterator.instance();
	
	public tarql(String[] args) {
		super(args);
		
		getUsage().startCategory("Output options");
		add(testQueryArg,     "--test", "Show CONSTRUCT template and first rows only (for query debugging)");
		add(writeBaseArg,     "--write-base", "Write @base if output is Turtle");
		add(nTriplesArg,      "--ntriples", "Write N-Triples instead of Turtle");
		add(dedupArg, "--dedup", "Window size in which to remove duplicate triples");

		getUsage().startCategory("Input options");
		add(stdinArg,         "--stdin", "Read input from STDIN instead of file");
		add(delimiterArg,     "-d   --delimiter", "Delimiting character of the input file");
		add(tabsArg,          "-t   --tabs", "Specifies that the input is tab-separated (TSV)");
		add(quoteArg,         "--quotechar", "Quote character used in the input file, or \"none\"");
		add(escapeArg,        "-p   --escapechar", "Character used to escape quotes in the input file, or \"none\"");
		add(encodingArg,      "-e   --encoding", "Override input file encoding (e.g., utf-8 or latin-1)");
		add(withoutHeaderArg, "-H   --no-header-row", "Input file has no header row; use variable names ?a, ?b, ...");
		add(withHeaderArg,    "--header-row", "Input file's first row is a header with variable names (default)");
		add(baseArg,          "--base", "Base IRI for resolving relative IRIs");
		
		getUsage().startCategory("Main arguments");
		getUsage().addUsage("query.sparql", "File containing a SPARQL query to be applied to an input file");
		getUsage().addUsage("table.csv", "CSV/TSV file to be processed; can be omitted if specified in FROM clause");
		modVersion.addClass(tarql.class);
	}
	
	@Override
    protected String getCommandName() {
		return Lib.className(this);
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " [options] query.sparql [table.csv [...]]";
	}

	@Override
	protected void processModulesAndArgs() {
		if (getPositional().isEmpty()) {
			printHelp();
		}
		queryFile = getPositionalArg(0);
		for (int i = 1; i < getPositional().size(); i++) {
			csvFiles.add(getPositionalArg(i));
		}
		if (hasArg(stdinArg)) {
			stdin = true;
		}
		if (hasArg(withHeaderArg)) {
			options.setColumnNamesInFirstRow(true);
		}
		if (hasArg(withoutHeaderArg)) {
			options.setColumnNamesInFirstRow(false);
		}
		if (hasArg(testQueryArg)) {
			testQuery = true;
		}
		if (hasArg(encodingArg)) {
			options.setEncoding(getValue(encodingArg));
		}
		if (hasArg(nTriplesArg)) {
			writeNTriples = true;
		}
		if (hasArg(tabsArg)) {
			options.setDefaultsForTSV();
		} else {
			options.setDefaultsForCSV();
		}
		if (hasArg(delimiterArg)) {
			Character d = getCharValue(delimiterArg);
			if (d == null) {
				cmdError("Value of --delimiter must be a single character");
			}
			options.setDelimiter(d);
		}
		if (hasArg(quoteArg)) {
			options.setQuoteChar(getCharValue(quoteArg));
		}
		if (hasArg(escapeArg)) {
			options.setEscapeChar(getCharValue(escapeArg));
		}
		if (hasArg(baseArg)) {
			baseIRI = getValue(baseArg);
		}
		if (hasArg(writeBaseArg)) {
			writeBase = true;
		}
		if (hasArg(dedupArg)) {
			if (getValue(dedupArg) == null) {
				cmdError("--dedup needs an integer value");
			}
			try {
				dedupWindowSize = Integer.parseInt(getValue(dedupArg));
			} catch (NumberFormatException ex) {
				dedupWindowSize = -1;
			}
			if (dedupWindowSize < 0) {
				cmdError("Value of --dedup must be integer >= 0");
			}
		}
	}

	@Override
	protected void exec() {
		initLogging();
		try {
			TarqlQuery q = baseIRI == null
					? new TarqlParser(queryFile).getResult()
					: new TarqlParser(queryFile, baseIRI).getResult();
			if (testQuery) {
				q.makeTest();
			}
			if (stdin) {
				processResults(TarqlQueryExecutionFactory.create(q, 
						InputStreamSource.fromStdin(), options));
			} else if (csvFiles.isEmpty()) {
				processResults(TarqlQueryExecutionFactory.create(q, options));
			} else {
				for (String csvFile: csvFiles) {
					URLOptionsParser parseResult = new URLOptionsParser(csvFile);
					processResults(TarqlQueryExecutionFactory.create(q, 
							InputStreamSource.fromFilenameOrIRI(parseResult.getRemainingURL()), 
							parseResult.getOptions(options)));
				}
			}
			if (resultTripleIterator.hasNext()) {
				StreamingRDFWriter writer = new StreamingRDFWriter(System.out, resultTripleIterator);
				writer.setDedupWindowSize(dedupWindowSize);
				if (writeNTriples) {
					writer.writeNTriples();
				} else {
					writer.writeTurtle(
							q.getPrologue().getBaseURI(),
							q.getPrologue().getPrefixMapping(), writeBase);
				}
			}
		} catch (NotFoundException ex) {
			error("Not found", ex);
		} catch (IOException ioe) {
			error("IOException", ioe);
		} catch (QueryParseException ex) {
			error("Error parsing SPARQL query", ex);
		} catch (TarqlException ex) {
			error(null, ex);
		}
	}

	private void error(String message, Throwable cause) {
		Logger.getLogger("org.deri.tarql").info(message == null ? "Error" : message, cause);
		if (message == null) {
			cmdError(cause.getMessage());
		} else {
			cmdError(message + ": " + cause.getMessage());
		}
	}
	
	private Character getCharValue(ArgDecl arg) {
		String value = getValue(arg);
		if (CSVOptions.charNames.containsKey(value)) {
			return CSVOptions.charNames.get(value);
		}
		if (value != null && value.length() == 1) {
			return value.charAt(0);
		}
		cmdError("Value of --" + arg.getKeyName() + " cannot be more than one character");
		return null;
	}
	
	private void processResults(TarqlQueryExecution ex) throws IOException {
		if (testQuery && ex.getFirstQuery().getConstructTemplate() != null) {
			IndentedWriter out = new IndentedWriter(System.out); 
			new FmtTemplate(out, new SerializationContext(ex.getFirstQuery())).format(ex.getFirstQuery().getConstructTemplate());
			out.flush();
		}
		if (ex.getFirstQuery().isSelectType()) {
			System.out.println(ResultSetFormatter.asText(ex.execSelect()));
		} else if (ex.getFirstQuery().isAskType()) {
			System.out.println(ResultSetFormatter.asText(ex.execSelect()));
		} else if (ex.getFirstQuery().isConstructType()) {
			resultTripleIterator = resultTripleIterator.andThen(ex.execTriples());
		} else {
			cmdError("Only query forms CONSTRUCT, SELECT and ASK are supported");
		}
	}
	
	// Not sure if this really works...
	private void initLogging() {
		if (isQuiet()) {
			Logger.getRootLogger().setLevel(Level.ERROR);
		}
		if (isVerbose()) {
			Logger.getLogger("org.deri.tarql").setLevel(Level.INFO);
		}
		if (isDebug()) {
			Logger.getLogger("org.deri.tarql").setLevel(Level.DEBUG);
		}
	}
}
