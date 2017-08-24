package com.parse.java;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.parse.java.constant.OperationMode;
import com.parse.java.framework.FrameworkHelper;
import com.parse.java.interfaces.IAliasSheetGenerator;
import com.parse.java.interfaces.IDBProvider;
import com.parse.java.interfaces.IScriptGenerator;
import com.parse.java.model.EntityModel;

public class Main {

	// JDBC driver name and database URL
	public static final String JDBC_DRIVER = "org.postgresql.Driver";

	// public static final String SCHEMA_NAME = "hhs_creation_test";
	public static final String SCHEMA_NAME = "hhs";

	public static final String DB_NAME = "WMDB";

	public static final String DB_URL = "jdbc:postgresql://54.152.186.92:5432/" + DB_NAME + "?currentSchema=" + SCHEMA_NAME;

	// Database credentials
	// public static final String USER = "hhs_test_user";
	// public static final String PASS = "hhs_test_user123";
	public static final String USER = "iceburger";

	public static final String PASS = "iceburger";

	static {
		FrameworkHelper.init();
	}

	private static Logger log = FrameworkHelper.getLogger(Main.class);

	public static void main(String[] args) throws Exception {

		if (checkDbConnectivity()) {
			OperationMode mode = OperationMode.GENERATE_SQL_FROM_EXCEL;
			// chooseMode();

			switch (mode) {
			case GENERATE_ALIAS_EXCEL_SHEET:
				generateAliasExcelSheet();
				break;
			case GENERATE_SQL_FROM_EXCEL:
				generateSqlFromExcel();
				break;
			}
			log.info("DONE...");
		}

	}

	private static boolean checkDbConnectivity() {
		System.out.println("Connecting to database...");
		try {
			Class.forName(JDBC_DRIVER);
		} catch (ClassNotFoundException e) {
			System.out.println("JDBC Driver not found, include in your library path!");
			e.printStackTrace();
			return false;
		}

		Connection connection = null;
		try {
			connection = DriverManager.getConnection(DB_URL, USER, PASS);

		} catch (SQLException e) {
			System.out.println("Connection Failed!");
			e.printStackTrace();
			return false;
		}
		if (connection != null) {
			System.out.println("Connected!");
			return true;
		} else {
			System.out.println("Failed to make connection!");
			return false;
		}
	}

	private static OperationMode chooseMode() {
		System.out.println("Enter 1 to GENERATE ALIAS EXCEL SHEET \nEnter 2 to GENERATE SQL FROM EXCEL");

		Scanner s = new Scanner(System.in);
		int N = s.nextInt();

		OperationMode mode = null;
		if (N == 1) {
			mode = OperationMode.GENERATE_ALIAS_EXCEL_SHEET;
		} else if (N == 2) {
			mode = OperationMode.GENERATE_SQL_FROM_EXCEL;
		} else {
			System.out.println("No such mode of operation.Please choose again. \n");
			chooseMode();
		}
		s.close();
		return mode;
	}

	private static void generateAliasExcelSheet() throws Exception {
		String aliasMappingFile = FrameworkHelper.getProperty("aliasMappingFile");
		String aliasInputFile = FrameworkHelper.getProperty("aliasInputFile");
		String aliasOutputFile = aliasInputFile.substring(0, aliasInputFile.lastIndexOf(".")) + "_Alias"
				+ aliasInputFile.substring(aliasInputFile.lastIndexOf("."), aliasInputFile.length());

		int ALIAS_MAPPING_START_SHEET = Integer.parseInt(FrameworkHelper.getProperty("aliasMappingStartSheetNumber"));
		int ALIAS_MAPPING_NUMBER_OF_SHEETS = Integer.parseInt(FrameworkHelper.getProperty("aliasMappingNumberOfSheets"));
		int ALIAS_MAPPING_KEY_COLUMN_NUMBER = Integer.parseInt(FrameworkHelper.getProperty("aliasMappingKeyColumnNumber"));
		int ALIAS_MAPPING_VALUE_COLUMN_NUMBER = Integer.parseInt(FrameworkHelper.getProperty("aliasMappingValueColumnNumber"));
		int ALIAS_INPUT_START_SHEET = Integer.parseInt(FrameworkHelper.getProperty("aliasInputStartSheetNumber"));
		int ALIAS_INPUT_NUMBER_OF_SHEETS = Integer.parseInt(FrameworkHelper.getProperty("aliasInputNumberOfSheets"));
		// Column number that needs to be replaced by value in map.
		int ALIAS_INPUT_KEY_COLUMN_NUMBER = Integer.parseInt(FrameworkHelper.getProperty("alisInputAbbreviatedColumnNumber"));

		HashMap<String, String> aliasMap = null;
		for (int aliasMappingSheetNumber = ALIAS_MAPPING_START_SHEET; aliasMappingSheetNumber < ALIAS_MAPPING_NUMBER_OF_SHEETS; aliasMappingSheetNumber++) {
			aliasMap = generateAliasMap(aliasMappingFile, aliasMappingSheetNumber, ALIAS_MAPPING_KEY_COLUMN_NUMBER,
					ALIAS_MAPPING_VALUE_COLUMN_NUMBER);
		}

		generateAliasExcel(aliasInputFile, ALIAS_INPUT_START_SHEET, ALIAS_INPUT_NUMBER_OF_SHEETS, ALIAS_INPUT_KEY_COLUMN_NUMBER,
				aliasMap, aliasOutputFile);
	}

