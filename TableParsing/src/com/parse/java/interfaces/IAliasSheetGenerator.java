package com.parse.java.interfaces;

import java.io.File;
import java.util.HashMap;

public interface IAliasSheetGenerator {

	HashMap<String, String> generateMap(String inputFilePath, int sheetNumber, int keyColumnNumber,
			int valueColumnNumber) throws Exception;
	

	File generateAliasSheet(String inputFilePath, int sheetNumber, int inputFileNumberOfSheets,
			int columnNumber, HashMap<String, String> aliasMap, int mappingSheetColumnNumber, String outputFilePath) throws Exception;
}
