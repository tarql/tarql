package org.deri.tarql;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.List;

import com.hp.hpl.jena.sparql.algebra.table.TableBase;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.engine.ExecutionContext;
import com.hp.hpl.jena.sparql.engine.QueryIterator;
import com.hp.hpl.jena.sparql.engine.binding.Binding;
import com.hp.hpl.jena.sparql.engine.iterator.QueryIterPlainWrapper;

public class CSVTable extends TableBase{

	protected final CSVToValues csvWrapper;
	
	public CSVTable(Reader reader, boolean varsFromHeader) throws IOException {
		csvWrapper = new CSVToValues(reader, varsFromHeader);
	}
	
	@Override
	public List<String> getVarNames() {
		return Var.varNames(csvWrapper.getVars());
	}

	@Override
	public List<Var> getVars() {
		return csvWrapper.getVars();
	}

	@Override
	public QueryIterator iterator(ExecutionContext ctxt) {
		return new QueryIterPlainWrapper(csvWrapper,ctxt);
	}

	@Override
	public Iterator<Binding> rows() {
		return csvWrapper;
	}

	@Override
	protected void closeTable() {
		try {
			csvWrapper.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int size() {
		throw new UnsupportedOperationException("Streamed CSVTable cannot answer a call to its size method");
	}

	public void reset() throws IOException{
		csvWrapper.reset();
	}
}
