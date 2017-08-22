package com.parse.java.aliasexcel.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.parse.java.interfaces.IAliasSheetGenerator;


public class AliasExcelSheetGenerator implements IAliasSheetGenerator {

	@Override
	public HashMap<String, String> generateMap(String inputFilePath, int sheetNumber, int keyColumnNumber, int valueColumnNumber) throws Exception {
		FileInputStream fileInputStream = new FileInputStream(new File(inputFilePath));
		OPCPackage fs = OPCPackage.open(fileInputStream);

	    Workbook wb = WorkbookFactory.create(fs);
	    Sheet sheet = wb.getSheetAt(sheetNumber);
	    Row row;
	    
	    int rows; // No of rows
	    rows = sheet.getPhysicalNumberOfRows();
	    
	    HashMap<String,String> aliasMap = null;
	    if(sheet != null){
	    	aliasMap = new HashMap<>();
		for (int r = 1; r < rows; r++) {
			row = sheet.getRow(r);
			if (row != null) {
				String key = null;
				String value = null;
				try{
					 key = row.getCell(keyColumnNumber).getStringCellValue();
				}
				//If exception is thrown for Numeric data
				catch(Exception e){
					if(e.getMessage().equals("Cannot get a text value from a numeric cell")){
						Double numericValue = row.getCell(keyColumnNumber).getNumericCellValue();
						//check if number was originally an int or double. 
						if(numericValue == Math.floor(numericValue)){
							key = String.valueOf(numericValue.intValue());
						}
						else{
							key = String.valueOf(numericValue);
						}
					}
					else if(e.getMessage().equals("Cannot get a text value from a boolean cell")){
						key = String.valueOf(row.getCell(keyColumnNumber).getBooleanCellValue());						
					}
				}
				
				try{
					 value = row.getCell(valueColumnNumber).getStringCellValue();
				}
				//If exception is thrown for Numeric data
				catch(Exception e){
					if(e.getMessage().equals("Cannot get a text value from a numeric cell")){
						Double numericValue = row.getCell(valueColumnNumber).getNumericCellValue();
						
						//check if number was originally an int or double. 
						if (numericValue == Math.floor(numericValue)){
							value = String.valueOf(numericValue.intValue());
						}
						else{
							value = String.valueOf(numericValue);
						}
					}
					else if(e.getMessage().equals("Cannot get a text value from a boolean cell")){
						value = String.valueOf(row.getCell(valueColumnNumber).getBooleanCellValue());
					}
				}
					aliasMap.put(key, value);
				}
			}
	    }		
		return aliasMap;
	}

	@Override
	public File generateAliasSheet(String inputFilePath, int inputFileStartSheetNumber, int inputFileNumberOfSheets,int inputSheetColumnNumber, HashMap<String,String> aliasMap,int mappingSheetColumnNumber,String outputFilePath) throws Exception{
		
		OPCPackage fs = OPCPackage.open(new File(inputFilePath));
		Workbook wb = WorkbookFactory.create(fs);
		Workbook outWb = new XSSFWorkbook();
		
		File outputFile = new File(outputFilePath);
		FileOutputStream outputStream = new FileOutputStream(outputFile);
		
		for (int s = inputFileStartSheetNumber; s < inputFileNumberOfSheets; s++) {

			Sheet sheet = wb.getSheetAt(s);
			Row row;			
			
			if (sheet != null) {

				int rows; // No of rows
				rows = sheet.getPhysicalNumberOfRows();
				int startRow = sheet.getFirstRowNum();
				int endRow = startRow + rows;

				// create a new sheet with the same name
				Sheet outSheet = outWb.createSheet(sheet.getSheetName());

				// Traverse rows of Input sheet
				for (int r = startRow; r < endRow; r++) {
					row = sheet.getRow(r);

					if (row != null) {
						Row outRow = outSheet.createRow(r);

						// Traverse columns of Input sheet
						for (int c = 1; c <= row.getLastCellNum(); c++) {
							
							Cell cell = row.getCell(c - 1); 
							CellStyle style = cell.getCellStyle();
							
							Cell outCell;
							CellStyle outCellStyle;
							
							//copy cell style
							outCell = outRow.createCell(c - 1);
							outCellStyle = outWb.createCellStyle();
							outCellStyle.cloneStyleFrom(style);
							outCell.setCellStyle(outCellStyle);
							
							// If column is the KEY column, replace it with
							// VALUE in Alias sheet.
							// c-1 because c starts from 1, columnNumber starts
							// from 0
							if (c - 1 == inputSheetColumnNumber) {
								String[] splitNames = splitColumnName(cell);
								StringBuilder aliasColumnName = new StringBuilder();

								// Traverse split words from Input sheet column
								// names
								for (String name : splitNames) {
									if (aliasMap.containsKey(name)) {
										aliasColumnName.append(aliasMap.get(name) + "_");
									} else {
										aliasColumnName.append(name + "_");
									}
								}

								// -1 to truncate last "_"
								outCell.setCellValue(aliasColumnName.toString().substring(0, aliasColumnName.length() - 1));
								
							}
							// Else copy the same value in Alias sheet.
							else {
								outCell.setCellValue(cell.getStringCellValue());
							}
						}
					}
				}

			}
			
		}
		outWb.write(outputStream);
		outputStream.close();
		return outputFile;
	}

	private String[] splitColumnName(Cell column) {
		String columnName = column.getStringCellValue();
		String[] splitNames = columnName.split("_");
		return splitNames;
	}

}
