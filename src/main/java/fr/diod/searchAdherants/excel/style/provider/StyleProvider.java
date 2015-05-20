package fr.diod.searchAdherants.excel.style.provider;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

public interface StyleProvider {

	/**
	 * Initialize Style Provider
	 * @param wb
	 */
	void initStyle(Workbook wb);
	
	/**
	 * Returns Cells Style format according to the score
	 * @param score
	 * @return
	 */
	CellStyle get(int score);

	/**
	 * Should you add a Comment according to the score
	 * @param score
	 * @return true if a comment should be added
	 */
	boolean needsComment(int score);

}
