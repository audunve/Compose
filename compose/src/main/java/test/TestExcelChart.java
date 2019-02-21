package test;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;
import org.apache.poi.xssf.usermodel.charts.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.FileOutputStream;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.*;
import org.apache.poi.ss.usermodel.charts.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFChart;

import org.openxmlformats.schemas.drawingml.x2006.chart.CTChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTPlotArea;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarChart;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBoolean;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTBarSer;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTAxDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumDataSource;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTNumRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTStrRef;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTSerTx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTCatAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTValAx;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTScaling;
import org.openxmlformats.schemas.drawingml.x2006.chart.CTLegend;
import org.openxmlformats.schemas.drawingml.x2006.chart.STAxPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STBarDir;
import org.openxmlformats.schemas.drawingml.x2006.chart.STOrientation;
import org.openxmlformats.schemas.drawingml.x2006.chart.STLegendPos;
import org.openxmlformats.schemas.drawingml.x2006.chart.STTickLblPos;

public class TestExcelChart {

	public static void main(String[] args) throws Exception {
		Workbook wb = new XSSFWorkbook();
		Sheet sheet = wb.createSheet("Sheet1");

		Row row;
		Cell cell;

		//for each confidence threshold
		String[] thresholds = {"0.1", "0.2", "0.3", "0.4", "0.5", "0.6", "0.7", "0.8", "0.9"};

		row = sheet.createRow(0);
		row.createCell(0);
		int counter = 0;

		for (int i = 0; i < thresholds.length; i++) {
			counter++;
			row.createCell(counter).setCellValue(thresholds[i]);

		}
		
//		double[] precisionArray = {0.3, 0.4, 0.5, 0.1, 0.7, 0.3, 0.2, 0.9, 1.0};
//		double[] recallArray = {0.3, 0.4, 0.5, 0.1, 0.7, 0.3, 0.2, 0.9, 1.0};
//		double[] fMeasureArray = {0.6, 0.8, 1.0, 0.2, 0.7, 0.4, 0.4, 0.9, 1.0};
//		
//		row = sheet.createRow(1);
//		cell = row.createCell(0);
//		cell.setCellValue("Precision");
//		for (int i = 0; i < precisionArray.length; i++) {
//			cell = row.createCell(i+1);
//			cell.setCellValue(precisionArray[i]);
//		}
//		
//		row = sheet.createRow(2);
//		cell = row.createCell(0);
//		cell.setCellValue("Recall");
//		for (int i = 0; i < recallArray.length; i++) {
//			cell = row.createCell(i+1);
//			cell.setCellValue(recallArray[i]);
//		}
//		
//		row = sheet.createRow(3);
//		cell = row.createCell(0);
//		cell.setCellValue("F-measure");
//		cell.setCellValue("F-measure");
//		for (int i = 0; i < fMeasureArray.length; i++) {
//			cell = row.createCell(i+1);
//			cell.setCellValue(fMeasureArray[i]);
//		}
		
//		double[] precisionArray = {0.3, 0.4, 0.5, 0.1, 0.7, 0.3, 0.2, 0.9, 1.0};
//		double[] recallArray = {0.3, 0.4, 0.5, 0.1, 0.7, 0.3, 0.2, 0.9, 1.0};
//		double[] fMeasureArray = {0.6, 0.8, 1.0, 0.2, 0.7, 0.4, 0.4, 0.9, 1.0};
		
		List<Double> precisionList = Arrays.asList(0.3, 0.4, 0.5, 0.1, 0.7, 0.3, 0.2, 0.9, 1.0);
		List<Double> recallList = Arrays.asList(0.3, 0.4, 0.5, 0.1, 0.7, 0.3, 0.2, 0.9, 1.0);
		List<Double> fMeasureList = Arrays.asList(0.6, 0.8, 1.0, 0.2, 0.7, 0.4, 0.4, 0.9, 1.0);
		
		row = sheet.createRow(1);
		cell = row.createCell(0);
		cell.setCellValue("Precision");
		for (int i = 0; i < precisionList.size(); i++) {
			cell = row.createCell(i+1);
			cell.setCellValue(precisionList.get(i));
		}
		
		row = sheet.createRow(2);
		cell = row.createCell(0);
		cell.setCellValue("Recall");
		for (int i = 0; i < recallList.size(); i++) {
			cell = row.createCell(i+1);
			cell.setCellValue(recallList.get(i));
		}
		
		row = sheet.createRow(3);
		cell = row.createCell(0);
		cell.setCellValue("F-measure");
		cell.setCellValue("F-measure");
		for (int i = 0; i < fMeasureList.size(); i++) {
			cell = row.createCell(i+1);
			cell.setCellValue(fMeasureList.get(i));
		}

		Drawing drawing = sheet.createDrawingPatriarch();
		ClientAnchor anchor = drawing.createAnchor(0, 0, 0, 0, 0, 5, 8, 20);

		Chart chart = drawing.createChart(anchor);

		CTChart ctChart = ((XSSFChart)chart).getCTChart();
		CTPlotArea ctPlotArea = ctChart.getPlotArea();
		CTBarChart ctBarChart = ctPlotArea.addNewBarChart();
		CTBoolean ctBoolean = ctBarChart.addNewVaryColors();
		ctBoolean.setVal(true);
		ctBarChart.addNewBarDir().setVal(STBarDir.COL);

		for (int r = 2; r < 6; r++) {
			CTBarSer ctBarSer = ctBarChart.addNewSer();
			CTSerTx ctSerTx = ctBarSer.addNewTx();
			CTStrRef ctStrRef = ctSerTx.addNewStrRef();
			ctStrRef.setF("Sheet1!$A$" + r);
			ctBarSer.addNewIdx().setVal(r-2);  
			CTAxDataSource cttAxDataSource = ctBarSer.addNewCat();
			ctStrRef = cttAxDataSource.addNewStrRef();
			ctStrRef.setF("Sheet1!$B$1:$J$1"); 
			CTNumDataSource ctNumDataSource = ctBarSer.addNewVal();
			CTNumRef ctNumRef = ctNumDataSource.addNewNumRef();
			ctNumRef.setF("Sheet1!$B$" + r + ":$J$" + r);

			//at least the border lines in Libreoffice Calc ;-)
			ctBarSer.addNewSpPr().addNewLn().addNewSolidFill().addNewSrgbClr().setVal(new byte[] {0,0,0});   

		} 

		//telling the BarChart that it has axes and giving them Ids
		ctBarChart.addNewAxId().setVal(123456);
		ctBarChart.addNewAxId().setVal(123457);

		//cat axis
		CTCatAx ctCatAx = ctPlotArea.addNewCatAx(); 
		ctCatAx.addNewAxId().setVal(123456); //id of the cat axis
		CTScaling ctScaling = ctCatAx.addNewScaling();
		ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
		ctCatAx.addNewDelete().setVal(false);
		ctCatAx.addNewAxPos().setVal(STAxPos.B);
		ctCatAx.addNewCrossAx().setVal(123457); //id of the val axis
		ctCatAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);

		//val axis
		CTValAx ctValAx = ctPlotArea.addNewValAx(); 
		ctValAx.addNewAxId().setVal(123457); //id of the val axis
		ctScaling = ctValAx.addNewScaling();
		ctScaling.addNewOrientation().setVal(STOrientation.MIN_MAX);
		ctValAx.addNewDelete().setVal(false);
		ctValAx.addNewAxPos().setVal(STAxPos.L);
		ctValAx.addNewCrossAx().setVal(123456); //id of the cat axis
		ctValAx.addNewTickLblPos().setVal(STTickLblPos.NEXT_TO);

		//legend
		CTLegend ctLegend = ctChart.addNewLegend();
		ctLegend.addNewLegendPos().setVal(STLegendPos.B);
		ctLegend.addNewOverlay().setVal(false);

		System.out.println(ctChart);

		FileOutputStream fileOut = new FileOutputStream("./files/BarChartTest.xlsx");
		wb.write(fileOut);
		fileOut.close();
	}
}