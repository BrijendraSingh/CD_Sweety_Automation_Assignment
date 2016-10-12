package framework_bps;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class BRIJ_Log {
	static Logger logger = Logger.getLogger(BRIJ_Log.class);
	
	public static void createLogger(){
		String log4jConfigFile = System.getProperty("user.dir")+ "\\Files\\log4j.properties.txt";
		//System.out.println(log4jConfigFile);
		PropertyConfigurator.configure(log4jConfigFile);
		logger.info("Logger Started");	
	}
	
	public static void log(String Message){
		logger.info(Message);
	}
	
	public static void error(String error){
		logger.error(error);
	}
	
	public static void warning(String warning){
		logger.warn(warning);
	}
}
