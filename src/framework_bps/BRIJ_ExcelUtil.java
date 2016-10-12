package framework_bps;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.internal.ProfilesIni;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import com.relevantcodes.extentreports.ExtentTest;

import businessComponents.Keywords;
import framework_bps.BRIJ_Log;


public class BRIJ_ExcelUtil {
	
	static String testCaseName, iterationMode, strCurrentKeyword, strTestCaseDescription;
	static Sheet currentSheet, dataSheet;
	static int sheet_rowCount,IterationStartRow_TestData,IterationCrntRow_TestData,subIterationRow_TestData ,startIteration, endIteration, currentIteration, intSubIterations, keywordCount, runSubiterationCount, intMasterTDRow,intBPSsubIteration;
	static Row keywordRow, dataRow;
	static String[] arrCurrentFlowData;
	static List<String> groupedKeywords;
	//static boolean breakWhileLoop;
	static Workbook RunManager,TestData;
	static Sheet main, currentSheet_key, currentSheet_Data;
	static String currentSheetName;
	static WebDriver driver;
	static ExtentTest ChildTest,IterationTest;
	static Keywords B_lib;
	static String ResultFolder;


	public static Workbook connectXl(String Wname){
		Workbook xl=null;		
		try {
			File xlFile = new File(System.getProperty("user.dir")+"\\Files\\" + Wname);
			FileInputStream fis = new FileInputStream(xlFile);
			xl = new XSSFWorkbook(fis);
		} catch (FileNotFoundException e) {
			//BRIJ_Log.warning("File not found " + Wname);
			//System.out.println("File not found " + Wname);
			e.printStackTrace();
		} catch (IOException e) {
			//System.out.println("IO exception while opening XSSF workbook");
			//BRIJ_Log.warning("IO exception while opening XSSF workbook");
			e.printStackTrace();
		}
		return xl;
	}
	

	public static void executeSheet(Sheet exSheet){
		currentSheet=exSheet;
		//System.out.println("Sheet Name is " + currentSheet.getSheetName().toString());
		sheet_rowCount=currentSheet.getLastRowNum()-currentSheet.getFirstRowNum();
		for (int i=1; i<=sheet_rowCount;i++){
			//if(currentSheet.getRow(i).getCell(1).toString().isEmpty()) break;
			if(currentSheet.getRow(i)==null || currentSheet.getRow(i).getCell(1)==null || currentSheet.getRow(i).getCell(1).toString().isEmpty())break; 										//null pointer check
			if(currentSheet.getRow(i).getCell(1).toString().equalsIgnoreCase("True")){
				testCaseName=currentSheet.getRow(i).getCell(0).toString();
				strTestCaseDescription = currentSheet.getRow(i).getCell(2).toString();
				keywordRow=currentSheet.getRow(i);
				
				//@ Start BRIJ_Reporter
				BRIJ_Reporter.StartReporterTest(testCaseName, strTestCaseDescription);
				BRIJ_Reporter.setMODULE(currentSheet.getSheetName().toString());
				//@ Start cases execution 
				executeTestCase();
				
				//@ End BRIJ_Reporter
				BRIJ_Reporter.endTest();
			}
		}
		//BRIJ_Log.log("@@Module {" + currentSheet.getSheetName() + "} Execution Completed");
	}
	
	public static boolean setIterationRow(){
		int intUsedRow=dataSheet.getLastRowNum()-dataSheet.getFirstRowNum();
		boolean flag=false;
		for (int sb=1; sb<=intUsedRow; sb++){
			if (dataSheet.getRow(sb).getCell(0).toString().equalsIgnoreCase(testCaseName) && dataSheet.getRow(sb).getCell(1).getNumericCellValue()==currentIteration){
				//System.out.println("Data Row pointer at " + sb + " ,Testcases is " + testCaseName + " , itration is " + currentIteration);
				//BRIJ_Log.log("Test Data Row is Pointing at {" + sb + "} ,TESTCASE {" + testCaseName + "} , ITERATION {" + currentIteration + "}");
				IterationCrntRow_TestData=sb;
				flag=true;
				break;
			}
		}
		return flag;
	}
	

