package com.parse.java.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EntityModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String tableName;
	private List<ColumnModel> columns;
	private String schemaName;
	private String comment;

	public String getTableName() {
		return tableName;
	}

	public void setTableName(String tableName) {
		this.tableName = tableName;
	}

	public List<ColumnModel> getColumns() {
		return columns;
	}

	public void setColumns(List<ColumnModel> columns) {
		this.columns = columns;
	}

	public String getSchemaName() {
		return schemaName;
	}

	public void setSchemaName(String schemaName) {
		this.schemaName = schemaName;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void addColumns(ColumnModel column) {
		if (this.columns == null) {
			this.columns = new ArrayList<>();
		}
		this.columns.add(column);
	}

}
