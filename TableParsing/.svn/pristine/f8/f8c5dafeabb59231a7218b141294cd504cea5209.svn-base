package com.parse.java;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;

public class Archived {
	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "org.postgresql.Driver";
	static final String DB_URL = "jdbc:postgresql://54.152.186.92:5432/iceburgers";

	// Database credentials
	static final String USER = "autoadmin";
	static final String PASS = "autoadmin123";
	
	//Properties
	private static final boolean generateTablesInDB = false;
	private static final String inputTablesTextFile = "resources/table layouts.txt";
	private static final String inputIndexesTextFile = "resources/index layouts.txt";

	private static final String generatedFileName = "/Users/shaaswatsharma/Documents/MDThink/Development/Misc/TableParsing/Output/tableGenerationScripts.sql";
	

	public static void mainOld(String[] args) {

		Connection conn = null;
		Statement stmt = null;

		BufferedReader bufferedReader;
		HashSet<String> tableNameSet = new HashSet<String>();
		try {
			if(generateTablesInDB){
				Class.forName(JDBC_DRIVER);
				System.out.println("Connecting to a selected database...");
				conn = DriverManager.getConnection(DB_URL, USER, PASS);
				System.out.println("Connected database successfully...");

				stmt = conn.createStatement();
			}

			System.out.println("Creating table in given database...");
			// read data from file
			bufferedReader = new BufferedReader(new FileReader(inputTablesTextFile)); // Path of Raw Input File
																		 
			tableNameSet = createTables(bufferedReader,  stmt, tableNameSet); //  create table in db
			
			System.out.println(tableNameSet);

			System.out.println("Creating Indexes in given database...");

			bufferedReader = new BufferedReader(new FileReader(inputIndexesTextFile)); // Path of index File
																						
			addRulesToCreatedTables(bufferedReader, stmt, tableNameSet); // add indexing in created table
			System.out.println("Index created Successfully");
			
		} catch (SQLException se) {
			// Handle errors for JDBC
			se.printStackTrace();
		} catch (Exception e) {
			// Handle errors for Class.forName
			e.printStackTrace();
		} finally {
			// finally block used to close resources
			if(generateTablesInDB){
				try {
					if (stmt != null){
						stmt.close();
					}
						
				} catch (SQLException se) {
					System.out.println("ERROR : Could not close Statement");
					se.printStackTrace();
				} 
				try {
					if (conn != null){
						conn.close();
					}
						
				} catch (SQLException se) {
					System.out.println("ERROR : Could not close Connection");
					se.printStackTrace();
				} 
			}
		} 
	}

	private static HashSet<String> createTables(BufferedReader bufferedReader, Statement stmt,
			HashSet<String> tableNameSet) throws IOException, SQLException {
		FileWriter fw = new FileWriter(generatedFileName); // To store queries in text file
		String line;
		HashSet<String> schemaNameSet = new HashSet<String>();
		String schemaName = "";
		String tableName = null;
		String sql = null;
		String notNullVar = "";
		String dataTypeLength = "";
		while ((line = bufferedReader.readLine()) != null) {
			notNullVar = "";
			dataTypeLength = " ";
			line = line.trim().replaceAll(" +", " ");
			String arr[] = line.split(" ");
			if (arr != null) {
				int length = arr.length;
				if (!((length == 6) || (length == 8)))
					continue;
				else {
					if (!(arr[0].equalsIgnoreCase("TBNAME") || (arr[0].contains("-----")))) {
						if (length == 8) {
							if (arr[4].contains("TIMESTMP"))
								arr[4] = "TIMESTAMP";
							if (arr[7].equalsIgnoreCase("N"))
								notNullVar = "NOT NULL";
							if (isLengthOfDataTypeAllowed(arr[4])) {
								if (arr[4].contains("DECIMAL"))
									dataTypeLength = "(" + arr[5] + ", " + arr[6] + ") ";
								else
									dataTypeLength = "(" + arr[5] + ") ";
							}
							if (arr[0].equalsIgnoreCase(tableName) && (tableName != null)) {	
								sql = "ALTER TABLE " + schemaName + "." + tableName + " ADD COLUMN " + arr[2] + " " + arr[4]
										+ dataTypeLength + " " + notNullVar + ";";
							} else {
								schemaName = arr[1];
								tableName = arr[0];
								
								if(!schemaNameSet.contains(schemaName)) {
									schemaNameSet.add(schemaName);
									sql = "CREATE SCHEMA " + schemaName + "; " ;
								} else {
									sql = "";
								}
								if(!tableNameSet.contains( schemaName + "." + tableName)) {
									tableNameSet.add(schemaName + "." + tableName);
								}
								sql += "CREATE TABLE " + schemaName + "." + tableName + " ( " + arr[2] + " " + arr[4] + dataTypeLength + " "
										+ notNullVar + ");";
							}

						} else if (tableName != null) {
							if (arr[2].contains("TIMESTMP"))
								arr[2] = "TIMESTAMP";
							if (arr[5].equalsIgnoreCase("N"))
								notNullVar = "NOT NULL";
							if (isLengthOfDataTypeAllowed(arr[2])) {
								if (arr[4].contains("DECIMAL"))
									dataTypeLength = "(" + arr[3] + ", " + arr[4] + ") ";
								else
									dataTypeLength = "(" + arr[3] + ") ";
							}
							sql = "ALTER TABLE " + schemaName + "." + tableName + " ADD COLUMN " + arr[0] + " " + arr[2] + dataTypeLength
									+ " " + notNullVar + ";";
						}
						System.out.println(sql); 
						if(generateTablesInDB){
							stmt.executeUpdate(sql); // TO Execute in database
						}
						 fw.write(sql + "\n");  // To write in file

					}
				}
			}
		}
		/* System.out.println(sql); */
		fw.close();
		return tableNameSet;

	}

