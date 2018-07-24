package compose.eval;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import compose.misc.StringUtilities;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;



	/**
	 * @author audunvennesland
	 * 9. apr. 2017 
	 */
	public class Evaluator {

		/**
		 * Evaluates a single alignment against a reference alignment and prints precision, recall, f-measure, true positives (TP), false positives (FP) and false negatives (FN)
		 * @param inputAlignmentFileName
		 * @param referenceAlignmentFileName
		 * @throws AlignmentException
		 * @throws URISyntaxException
		 */
		public static void evaluateSingleAlignment (String inputAlignmentFileName, String referenceAlignmentFileName) throws AlignmentException, URISyntaxException {

			AlignmentParser refAlignParser = new AlignmentParser(0);
			AlignmentParser evalAlignParser = new AlignmentParser(1);

			Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
			BasicAlignment inputAlignment = (BasicAlignment) evalAlignParser.parse(new URI(StringUtilities.convertToFileURL(inputAlignmentFileName)));

			Properties p = new Properties();
			PRecEvaluator eval = new PRecEvaluator(referenceAlignment, inputAlignment);

			eval.eval(p);

			System.out.println("------------------------------");
			System.out.println("Evaluator scores for " + inputAlignmentFileName);
			System.out.println("------------------------------");
			System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

			System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

			int fp = eval.getFound() - eval.getCorrect();
			System.out.println("False positives (FP): " + fp);
			int fn = eval.getExpected() - eval.getCorrect();
			System.out.println("False negatives (FN): " + fn);
			System.out.println("\n");

		}
		
		/**
		 * Evaluates a single alignment against a reference alignment and prints precision, recall, f-measure, true positives (TP), false positives (FP) and false negatives (FN)
		 * @param inputAlignmentFileName
		 * @param referenceAlignmentFileName
		 * @throws AlignmentException
		 * @throws URISyntaxException
		 */
		public static void evaluateSingleAlignment (BasicAlignment inputAlignment, String referenceAlignmentFileName) throws AlignmentException, URISyntaxException {

			AlignmentParser refAlignParser = new AlignmentParser(0);
			AlignmentParser evalAlignParser = new AlignmentParser(1);

			Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));
			//BasicAlignment inputAlignment = (BasicAlignment) evalAlignParser.parse(new URI(StringUtilities.convertToFileURL(inputAlignmentFileName)));

			Properties p = new Properties();
			PRecEvaluator eval = new PRecEvaluator(referenceAlignment, inputAlignment);

			eval.eval(p);

			System.out.println("------------------------------");
			System.out.println("Evaluator scores for " + inputAlignment.getType());
			System.out.println("------------------------------");
			System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
			System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
			System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

			System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

			int fp = eval.getFound() - eval.getCorrect();
			System.out.println("False positives (FP): " + fp);
			int fn = eval.getExpected() - eval.getCorrect();
			System.out.println("False negatives (FN): " + fn);
			System.out.println("\n");

		}

		/**
		 * Evaluates all alignments in a folder against a reference alignment prints for each alignment: precision, recall, f-measure, true positives (TP), false positives (FP) and false negatives (FN)
		 * @param folderName The folder holding all alignments
		 * @param referenceAlignmentFileName
		 * @throws AlignmentException
		 * @throws URISyntaxException
		 */
		public static void evaluateAlignmentFolder (String folderName, String referenceAlignmentFileName) throws AlignmentException, URISyntaxException {

			AlignmentParser aparser = new AlignmentParser(0);
			Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));

			Properties p = new Properties();

			File folder = new File(folderName);
			File[] filesInDir = folder.listFiles();
			Alignment evaluatedAlignment = null;
			PRecEvaluator eval = null;

			for (int i = 0; i < filesInDir.length; i++) {

				String URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[i].toString());
				System.out.println("Evaluating file " + URI);
				evaluatedAlignment = aparser.parse(new URI(URI));

				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

				eval.eval(p);

				System.out.println("Number of relations in alignment: " + evaluatedAlignment.nbCells());

				System.out.println("------------------------------");
				System.out.println("Evaluator scores for " + StringUtilities.stripPath(filesInDir[i].toString()));
				System.out.println("------------------------------");
				System.out.println("F-measure: " + eval.getResults().getProperty("fmeasure").toString());
				System.out.println("Precision: " + eval.getResults().getProperty("precision").toString());
				System.out.println("Recall: " + eval.getResults().getProperty("recall").toString());

				System.out.println("True positives (TP): " + eval.getResults().getProperty("true positive").toString());

				int fp = eval.getFound() - eval.getCorrect();
				System.out.println("False positives (FP): " + fp);
				int fn = eval.getExpected() - eval.getCorrect();
				System.out.println("False negatives (FN): " + fn);
				System.out.println("\n");
			}
		}



		/**
		 * Produces a Map of key: matcher (i.e. alignment produced by a particular matcher) and value: F-measure score from evaluation of against the alignment for that particular matcher
		 * @param folderName The folder holding the alignments to be evaluated
		 * @param referenceAlignmentFileName
		 * @return A Map<String, Double) holding the matcher (alignment) and F-measure score for that particular matcher (alignment)
		 * @throws AlignmentException
		 * @throws URISyntaxException
		 */
		public static Map<String, EvaluationScore> evaluateAlignmentFolderMap (String folderName, String referenceAlignmentFileName) throws AlignmentException, URISyntaxException {

			Map<String, EvaluationScore> evalFolderMap = new HashMap<String, EvaluationScore>();

			double precision = 0;
			double recall = 0;		
			double fMeasure = 0;

			AlignmentParser aparser = new AlignmentParser(0);
			Alignment referenceAlignment = aparser.parse(new URI(StringUtilities.convertToFileURL(referenceAlignmentFileName)));

			Properties p = new Properties();
			File folder = new File(folderName);
			File[] filesInDir = folder.listFiles();

			Alignment evaluatedAlignment = null;
			PRecEvaluator eval = null;



			for (int i = 0; i < filesInDir.length; i++) {
				
				EvaluationScore evalScore = new EvaluationScore();

				String URI = StringUtilities.convertToFileURL(folderName) + "/" + StringUtilities.stripPath(filesInDir[i].toString());

				evaluatedAlignment = aparser.parse(new URI(URI));

				eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

				eval.eval(p);

				precision = Double.valueOf(eval.getResults().getProperty("precision").toString());
				recall = Double.valueOf(eval.getResults().getProperty("recall").toString());
				fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

				evalScore.setPrecision(precision);
				evalScore.setRecall(recall);
				evalScore.setfMeasure(fMeasure);
				
//				if (URI.toString().contains("_")) {
//				evalFolderMap.put(URI.substring(URI.lastIndexOf("-") +1), evalScore);
//				System.out.println("Adding " + URI.substring(URI.lastIndexOf("- ") +1) + "  to foldermap") ;
//				} else {
				evalFolderMap.put(URI.substring(URI.lastIndexOf("/") +1), evalScore);
				//System.out.println("Adding " + URI.substring(URI.lastIndexOf("/") -3) + "  to foldermap") ;
				//}
				
				//evalFolderMap.put(URI, evalScore);
			}

			return evalFolderMap;

		}

		/**
		 * Runs a complete evaluation producing F-measure scores for individual matchers and combination strategies. The F-measure scores are printed to console.
		 * @throws AlignmentException
		 * @throws URISyntaxException
		 * @throws FileNotFoundException 
		 */
		public static void runCompleteEvaluation (String alignmentsFolder, String referenceAlignment, String outputPath, String datasetName) throws AlignmentException, URISyntaxException, FileNotFoundException {

			File allIndividualAlignments = new File(alignmentsFolder);

			File[] folders = allIndividualAlignments.listFiles();
			System.err.println("Size of folders: " + folders.length);

			XSSFWorkbook workbook = new XSSFWorkbook();		

			XSSFSheet spreadsheet = null;


			//get a map<matcherName, fMeasureValue>
			Map<String, EvaluationScore> evalMap = evaluateAlignmentFolderMap(alignmentsFolder, referenceAlignment);

			spreadsheet = workbook.createSheet(datasetName);


			Cell cell = null;

			//Create a new font and alter it.
			XSSFFont font = workbook.createFont();
			font.setFontHeightInPoints((short) 30);
			font.setItalic(true);
			font.setBold(true);

			//Set font into style
			CellStyle style = workbook.createCellStyle();
			style.setFont(font);

			int rowNum = 0;

			Row headerRow = spreadsheet.createRow(0);

			//style=row.getRowStyle();
			headerRow.createCell(0).setCellValue("Matcher");
			headerRow.createCell(1).setCellValue("Precision");
			headerRow.createCell(2).setCellValue("Recall");
			headerRow.createCell(3).setCellValue("F-measure");
			
			EvaluationScore es = new EvaluationScore();
			double precision = 0;
			double recall = 0;
			double fMeasure = 0;

			for (Entry<String, EvaluationScore> e : evalMap.entrySet()) {
				
				es = e.getValue();
				precision = es.getPrecision();
				recall = es.getRecall();
				fMeasure = es.getfMeasure();

				int cellnum = 0;

				Row evalRow = spreadsheet.createRow(rowNum++);
				cell = evalRow.createCell(cellnum++);
				cell.setCellValue(e.getKey());
				cell = evalRow.createCell(cellnum++);
				cell.setCellValue(precision);
				cell = evalRow.createCell(cellnum++);
				cell.setCellValue(recall);
				cell = evalRow.createCell(cellnum++);
				cell.setCellValue(fMeasure);

			}

			try {
				FileOutputStream outputStream = 
						new FileOutputStream(new File(outputPath));
				workbook.write(outputStream);
				outputStream.close();
				System.out.println("Excel written successfully..");

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}



		

		public static void main(String[] args) throws AlignmentException, URISyntaxException, FileNotFoundException {

			//String singleAlignment = "./files/KEOD18/datasets_refined/d1/alignments/equivalence/Rev-AML-0.9.rdf";
			String alignmentFolder = "./files/DBLP-Scholar/alignments/new";
			String refalign = "./files/DBLP-Scholar/refalign.rdf";
			String outputEvaluationFile = "./files/DBLP-Scholar/evaluation/DBLP-Scholar.xslx";
			String datasetName = "DBLP-Scholar";

			//evaluateSingleAlignment(singleAlignment, refalign);
			evaluateAlignmentFolder(alignmentFolder,refalign);
			runCompleteEvaluation(alignmentFolder, refalign, outputEvaluationFile, datasetName);

			//public static Map<String, Double> evaluateAlignmentFolderMap (String folderName, String referenceAlignmentFileName) throws AlignmentException, URISyntaxException {
			//		Map<String, Double> evalMap = evaluateAlignmentFolderMap(alignmentFolder, refalign);
			//		for (Entry<String, Double> e : evalMap.entrySet()) {
			//			System.out.println(e);
			//		}


		}

	}



	


