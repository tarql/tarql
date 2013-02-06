package org.deri.tarql;

import java.io.Reader;

import org.openjena.atlas.logging.Log;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryException;
import com.hp.hpl.jena.query.QueryParseException;
import com.hp.hpl.jena.shared.JenaException;
import com.hp.hpl.jena.sparql.lang.SyntaxVarScope;
import com.hp.hpl.jena.sparql.lang.sparql_11.ParseException;
import com.hp.hpl.jena.sparql.lang.sparql_11.SPARQLParser11;

public class TarqlParser {
	
	public static TarqlQuery parse(Reader reader) {
		return new TarqlParser(reader).getResult();
	}
	
	private final Reader reader;
	private final TarqlQuery result = new TarqlQuery();
	private boolean done = false;
	private boolean seenSelectOrAsk = false;
	
	public TarqlParser(Reader reader) {
		this.reader = reader;
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
		} while (parser.getToken(1).kind != SPARQLParser11.EOF);
	}

	// Adapted from ARQ ParserSPARQL11.java
	private void parse() {
		if (done) return;
		done = true;
		SPARQLParser11 parser = new SPARQLParser11(reader) ;
		try {
			parseDo(parser);
		} catch (com.hp.hpl.jena.sparql.lang.sparql_11.ParseException ex) { 
			throw new QueryParseException(ex.getMessage(),
					ex.currentToken.beginLine,
					ex.currentToken.beginColumn);
		} catch (com.hp.hpl.jena.sparql.lang.sparql_11.TokenMgrError tErr) {
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
}
