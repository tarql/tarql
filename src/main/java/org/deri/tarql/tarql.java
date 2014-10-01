package org.deri.tarql;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Manifest;

import org.apache.jena.atlas.io.IndentedWriter;
import org.deri.tarql.CSVOptions.ParseResult;

import arq.cmdline.ArgDecl;
import arq.cmdline.CmdGeneral;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.ResultSetFormatter;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.sparql.serializer.FmtTemplate;
import com.hp.hpl.jena.sparql.serializer.SerializationContext;
import com.hp.hpl.jena.sparql.util.Utils;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.NullIterator;

/**
 * The <code>tarql</code> CLI command.
 */
public class tarql extends CmdGeneral {

	// This will be displayed by --version
	public static final String VERSION;
	public static final String BUILD_DATE;
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
	}
	
	public static void main(String... args) {
		new tarql(args).mainRun();
	}

	private final ArgDecl testQueryArg = new ArgDecl(false, "test");
	private final ArgDecl withHeaderArg = new ArgDecl(false, "header-row", "header");
	private final ArgDecl withoutHeaderArg = new ArgDecl(false, "no-header-row", "no-header", "H");
	private final ArgDecl encodingArg = new ArgDecl(true, "encoding", "e");
	private final ArgDecl nTriplesArg = new ArgDecl(false, "ntriples");
	private final ArgDecl delimiterArg = new ArgDecl(true, "delimiter", "d");
	private final ArgDecl tabsArg = new ArgDecl(false, "tabs", "tab", "t");
	private final ArgDecl quoteArg = new ArgDecl(true, "quotechar");
	private final ArgDecl escapeArg = new ArgDecl(true, "escapechar", "p");
	
	private String queryFile;
	private List<String> csvFiles = new ArrayList<String>();
	private Boolean header = null;
	private boolean testQuery = false;
	private String encoding = null;
	private boolean writeNTriples = false;
	private Character delimiter = null;
	private Character quote = null;
	private Character escape = null;

	private ExtendedIterator<Triple> resultTripleIterator = NullIterator.instance();
	
	public tarql(String[] args) {
		super(args);
		getUsage().startCategory("Options");
		add(testQueryArg,     "--test", "Show CONSTRUCT template and first rows only (for query debugging)");
		add(delimiterArg,     "-d   --delimiter", "Delimiting character of the CSV file");
		add(tabsArg,          "-t   --tabs", "Specifies that the input is tab-separagted (TSV), overriding -d");
		add(quoteArg,         "--quotechar", "Quote character used in the CSV file");
		add(escapeArg,        "-p   --escapechar", "Character used to escape quotes in the CSV file");
		add(encodingArg,      "-e   --encoding", "Override CSV file encoding (e.g., utf-8 or latin-1)");
		add(withoutHeaderArg, "-H   --no-header-row", "CSV file has no header row; use variable names ?a, ?b, ...");
		add(withHeaderArg,    "--header-row", "CSV file's first row is a header with variable names (default)");
		add(nTriplesArg,      "--ntriples", "Write N-Triples instead of Turtle");
		getUsage().startCategory("Main arguments");
		getUsage().addUsage("query.sparql", "File containing a SPARQL query to be applied to a CSV file");
		getUsage().addUsage("table.csv", "CSV file to be processed; can be omitted if specified in FROM clause");
		modVersion.addClass(tarql.class);
	}
	
	@Override
    protected String getCommandName() {
		return Utils.className(this);
	}
	
	@Override
	protected String getSummary() {
		return getCommandName() + " [options] query.sparql [table.csv [...]]";
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
		if (hasArg(withHeaderArg)) {
			header = true;
		}
		if (hasArg(withoutHeaderArg)) {
			header = false;
		}
		if (hasArg(testQueryArg)) {
			testQuery = true;
		}
		if (hasArg(encodingArg)) {
			encoding = getValue(encodingArg);
		}
		if (hasArg(nTriplesArg)) {
			writeNTriples = true;
		}
		if (hasArg(delimiterArg)) {
			String d = getValue(delimiterArg);
			if (d == null || d.length() != 1) {
				cmdError("Value of --delimiter must be a single character");
			}
			delimiter = d.charAt(0);
		}
		if (hasArg(tabsArg)) {
			delimiter = '\t';
		}
		if (hasArg(quoteArg)) {
			String q = getValue(quoteArg);
			if (q == null || q.length() != 1) {
				cmdError("Value of --quotechar must be a single character");
			}
			quote = q.charAt(0);
		}
		if (hasArg(escapeArg)) {
			String e = getValue(escapeArg);
			if (e == null || e.length() != 1) {
				cmdError("Value of --escapechar must be a single character");
			}
			escape = e.charAt(0);
		}
	}

	@Override
	protected void exec() {
		try {
			TarqlQuery q = new TarqlParser(queryFile).getResult();
			if (testQuery) {
				q.makeTest();
			}
			CSVOptions options = new CSVOptions();
			if (header != null) {
				options.setColumnNamesInFirstRow(header);
			}
			if (encoding != null) {
				options.setEncoding(encoding);
			}
			if (delimiter != null) {
				options.setDelimiter(delimiter);
			}
			if (quote != null) {
				options.setQuoteChar(quote);
			}
			if (escape != null) {
				options.setEscapeChar(escape);
			}
			if (csvFiles.isEmpty()) {
				processResults(TarqlQueryExecutionFactory.create(q, options));
			} else {
				for (String csvFile: csvFiles) {
					ParseResult parseResult = CSVOptions.parseIRI(csvFile);
					processResults(TarqlQueryExecutionFactory.create(q, 
							InputStreamSource.fromFilenameOrIRI(parseResult.getRemainingIRI()), 
							parseResult.getOptions(options)));
				}
			}
			if (resultTripleIterator.hasNext()) {
				StreamingRDFWriter writer = new StreamingRDFWriter(System.out, resultTripleIterator);
				if (writeNTriples) {
					writer.writeNTriples();
				} else {
					writer.writeTurtle(
							q.getPrologue().getBaseURI(),
							q.getPrologue().getPrefixMapping());
				}
			}
		} catch (NotFoundException ex) {
			cmdError("Not found: " + ex.getMessage());
		} catch (IOException ioe) {
			cmdError("IOException: " + ioe.getMessage());
		}
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
}
