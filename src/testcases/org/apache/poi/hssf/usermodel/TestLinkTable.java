package org.apache.poi.hssf.usermodel;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.apache.poi.hssf.HSSFTestDataSamples;
/**
 * Tests for LinkTable
 *
 * @author Josh Micich
 */
public final class TestLinkTable extends TestCase {

	/**
	 * The example file attached to bugzilla 45046 is a clear example of Name records being present
	 * without an External Book (SupBook) record.  Excel has no trouble reading this file.<br/>
	 * TODO get OOO documentation updated to reflect this (that EXTERNALBOOK is optional).
	 *
	 * It's not clear what exact steps need to be taken in Excel to create such a workbook
	 */
	public void testLinkTableWithoutExternalBookRecord_bug45046() {
		HSSFWorkbook wb;

		try {
			wb = HSSFTestDataSamples.openSampleWorkbook("ex45046-21984.xls");
		} catch (RuntimeException e) {
			if ("DEFINEDNAME is part of LinkTable".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 45046 b");
			}
			throw e;
		}
		// some other sanity checks
		assertEquals(3, wb.getNumberOfSheets());
		String formula = wb.getSheetAt(0).getRow(4).getCell(13).getCellFormula();

		if ("ipcSummenproduktIntern($P5,N$6,$A$9,N$5)".equals(formula)) {
			// The reported symptom of this bugzilla is an earlier bug (already fixed)
			throw new AssertionFailedError("Identified bug 41726");
			// This is observable in version 3.0
		}

		assertEquals("ipcSummenproduktIntern($C5,N$2,$A$9,N$1)", formula);
	}

	public void testMultipleExternSheetRecords_bug45698() {
		HSSFWorkbook wb;

		try {
			wb = HSSFTestDataSamples.openSampleWorkbook("ex45698-22488.xls");
		} catch (RuntimeException e) {
			if ("Extern sheet is part of LinkTable".equals(e.getMessage())) {
				throw new AssertionFailedError("Identified bug 45698");
			}
			throw e;
		}
		// some other sanity checks
		assertEquals(7, wb.getNumberOfSheets());
	}

	public void testExtraSheetRefs_bug45978() {
		HSSFWorkbook wb = HSSFTestDataSamples.openSampleWorkbook("ex45978-extraLinkTableSheets.xls");
		/*
		ex45978-extraLinkTableSheets.xls is a cut-down version of attachment 22561.
		The original file produces the same error.

		This bug was caused by a combination of invalid sheet indexes in the EXTERNSHEET
		record, and eager initialisation of the extern sheet references. Note - the worbook
		has 2 sheets, but the EXTERNSHEET record refers to sheet indexes 0, 1 and 2.

		Offset 0x3954 (14676)
		recordid = 0x17, size = 32
		[EXTERNSHEET]
		   numOfRefs	 = 5
		refrec		 #0: extBook=0 firstSheet=0 lastSheet=0
		refrec		 #1: extBook=1 firstSheet=2 lastSheet=2
		refrec		 #2: extBook=2 firstSheet=1 lastSheet=1
		refrec		 #3: extBook=0 firstSheet=-1 lastSheet=-1
		refrec		 #4: extBook=0 firstSheet=1 lastSheet=1
		[/EXTERNSHEET]

		As it turns out, the formula in question doesn't even use externSheetIndex #1 - it
		uses #4, which resolves to sheetIndex 1 -> 'Data'.

		It is not clear exactly what externSheetIndex #4 would refer to.  Excel seems to
		display such a formula as "''!$A2", but then complains of broken link errors.
		*/

		HSSFCell cell = wb.getSheetAt(0).getRow(1).getCell(1);
		String cellFormula;
		try {
			cellFormula = cell.getCellFormula();
		} catch (IndexOutOfBoundsException e) {
			if (e.getMessage().equals("Index: 2, Size: 2")) {
				throw new AssertionFailedError("Identified bug 45798");
			}
			throw e;
		}
		assertEquals("Data!$A2", cellFormula);
	}
}
