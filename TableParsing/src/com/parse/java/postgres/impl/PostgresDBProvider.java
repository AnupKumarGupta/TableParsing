package com.parse.java.postgres.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.parse.java.Main;
import com.parse.java.constant.Constants;
import com.parse.java.interfaces.IDBProvider;
import com.parse.java.model.ColumnModel;
import com.parse.java.model.EntityModel;

public class PostgresDBProvider implements IDBProvider, Constants {
	Connection con;

	@Override
	public String generateCreateTableStatement(EntityModel entity) throws Exception {

		con = DriverManager.getConnection(Main.DB_URL, Main.USER, Main.PASS);

		DatabaseMetaData databaseMetaData = con.getMetaData();

		ResultSet rs = null;
		try {
			String sql = "select * from " + entity.getTableName();
			Statement stmt = con.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			e.printStackTrace();
		}

		if (rs == null) {
			return createSqlQueryFromExcel(entity);
		} else {
			return printSchemaFromSql(con, databaseMetaData, rs, entity);
		}
	}

	private String printSchemaFromSql(Connection con, DatabaseMetaData databaseMetaData, ResultSet rs,
			EntityModel entity) throws SQLException {

		System.out.println(createSqlQueryFromExcel(entity));
		System.out.println("--------------");

		// -----------primary key--------------
		ResultSet resultForPk = databaseMetaData.getPrimaryKeys("", Main.SCHEMA_NAME,
				entity.getTableName().toLowerCase());

		String pkey = "";
		while (resultForPk.next()) {
			pkey = resultForPk.getString(4);
		}

		// -----------unique keys--------------
		ResultSet resultForUnique = databaseMetaData.getIndexInfo("", Main.SCHEMA_NAME,
				entity.getTableName().toLowerCase(), true, false);

		ArrayList<String> uniqueColumns = new ArrayList<String>();
		while (resultForUnique.next()) {
			uniqueColumns.add(resultForUnique.getString(9));
		}

		// -----------default value--------------
		/*try {
			ResultSet result = null;
			String sql = "select column_name, data_type, character_maximum_length,column_default,is_nullable as IS_NULLABLE,character_maximum_length,is_identity from INFORMATION_SCHEMA.COLUMNS where table_name = '"
					+ entity.getTableName().toLowerCase() + "';";
			Statement stmt = con.createStatement();
			result = stmt.executeQuery(sql);
			String columnDefaultVal = "";
			if (result.next()) {
				columnDefaultVal = result.getString("COLUMN_DEF");
			}
			System.out.println("Default Value of Column is " + columnDefaultVal);
		} catch (SQLException e) {
			e.printStackTrace();
		}*/

		/*for (ColumnModel column : entity.getColumns()) {

			ResultSet resultForDefault = databaseMetaData.getColumns(con.getCatalog(), databaseMetaData.getUserName(),
					entity.getTableName().toLowerCase(), column.getColumnName().toLowerCase());
			String columnDefaultVal = "";
			if (resultForDefault.next()) {
				columnDefaultVal = resultForDefault.getString("COLUMN_DEF");
			}

			System.out.println("Default Value of Column is " + columnDefaultVal);
		}*/

		ResultSetMetaData rsmd = rs.getMetaData();
		int columnCount = rsmd.getColumnCount();
		StringBuilder sb = new StringBuilder(1024);
		if (columnCount > 0) {
			sb.append("CREATE TABLE ").append(rsmd.getTableName(1)).append(" (");
		}
		for (int i = 1; i <= columnCount; i++) {
			if (i > 1){
				sb.append(", ");
			}
			String columnName = rsmd.getColumnLabel(i);
			String columnType = rsmd.getColumnTypeName(i);

			sb.append(columnName).append(" ");

			if (columnType.equals("varchar")) {
				sb.append("CHARACTER VARYING");
			} else {
				sb.append(columnType);
			}

			int precision = rsmd.getPrecision(i);
			if (precision != 0) {
				sb.append("(").append(precision);

				if (columnType.equalsIgnoreCase("numeric")) {
					sb.append(',').append(rsmd.getScale(i));
				}

				sb.append(")");
			}

			if (columnType.equals("timestamp")) {
				sb.append(" WITHOUT TIME ZONE");
			}

			if (pkey.equals(columnName)) {
				sb.append(" PRIMARY KEY");
			} else if (uniqueColumns.contains(columnName)) {
				sb.append(" UNIQUE");
			}

			if (rsmd.isNullable(i) == ResultSetMetaData.columnNoNulls && !pkey.equals(columnName)) {
				sb.append(" NOT NULL");
			}

		} // for columns
		sb.append(" ) ");

		System.out.println(sb.toString());
		System.out.println("--------------");

		// generate alter query

		List<ColumnModel> columns = entity.getColumns(); // columns parsed from
															// excel sheet for
															// db
		List<ColumnModel> newColumns = new ArrayList<>(); // list of added
															// columns
		List<String> removedColumns = new ArrayList<>(); // list of removed
															// columns

		for (ColumnModel obj : columns) {
			newColumns.add(obj);
		}

		List<String> modifiedcolumns = new ArrayList<>(); // list of
															// modified
															// columns

		String alterQuery = "";
		for (int i = 1; i <= columnCount; i++) {
			String columnName = rsmd.getColumnLabel(i);

			ColumnModel cl = null;
			for (ColumnModel column : columns) {
				if (column.getColumnName().equalsIgnoreCase(columnName)) {
					cl = column;
					break;
				}
			}

			if (cl == null) {
				removedColumns.add(columnName);
			} else {
				alterQuery = Helper.getAlterQuery(rsmd, pkey, uniqueColumns, i, cl);

				if (!alterQuery.isEmpty()) {
					modifiedcolumns.add(alterQuery);
				}
			}

			for (ColumnModel obj : newColumns) {
				if (obj.getColumnName().equalsIgnoreCase(columnName)) {
					newColumns.remove(obj);
					break;
				}
			}
		} // for columns
		sb.append(" ) ");

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("New columns :" + newColumns.size() + "\nRemoved columns : " + removedColumns.size()
				+ "\nModified columns : " + modifiedcolumns.size());
		System.out.println(stringBuilder.toString());
		System.out.println("--------------");

		// alter query begins
		StringBuilder query = new StringBuilder();

		System.out.println("\n##########Alter query##########");

		int alteredColumns = 0;

		// add
		for (ColumnModel column : newColumns) {

			if (alteredColumns++ > 0) {
				query.append(',');
			}

			query.append(" ADD COLUMN ").append(column.getColumnName()).append(" ")
					.append(Helper.getColumnType(column));
		}

		// drop
		for (String column : removedColumns) {

			if (alteredColumns++ > 0) {
				query.append(',');
			}

			query.append(" DROP COLUMN ").append(column);
		}

		// modified

		for (String alterQuery1 : modifiedcolumns) {

			query.append(alterQuery1);
		}
		
		if(!query.toString().isEmpty()){
			System.out.println("ALTER TABLE " + entity.getTableName()+ query.toString());
		}
		System.out.println("--------------");

		return sb.toString();
	}

