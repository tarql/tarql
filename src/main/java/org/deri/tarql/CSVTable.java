package org.deri.tarql;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.algebra.Table;
import org.apache.jena.sparql.algebra.table.TableBase;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.ExecutionContext;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.iterator.QueryIterPlainWrapper;
import org.apache.jena.util.iterator.ClosableIterator;


/**
 * Implementation of ARQ's {@link Table} interface over a CSV file.
 * Supports opening multiple iterators over the input, which
 * will lead to multiple passes over the input CSV file.
 * Connects to the input as lazily as possible, while still
 * supporting the entire Table interface including {@link #size()}.
 */
public class CSVTable extends TableBase implements Table {
	private final InputStreamSource source;
	private final CSVOptions options;
	private final List<ClosableIterator<Binding>> openIterators = new ArrayList<ClosableIterator<Binding>>();
	private ClosableIterator<Binding> nextParser = null;
	private List<Var> varsCache = null;
	private Boolean isEmptyCache = null;
	private Integer sizeCache = null;
	
	public CSVTable(InputStreamSource source) {
		this(source, new CSVOptions());
	}

	public CSVTable(InputStreamSource source, CSVOptions options) {
		this.source = source;
		this.options = options;
	}
	
	@Override
	public QueryIterator iterator(ExecutionContext ctxt) {
		// QueryIteratorPlainWrapper doesn't close wrapped 
		// ClosableIterators, so we do that ourselves.
		final ClosableIterator<Binding> wrapped = rows();
		return new QueryIterPlainWrapper(wrapped, ctxt) {
			@Override
			protected void closeIterator() {
				super.closeIterator();
				wrapped.close();
			}
		};
	}

	@Override
	public ClosableIterator<Binding> rows() {
		ensureHasParser();
		final ClosableIterator<Binding> wrappedIterator = nextParser;
		nextParser = null;
		// We will add a wrapper to the iterator that removes it
		// from the list of open iterators once it is closed and
		// exhausted, and that fills the size cache once the
		// iterator is exhausted.
		return new ClosableIterator<Binding>() {
			private int count = 0;
			@Override
			public boolean hasNext() {
				if (wrappedIterator.hasNext()) return true;
				if (sizeCache == null) sizeCache = count;
				openIterators.remove(wrappedIterator);
				return false;
			}
			@Override
			public Binding next() {
				count++;
				return wrappedIterator.next();
			}
			@Override
			public void remove() {
				wrappedIterator.remove();
			}
			@Override
			public void close() {
				openIterators.remove(wrappedIterator);
				wrappedIterator.close();
			}
		};
	}
	
	@Override
	public List<Var> getVars() {
		ensureHasParser();
		return varsCache;
	}
	
	@Override
	public List<String> getVarNames() {
		return Var.varNames(getVars());
	}

	/**
	 * Returns <code>true</code> if the table has zero rows.
	 * Is fast.
	 */
	@Override
	public boolean isEmpty() {
		ensureHasParser();
		return isEmptyCache;
	}

	/**
	 * Returns the number of rows in the table. Is fast if an iterator
	 * over the table has already been exhausted. Otherwise, it will
	 * make a complete parsing pass over the input.
	 */
	@Override
	public int size() {
		if (sizeCache == null) {
			// This fills the cache.
			Iterator<Binding> it = rows();
			while (it.hasNext()) it.next();
		}
		return sizeCache;
	}
	
	/**
	 * Closes any open iterators over the table.
	 */
	@Override
	public void closeTable() {
		while (!openIterators.isEmpty()) {
			ClosableIterator<Binding> next = openIterators.remove(0);
			next.close();
		}
	}
	
	private void ensureHasParser() {
		if (nextParser == null) {
			CSVParser parser = createParser();
			if (varsCache == null) {
				varsCache = parser.getVars();
			}
			if (isEmptyCache == null) {
				isEmptyCache = !parser.hasNext();
			}
			nextParser = parser;
		}
	}
	
	private CSVParser createParser() {
		try {
			CSVParser result = options.openParserFor(source);
			openIterators.add(result);
			return result;
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}
	}
}
