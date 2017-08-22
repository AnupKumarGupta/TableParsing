package com.parse.java.interfaces;

import java.sql.Connection;
import java.sql.Statement;

import com.parse.java.model.EntityModel;

public interface IDBProvider {

	String generateCreateTableStatement(EntityModel entity) throws Exception;
	
	void generateTablesByFile(Connection connection, String inputFile) throws Exception;
	
	void generateTablesThroughStatement(Statement statement, String statementString) throws Exception;
	
}
