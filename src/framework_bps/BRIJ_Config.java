/**
 * 
 */
package framework_bps;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/** FW_Logger
 * ----------------------------------------------------------------------------------------------------
 * @author: Brijendra Singh
 * @Date  : May 03, 2016 
 * @Discription: FW_Logger, Manages the Framework logfile and logging methods
 * -----------------------------------------------------------------------------------------------------
 */
public class BRIJ_Config {
	
	public static Properties config;
	
	/** ConfiGFileSetup
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 05, 2016 
	 * @Discription: ConfiGFileSetup,load and setup the Framework Configuration File
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static Properties ConfiGFileSetup(){
		config=null;
		try {
			File configFile = new File(System.getProperty("user.dir")+"\\Files\\Config.property");
			FileInputStream configFIS = new FileInputStream(configFile);
			config=new Properties();
			config.load(configFIS);	
			BRIJ_Log.log("Config file loaded " + System.getProperty("user.dir")+"\\Files\\Config.property");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//System.out.println("Error creating/loading the Properties File " +e.getCause() );
			BRIJ_Log.error("Error creating/loading the Properties File " +e.getCause());
			e.printStackTrace();
		}
		return config;
	}
	
}