	private String createSqlQueryFromExcel(EntityModel entity) {
		StringBuilder stringComment = new StringBuilder();
		StringBuilder stringSequence = new StringBuilder();
		StringBuilder string = new StringBuilder("CREATE TABLE " + entity.getTableName().toUpperCase() + "(");
		int firstElement = 0;
		for (ColumnModel column : entity.getColumns()) {
			if (firstElement != 0) {
				string.append(",");
				if (formatSQL) {
					string.append("\n");
				}
			}
			firstElement++;

			string.append(column.getColumnName().toUpperCase() + StringUtils.SPACE);
			boolean isTimestamp = false;
			boolean isVarchar = false;
			boolean isSizeBracketRequired = true;
			if (numericValues.contains(column.getType().toUpperCase())) {
				string.append("NUMERIC(" + column.getSize());
				if (StringUtils.isEmpty(column.getPercision())) {
					string.append(",0");
				} else {
					string.append("," + column.getPercision());
				}
			} else if (stringValues.contains(column.getType().toUpperCase())) {
				isVarchar = true;
				string.append("CHARACTER VARYING(" + column.getSize());

			} else if (timeValues.contains(column.getType().toUpperCase())) {
				string.append("TIMESTAMP(" + column.getSize());
				isTimestamp = true;
			} else {
				if (StringUtils.isNotEmpty(column.getSize()) && !"0".equalsIgnoreCase(column.getSize())) {
					string.append(column.getType() + "(" + column.getSize());
				} else {
					string.append(column.getType());
					isSizeBracketRequired = false;
				}
			}
			if (isSizeBracketRequired) {
				string.append(")");
			}

			if (column.isPrimaryKey()) {
				string.append(" PRIMARY KEY");
			}
			if (column.isUnique()) {
				string.append(" UNIQUE");
			}
			if (isTimestamp) {
				string.append(" WITHOUT TIME ZONE");
			}
			if (column.isAutoIncrement()) {
				stringSequence.append(
						"CREATE SEQUENCE " + entity.getTableName() + "_" + column.getColumnName() + "_SEQ START 1;");
				column.setDefaultValue(
						"nextval('" + entity.getTableName() + "_" + column.getColumnName() + "_SEQ" + "')");
			}
			if (StringUtils.isNotEmpty(column.getDefaultValue())) {
				string.append(" DEFAULT ");
				if (isVarchar) {
					string.append("'" + column.getDefaultValue() + "'");
				} else if (isTimestamp) {
					string.append("now()");
				} else {
					string.append(column.getDefaultValue());
				}
			}
			if (!column.isNullable() && !column.isPrimaryKey()) {
				string.append(" NOT NULL");
			}
			if (column.isForeignKey()) {
				string.append(
						" REFERENCES " + column.getForeignKeyTableName() + " (" + column.getForeignKeyName() + ")");
			}
			if (StringUtils.isNotBlank(column.getComments())) {
				String comment = "COMMENT ON COLUMN " + entity.getTableName() + "." + column.getColumnName() + " IS '"
						+ column.getComments() + "';";
				stringComment.append(comment);
			}

		}
		string.append(");");
		if (StringUtils.isNotBlank(entity.getComment())) {
			String comment = "COMMENT ON TABLE " + entity.getTableName() + " IS '" + entity.getComment() + "';";
			string.append(comment);
		}

		// System.out.println(string.toString());

		return string.toString();

		// return stringSequence.toString() + "\n" + string.toString() + "\n" +
		// stringComment.toString();
	}

	@Override
	public void generateTablesByFile(Connection connection, String inputFile) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void generateTablesThroughStatement(Statement statement, String statementString) throws Exception {
		// TODO Auto-generated method stub

	}

}
