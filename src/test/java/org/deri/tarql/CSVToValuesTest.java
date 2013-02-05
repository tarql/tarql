package org.deri.tarql;

import static org.deri.tarql.Helpers.vars;
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
		assertEquals(3, readCSV(csv).getVars().size());
	}
	
	@Test
	public void testHeading() {
		String csv = "1,2,3,4,5";
		assertEquals(vars("a", "b", "c", "d", "e"), readCSV(csv).getVars());
	}
	
	@Test
	public void testUnbound() {
		String csv = "1\n1,1";
		assertEquals(null, readCSV(csv).getRows().get(0).get(Var.alloc("b")));
	}

	@Test
	public void testNoEmptyStrings() {
		String csv = ",1";
		assertEquals(null, readCSV(csv).getRows().get(0).get(Var.alloc("a")));
	}
	
	@Test
	public void testSkipEmptyLines() {
		String csv = "\n,,,,\n1";
		assertEquals(1, readCSV(csv).getRows().size());
	}
	
	private static TableData readCSV(String csv) {
		return new CSVToValues(new StringReader(csv)).read();
	}
}
