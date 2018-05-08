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

import compose.misc.StringUtils;
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

		Alignment referenceAlignment = refAlignParser.parse(new URI(StringUtils.convertToFileURL(referenceAlignmentFileName)));
		BasicAlignment inputAlignment = (BasicAlignment) evalAlignParser.parse(new URI(StringUtils.convertToFileURL(inputAlignmentFileName)));

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
	 * Evaluates all alignments in a folder against a reference alignment prints for each alignment: precision, recall, f-measure, true positives (TP), false positives (FP) and false negatives (FN)
	 * @param folderName The folder holding all alignments
	 * @param referenceAlignmentFileName
	 * @throws AlignmentException
	 * @throws URISyntaxException
	 */
	public static void evaluateAlignmentFolder (String folderName, String referenceAlignmentFileName) throws AlignmentException, URISyntaxException {

		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtils.convertToFileURL(referenceAlignmentFileName)));
		


		Properties p = new Properties();

		File folder = new File(folderName);
		File[] filesInDir = folder.listFiles();
		Alignment evaluatedAlignment = null;
		PRecEvaluator eval = null;

		for (int i = 0; i < filesInDir.length; i++) {

			String URI = StringUtils.convertToFileURL(folderName) + "/" + StringUtils.stripPath(filesInDir[i].toString());
			System.out.println("Evaluating file " + URI);
			evaluatedAlignment = aparser.parse(new URI(URI));

			eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

			eval.eval(p);
			
			System.out.println("Number of relations in alignment: " + evaluatedAlignment.nbCells());

			System.out.println("------------------------------");
			System.out.println("Evaluator scores for " + StringUtils.stripPath(filesInDir[i].toString()));
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
	public static Map<String, Double> evaluateAlignmentFolderMap (String folderName, String referenceAlignmentFileName) throws AlignmentException, URISyntaxException {

		Map<String, Double> evalFolderMap = new HashMap<String, Double>();

		double fMeasure = 0;

		AlignmentParser aparser = new AlignmentParser(0);
		Alignment referenceAlignment = aparser.parse(new URI(StringUtils.convertToFileURL(referenceAlignmentFileName)));

		Properties p = new Properties();

		File folder = new File(folderName);
		File[] filesInDir = folder.listFiles();
		Alignment evaluatedAlignment = null;
		PRecEvaluator eval = null;

		for (int i = 0; i < filesInDir.length; i++) {

			String URI = StringUtils.convertToFileURL(folderName) + "/" + StringUtils.stripPath(filesInDir[i].toString());
			evaluatedAlignment = aparser.parse(new URI(URI));

			eval = new PRecEvaluator(referenceAlignment, evaluatedAlignment);

			eval.eval(p);

			fMeasure = Double.valueOf(eval.getResults().getProperty("fmeasure").toString());

			evalFolderMap.put(URI, fMeasure);
		}

		return evalFolderMap;

	}

	/**
	 * Runs a complete evaluation producing F-measure scores for individual matchers and combination strategies. The F-measure scores are printed to console.
	 * TO-DO: Implement an "Excel printer" that prints the F-measure scores to an Excel sheet for easy chart-making, e.g. using Apache POI.
	 * @throws AlignmentException
	 * @throws URISyntaxException
	 * @throws FileNotFoundException 
	 */
	public static void runCompleteEvaluation () throws AlignmentException, URISyntaxException, FileNotFoundException {

		File allIndividualAlignments = new File("./files/OAEI2009/alignments");
		File allCombinedStrategiesAlignments = new File("./files/OAEI2009/combinedAlignments");
		File refAlignFolder = new File("./files/OAEI2009/referencealignments");
		

		File[] folders = allIndividualAlignments.listFiles();
		System.err.println("Size of folders: " + folders.length);
		File[] combinedAlignmentFolders = allCombinedStrategiesAlignments.listFiles();


		String refAlign = null;
		
		XSSFWorkbook workbook = new XSSFWorkbook();		

		XSSFSheet spreadsheet = null;


		//***** FOR EVALUATING INDIVIDUAL MATCHERS (ALIGNMENTS) ***
		System.out.println("\n*******************Individual Matchers*******************");
		
		for (int i = 0; i < folders.length; i++) {
			refAlign = refAlignFolder + "/101-" + folders[i].getName() + ".rdf";

			//get a map<matcherName, fMeasureValue>
			Map<String, Double> evalMap = evaluateAlignmentFolderMap(folders[i].getPath(), refAlign);
			System.out.println("Dataset: " + folders[i].getName());
			
			spreadsheet = workbook.createSheet(folders[i].getName());
			int rowNum = 0;
			
			Cell cell = null;
		
			//Create a new font and alter it.
		      XSSFFont font = workbook.createFont();
		      font.setFontHeightInPoints((short) 30);
		      font.setItalic(true);
		      font.setBold(true);

		      //Set font into style
		      CellStyle style = workbook.createCellStyle();
		      style.setFont(font);

		      for (Entry<String, Double> e : evalMap.entrySet()) {
				Row header = spreadsheet.createRow(0);
				//style=header.getRowStyle();
			    header.createCell(0).setCellValue("Alignment");
			    header.createCell(1).setCellValue("F-measure");
			    
				int cellnum = 0;
				Row row = spreadsheet.createRow(rowNum++);
				cell = row.createCell(cellnum++);
				cell.setCellValue(e.getKey());
				cell = row.createCell(cellnum++);
				cell.setCellValue(e.getValue());
				
				System.out.println(e.getKey() + ": " + e.getValue().toString());
			}
			
			try {
				FileOutputStream outputStream = 
						new FileOutputStream(new File("./files/Amazon-GoogleProducts/evaluation/excel.xlsx"));
				workbook.write(outputStream);
				outputStream.close();
				System.out.println("Excel written successfully..");
				
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		}

		/* *** FOR EVALUATING COMBINATION STRATEGIES ***
		System.out.println("\n*******************Combinations*******************");
		for (int i = 0; i < combinedAlignmentFolders.length; i++) {
			refAlign = refAlignFolder + "/101-" + combinedAlignmentFolders[i].getName() + ".rdf";
			
			//get a map<matcherName, fMeasureValue>
			Map<String, Double> evalMap = evaluateAlignmentFolderMap(combinedAlignmentFolders[i].getPath(), refAlign);
			System.out.println("Dataset: " + combinedAlignmentFolders[i].getName());
			
			for (Entry<String, Double> e : evalMap.entrySet()) {
				System.out.println(e.getKey() + ": " + e.getValue().toString());
			}

		}
		 */

	


	public static void main(String[] args) throws AlignmentException, URISyntaxException, FileNotFoundException {

		String singleAlignment = "./files/wndomainsexperiment/alignments/AML_bibframe-schemaorg-ISub08.rdf";
		String alignmentFolder = "./files/Amazon-GoogleProducts/alignments";
		String refalign = "./files/Amazon-GoogleProducts/refalign.rdf";

		//evaluateSingleAlignment(singleAlignment, refalign);
		//evaluateAlignmentFolder(alignmentFolder,refalign);
		runCompleteEvaluation();


	}

}
