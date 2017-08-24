package com.parse.java.postgres.impl;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;

import com.parse.java.constant.Constants;
import com.parse.java.model.ColumnModel;

public class Helper implements Constants {

	public static String getAlterQuery(ResultSetMetaData rsmd, String pkey, ArrayList<String> uniqueColumns, int i,
			ColumnModel column) throws SQLException {

		StringBuilder newAlterStatement = new StringBuilder();

		String columnName = rsmd.getColumnLabel(i);
		String columnType = rsmd.getColumnTypeName(i);

		StringBuilder sqlQueryBuilder = new StringBuilder(1024);

		if (columnType.equals("varchar")) {
			sqlQueryBuilder.append("CHARACTER VARYING");
		} else {
			sqlQueryBuilder.append(columnType);
		}

		int precision = rsmd.getPrecision(i);
		if (precision != 0) {
			sqlQueryBuilder.append("(").append(precision);

			if (columnType.equalsIgnoreCase("numeric")) {
				sqlQueryBuilder.append(',').append(rsmd.getScale(i));
			}

			sqlQueryBuilder.append(")");
		}

		if (columnType.equals("timestamp")) {
			sqlQueryBuilder.append(" WITHOUT TIME ZONE");
		}

		StringBuilder excelStringBuilder = new StringBuilder(1024);
		boolean isTimestamp = false;
		boolean isVarchar = false;
		boolean isSizeBracketRequired = true;
		if (numericValues.contains(column.getType().toUpperCase())) {
			excelStringBuilder.append(column.getType() + "(" + column.getSize());
			if (StringUtils.isEmpty(column.getPercision())) {
				excelStringBuilder.append(",0");
			} else {
				excelStringBuilder.append("," + column.getPercision());
			}
		} else if (stringValues.contains(column.getType().toUpperCase())) {
			isVarchar = true;
			excelStringBuilder.append("CHARACTER VARYING(" + column.getSize());

		} else if (timeValues.contains(column.getType().toUpperCase())) {
			excelStringBuilder.append("TIMESTAMP(" + column.getSize());
			isTimestamp = true;
		} else {
			if (StringUtils.isNotEmpty(column.getSize()) && !"0".equalsIgnoreCase(column.getSize())) {
				excelStringBuilder.append(column.getType() + "(" + column.getSize());
			} else {
				excelStringBuilder.append(column.getType());
				isSizeBracketRequired = false;
			}
		}
		if (isSizeBracketRequired) {
			excelStringBuilder.append(")");
		}

		if (isTimestamp) {
			excelStringBuilder.append(" WITHOUT TIME ZONE");
		}

		if (!excelStringBuilder.toString().equalsIgnoreCase(sqlQueryBuilder.toString())) {
			newAlterStatement.append("ALTER COLUMN ").append(columnName).append(" TYPE ").append(excelStringBuilder.toString());
		}

		if ((uniqueColumns.contains(columnName) && !pkey.equalsIgnoreCase(columnName)) ^ column.isUnique()) {

			if (!newAlterStatement.toString().isEmpty()) {
				newAlterStatement.append(',');
			}

			if (column.isUnique()) {
				// Add constraint

				newAlterStatement.append("ADD CONSTRAINT ").append(columnName).append("_key UNIQUE (").append(columnName)
						.append(")");

			} else {
				// Drop constraint
				newAlterStatement.append("DROP CONSTRAINT ").append(columnName).append("_key");
			}
		}

		if ((rsmd.isNullable(i) == ResultSetMetaData.columnNullable && !pkey.equalsIgnoreCase(columnName))
				^ column.isNullable()) {

			if (!newAlterStatement.toString().isEmpty()) {
				newAlterStatement.append(',');
			}

			if (column.isNullable()) {
				// Add constraint

				newAlterStatement.append("ALTER COLUMN ").append(columnName).append(" DROP NOT NULL");

			} else {
				// Drop constraint
				newAlterStatement.append("ALTER COLUMN ").append(columnName).append(" SET NOT NULL");
			}
		}

		if (!newAlterStatement.toString().isEmpty()) {
			return ", " + newAlterStatement.toString();
		} else {
			return newAlterStatement.toString();
		}
	}

	public static String getColumnType(ColumnModel column) {
		boolean isTimestamp = false;
		boolean isVarchar = false;
		boolean isSizeBracketRequired = true;
		StringBuilder string = new StringBuilder();
		if (numericValues.contains(column.getType().toUpperCase())) {
			string.append(column.getType() + "(" + column.getSize());
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
		if ((StringUtils.isNotEmpty(column.getDefaultValue()) || !column.isNullable()) && !column.isPrimaryKey()) {
			string.append(" DEFAULT ");
			String def = column.getDefaultValue();
			if (isVarchar) {
				string.append("'" + def + "'");
			} else if (isTimestamp) {
				string.append("now()");
			} else {
				if (StringUtils.isEmpty(def)) {
					def = "0";
				}

				string.append(def);
			}
		}
		if (!column.isNullable() && !column.isPrimaryKey()) {
			string.append(" NOT NULL");
		}
		if (column.isForeignKey()) {
			string.append(" REFERENCES " + column.getForeignKeyTableName() + " (" + column.getForeignKeyName() + ")");
		}
		return string.toString();
	}

}