	public static void executeTestCase(){
		
		//BRIJ_Log.log("TEST CASE under Execution [" + testCaseName +"]");
		//-----------------------
		readIteration();
		setDataRow();
		//----------------------
		
		IterationCrntRow_TestData=IterationStartRow_TestData;
		subIterationRow_TestData = IterationCrntRow_TestData;
		
		while(currentIteration <= endIteration){
			//logic to validate incorrect iteration row
			if (dataSheet.getRow(IterationCrntRow_TestData)==null) break;    //null pointer check
			if(!dataSheet.getRow(IterationCrntRow_TestData).getCell(0).toString().equalsIgnoreCase(testCaseName)){
				if (iterationMode.equalsIgnoreCase("Run from <Start Iteration> to <End Iteration>") || currentIteration==1){
					//BRIJ_Log.log("Iteration started (" + currentIteration + ")");
					//BRIJ_Log.log("Iteration Completed (" + currentIteration + ")");
					//BRIJ_Log.warning("No test data found for this test case iteration ! All subsequent iterations aborted");
				}
				break;
			}
			
			//set iteration row number and check for correct data row
			if (!setIterationRow()){
				if (endIteration==65535){
					//BRIJ_Log.warning("ITERATION COMPLETED - NO TEST DATA ROW AVAILABLE");
					break;
				}else{
					//BRIJ_Log.warning("Test Data row not found for Iteration {" + currentIteration + "} , ! All subsequent iterations aborted");
					break;
				}
			}
			
			//BRIJ_Log.log("ITERATION STARTED [" + currentIteration + "]");
			//IterationTest = BRIJ_Reporter.StartIteration_ReporterTest("Iteration " + Integer.toString(currentIteration));
			//ChildTest = BRIJ_Reporter.StartChild_ReporterTest("Iteration " + Integer.toString(currentIteration));
			//run loop for available keywords
			//keywordCount=keywordRow.getLastCellNum()-6;
			keywordCount=keywordRow.getLastCellNum();
			//KEYWORDS LOOOP----------------------------------------------------------------------
			groupedKeywords = new ArrayList<String>();
			for (int k=6;k<=keywordCount;k++){
				if(keywordRow.getCell(k)==null) break;  //null pointer check
				if (!keywordRow.getCell(k).toString().isEmpty()){					
					arrCurrentFlowData = keywordRow.getCell(k).toString().split(",");
					strCurrentKeyword=arrCurrentFlowData[0];
					//intBPSsubIteration=0;
						//NO SUB ITERATION KEYWORD , without [,]
						if (arrCurrentFlowData.length==1){
							intSubIterations=1;
							groupedKeywords.add(strCurrentKeyword);
							intMasterTDRow=IterationCrntRow_TestData;
							
							////BRIJ_Log.log("Master test data Row " + intMasterTDRow);
							//BRIJ_Log.log("TEST_DATA_ROW {" + IterationCrntRow_TestData + "}   , TEST_CASE {" + testCaseName + "}, ITERATION {" + currentIteration +"}"  );
							//EXECUTION OF NON SUB ITERATION KEYWORDS
							for (String subg: groupedKeywords){
								////BRIJ_Log.log(subg);
								executeKeyword(subg);
							}
							
							groupedKeywords.clear();
							
						//SUB ITERATION KEYWORDS, 	WITH [,]	
						}else{
							intSubIterations= Integer.valueOf(arrCurrentFlowData[1]);
							
							//GROUPING KEYWORDS FOR SAME SUBITERATION
							if (intSubIterations==0){
								groupedKeywords.add(strCurrentKeyword);
							
							//EXECUTION OF GROUPED KEYWORDS
							}else if (intSubIterations>0){
								groupedKeywords.add(strCurrentKeyword);
								runSubiterationCount=intSubIterations;
						
								int groupIndex=1;
								for ( int subIt = IterationCrntRow_TestData; subIt<IterationCrntRow_TestData+intSubIterations;subIt++ ){
									intMasterTDRow=subIt;
									
									//BRIJ_Log.log("Master test data Row " + intMasterTDRow);
									//BRIJ_Log.log("TEST_DATA_ROW {" + subIt + "}   , TEST_CASE {" + testCaseName + "}, ITERATION {" + currentIteration + "}, SUBITERATION {" + groupIndex +"}" );
									intBPSsubIteration = groupIndex;
									for (String subg: groupedKeywords){
										////BRIJ_Log.log(subg);
										executeKeyword(subg);
									}
									groupIndex=groupIndex+1;
								}
								//intBPSsubIteration=0;
								groupedKeywords.clear();
								////BRIJ_Log.log("------------------------------------------------------------------");
							}
						}										
					}else{
						////BRIJ_Log.log("------------------------------------------------------------------");
						break;
					}
				}
			//BRIJ_Reporter.Append_IterationTest(ChildTest);
			//BRIJ_Reporter.Append_ChildTest(ChildTest);
			//BRIJ_Log.log("ITERATION COMPLETED [" + currentIteration + "] ");
			////BRIJ_Log.log("------------------------------------------------------------------");
			currentIteration=currentIteration+1;  			
		}
		//BRIJ_Log.log("TEST CASE EXECUTION COMPLETED [" + testCaseName +"]\n");
	}
	
