package org.deri.tarql;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.riot.system.IRIResolver;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.lang.SyntaxVarScope;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.sparql.lang.sparql_11.SPARQLParser11;
import org.apache.jena.sparql.lang.sparql_11.TokenMgrError;
import org.apache.jena.util.FileManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Parses a {@link TarqlQuery} provided as a string or reader.
 */
public class TarqlParser {
	private final static Logger log = LoggerFactory.getLogger(TarqlParser.class);
	
	private final Reader reader;
	private final TarqlQuery result = new TarqlQuery();
	private boolean done = false;
	private boolean seenSelectOrAsk = false;
	
	public TarqlParser(String filenameOrURL) {
		this(open(filenameOrURL), FileManager.get().mapURI(filenameOrURL));
	}
	
	public TarqlParser(Reader reader) {
		this(reader, null);
	}
	
	public TarqlParser(Reader reader, String baseIRI) {
		this.reader = reader;
		result.getPrologue().setResolver(IRIResolver.create(baseIRI));
		addTarqlPrefix();
	}
	
	private static Reader open(String filenameOrURL) {
		try {
			InputStream in = FileManager.get().open(filenameOrURL);
			if (in == null) throw new NotFoundException(filenameOrURL);
			return new InputStreamReader(in, "UTF-8");
		} catch (UnsupportedEncodingException ex) {
			// Can't happen, UTF-8 is always supported
			return null;
		}
	}
	
	public TarqlQuery getResult() {
		parse();
		return result;
	}
	
	private void parseDo(SPARQLParser11 parser) throws ParseException {
		do {
			int beginLine = parser.getToken(1).beginLine;
			int beginColumn = parser.getToken(1).beginColumn;

			Query query = new Query(result.getPrologue());

			// You'd assume that a query initialized via "new Query(prologue)"
			// has the IRI resolver from prologue.getResolver(), but that doesn't
			// appear to be the case in Jena 2.12.0, so we set it manually
			query.getPrologue().setResolver(result.getPrologue().getResolver());

			result.addQuery(query);
			parser.setQuery(query);
			parser.Query();

			if (query.isSelectType() || query.isAskType()) {
				seenSelectOrAsk = true;
			}
			if (seenSelectOrAsk && result.getQueries().size() > 1) {
				throw new QueryParseException("" +
						"Multiple queries per file are only supported for CONSTRUCT", 
						beginLine, beginColumn);
			}
			
			// From Parser.validateParsedQuery, which we can't call directly
			SyntaxVarScope.check(query);
			
			result.getPrologue().usePrologueFrom(query);
			if (log.isDebugEnabled()) {
				log.debug(query.toString());
			}
		} while (parser.getToken(1).kind != SPARQLParser11.EOF);
		removeTarqlPrefix();
	}

	// Adapted from ARQ ParserSPARQL11.java
	private void parse() {
		if (done) return;
		done = true;
		SPARQLParser11 parser = new SPARQLParser11(reader) ;
		try {
			parseDo(parser);
		} catch (ParseException ex) { 
			throw new QueryParseException(ex.getMessage(),
					ex.currentToken.beginLine,
					ex.currentToken.beginColumn);
		} catch (TokenMgrError tErr) {
			// Last valid token : not the same as token error message - but this should not happen
			int col = parser.token.endColumn;
			int line = parser.token.endLine;
			throw new QueryParseException(tErr.getMessage(), line, col);
		} catch (QueryException ex) {
			throw ex;
		} catch (JenaException ex) {
			throw new QueryException(ex.getMessage(), ex);
		} catch (Error err) {
			// The token stream can throw errors.
			throw new QueryParseException(err.getMessage(), err, -1, -1);
		} catch (Throwable th) {
			Log.warn(TarqlParser.class, "Unexpected throwable: ",th);
			throw new QueryException(th.getMessage(), th);
		}
	}
	
	private void addTarqlPrefix() {
		if (result.getPrologue().getPrefix("tarql") == null) {
			result.getPrologue().getPrefixMapping().setNsPrefix("tarql", tarql.NS);
		}
	}
	
	private void removeTarqlPrefix() {
		if (tarql.NS.equals(result.getPrologue().getPrefix("tarql"))) {
			PrefixMapping prefixes = new PrefixMappingImpl();
			prefixes.setNsPrefixes(result.getPrologue().getPrefixMapping());
			prefixes.removeNsPrefix("tarql");
			result.getPrologue().setPrefixMapping(prefixes);
		}
	}
}
