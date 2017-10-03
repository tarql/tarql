package org.deri.tarql;

import static org.deri.tarql.Helpers.binding;
import static org.deri.tarql.Helpers.removePseudoVars;
import static org.deri.tarql.Helpers.vars;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.junit.Test;


public class CSVParserTest {

	@Test
	public void testColumnName() {
		assertEquals("a", CSVParser.getColumnName(0));
		assertEquals("b", CSVParser.getColumnName(1));
		assertEquals("c", CSVParser.getColumnName(2));
		assertEquals("z", CSVParser.getColumnName(25));
		assertEquals("aa", CSVParser.getColumnName(26));
		assertEquals("ab", CSVParser.getColumnName(27));
	}
	
	@Test
	public void testCountVars() throws IOException {
		String csv = "1\n1,1,1\n1,1";
		assertEquals(3, countRows(csv, false));
	}
	
	
	@Test
	public void testHeading() throws IOException {
		String csv = "1,2,3,4,5";
		assertEquals(vars("a", "b", "c", "d", "e"), getNonPseudoVars(csv, false));
	}
	
	@Test
	public void testUnbound() throws IOException {
		String csv = "1\n1,1";
		Binding binding = readCSV(csv, false).next();
		assertEquals(null, binding.get(Var.alloc("b")));
	}

	
	@Test
	public void testNoEmptyStrings() throws IOException {
		String csv = ",1";
		assertEquals(null, readCSV(csv, false).next().get(Var.alloc("a")));
	}
	
	@Test
	public void testSkipEmptyLines() throws IOException {
		String csv = "\n,,,,\n1";
		assertEquals(1, countRows(csv, false));
	}

	@Test
	public void testWithHeaders() throws IOException {
		String csv = "X,Y\n1,2";
		assertEquals(1, countRows(csv, true));
		assertEquals(vars("X", "Y"), getNonPseudoVars(csv, true));
		assertEquals(binding(vars("X", "Y"), "\"1\"", "\"2\""), removePseudoVars(readCSV(csv,true).next()));
	}
	
