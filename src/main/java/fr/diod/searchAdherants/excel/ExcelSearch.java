package fr.diod.searchAdherants.excel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;

import fr.diod.searchAdherants.excel.style.provider.DefaultStyleProvider;
import fr.diod.searchAdherants.excel.style.provider.StyleProvider;

public class ExcelSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExcelSearch.class);
	private Workbook wb;
	private DatabaseStorage db;
	private StyleProvider styleProvider;

	public ExcelSearch(Workbook wb, DatabaseStorage db, StyleProvider styleProvider) {
		this.wb = wb;
		this.db = db;
		this.styleProvider = styleProvider;
		styleProvider.initStyle(wb);
	}

	public void scanCells(int sheetNumberInput) {
		LOGGER.info("scanCells of sheet {}", sheetNumberInput);
		Sheet sheet = wb.getSheetAt(sheetNumberInput);
		Drawing drawing = sheet.createDrawingPatriarch();
		CreationHelper factory = wb.getCreationHelper();

		for (Row row : sheet) {
			try {
				LOGGER.debug("Scan new ROW");
				List<String> values = new ArrayList<String>();
				for (Cell cell : row) {
					String cellValue = formatCell(cell);
					LOGGER.debug("({}, {}) {}", cell.getColumnIndex(), cell.getRowIndex(), cellValue);
					values.add(cellValue);
				}

				Optional<AdherantScore> adherantOptional = db.searchAdherant(values.toArray(new String[values.size()]));

				if (adherantOptional.isPresent()) {
					Cell cell = row.getCell(0);
					
					if (cell == null) {
						cell = row.createCell(0);
					}
					LOGGER.debug("({}, {})", cell.getColumnIndex(), cell.getRowIndex());
					addCellComment(factory, drawing, row, cell, adherantOptional.get());
					
					highLightRow(adherantOptional.get().score, row);
				}
			} catch (NullPointerException ex) {
				LOGGER.error("NPE", ex);
			}
		} 
	}

	public static CellStyle createCellStyle(Workbook wb, short indexColor) {
		CellStyle style = wb.createCellStyle();
		style.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		style.setFillForegroundColor(indexColor);
		style.setFillBackgroundColor(indexColor);
		return style;
	}

	private void addCellComment(CreationHelper factory, Drawing drawing, Row row, Cell cell, AdherantScore adherantScore) {
		// When the comment box is visible, have it show in a 1x3 space
		ClientAnchor anchor = factory.createClientAnchor();
		anchor.setCol1(cell.getColumnIndex());
		anchor.setCol2(cell.getColumnIndex()+5);
		anchor.setRow1(row.getRowNum());
		anchor.setRow2(row.getRowNum()+7);

		// Create the comment and set the text+author
		Comment comment = drawing.createCellComment(anchor);
		RichTextString str = factory.createRichTextString(adherantScore.score + "% Matched " + adherantScore.toString());
		comment.setString(str);
		comment.setAuthor("JEAN-FRED");
		comment.setVisible(false);
		// Assign the comment to the cell
		cell.setCellComment(comment);
	}

	private void highLightRow(CellStyle style, Row row) {
		for (Cell cell : row) {
			cell.setCellStyle(style);
		}
	}

	/**
	 * Highlight row according to styleProvider Rules
	 * @param score
	 * @param row
	 */
	private void highLightRow(int score, Row row) {
		CellStyle style = styleProvider.get(score);
		
		highLightRow(style, row);
	}

	public static String transform(String cellValue) {
		String value = cellValue.replaceAll("[éèêëÉÈÊË]", "e");
		value = value.replaceAll("[äâàÀÁÂ]", "a");
		value = value.replaceAll("[^a-zA-Z0-9]", "");
		return value;
	}

	private static final char[] FR_FR_MAP = "01230970072455012683090808".toCharArray();

	@SuppressWarnings("unused")
	private boolean compareString(String cellValue) {
		RefinedSoundex compareAlgo = new RefinedSoundex(FR_FR_MAP);

		float similarity = -1;
		try {
			similarity = compareAlgo.difference("LABARNEDE", cellValue);
		} catch (EncoderException e) {
			e.printStackTrace();
		}
		LOGGER.info("LABARNEDE {} => {}", cellValue, similarity);

		if ("LABERNEDE".equals(cellValue)) {
			LOGGER.info("ICI");
		}

		BeiderMorseEncoder encoder = new BeiderMorseEncoder();
		try {
			LOGGER.info("{} {}", new Object[] {encoder.encode("LABARNEDE"), encoder.encode(cellValue)});
		} catch (EncoderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return similarity >= 4;
	}

	public void writeFile(String fileName) {
		if(wb instanceof XSSFWorkbook) fileName += "x";
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void writeFile(OutputStream out) {
		try {
			wb.write(out);
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private String formatCell(Cell cell) {
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

	public static InputStream computeResult(File dbFile, int sheetNumberDb, File inputFile, int sheetNumberInput) {
		return computeResult(dbFile, sheetNumberDb, inputFile, sheetNumberInput, new DefaultStyleProvider());
	}
	
	public static InputStream computeResult(File dbFile, int sheetNumberDb, File inputFile, int sheetNumberInput, StyleProvider styleProvider) {
		Workbook wb;
		try {
			DatabaseStorage db = new DatabaseStorage();
			LOGGER.debug("DATABASE STORAGE : BEFORE POPULATE");
			db.populate(dbFile, sheetNumberDb);
			LOGGER.debug("DATABASE STORAGE : AFTER POPULATE");

			wb = WorkbookFactory.create(inputFile);
			LOGGER.debug("input workbook creation");
			final ExcelSearch search = new ExcelSearch(wb, db, styleProvider);
			LOGGER.debug("EXCEL SEARCH : BEFORE SEARCH");
			search.scanCells(sheetNumberInput);
			LOGGER.info("EXCEL SEARCH : AFTER SEARCH");
			PipedInputStream in = new PipedInputStream();
			final PipedOutputStream out = new PipedOutputStream(in);
			new Thread(
					new Runnable(){
						public void run(){
							//data can be read from the pipedInputStream here.      
							search.writeFile(out);
						}
					}
					).start();

			return in;

		} catch (InvalidFormatException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void main(String[] args) {
		// create the command line parser
		CommandLineParser parser = new BasicParser();

		Options options = new Options();
		options.addOption( "in", "inputFile", true, "Liste de l'inspection academique");

		@SuppressWarnings("static-access")
		Option optionInputSN =	OptionBuilder.hasArg()
		.withArgName("inputSheetNumber")
		.withLongOpt("inputSheetNumber")
		.withDescription("Numéro de la feuille de calcul contenant les données [0..n]")
		.isRequired(false)
		.create("inSN");
		options.addOption(optionInputSN);

		options.addOption( "db", "adherantFile", true, "Liste des adherants" );

		@SuppressWarnings("static-access")
		Option optionDbSN =	OptionBuilder.hasArg()
		.withArgName("adherantSheetNumber")
		.withLongOpt("adherantSheetNumber")
		.withDescription("Numéro de la feuille de calcul contenant les données [0..n]")
		.isRequired(false)
		.create("dbSN");
		options.addOption(optionDbSN);

		options.addOption("out", "outputFile", true, "Fichier résultat");

		String inputFileName="";
		String dbFileName="";
		int sheetNumberInput = 0;
		int sheetNumberDb = 0;
		String outputFileName = "";

		boolean error = false;
		try {
			CommandLine line = parser.parse( options, args );
			inputFileName = line.getOptionValue("in");
			dbFileName = line.getOptionValue("db");
			outputFileName = line.getOptionValue("out");

			if (inputFileName == null || !Files.exists(Paths.get(inputFileName))) {
				System.out.println("inputFile error");
				error = true;
			} else if (dbFileName == null || !Files.exists(Paths.get(dbFileName))) {
				System.out.println("adherant db File error");
				error = true;
			} else if (outputFileName == null) {
				System.out.println("output File error");
				error = true;
			}

			if (line.hasOption("inSN")) {
				try {
					sheetNumberInput = Integer.valueOf(line.getOptionValue("inSN"));
				} catch (NumberFormatException  e1) {
					System.out.println("inSN");
					error = true;
				}
			}
			if (line.hasOption("dbSN")) {
				try {
					sheetNumberDb = Integer.valueOf(line.getOptionValue("dbSN"));
				} catch (NumberFormatException e1) {
					System.out.println("dbSN");
					error = true;
				}
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
			error = true;
		}

		if (error) {
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "ant", options );
			return;
		}

		Workbook wb;
		try {
			DatabaseStorage db = new DatabaseStorage();
			db.populate(dbFileName, sheetNumberDb);

			wb = WorkbookFactory.create(new File(inputFileName));
			ExcelSearch search = new ExcelSearch(wb, db, new DefaultStyleProvider());
			search.scanCells(sheetNumberInput);
			search.writeFile(outputFileName);

		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
