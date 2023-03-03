package org.deri.tarql;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.apache.jena.atlas.logging.Log;
import org.apache.jena.irix.IRIs;
import org.apache.jena.irix.IRIx;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryException;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.riot.system.stream.StreamManager;
import org.apache.jena.shared.JenaException;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.shared.PrefixMapping;
import org.apache.jena.shared.impl.PrefixMappingImpl;
import org.apache.jena.sparql.ARQConstants;
import org.apache.jena.sparql.lang.SyntaxVarScope;
import org.apache.jena.sparql.lang.arq.ARQParser;
import org.apache.jena.sparql.lang.arq.ParseException;
import org.apache.jena.sparql.lang.arq.TokenMgrError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Parses a {@link TarqlQuery} provided as a string or reader.
 */
public class TarqlParser {
	private final static Logger log = LoggerFactory.getLogger(TarqlParser.class);

	private final static PrefixMapping builtInPrefixes = new PrefixMappingImpl() {{
		setNsPrefix("tarql", tarql.NS);
		setNsPrefix("apf", ARQConstants.ARQPropertyFunctionLibraryURI);
		setNsPrefix("afn", ARQConstants.ARQFunctionLibraryURI);
	}};

	private final Reader reader;
	private final TarqlQuery result = new TarqlQuery();
	private boolean done = false;
	private boolean seenSelectOrAsk = false;

	public TarqlParser(String filenameOrURL) {
		this(open(filenameOrURL), null);
	}

	public TarqlParser(String filenameOrURL, String baseIRI) {
		this(open(filenameOrURL), baseIRI(baseIRI, filenameOrURL));
	}

    public TarqlParser(Reader reader) {
		this(reader, null);
	}

	public TarqlParser(Reader reader, String baseIRI) {
		this.reader = reader;
		if ( baseIRI != null )
		    // This causes a BASE in the output.
		    result.getPrologue().setBase(IRIx.create(baseIRI));
		addBuiltInPrefixes();
	}

	// Decide on the base URI
	// Base URIs are, where possible, resolved, absolute URIs.
    private static String baseIRI(String baseIRI, String dftBaseURI) {
        if ( baseIRI != null )
            return IRIs.resolve(baseIRI);
        if ( dftBaseURI != null )
            return IRIs.resolve(dftBaseURI);
        return null;
    }

	private static Reader open(String filenameOrURL) {
		try {
			InputStream in = StreamManager.get().open(filenameOrURL);
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

	private void parseDo(ARQParser parser) throws ParseException {
		do {
			int beginLine = parser.getToken(1).beginLine;
			int beginColumn = parser.getToken(1).beginColumn;

			Query query = new Query(result.getPrologue());

			result.addQuery(query);
			parser.setQuery(query);
			parser.ByteOrderMark();
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

			result.setPrologue(query);
			if (log.isDebugEnabled()) {
				log.debug(query.toString());
			}
		} while (parser.getToken(1).kind != ARQParser.EOF);
		removeBuiltInPrefixes();
	}

	// Adapted from ARQ ParserSPARQL11.java
	private void parse() {
		if (done) return;
		done = true;
		ARQParser parser = new ARQParser(reader) ;
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

    private void addBuiltInPrefixes() {
        for ( String prefix : builtInPrefixes.getNsPrefixMap().keySet() ) {
            if ( result.getPrologue().getPrefix(prefix) != null )
                continue;
            result.getPrologue().getPrefixMapping().setNsPrefix(prefix, builtInPrefixes.getNsPrefixURI(prefix));
        }
    }

	private void removeBuiltInPrefixes() {
		PrefixMapping prefixes = null;
		for (String prefix: builtInPrefixes.getNsPrefixMap().keySet()) {
			String uri = builtInPrefixes.getNsPrefixURI(prefix);
			if (!uri.equals(result.getPrologue().getPrefix(prefix))) continue;
			if (prefixes == null) {
				prefixes = new PrefixMappingImpl();
				prefixes.setNsPrefixes(result.getPrologue().getPrefixMapping());

			}
			prefixes.removeNsPrefix(prefix);
		}
		if (prefixes != null) {
			result.getPrologue().setPrefixMapping(prefixes);
		}
	}
}
