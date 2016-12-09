import java.io.FileInputStream;
import java.util.ArrayList;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

public class MyXls2Csv {

	FormulaEvaluator evaluator;
	DataFormatter formatter;

	private static String rowToCSV(Row row, FormulaEvaluator evaluator, DataFormatter formatter) {
		if (row == null)
			return "";

		StringBuilder sb = new StringBuilder();
		Cell cell = null;
		int lastCellNum = 0;

		lastCellNum = row.getLastCellNum();
		for (int i = 0; i <= lastCellNum; i++) {
			cell = row.getCell(i);
			String value = "";
			if (cell != null) {
				value = formatter.formatCellValue(cell, evaluator);
/*				if (cell.getCellTypeEnum() != CellType.FORMULA) {
					value = formatter.formatCellValue(cell);
				} else {
					value = formatter.formatCellValue(cell, evaluator);
				}
*/
			}
			if (i > 0)
				sb.append("\t");
			sb.append(value);
		}
		return sb.toString();
	}

	private void convertToCSV(Workbook workbook) {
		Sheet sheet = null;
		Row row = null;
		int lastRowNum = 0;

		int numSheets = workbook.getNumberOfSheets();
		for (int i = 0; i < numSheets; i++) {
			sheet = workbook.getSheetAt(i);
			StringBuilder sb = new StringBuilder();
			if (sheet.getPhysicalNumberOfRows() > 0) {
				lastRowNum = sheet.getLastRowNum();
				for (int j = 0; j <= lastRowNum; j++) {
					row = sheet.getRow(j);
					sb.append(rowToCSV(row, evaluator, formatter));
					sb.append('\n');
				}
			}
			System.out.println("--------- " + sheet.getSheetName());
			System.out.println(sb.toString());
		}
	}

	void doIt() throws Exception {
		FileInputStream fis = new FileInputStream("/home/spetrov/vod_series_data proposal from Slavyan.xlsx");

		Workbook workbook = WorkbookFactory.create(fis);
		evaluator = workbook.getCreationHelper().createFormulaEvaluator();
		formatter = new DataFormatter(true);
		fis.close();
		
		convertToCSV(workbook);

		System.out.println();
	}
	
	public static void main(String[] args) throws Exception {
		new MyXls2Csv().doIt();
		System.out.println("Done.");
	}
}
