package com.parse.java.framework;

import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class FrameworkHelper {

	private static final String BASE_CONFIG_FILE = "resources/config.properties";

	private static Properties properties;
	
	private static Map<String, Object> instanceMap = new HashMap<>();

	public static void init()  {
		try{
			properties = new Properties();
			String frameworkImplFile = System.getProperty("ConfigFile");
			FileInputStream inputStream = null;
			if (StringUtils.isNotBlank(frameworkImplFile)) {
				inputStream = new FileInputStream(frameworkImplFile);
			} else {
				inputStream = new FileInputStream(BASE_CONFIG_FILE);
			}
			properties.load(inputStream);
		    System.setProperty("log4j.configuration", "file:resources/log4j.properties");

		} catch(Exception e){
			System.out.println("Unable to initialize framework with reason : ");
			e.printStackTrace();
			//Force Shut the process.
			System.exit(0);
		}
		
	}

	@SuppressWarnings("unchecked")
	public static <T> T getImplementation(Class<T> clz) throws Exception {
		if (properties == null) {
			throw new Exception("Framework is not initialized. Call FrameworkHelper.init()");
		}

		if(instanceMap.get(clz.getName())==null){
			Class<T> implClz = (Class<T>) Class
					.forName(StringUtils.trimToEmpty(properties.getProperty(clz.getSimpleName())));
			instanceMap.put(clz.getName(), implClz.getConstructor().newInstance());
		}
		
		return (T)instanceMap.get(clz.getName());

	}
	
	public static Logger getLogger(@SuppressWarnings("rawtypes") Class clz){
		Logger logger = Logger.getLogger(clz);
		return logger;
	}
	
	public static String getProperty(String key) throws Exception{
		if (properties == null) {
			throw new Exception("Framework is not initialized. Call FrameworkHelper.init()");
		}
		return properties.getProperty(key);
	}
	

}