	public static void readIteration(){
		iterationMode=keywordRow.getCell(3).toString();
		//System.out.println();
		//System.out.println("@@@@@@@@@@@@----------Iteration Mode is: " + iterationMode);
		////BRIJ_Log.log("------------------------------------------------------------------");
		//BRIJ_Log.log("ITERATION MODE: [" + iterationMode + "]");
		if (iterationMode.equalsIgnoreCase("Run one iteration only")){
			startIteration=1; 
			endIteration=1;
			currentIteration=1;
			
		}else if(iterationMode.equalsIgnoreCase("Run all iterations")){
			startIteration=1; 
			endIteration=65535;
			currentIteration=1;
			
		}else if(iterationMode.equalsIgnoreCase("Run from <Start Iteration> to <End Iteration>")){
			if (keywordRow.getCell(4).toString().isEmpty()){
				startIteration=1;
			}else{
				startIteration=(int)keywordRow.getCell(4).getNumericCellValue(); 
			}
			
			if (keywordRow.getCell(5).toString().isEmpty()){
				endIteration=1;
			}else{
				endIteration=(int)keywordRow.getCell(5).getNumericCellValue(); 
			}
			//BRIJ_Log.log("Start_Iteration [" + startIteration + "]  !! End_Iterattion [" + endIteration +"]");
			//System.out.println("Start iteration: " + startIteration + " , End iterattion is: " + endIteration);
			
			currentIteration=startIteration;
		}else{
			//BRIJ_Log.log("No iteration mode is found do assuming only 1 iteration");
			//System.out.println("No iteration mode is found do assuming only 1 iteration");
			startIteration=1; 
			endIteration=1;
			currentIteration=1;
		}
		////BRIJ_Log.log("------------------------------------------------------------------");
	}
	

	public static void setDataSheet(Sheet datasheet){
		dataSheet=datasheet;
	}
	

	static void setTestDataWB(Workbook testDataWB){
		TestData=testDataWB;
	}
	

	public static void setDataRow(){
		int intRow=0;
		//System.out.println("Test Case name from Data sheet -" + dataSheet.getRow(intRow).getCell(0));
		while(!dataSheet.getRow(intRow).getCell(0).toString().isEmpty()){
			//System.out.println("0000000000 - test data sheet - TC " + dataSheet.getRow(intRow).getCell(0) + " , row num  " + dataSheet.getRow(intRow).getCell(10) );
			if (dataSheet.getRow(intRow).getCell(0).toString().equalsIgnoreCase(testCaseName) && dataSheet.getRow(intRow).getCell(1).getNumericCellValue()==startIteration ){
				dataRow=dataSheet.getRow(intRow);
				//System.out.println("~~~~~~~~~Data sheet TC name- " + dataSheet.getRow(intRow).getCell(0) + " , data row Num " + IterationStartRow_TestData);
				break;
			}
			intRow=intRow+1;
			IterationStartRow_TestData= intRow;
		}
		
	}
	