	private static void generateSqlFromExcel() throws Exception {

		String inputFile = FrameworkHelper.getProperty("inputFile");
		String outputFile = FrameworkHelper.getProperty("outputFile");

		int START_SHEET = Integer.parseInt(FrameworkHelper.getProperty("startSheetNumber"));
		int NUMBER_OF_SHEETS = Integer.parseInt(FrameworkHelper.getProperty("numberOfSheets"));

		for (int sheetNumber = START_SHEET; sheetNumber < NUMBER_OF_SHEETS; sheetNumber++) {
			FileWriter fw = new FileWriter(String.format(outputFile, sheetNumber + 1));
			List<EntityModel> entities = readExcel(inputFile, sheetNumber);
			log.info("Total Number of entities read : " + entities.size() + " on sheet number : " + (sheetNumber + 1) + "\n");
			List<String> strings = generateSQLStatements(entities);
			log.debug("********************* CREATE TABLE STATEMENTS START **********************\n");
			for (String string : strings) {
				String displayString = string.replaceAll(";", ";\n");
				log.debug(displayString);
				fw.append(displayString + "\n\n");
			}
			log.debug("********************* CREATE TABLE STATEMENTS END ************************");
			fw.close();
		}
	}

	private static List<EntityModel> readExcel(String inputFile, int sheetNumber) throws Exception {
		IScriptGenerator scriptGenerator = FrameworkHelper.getImplementation(IScriptGenerator.class);
		return scriptGenerator.generateEntityModelByFile(inputFile, sheetNumber);
	}

	private static List<String> generateSQLStatements(List<EntityModel> entities) throws Exception {
		IDBProvider dbProvider = FrameworkHelper.getImplementation(IDBProvider.class);

		List<String> strings = new ArrayList<>();
		for (EntityModel entity : entities) {
			String query = dbProvider.generateCreateTableStatement(entity);
			System.out.println(query);
			//System.out.println('\n');
			strings.add(query);
		}
		return strings;
	}

	private static HashMap<String, String> generateAliasMap(String inputFilePath, int sheetNumber,
			int aliasMappingKeyColumnNumber, int aliasMappingValueColumnNumber) throws Exception {
		IAliasSheetGenerator aliasSheetGenerator = FrameworkHelper.getImplementation(IAliasSheetGenerator.class);
		return aliasSheetGenerator.generateMap(inputFilePath, sheetNumber, aliasMappingKeyColumnNumber,
				aliasMappingValueColumnNumber);
	}

	private static File generateAliasExcel(String inputFile, int inputFileStartSheetNumber, int inputFileNumberOfSheets,
			int columnNumber, HashMap<String, String> aliasMap, String outputFilePath) throws Exception {
		IAliasSheetGenerator aliasSheetGenerator = FrameworkHelper.getImplementation(IAliasSheetGenerator.class);
		return aliasSheetGenerator.generateAliasSheet(inputFile, inputFileStartSheetNumber, inputFileNumberOfSheets, columnNumber,
				aliasMap, 1, outputFilePath);
	}

}
