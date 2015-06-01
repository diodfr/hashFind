package fr.diod.searchAdherants.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExcelTool {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelTool.class);

	public static final String formatCell(Cell cell) {
		if (cell == null) return "";
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			return cell.getRichStringCellValue().getString();
		case Cell.CELL_TYPE_NUMERIC:
			if (DateUtil.isCellDateFormatted(cell)) {
				return cell.getDateCellValue().toString();
			} else {
				return cell.getNumericCellValue() + "";
			}
		case Cell.CELL_TYPE_BOOLEAN:
			return cell.getBooleanCellValue() + "";
		case Cell.CELL_TYPE_FORMULA:
			return cell.getCellFormula();
		default:
			return "EMPTY";
		}
	}

	public static final boolean isString(Cell cell) {
		if (cell != null) {
			return Cell.CELL_TYPE_STRING == cell.getCellType();
		} else {
			return true;
		}
	}

	public static final String getString(Cell cell) {
		if (cell != null) {
			return cell.getRichStringCellValue().getString();
		} else {
			return "";
		}

	}

	public static final Date getDate(Cell cell) {
		if (cell != null) {
			return cell.getDateCellValue();
		} else {
			return null;
		}
	}

	public static final boolean isDate(Cell cell) {
		if (cell != null) {
			return Cell.CELL_TYPE_NUMERIC == cell.getCellType() && DateUtil.isCellDateFormatted(cell);
		} else return false;
	}

	public static List<Adherant> populate(String fileName, int sheetNumber, String nom, String nom_jeune_fille, String prenom, String date, String portable, String telephone, String email ) {
		return populate(new File(fileName), sheetNumber, nom, nom_jeune_fille, prenom, date, portable, telephone, email);
	}

	/**
	 * Reads an excel file to a list of adherants.
	 * @param file excel file
	 * @param sheetNumber sheet of the excel file
	 * @param nom label entête colonne nom
	 * @param nom_jeune_fille label entête colonne nom de jeune fille
	 * @param prenom label entête colonne prenom
	 * @param date label entête colonne birthday
	 * @param portable label entête colonne portable
	 * @param telephone label entête colonne telephone
	 * @param email label entête colonne email
	 * @return une liste d'adherant
	 */
	public static List<Adherant> populate(File file, int sheetNumber, String nom, String nom_jeune_fille, String prenom, String date, String portable, String telephone, String email ) {	
		List<Adherant> adherants = new ArrayList<Adherant>();

		try {
			Workbook wb = WorkbookFactory.create(file);
			Sheet sheet = wb.getSheetAt(sheetNumber);

			LOGGER.info("=>Populate DataBase with sheet {}", sheet.getSheetName());

			ColumnIdx columnIdx = searchColumnIndex(nom, nom_jeune_fille, prenom, date, portable, telephone, email, sheet);

			LOGGER.debug("col IDx for name {} maiden {} first {} birth {} mobile {} telephone {} email {}",
					columnIdx.nameIdx, columnIdx.nameJFIdx, columnIdx.firstNameIdx, columnIdx.dateIdx, columnIdx.portableIdx, columnIdx.telephoneIdx, columnIdx.emailIdx);

			boolean firstRow = true;
			for (Row row : sheet) {
				if (firstRow) {
					firstRow = false;
					continue;
				}

				LOGGER.trace("row {}", row.getRowNum());
				Adherant adherant = retrieveAdherant(columnIdx, row);

				if (adherant != null) {
					LOGGER.debug("{}",adherant.toString());
					adherants.add(adherant);
				}
			}

			LOGGER.info("populate with {} adherants ", adherants.size());

		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return adherants;
	}

	/**
	 * Récupère un adherant sur une ligne.
	 * @param columnIdx
	 * @param row
	 * @return
	 */
	private static Adherant retrieveAdherant(ColumnIdx columnIdx, Row row) {
		if (columnIdx.nameIdx < 0) return null;
		
		Adherant adherant = new Adherant();
		Cell cellName = row.getCell(columnIdx.nameIdx);
		if (cellName == null || cellName.getCellType() == Cell.CELL_TYPE_BLANK) {
			adherant = null;
		} else {
			adherant.name = ExcelTool.getString(cellName);
			LOGGER.debug("Name : {}", adherant.name);
			if (columnIdx.nameJFIdx > 0)
				adherant.maidenName = ExcelTool.getString(row.getCell(columnIdx.nameJFIdx));
			if (columnIdx.firstNameIdx > 0)
				adherant.firstName = ExcelTool.getString(row.getCell(columnIdx.firstNameIdx));
			if (columnIdx.dateIdx > 0) {
				Cell cell = row.getCell(columnIdx.dateIdx);
				if (ExcelTool.isDate(cell)) {
					adherant.birth = ExcelTool.getDate(cell).toString();
				}
			}
			if (columnIdx.portableIdx > 0)
				adherant.portable = ExcelTool.getString(row.getCell(columnIdx.portableIdx));
			if (columnIdx.telephoneIdx > 0)
				adherant.telephone = ExcelTool.getString(row.getCell(columnIdx.telephoneIdx));
			if (columnIdx.emailIdx > 0)
				adherant.email = ExcelTool.getString(row.getCell(columnIdx.emailIdx));

		}
		return adherant;
	}

	/**
	 * Cherche les indexes des colonnes
	 * @param nom
	 * @param nom_jeune_fille
	 * @param prenom
	 * @param date
	 * @param portable
	 * @param telephone
	 * @param email
	 * @param sheet
	 * @return
	 */
	private static ColumnIdx searchColumnIndex(String nom, String nom_jeune_fille, String prenom, String date, String portable, String telephone, String email, Sheet sheet) {

		ColumnIdx columnIdx = new ColumnIdx();

		nom = nom.trim().toLowerCase();
		nom_jeune_fille = nom_jeune_fille.trim().toLowerCase();
		prenom = prenom.trim().toLowerCase();
		date = date.trim().toLowerCase();
		portable = portable.trim().toLowerCase();
		telephone = telephone.trim().toLowerCase();
		email = email.trim().toLowerCase();

		{
			Row row = sheet.getRow(0);
			for (Cell cell : row) {
				if (cell != null && cell.getCellType() == Cell.CELL_TYPE_STRING) {
					String title = cell.getStringCellValue().toLowerCase();
					LOGGER.debug("title : {}", title);
					if (nom.equals(title)) {
						columnIdx.nameIdx = cell.getColumnIndex();
						LOGGER.debug("Name Col Index : {}", columnIdx.nameIdx);
					} else if (nom_jeune_fille.equals(title)) {
						columnIdx.nameJFIdx = cell.getColumnIndex();
						LOGGER.debug("Name JF Col Index : {}", columnIdx.nameJFIdx);
					} else if (prenom.equals(title)) {
						columnIdx.firstNameIdx = cell.getColumnIndex();
						LOGGER.debug("Prénom Col Index : {}", columnIdx.firstNameIdx);
					} else if (date.equals(title)) {
						columnIdx.dateIdx = cell.getColumnIndex();
						LOGGER.debug("Date Col Index : {}", columnIdx.dateIdx);
					} else if (portable.equals(title)) {
						columnIdx.portableIdx = cell.getColumnIndex();
						LOGGER.debug("Portable Col Index : {}", columnIdx.portableIdx);
					} else if (telephone.equals(title)) {
						columnIdx.telephoneIdx = cell.getColumnIndex();
						LOGGER.debug("Telephone Col Index : {}", columnIdx.telephoneIdx);
					} else if (email.equals(title)) {
						columnIdx.emailIdx = cell.getColumnIndex();
						LOGGER.debug("Email Col Index : {}", columnIdx.portableIdx);
					}
				}
			}
		}

		return columnIdx;
	}

	public static class ColumnIdx {
		int nameIdx = -1;
		int nameJFIdx = -1;
		int firstNameIdx = -1;
		int dateIdx = -1;
		int portableIdx = -1;
		int telephoneIdx = -1;
		int emailIdx = -1;
	}
}
