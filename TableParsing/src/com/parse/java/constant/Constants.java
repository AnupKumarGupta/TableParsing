package com.parse.java.constant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public interface Constants {

	int INDEX_COLUMN_NAME = 0;
	int INDEX_COLUMN_CONSTRAINT = 1;
	int INDEX_COLUMN_TYPE = 2;
	int INDEX_COLUMN_SIZE = 3;
	int INDEX_COLUMN_PERCISION= 4;
	int INDEX_COLUMN_DEFAULT_VALUE = 5;
	int INDEX_COLUMN_NULLABLE = 6;
	int INDEX_COLUMN_AUTOINCREMENT = 7;
	int INDEX_COLUMN_FOREIGN_TABLE_NAME = 8;

	int INDEX_COLUMN_COMMENTS = 9;
	
	String SCHEMA_NAME = "";
	//String TABLE_PREFIX = "CS_";
	String FOREIGN_KEY_SUFFIX = "_ID";
	
	List<String> numericValues = new ArrayList<>(Arrays.asList("NUMERIC", "DECIMAL","BIGINT","INT"));
	List<String> stringValues = new ArrayList<>(Arrays.asList("VARCHAR", "CHARACTER VARYING", "CHAR"));
	List<String> timeValues = new ArrayList<>(Arrays.asList("TIMESTAMP", "TIME"));
	
	boolean formatSQL = true;

}
