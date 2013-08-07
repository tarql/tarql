package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.removePseudoVars;
import static org.deri.tarql.Helpers.vars;
import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

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
		assertEquals(3, getNonPseudoVars(readCSV(csv, false)).size());
	}
	
	@Test
	public void testHeading() {
		String csv = "1,2,3,4,5";
		assertEquals(vars("a", "b", "c", "d", "e"), getNonPseudoVars(readCSV(csv, false)));
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
		assertEquals(vars("X", "Y"), getNonPseudoVars(table));
		assertEquals(binding(vars("X", "Y"), "\"1\"", "\"2\""), removePseudoVars(table.getRows().get(0)));
	}
	
	@Test
	public void testSkipEmptyRowsBeforeHeader() {
		String csv = "\n\nX,Y\n1,2";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "Y"), getNonPseudoVars(table));
	}
	
	@Test
	public void testFillAdditionalColumnsNotInHeader() {
		String csv = "X\n1,2,3";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "b", "c"), getNonPseudoVars(table));
	}
	
	@Test
	public void testFillNonColumnsInHeader() {
		String csv = "X,,Y\n1,2,3";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "b", "Y"), getNonPseudoVars(table));
	}
	
	@Test
	public void testHandleSpacesInColumnNames() {
		String csv = "Total Value\n123";
		TableData table = readCSV(csv, true);
		assertEquals(vars("Total_Value"), getNonPseudoVars(table));
	}
	
	@Test
	public void testDuplicateColumnName() {
		String csv = "X,X\n1,2";
		TableData table = readCSV(csv, true);
		assertEquals(vars("X", "b"), getNonPseudoVars(table));
	}
	
	@Test
	public void testHandleClashWhenFillingInVarNames1() {
		String csv = "a,b,,c";
		TableData table = readCSV(csv, true);
		assertEquals(vars("a", "b", "c", "d"), getNonPseudoVars(table));
	}
	
	@Test
	public void testHandleClashWhenFillingInVarNames2() {
		String csv = "a,c,,d";
		TableData table = readCSV(csv, true);
		assertEquals(vars("a", "c", "_c", "d"), getNonPseudoVars(table));
	}
	
	@Test
	public void testAssignNewNameToReservedColumnName() {
		String csv = "ROWNUM";
		TableData table = readCSV(csv, true);
		assertEquals(vars("a"), getNonPseudoVars(table));
	}
	
	@Test
	public void testIncludesROWNUM() {
		String csv = "a,b";
		TableData table = readCSV(csv, true);
		assertEquals(vars("a", "b", "ROWNUM"), table.getVars());
	}
	
	private static TableData readCSV(String csv, boolean varsFromHeader) {
		return new CSVToValues(new StringReader(csv), varsFromHeader).read();
	}
	
	private static List<Var> getNonPseudoVars(TableData table) {
		List<Var> result = new ArrayList<Var>(table.getVars());
		result.remove(TarqlQuery.ROWNUM);
		return result;
	}
}
