package fr.diod.searchAdherants.excel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.language.RefinedSoundex;
import org.apache.commons.codec.language.bm.BeiderMorseEncoder;
import org.docx4j.jaxb.Context;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.CommentsPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.CommentRangeEnd;
import org.docx4j.wml.CommentRangeStart;
import org.docx4j.wml.Comments;
import org.docx4j.wml.Comments.Comment;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.diod.searchAdherants.excel.style.provider.DefaultStyleProvider;
import fr.diod.searchAdherants.excel.style.provider.StyleProvider;

public class WordSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(WordSearch.class);
	private WordprocessingMLPackage document; 
	private DatabaseStorage db;
	private StyleProvider styleProvider;
	private BufferedReader inputReader;
	private String[] separators;

	public WordSearch(File textFile, DatabaseStorage db, StyleProvider styleProvider, String[] separators) {

		try {
			inputReader = new BufferedReader(new FileReader(textFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		this.db = db;
		this.styleProvider = styleProvider;
		this.separators = separators;

		try {
			this.document = WordprocessingMLPackage.createPackage();
		} catch (org.docx4j.openpackaging.exceptions.InvalidFormatException e) {
			LOGGER.error("Erreur à la création du docx", e);
		}
	}

	public void scanText() throws IOException {
		LOGGER.info("scan Text");

		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

		org.docx4j.wml.P para = createPara();

		String line;
		List<String> fields = new ArrayList<String>();

		// Create and add a Comments Part
		CommentsPart cp;
		try {
			cp = new CommentsPart();
			document.getMainDocumentPart().addTargetPart(cp);
		} catch (InvalidFormatException e) {
			LOGGER.error("Invalid Format", e);
			return;
		}

		// Part must have minimal contents
		Comments comments = factory.createComments();
		cp.setJaxbElement(comments);
		
		int lineInParagraph = 0;
		while ((line = inputReader.readLine()) != null) {

			if (newRecordStart(line)) {
				LOGGER.debug("New Record");

				treatRecord(para, fields, comments);

				para = createPara();
				fields.clear();
				lineInParagraph = 0;
			} else {
				lineInParagraph ++;

				if (lineInParagraph < 10) {
					LOGGER.debug("Parse line {}" , lineInParagraph);
					fields.addAll(Arrays.asList(line.split(" ")));
				}

				LOGGER.trace(line);

				addText(para, line);
			}
		}

		treatRecord(para, fields, comments);

	}

	public org.docx4j.wml.P createPara() {
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

		return factory.createP();
	}

	public void addPara(MainDocumentPart mdp, P para) {	
		mdp.getContent().add(para);
	}

	private void addText(P para, String text) {
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.Text t = factory.createText();
		t.setValue(text + "\n");

		R run = factory.createR();
		run.getContent().add(t);
		para.getContent().add(run);

		Br br = factory.createBr(); // this Br element is used break the current and go for next line
		para.getContent().add(br);

	}

	private boolean newRecordStart(String line) {
		for (String separator : separators) {
			if (line.contains(separator)) {
				return true;
			}
		}
		return false;
	}

	private void treatRecord(P para, List<String> fields, Comments comments) {
		AdherentScore adherant = db.searchAdherant(fields.toArray(new String[fields.size()]));

		addComment(para, adherant, comments);

		addPara(document.getMainDocumentPart(), para);
	}

	private void addComment(P para, AdherentScore adherantScore, Comments comments) {
		if (!styleProvider.needsComment(adherantScore.score)) {
			return;
		}

		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();

		// Create object for commentRangeStart
		CommentRangeStart commentrangestart = factory.createCommentRangeStart(); 
		commentrangestart.setId( commentId );  // substitute your comment id


		// The actual content, in the middle
		para.getContent().add(0,commentrangestart);

		String comment = adherantScore.score + "% Matched " + adherantScore.toString();
		Comment theComment = createComment(commentId, "JEAN-FRED", null, comment );
		comments.getComment().add(theComment);

		// Create object for commentRangeEnd
		CommentRangeEnd commentrangeend = factory.createCommentRangeEnd(); 
		commentrangeend.setId( commentId );  // substitute your comment id

		para.getContent().add(commentrangeend);

		// Add comment reference to document
		para.getContent().add(createRunCommentReference(commentId));

		// ++, for next comment ...
		commentId = commentId.add(java.math.BigInteger.ONE);
	}

	private static org.docx4j.wml.R createRunCommentReference(java.math.BigInteger commentId) {
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.R run = factory.createR();
		org.docx4j.wml.R.CommentReference commentRef = factory.createRCommentReference();
		run.getContent().add(commentRef);
		commentRef.setId( commentId );	

		return run;

	}

	private static org.docx4j.wml.Comments.Comment createComment(java.math.BigInteger commentId, String author, Calendar date, String message) {
		org.docx4j.wml.ObjectFactory factory = Context.getWmlObjectFactory();
		org.docx4j.wml.Comments.Comment comment = factory.createCommentsComment();
		comment.setId( commentId );
		if (author!=null) {
			comment.setAuthor(author);
		}
		if (date!=null) {
			//			String dateString = RFC3339_FORMAT.format(date.getTime()) ;	
			//			comment.setDate(value)
			// TODO - at present this is XMLGregorianCalendar
		}
		org.docx4j.wml.P commentP = factory.createP();
		comment.getContent().add(commentP);
		org.docx4j.wml.R commentR = factory.createR();
		commentP.getContent().add(commentR);
		org.docx4j.wml.Text commentText = factory.createText();
		commentR.getContent().add(commentText);

		commentText.setValue(message);

		return comment;
	}

	//	private void highLightRow(CellStyle style, Row row) {
	//		for (Cell cell : row) {
	//			cell.setCellStyle(style);
	//		}
	//	}

	//	/**
	//	 * Highlight row according to styleProvider Rules
	//	 * @param score
	//	 * @param row
	//	 */
	//	private void highLightRow(int score, Row row) {
	//		CellStyle style = styleProvider.get(score);
	//
	//		highLightRow(style, row);
	//	}

	public static String transform(String cellValue) {
		String value = cellValue.replaceAll("[éèêëÉÈÊË]", "e");
		value = value.replaceAll("[äâàÀÁÂ]", "a");
		value = value.replaceAll("[^a-zA-Z0-9]", "");
		return value;
	}

	private static final char[] FR_FR_MAP = "01230970072455012683090808".toCharArray();
	private java.math.BigInteger commentId = BigInteger.valueOf(0);

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
		try {
			FileOutputStream out = new FileOutputStream(fileName);
			document.save(out);
			out.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("Write word result error :", e);
		} catch (IOException e) {
			LOGGER.error("Write word result error :", e);
		} catch (Docx4JException e) {
			LOGGER.error("Write word result error :", e);
		}

	}

	public void writeFile(OutputStream out) {
		try {
			document.save(out);
			out.close();
		} catch (FileNotFoundException e) {
			LOGGER.error("Write word result error :", e);
		} catch (IOException e) {
			LOGGER.error("Write word result error :", e);
		} catch (Docx4JException e) {
			LOGGER.error("Write word result error :", e);
		}

	}

	//	private String formatCell(Cell cell) {
	//		switch (cell.getCellType()) {
	//		case Cell.CELL_TYPE_STRING:
	//			return cell.getRichStringCellValue().getString();
	//		case Cell.CELL_TYPE_NUMERIC:
	//			if (DateUtil.isCellDateFormatted(cell)) {
	//				return cell.getDateCellValue().toString();
	//			} else {
	//				return cell.getNumericCellValue() + "";
	//			}
	//		case Cell.CELL_TYPE_BOOLEAN:
	//			return cell.getBooleanCellValue() + "";
	//		case Cell.CELL_TYPE_FORMULA:
	//			return cell.getCellFormula();
	//		default:
	//			return "EMPTY";
	//		}
	//	}

	public static InputStream computeResult(File dbFile, int sheetNumberDb, File inputFile, String[] separators) {
		return computeResult(dbFile, sheetNumberDb, inputFile, separators, new DefaultStyleProvider());
	}

	public static InputStream computeResult(File dbFile, int sheetNumberDb, File inputFile, String[] separators, StyleProvider styleProvider) {
		try {
			DatabaseStorage db = new DatabaseStorage();
			LOGGER.debug("DATABASE STORAGE : BEFORE POPULATE");
			db.populate(dbFile, sheetNumberDb);
			LOGGER.debug("DATABASE STORAGE : AFTER POPULATE");

			final WordSearch search = new WordSearch(inputFile, db, styleProvider, separators);
			LOGGER.debug("Word SEARCH : BEFORE SEARCH");
			search.scanText();
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
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
