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

	public static List<Adherant> populate(File file, int sheetNumber, String nom, String nom_jeune_fille, String prenom, String date, String portable, String telephone, String email ) {	
		List<Adherant> adherants = new ArrayList<Adherant>();

		try {
			Workbook wb = WorkbookFactory.create(file);
			Sheet sheet = wb.getSheetAt(sheetNumber);

			LOGGER.info("=>Populate DataBase with sheet {}", sheet.getSheetName());

			int nameIdx = -1;
			int nameJFIdx = -1;
			int firstNameIdx = -1;
			int dateIdx = -1;
			int portableIdx = -1;
			int telephoneIdx = -1;
			int emailIdx = -1;

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
							nameIdx = cell.getColumnIndex();
							LOGGER.debug("Name Col Index : {}", nameIdx);
						} else if (nom_jeune_fille.equals(title)) {
							nameJFIdx = cell.getColumnIndex();
							LOGGER.debug("Name JF Col Index : {}", nameJFIdx);
						} else if (prenom.equals(title)) {
							firstNameIdx = cell.getColumnIndex();
							LOGGER.debug("Prénom Col Index : {}", firstNameIdx);
						} else if (date.equals(title)) {
							dateIdx = cell.getColumnIndex();
							LOGGER.debug("Date Col Index : {}", dateIdx);
						} else if (portable.equals(title)) {
							portableIdx = cell.getColumnIndex();
							LOGGER.debug("Portable Col Index : {}", portableIdx);
						} else if (telephone.equals(title)) {
							telephoneIdx = cell.getColumnIndex();
							LOGGER.debug("Telephone Col Index : {}", telephoneIdx);
						} else if (email.equals(title)) {
							emailIdx = cell.getColumnIndex();
							LOGGER.debug("Email Col Index : {}", portableIdx);
						}
					}
				}
			}

			LOGGER.debug("col IDx for name {} maiden {} first {} birth {} mobile {} telephone {} email {}",
					nameIdx, nameJFIdx, firstNameIdx, dateIdx, portableIdx, telephoneIdx, emailIdx);
			boolean firstRow = true;
			for (Row row : sheet) {
				if (firstRow) {
					firstRow = false;
					continue;
				}

				Adherant adherant = new Adherant();

				Cell cellName = row.getCell(nameIdx);
				if (cellName == null || cellName.getCellType() == Cell.CELL_TYPE_BLANK)
					continue;
				if (nameIdx > 0) 
					adherant.name = ExcelTool.getString(cellName);
				if (nameJFIdx > 0)
					adherant.maidenName = ExcelTool.getString(row.getCell(nameJFIdx));
				if (firstNameIdx > 0)
					adherant.firstName = ExcelTool.getString(row.getCell(firstNameIdx));
				if (dateIdx > 0) {
					Cell cell = row.getCell(dateIdx);
					if (ExcelTool.isDate(cell)) {
						adherant.birth = ExcelTool.getDate(cell).toString();
					}
				}
				if (portableIdx > 0)
					adherant.portable = ExcelTool.getString(row.getCell(portableIdx));
				if (telephoneIdx > 0)
					adherant.telephone = ExcelTool.getString(row.getCell(telephoneIdx));
				if (emailIdx > 0)
					adherant.email = ExcelTool.getString(row.getCell(emailIdx));
				

				LOGGER.debug("{}",adherant.toString());
				adherants.add(adherant);
			}

			LOGGER.info("populate with {} adherants ", adherants.size());

		} catch (InvalidFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return adherants;
	}
}
