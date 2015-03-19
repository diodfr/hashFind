package fr.diod.searchAdherants.excel.style.provider;

import fr.diod.searchAdherants.excel.ExcelSearch;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class DefaultStyleProvider implements StyleProvider {

	private CellStyle styleFound;
	private CellStyle styleNearlyFound;
	
	public void initStyle(Workbook wb) {
		styleFound = ExcelSearch.createCellStyle(wb, IndexedColors.BRIGHT_GREEN.getIndex());
		styleNearlyFound = ExcelSearch.createCellStyle(wb, IndexedColors.ORANGE.getIndex());
	}
	
	@Override
	public CellStyle get(int score) {
		return score == 100 ? styleFound : styleNearlyFound;
	}

}
