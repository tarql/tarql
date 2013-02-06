package org.deri.tarql;

import static org.deri.tarql.Helpers.vars;
import static org.deri.tarql.Helpers.binding;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;

import org.junit.Test;

import com.hp.hpl.jena.sparql.algebra.table.TableData;
import com.hp.hpl.jena.sparql.core.Var;

public class CSVToValuesTest {

	@Test
	public void testColumnName() {
		assertEquals("a", CSVToValues.getColumnName(0));
		assertEquals("b", CSVToValues.getColumnName(1));
		assertEquals("c", CSVToValues.getColumnName(2));
		assertEquals("z", CSVToValues.getColumnName(25));
		assertEquals("aa", CSVToValues.getColumnName(26));
		assertEquals("ab", CSVToValues.getColumnName(27));
	}
	
	@Test
	public void testCountVars() {
		String csv = "1\n1,1,1\n1,1";
		assertEquals(3, readCSV(csv, false).getVars().size());
	}
	
	@Test
	public void testHeading() {
		String csv = "1,2,3,4,5";
		assertEquals(vars("a", "b", "c", "d", "e"), readCSV(csv, false).getVars());
	}
	
	@Test
	public void testUnbound() {
		String csv = "1\n1,1";
		assertEquals(null, readCSV(csv, false).getRows().get(0).get(Var.alloc("b")));
	}

	@Test
	public void testNoEmptyStrings() {
		String csv = ",1";
		assertEquals(null, readCSV(csv, false).getRows().get(0).get(Var.alloc("a")));
	}
	
	@Test
	public void testSkipEmptyLines() {
		String csv = "\n,,,,\n1";
		assertEquals(1, readCSV(csv, false).getRows().size());
	}

	@Test
	public void testWithHeaders() {
		String csv = "X,Y\n1,2";
		TableData table = readCSV(csv, true);
		assertEquals(1, table.getRows().size());
		assertEquals(vars("X", "Y"), table.getVars());
		assertEquals(binding(vars("X", "Y"), "\"1\"", "\"2\""), table.getRows().get(0));
	}
	
	@Test
	public void testSkipEmptyRowsBeforeHeader() {
		String csv = "\n\nX,Y\n1,2";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "Y"), table.getVars());
	}
	
	@Test
	public void testFillAdditionalColumnsNotInHeader() {
		String csv = "X\n1,2,3";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "b", "c"), table.getVars());
	}
	
	@Test
	public void testFillNonColumnsInHeader() {
		String csv = "X,,Y\n1,2,3";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "b", "Y"), table.getVars());
	}
	
	@Test
	public void testHandleSpacesInColumnNames() {
		String csv = "Total Value\n123";
		TableData table = readCSV(csv, true);
		assertEquals(vars("Total_Value"), table.getVars());
	}
	
	@Test
	public void testDuplicateColumnName() {
		String csv = "X,X\n1,2";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "b"), table.getVars());
	}
	
	@Test
	public void testHandleClashWhenFillingInVarNames1() {
		String csv = "a,b,,c";
		TableData table = readCSV(csv, true);
		assertEquals(vars("a", "b", "c", "d"), table.getVars());
	}
	
	@Test
	public void testHandleClashWhenFillingInVarNames2() {
		String csv = "a,c,,d";
		TableData table = readCSV(csv, true);
		assertEquals(vars("a", "c", "_c", "d"), table.getVars());
	}
	
	private static TableData readCSV(String csv, boolean varsFromHeader) {
		return new CSVToValues(new StringReader(csv), varsFromHeader).read();
	}
}
