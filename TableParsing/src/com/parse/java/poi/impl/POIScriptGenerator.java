package com.parse.java.poi.impl;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.util.CellRangeAddress;

import com.parse.java.constant.Constants;
import com.parse.java.framework.FrameworkHelper;
import com.parse.java.interfaces.IDBProvider;
import com.parse.java.interfaces.IScriptGenerator;
import com.parse.java.model.ColumnModel;
import com.parse.java.model.EntityModel;

public class POIScriptGenerator implements IScriptGenerator {

	private Logger log = FrameworkHelper.getLogger(POIScriptGenerator.class);

	@Override
	public List<EntityModel> generateEntityModelByFile(String inputFilePath, int sheetNumber) throws Exception {
		// POIFSFileSystem fs = new POIFSFileSystem(new
		// FileInputStream(inputFilePath));
		OPCPackage fs = OPCPackage.open(new File(inputFilePath));

		Workbook wb = WorkbookFactory.create(fs);
		Sheet sheet = wb.getSheetAt(sheetNumber);
		Row row;

		int rows; // No of rows
		rows = sheet.getPhysicalNumberOfRows();

		int cols = 0; // No of columns
		int tmp = 0;

		// This trick ensures that we get the data properly even if it doesn't
		// start from first few rows
		for (int i = 0; i < 10 || i < rows; i++) {
			row = sheet.getRow(i);
			if (row != null) {
				tmp = sheet.getRow(i).getPhysicalNumberOfCells();
				if (tmp > cols)
					cols = tmp;
			}
		}

		List<EntityModel> entites = new ArrayList<>();
		EntityModel entity = null;
		for (int r = 2; r < rows; r++) {

			row = sheet.getRow(r);
			if (row != null) {
				if (isMergedCell(sheet, row)) {
					if (entity != null) {
						if (entity.getColumns() != null && entity.getColumns().size() > 0) {
							entites.add(entity);
							entity = null;
						} else {
							log.error("Entity skipped because no columns were found : " + entity.getTableName());
						}
					}
					entity = new EntityModel();
					entity.setTableName(row.getCell(0).getStringCellValue());
				} else {
					if (entity != null && StringUtils.isNotBlank(entity.getTableName())) {
						ColumnModel column = createColumnFromRow(row, cols,
								entity.getTableName().substring(0, entity.getTableName().indexOf("_")));
						if (column != null) {
							entity.addColumns(column);
						} else {
							//log.error("Column skipped in the table : " + entity.getTableName());
						}
					} else {
						log.error("Row found belonging to no entity : " + row.getRowNum());
					}

				}

			} else {
				if (entity != null) {
					if (entity.getColumns() != null && entity.getColumns().size() > 0) {
						entites.add(entity);
						entity = null;
					} else {
						log.error("Entity skipped because no columns were found : " + entity.getTableName());
					}
				}
			}
		}
		// For the last table
		if (entity != null) {
			if (entity.getColumns() != null && entity.getColumns().size() > 0) {
				entites.add(entity);
			} else {
				log.error("Entity skipped because no columns were found : " + entity.getTableName());
			}
		}
		return entites;

	}

	private boolean isMergedCell(Sheet sheet, Row row) {
		for (int i = 0; i < sheet.getNumMergedRegions(); i++) {
			CellRangeAddress mergedRegion = sheet.getMergedRegion(i);
			if (row.getRowNum() == mergedRegion.getFirstRow()) {
				return true;
			}
		}
		return false;
	}

	private ColumnModel createColumnFromRow(Row row, int cols, String tablePrefix) {

		if (cols < 7) {
			log.error("Skipping Table Column on row : " + row.getRowNum());
			return null;
		}
		if (row.getCell(Constants.INDEX_COLUMN_NAME) == null
				|| StringUtils.isEmpty(row.getCell(Constants.INDEX_COLUMN_NAME).getStringCellValue())) {
			return null;
		}
		ColumnModel column = new ColumnModel();

		// Column Name
		column.setColumnName(row.getCell(Constants.INDEX_COLUMN_NAME).getStringCellValue().toString().trim());

		// Constraints
		Cell contraintCol = row.getCell(Constants.INDEX_COLUMN_CONSTRAINT);
		if (contraintCol != null && StringUtils.isNotEmpty(contraintCol.getStringCellValue())) {
			if (row.getCell(Constants.INDEX_COLUMN_CONSTRAINT).getStringCellValue().equalsIgnoreCase("Primary Key")) {
				column.setPrimaryKey(true);
			} else if (row.getCell(Constants.INDEX_COLUMN_CONSTRAINT).getStringCellValue().equalsIgnoreCase("Unique")) {
				column.setUnique(true);
			} else if (row.getCell(Constants.INDEX_COLUMN_CONSTRAINT).getStringCellValue().equalsIgnoreCase("Foreign Key")) {
				column.setForeignKey(true);
				if (StringUtils.isNotBlank(row.getCell(Constants.INDEX_COLUMN_FOREIGN_TABLE_NAME).getStringCellValue())) {
					column.setForeignKeyTableName(row.getCell(Constants.INDEX_COLUMN_FOREIGN_TABLE_NAME).getStringCellValue());
				} else {
					column.setForeignKeyTableName(
							tablePrefix + "_" + column.getColumnName().replace(Constants.FOREIGN_KEY_SUFFIX, StringUtils.EMPTY));
				}
				column.setForeignKeyName(column.getColumnName());
			}
		}

		// Column Type
		column.setType(row.getCell(Constants.INDEX_COLUMN_TYPE).getStringCellValue());

		// Column SIZE
		column.setSize(String.valueOf((int) row.getCell(Constants.INDEX_COLUMN_SIZE).getNumericCellValue()));

		// Column SIZE
		column.setPercision(String.valueOf((int) row.getCell(Constants.INDEX_COLUMN_PERCISION).getNumericCellValue()));

		// Column DEFAULT Value
		Cell defaultCol = row.getCell(Constants.INDEX_COLUMN_DEFAULT_VALUE);
		if (defaultCol != null) {
			column.setDefaultValue(defaultCol.getStringCellValue());
		}

		// Nullable
		if (StringUtils.isNotEmpty(row.getCell(Constants.INDEX_COLUMN_NULLABLE).getStringCellValue())) {
			if (row.getCell(Constants.INDEX_COLUMN_NULLABLE).getStringCellValue().equalsIgnoreCase("Yes")) {
				column.setNullable(true);
			}
		}

		// AutoIncrement
		if (StringUtils.isNotEmpty(row.getCell(Constants.INDEX_COLUMN_AUTOINCREMENT).getStringCellValue())) {
			if (row.getCell(Constants.INDEX_COLUMN_AUTOINCREMENT).getStringCellValue().equalsIgnoreCase("Yes")) {
				column.setAutoIncrement(true);
			}
		}

		// Column DEFAULT Value
		column.setComments(row.getCell(Constants.INDEX_COLUMN_COMMENTS).getStringCellValue());

		return column;
	}

	@Override
	public FileWriter generateSriptsFromModel(String outputFilePath, IDBProvider dbProvider, List<EntityModel> entities)
			throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