	public static void executeKeyword(String keyword){
		//@@ Child test started
		ChildTest = BRIJ_Reporter.StartChild_ReporterTest("Iteration [" + currentIteration + "] ......." + keyword);

		
		//@@ check for launch browser to setup the browser and pass the driver
		if (keyword.equalsIgnoreCase("LaunchBrowser")){
			driver=BRIJ_BrowserSetup.LaunchBrowser();
			B_lib =  new Keywords(driver);
		}else{
			Method method;
			try {
				//launch keyword
				method = Keywords.class.getDeclaredMethod(keyword,null);
				method.invoke(B_lib, null);	
				if (BRIJ_Config.config.getProperty("TerminateTC").equalsIgnoreCase("yes")){
					//framework_ReportClass.logFATAL("Terminating This TC Execution", "FATAL ERROR");
					BRIJ_Config.config.setProperty("TerminateTC", "NO");
					driver.close();
					driver.quit();
					//break;
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				//BRIJ_Log.log("Problem execution/Invoking the Keyword " + keyword + ", Casue - " + e.getCause()+ " , Message: " + e.getStackTrace());
				BRIJ_Reporter.logFATAL(keyword + "could not be executed ", e.getCause() + " | " + e.getStackTrace());
			}
		}
		BRIJ_Reporter.Append_ChildTest(ChildTest);
	}
	
	
	/** get_TestData
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 03, 2016 
	 * @Discription: get_TestData, Take the testData from the testData sheet for corresponding parameter
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static String BPS_GetTestData(String paramname){
		
		String paramvalue="",commonDataIdentifier,CommonDataSheetName,keyVal,cellValue=" ",Bkey="",Ckey="" ;
		int intindexrow, intparamCol=0;
		boolean paramFlag=false, cmnFlag=false;;
			
		intindexrow = dataSheet.getRow(0).getLastCellNum()-dataSheet.getRow(0).getFirstCellNum();
		//cell formula handeling
		
		FormulaEvaluator fe = TestData.getCreationHelper().createFormulaEvaluator();
		//
		
		for (int ic=0; ic<=intindexrow; ic++){
			if (dataSheet.getRow(0).getCell(ic).toString().isEmpty()){
				cellValue="";
			}else{
				cellValue = fe.evaluate(dataSheet.getRow(0).getCell(ic)).formatAsString().replaceAll("\"", "");
			}
			//if (dataSheet.getRow(0).getCell(ic).toString().equalsIgnoreCase(paramname)){
			if (cellValue.equalsIgnoreCase(paramname)){
				intparamCol=ic;
				paramFlag=true;
				//BRIJ_Log.log("get_TestData (" + paramname + ") discovered at col: " + intparamCol);
				break;
			}
		}
		
		if (paramFlag){
			
			//keyVal = dataSheet.getRow(intMasterTDRow).getCell(intparamCol).toString();
			if (dataSheet.getRow(intMasterTDRow).getCell(intparamCol).toString().isEmpty()){
				keyVal="";
			}else{	
				keyVal = fe.evaluate(dataSheet.getRow(intMasterTDRow).getCell(intparamCol)).formatAsString().replaceAll("\"", "");;
			}
			commonDataIdentifier=BRIJ_Config.config.getProperty("CommonDataIdentifier");
			CommonDataSheetName =BRIJ_Config.config.getProperty("CommonnDataSheet_Name");
			
			if (keyVal.indexOf(commonDataIdentifier)==0){
				Sheet CDSheet = TestData.getSheet(CommonDataSheetName);
				String CD_name = keyVal.substring(1).toString();
				
				int CD_usedRow, CD_usedCol;
				CD_usedRow=CDSheet.getLastRowNum()-CDSheet.getFirstRowNum();
				CD_usedCol=CDSheet.getRow(0).getLastCellNum()-CDSheet.getRow(0).getFirstCellNum();
				
				for(int loopR=1;loopR<CD_usedRow;loopR++){
					Row CD_row = CDSheet.getRow(loopR);
					//if (CD_row.getCell(0).toString().equalsIgnoreCase(CD_name)){
					if (CD_row.getCell(0).toString().isEmpty()){
						Bkey="";
					}else{
						Bkey=fe.evaluate(CD_row.getCell(0)).formatAsString().replaceAll("\"", "");
					}
					
					if (Bkey.equalsIgnoreCase(CD_name)){
						//find out the coulumn
						for (int loopC=1;loopC<CD_usedCol;loopC++){
							//if (CDSheet.getRow(0).getCell(loopC).toString().equalsIgnoreCase(paramname)){
							if(CDSheet.getRow(0).getCell(loopC).toString().isEmpty()){
								Ckey="";
							}else{
								Ckey=fe.evaluate(CDSheet.getRow(0).getCell(loopC)).formatAsString().replaceAll("\"", "");
							}
							
							if ( Ckey.equalsIgnoreCase(paramname)){
								//paramvalue=CDSheet.getRow(loopR).getCell(loopC).toString();
								paramvalue=fe.evaluate(CDSheet.getRow(loopR).getCell(loopC)).formatAsString().replaceAll("\"", "");
								//System.out.println(key + " is(CommonData) " + keyVal);
								//BRIJ_Log.log("{" + paramname + "} (CommonData) Value: " + paramvalue);
								cmnFlag=true;
								break;
							}
						}
						if (!cmnFlag){
							//BRIJ_Log.log(paramname + " is(CommonData) and could not be identified " );
						}
					}
				}
			}else{
				paramvalue=keyVal;
				//BRIJ_Log.log("{"+paramname + "} Value: " + paramvalue);
			}
			
		}else{
			//BRIJ_Log.error("get_TestData param key " + paramname + " is not discovered in data sheet module " + dataSheet.getSheetName());
		}
		return paramvalue;
	}
	
	
	/** BPS_SetTestData
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 03, 2016 
	 * @Discription: BPS_SetTestData, set the testData from the run time and set it in test data sheet
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void BPS_SetTestData(String paramName, String paramVal){
		
		int intindexrow, intparamCol=0;
		boolean paramFlag=false ;
			
		intindexrow = dataSheet.getRow(0).getLastCellNum()-dataSheet.getRow(0).getFirstCellNum();
		
		for (int ic=0; ic<=intindexrow; ic++){
			if (dataSheet.getRow(0).getCell(ic).toString().equalsIgnoreCase(paramName)){
				intparamCol=ic;
				paramFlag=true;
				//BRIJ_Log.log("BPS_SetTestData param key " + paramName + " discovered at col " + intparamCol);
				break;
			}
		}
		
		if (paramFlag){
			dataSheet.getRow(intMasterTDRow).createCell(intparamCol).setCellValue(paramVal.toString());
			//BRIJ_Log.log(paramName + " is set to value " + dataSheet.getRow(intMasterTDRow).getCell(intparamCol).toString());
			BRIJ_Reporter.logINFO("BPS_SetTestData [" + paramName + "]", "Is set to [" + dataSheet.getRow(intMasterTDRow).getCell(intparamCol).toString() + "]");
		}else{
			//BRIJ_Log.log(paramName + " is not found in test data sheet");
		}
	}
	
	public static void launchResult(){
		ResultFolder = BRIJ_Reporter.ResultFolderName;		
		String resultPath = System.getProperty("user.dir")+"\\Results\\Run_"+ResultFolder+"\\Report.html";
		try {	
			File htmlFile = new File(resultPath);
			Desktop.getDesktop().browse(htmlFile.toURI());
			System.out.println("Result Path is ::   " + resultPath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("Unable to launch result, Please open it manually  " + resultPath);
		}
	}

}
