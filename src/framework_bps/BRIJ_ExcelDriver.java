package framework_bps;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.relevantcodes.extentreports.ExtentTest;

public class BRIJ_ExcelDriver {
	static Workbook RunManager,TestData;
	static Sheet main, currentSheet_key, currentSheet_Data;
	static String currentSheetName;
	static ExtentTest child;

	public static void main(String[] args){
		int main_userRow;

		BRIJ_Log.createLogger();
		BRIJ_Config.ConfiGFileSetup();
		BRIJ_Reporter.StartReporter();
		
		//connect the test data and run manager sheet
		String Runamanger = "RunManager.xlsx", Testdata="TestData.xlsx";
		RunManager = BRIJ_ExcelUtil.connectXl(Runamanger);
		TestData = BRIJ_ExcelUtil.connectXl(Testdata);
		
		BRIJ_ExcelUtil.setTestDataWB(TestData);
		//connecting with main sheet in Runmanager
		main = RunManager.getSheet("Main");
		main_userRow = main.getLastRowNum()-main.getFirstRowNum();
		
		//read main sheet-run module
		for(int i=1; i<=main_userRow ; i++){
			if (main.getRow(i)==null || main.getRow(i).getCell(1)==null) break;			//Brijendra- null pointer check
			if(main.getRow(i).getCell(1).toString().equalsIgnoreCase("TRUE")){
				BRIJ_Log.log("\n");
				currentSheetName = main.getRow(i).getCell(0).toString(); 
				BRIJ_Log.log("MODULE UNDER EXECUTION [" + currentSheetName + "]");
				
				//Bases on the Execution flag, take the control over the current sheet for both the Exel files
				currentSheet_key = RunManager.getSheet(currentSheetName);
				currentSheet_Data=TestData.getSheet(currentSheetName);
				BRIJ_ExcelUtil.setDataSheet(currentSheet_Data);
				//read moduleSheet to run test cases
				BRIJ_ExcelUtil.executeSheet(currentSheet_key);
			}
		}
		BRIJ_Reporter.flushReporter();
		BRIJ_Log.log("%%%%%%%%%%%%%     All applicable Modules are executed       %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
		//System.out.println("%%%%%%%%%%%%%All applicable Modules are executed%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%")
		//launch result
		BRIJ_ExcelUtil.launchResult();
		
	}
	
}