	private static void addRulesToCreatedTables(BufferedReader bufferedReader, Statement stmt,
			HashSet<String> tableNameSet) throws IOException, SQLException {
		FileWriter fw = new FileWriter(generatedFileName); // To store queries in text file
		String line;
		String schemaName = null;
		String[] isTableExist = { "", "" };
		String indexName = null;
		String order = "";
		String sqlArchive = "";
		String sqlView = "";
		String uniqueRule = null;
		String notNullColumn = "";
		while ((line = bufferedReader.readLine()) != null) {
			uniqueRule = "";
			order = "";
			line = line.trim().replaceAll(" +", " ");
			String arr[] = line.split(" ");
			/*
			 * System.out.println(line);
			 */ if (arr != null) {
				int length = arr.length;
				if (!((length >= 6) || (length == 5) || (length == 2) || (length == 4)))
					continue;
				else {
					if (length == 6) {
						if ((arr[0].equalsIgnoreCase("DBNAME") || (arr[0].contains("-----"))))
							continue;
					} else if (length == 5) {
						if ((arr[2].length() != 1) || (arr[4].length() != 1))
							continue;
					}
					if (((length >= 6) || (length == 5) || (length == 4))) {
						if (!sqlArchive.isEmpty() && isTableExist[0].startsWith("A")) {
							sqlArchive += ") WHERE " + notNullColumn + " IS NOT NULL; ";

							System.out.println(sqlArchive);
							 fw.write(sqlArchive + "\n");  // To write in file
							 if(generateTablesInDB){
								stmt.executeUpdate(sqlArchive);
							 }
							
							sqlArchive = "";
						}
						if (!sqlView.isEmpty() && isTableExist[1].startsWith("V")) {
							sqlView += ") WHERE " + notNullColumn + " IS NOT NULL; ";
							System.out.println(sqlView);
							fw.write(sqlView + "\n");  // To write in file
							if(generateTablesInDB){
								stmt.executeUpdate(sqlView);
							}
							
							sqlView = "";
						}
					}
					if( length > 6) {                                      // this case is only for "T _case" table present in index layouts file
						schemaName = arr[0];
					} else if (length == 6) {
						schemaName = arr[0];
						indexName = arr[2];
						notNullColumn = arr[4];
						isTableExist = isArchiveAndViewTableExist(schemaName, arr[1].substring(1), tableNameSet, isTableExist);
						order = orderingOfIndexes(arr[5]);
						uniqueRule = uniqueRuleGenerated(arr[3]);
						if (isTableExist[0].startsWith("A")) {
							if (arr[3].equalsIgnoreCase("P"))
								sqlArchive += "alter table " + schemaName + "." + isTableExist[0] + " add constraint pk_" + isTableExist[0]
										+ " primary key (" + notNullColumn + "); ";
							sqlArchive += "CREATE" + uniqueRule + " INDEX " + indexName + " ON "
									+ schemaName + "." + isTableExist[0] + " (" + arr[4] + " " + order;
						}

						if (isTableExist[1].startsWith("V")) {
							if (arr[3].equalsIgnoreCase("P"))
								sqlView += "alter table " + schemaName + "." +  isTableExist[1] + " add constraint pk_"+ schemaName + "_" + isTableExist[1]
										+ " primary key (" + notNullColumn + "); ";
							sqlView += "CREATE" + uniqueRule + " INDEX " + indexName + " ON "
									+ schemaName + "." + isTableExist[1] + " (" + arr[4] + " " + order;
						}

					} else if (length == 5) {
						indexName = arr[1];
						notNullColumn = arr[3];
						isTableExist = isArchiveAndViewTableExist(schemaName, arr[0].substring(1), tableNameSet, isTableExist);
						order = orderingOfIndexes(arr[4]);
						uniqueRule = uniqueRuleGenerated(arr[2]);
						if (isTableExist[0].startsWith("A")) {
							if (arr[2].equalsIgnoreCase("P"))
								sqlArchive += "alter table " + schemaName + "." +  isTableExist[0] + " add constraint pk_" + isTableExist[0]
										+ " primary key (" + notNullColumn + "); ";
							sqlArchive += "CREATE" + uniqueRule + " INDEX " + indexName + " ON "
									+ schemaName + "." + isTableExist[0] + " (" + arr[3] + " " + order;
						}
						if (isTableExist[1].startsWith("V")) {
							if (arr[2].equalsIgnoreCase("P"))
								sqlView += "alter table " + schemaName + "." +  isTableExist[1] + " add constraint pk_" + isTableExist[1]
										+ " primary key (" + notNullColumn + "); ";
							sqlView += "CREATE" + uniqueRule + " INDEX " + indexName + " ON "
									+ schemaName + "." + isTableExist[1] + " (" + arr[3] + " " + order;
						}
					} else if (length == 4) {
						indexName = arr[0];
						notNullColumn = arr[2];
						order = orderingOfIndexes(arr[3]);
						uniqueRule = uniqueRuleGenerated(arr[1]);
						if (isTableExist[0].startsWith("A")) {
							if (arr[1].equalsIgnoreCase("P"))
								sqlArchive += "alter table " + schemaName + "." +  isTableExist[0] + " add constraint pk_" + isTableExist[0]
										+ " primary key (" + notNullColumn + "); ";
							sqlArchive += "CREATE" + uniqueRule + " INDEX " + indexName + " ON "
									+ schemaName + "." + isTableExist[0] + " (" + arr[2] + " " + order;
						}
						if (isTableExist[1].startsWith("V")) {
							if (arr[1].equalsIgnoreCase("P"))
								sqlView += "alter table " + schemaName + "." +  isTableExist[1] + " add constraint pk_" + isTableExist[1]
										+ " primary key (" + notNullColumn + "); ";
							sqlView += "CREATE" + uniqueRule + " INDEX " + indexName + " ON "
									+ schemaName + "." + isTableExist[1] + " (" + arr[2] + " " + order;
						}

					} else {
						order = orderingOfIndexes(arr[1]);
						if (!sqlArchive.isEmpty() && isTableExist[0].startsWith("A"))
							sqlArchive += ", " + arr[0] + " " + order;
						if (!sqlView.isEmpty() &&  isTableExist[1].startsWith("V"))
							sqlView += ", " + arr[0] + " " + order;
					}
				}
			}

		}
		fw.close();

	}

