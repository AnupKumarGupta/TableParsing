package com.parse.java.interfaces;

import java.io.FileWriter;
import java.util.List;

import com.parse.java.model.EntityModel;

public interface IScriptGenerator {

	List<EntityModel> generateEntityModelByFile(String inputFilePath,int sheetNumber) throws Exception;
	
	FileWriter generateSriptsFromModel(String outputFilePath, IDBProvider dbProvider, List<EntityModel> entities) throws Exception;
	
}