	@Test
	public void testSkipEmptyRowsBeforeHeader() throws IOException {
		String csv = "\n\nX,Y\n1,2";
		assertEquals(vars("X", "Y"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testFillAdditionalColumnsNotInHeader() throws IOException {
		String csv = "X\n1,2,3";
		assertEquals(vars("X", "b", "c"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testFillNonColumnsInHeader() throws IOException {
		String csv = "X,,Y\n1,2,3";
		assertEquals(vars("X", "b", "Y"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testHandleSpacesInColumnNames() throws IOException {
		String csv = "Total Value\n123";
		assertEquals(vars("Total_Value"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testHandleDashesInColumnNames() throws IOException {
		String csv = "Total-Value\n123";
		assertEquals(vars("Total_Value"), getNonPseudoVars(csv, true));
	}
	@Test
	public void testHandleQuestionMarkInColumnNames() throws IOException {
		String csv = "Is-Estimated?\nYes";
		assertEquals(vars("Is_Estimated_"), getNonPseudoVars(csv, true));
	}

	@Test
	public void testHandlePercentInColumnNames() throws IOException {
		String csv = "Profit%\n80";
		assertEquals(vars("Profit_"), getNonPseudoVars(csv, true));
	}

	@Test
	public void testHandleRoundBracketsInColumnNames() throws IOException {
		String csv = "Weight(mg)\n0.33";
		assertEquals(vars("Weight_mg_"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testDuplicateColumnName() throws IOException {
		String csv = "X,X\n1,2";
		assertEquals(vars("X", "b"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testHandleClashWhenFillingInVarNames1() throws IOException {
		String csv = "a,b,,c";
		assertEquals(vars("a", "b", "c", "d"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testHandleClashWhenFillingInVarNames2() throws IOException {
		String csv = "a,c,,d";
		assertEquals(vars("a", "c", "_c", "d"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testAssignNewNameToReservedColumnName() throws IOException {
		String csv = "ROWNUM";
		assertEquals(vars("a"), getNonPseudoVars(csv, true));
	}
	
	@Test
	public void testIncludesROWNUM() throws IOException {
		String csv = "a,b";
		assertEquals(vars("a", "b", "ROWNUM"), readCSV(csv, true).getVars());
	}
	
	@Test
	public void testEmptyColumn() throws IOException {
		String csv = "x,,y";
		assertEquals(vars("a", "b", "c"), getNonPseudoVars(csv, false));
	}

	@Test
	public void testTabSeparated() throws IOException {
		String csv = "foo\tbar\n1\t2";
		List<Var> vars = vars("foo", "bar");
		assertEquals(binding(vars, "\"1\"", "\"2\""), removePseudoVars(readCSV(csv, true, '\t', '"').next()));
	}
	
	@Test
	public void testSemicolonSeparated() throws IOException {
		String csv = "foo;bar\n1,5;2,0";
		assertEquals(binding(vars("foo", "bar"), "\"1,5\"", "\"2,0\""), 
				removePseudoVars(readCSV(csv, true, ';', '"').next()));
	}

	@Test
	public void testStandardQuotes() throws IOException {
		String csv = "Value\n\"This, too\"";
		assertEquals(binding(vars("Value"), "\"This, too\""), 
				removePseudoVars(readCSV(csv, true).next()));
	}
	
	@Test
	public void testSingleQuotes() throws IOException {
		String csv = "Value\n\'This, too\'";
		assertEquals(binding(vars("Value"), "\"This, too\""), 
				removePseudoVars(readCSV(csv, true, ',', '\'').next()));
	}
	
	@Test
	public void testQuoteDoubling() throws IOException {
		String csv = "Value\nJoseph \"\"Joe\"\" Smith";
		assertEquals(binding(vars("Value"), "\"Joseph \\\"Joe\\\" Smith\""), 
				removePseudoVars(readCSV(csv, true).next()));
	}
	
	@Test
	public void testEscapingQuoteWithBackslash() throws IOException {
		String csv = "Value\n\"Joseph \\\"Joe\\\" Smith\"";
		assertEquals(binding(vars("Value"), "\"Joseph \\\"Joe\\\" Smith\""), 
				removePseudoVars(readCSV(csv, true, '\\').next()));
	}

	/* OpenCSV only uses the escape character for quotes, not for delimiters */
	//@Test
	public void testEscapingDelimiterWithBackslash() throws IOException {
		String csv = "Value\nThis\\, too";
		assertEquals(binding(vars("Value"), "\"This, too\""), 
				removePseudoVars(readCSV(csv, true, '\\').next()));
	}
	
	private static CSVParser readCSV(String csv, boolean varsFromHeader) throws IOException {
		return new CSVParser(new StringReader(csv), varsFromHeader, null, '"', null);
	}
	
	private static CSVParser readCSV(String csv, boolean varsFromHeader, char delimiter, char quote) throws IOException {
		return new CSVParser(new StringReader(csv), varsFromHeader, delimiter, quote, null);
	}
	
	private static CSVParser readCSV(String csv, boolean varsFromHeader, char escape) throws IOException {
		return new CSVParser(new StringReader(csv), varsFromHeader, null, '"', escape);
	}
	
	private static long countRows(String csv, boolean varsFromHeader) throws IOException {
		Iterator<Binding> table = readCSV(csv, varsFromHeader);
		long count = 0;
		while(table.hasNext()) {
			table.next();
			count +=1;
		}
		return count;
	}
	
	private static List<Var> getNonPseudoVars(String csv, boolean varsFromHeader) throws IOException {
		CSVParser table = readCSV(csv, varsFromHeader);
		while(table.hasNext()) {
			table.next();
		}
		List<Var> result = new ArrayList<Var>(table.getVars());
		result.remove(TarqlQuery.ROWNUM);
		return result;
	}
}