	private static boolean isLengthOfDataTypeAllowed(String dataType) {
		if (dataType.contains("CHAR") || dataType.contains("TIME") || dataType.contains("DECIMAL"))
			return true;
		else
			return false;
	}

	private static String orderingOfIndexes(String order) {
		if (order.equalsIgnoreCase("A"))
			return "asc";
		else if (order.equalsIgnoreCase("D"))
			return "desc";
		return "";
	}

	private static String[] isArchiveAndViewTableExist(String schema, String substring, HashSet<String> tableNameSet,
			String[] isTableExist) {
		if (tableNameSet.contains(schema + ".A" + substring))
			isTableExist[0] = "A" + substring;
		else
			isTableExist[0] = "";
		if (tableNameSet.contains(schema + ".V" + substring))
			isTableExist[1] = "V" + substring;
		else
			isTableExist[1] = "";
		return isTableExist;
	}

	private static String uniqueRuleGenerated(String uniqueRule) {
		if (uniqueRule.equalsIgnoreCase("U") || uniqueRule.equalsIgnoreCase("P") || uniqueRule.equalsIgnoreCase("N"))
			return " UNIQUE";
		else if (uniqueRule.equalsIgnoreCase("D"))
			return " ";
		/*
		 * else if(uniqueRule.equalsIgnoreCase("P")) return
		 * " UNIQUE PRIMARY KEY"; else if(uniqueRule.equalsIgnoreCase("N"))
		 * return " UNIQUE NOT NULL";
		 */

		return "";
	}
}
