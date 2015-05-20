package fr.diod.searchAdherants.excel.style.provider;

import fr.diod.searchAdherants.excel.ExcelSearch;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class LevelStyleProvider implements StyleProvider {

	private NavigableMap<Integer, Short> colors = new TreeMap<Integer, Short>();
	private Map<Integer, CellStyle> mapCellStyle = new HashMap<Integer, CellStyle>();
	private Workbook wb;
	private int minScoreForComments;
	
	
	public void initStyle(Workbook wb) {
		this.wb = wb;
	}
	
	public void addLevel(int level, short color) {
		colors.put(level, color);
	}
	
	@Override
	public CellStyle get(int score) {
		Entry<Integer, Short> colorEntry = colors.floorEntry(score);
		
		if (!mapCellStyle.containsKey(colorEntry.getKey())) {
			CellStyle style = createStyle(colorEntry.getValue());
			
			mapCellStyle.put(colorEntry.getKey(), style);
		}
		return mapCellStyle.get(colorEntry.getKey());
	}

	CellStyle createStyle(short colorIndex) {
		return ExcelSearch.createCellStyle(wb, colorIndex);
	}

	@Override
	public boolean needsComment(int score) {
		return minScoreForComments <= score;
	}
	
	/**
	 * Sets the minimum score needed to display comments
	 * @param minScoreForComments score between 0 to 100
	 */
	public void setMinScoreForComments(int minScoreForComments) {
		this.minScoreForComments = minScoreForComments;
	}
}
