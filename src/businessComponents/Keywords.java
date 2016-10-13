package businessComponents;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import framework_bps.BRIJ_BrowserSetup;
import framework_bps.BRIJ_ExcelUtil;
import framework_bps.BRIJ_Reporter;
import pageobjects.Sweety_LoginPage;



public class Keywords {
	
	//@@ WebDriver 
	static WebDriver driver;
	static WebDriverWait wait;
	
	//@@ Page Class declaration
	static Sweety_LoginPage sweetyLoginPage;
		
	//@@ Constructor to set the page classes constructor and webdriver
	public Keywords(WebDriver ldriver) {
		driver=ldriver;
		sweetyLoginPage= PageFactory.initElements(driver, Sweety_LoginPage.class);
		wait= new WebDriverWait(driver, 20);
	}

	//*****************************************************************************************************
	/**----------------------------------------------------------------------------------------------------
	 *					User Define Methods- Keywords
	 -----------------------------------------------------------------------------------------------------*/

	/** LaunchAndLogin
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @throws AWTException 
	 * @throws  
	 * @Date  : Oct 08, 2016 
	 * @Discription: LaunchAndLogin, Provide username and password to perform login
	 * -----------------------------------------------------------------------------------------------------
	 */
	public void LaunchAndLogin(){
		LaunchUrl();
		UserLogin();	
	}
	
	/** LaunchUrl
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @throws AWTException 
	 * @Date  : Oct 08, 2016 
	 * @Discription: LaunchUrl, Provide url
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void LaunchUrl(){
		String url   	 = BRIJ_ExcelUtil.BPS_GetTestData("App_URL");
		String browser   = BRIJ_ExcelUtil.BPS_GetTestData("Browser_Name");
		driver.navigate().to(url);
		
		if (browser.toString().equalsIgnoreCase("chrome")){
			try {
				Robot robot = new Robot();
				robot.keyPress(KeyEvent.VK_ESCAPE);
				robot.keyRelease(KeyEvent.VK_ESCAPE);
			} catch (AWTException e) {
				e.printStackTrace();
				System.out.println("error with robot class " + e.getMessage());
			}
		}
		
		if (driver.getTitle().toString().length()>1){
			BRIJ_Reporter.logINFO("Launch URL", url + " Launched successfully");
		}else{
			BRIJ_Reporter.logFAIL("Launch Url", url + " , issue with launching the url");
		}	
	}

	/** UserLogin
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 08, 2016 
	 * @Discription: UserLogin, Provide username and password to perform login
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void UserLogin(){
		driver.navigate().refresh();
		String uname   = BRIJ_ExcelUtil.BPS_GetTestData("EmailID");
		String pword   = BRIJ_ExcelUtil.BPS_GetTestData("Password");
		String passFlg = BRIJ_ExcelUtil.BPS_GetTestData("ValidCredentials");
		WebElement emailBox = wait.until(ExpectedConditions.elementToBeClickable(sweetyLoginPage.loginEmail));
		emailBox.clear();
		emailBox.sendKeys(uname);
		
		sweetyLoginPage.loginPassword.clear();
		sweetyLoginPage.loginPassword.sendKeys(pword);	
		sweetyLoginPage.loginPBtn.click();
		
		for (int loopc=0;loopc<20;loopc++){
			try{
				WebElement error=wait.until(ExpectedConditions.elementToBeClickable(sweetyLoginPage.loginError));
				break;
			}catch(StaleElementReferenceException e){
				System.out.println("\n" + e.getLocalizedMessage() + " error is displayed trying again to find object \n");
				putWAIT(200);
			}
		}
		WebElement error=wait.until(ExpectedConditions.elementToBeClickable(sweetyLoginPage.loginError));
		if (error!=null){
			System.out.println(error.getText());
			if(error.getText().equals("Signed in successfully.")){
				if (!passFlg.equalsIgnoreCase("no")){
					BRIJ_Reporter.logPASS("Sweety App Login", "Login Successfull");
				}else{
					BRIJ_Reporter.logFAIL("Sweety App Login", "Login Successfull for Invalid Credentials");
				}
			}else{
				if (!passFlg.equalsIgnoreCase("no")){
					BRIJ_Reporter.logFAIL("Sweety App Login", "Login failed");
				}else{
					BRIJ_Reporter.logPASS("Sweety App Login", "Login failed for Invalid Credentials");
				}	
			}
		}else{
			BRIJ_Reporter.logFAIL("Sweety App Login", "Login failed, Please enter en valid email address with @");
		}
		
	}
	
	//validate recent entries in level page
	/** delete_RecentEntries_LevelPage
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 08, 2016 
	 * @Discription: delete_RecentEntries_LevelPage,details needed
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void delete_RecentEntries_LevelPage(){
		String dateNtime, Value, tmpdate;
		dateNtime	=BRIJ_ExcelUtil.BPS_GetTestData("Delete_DateNTime");
		Value		=BRIJ_ExcelUtil.BPS_GetTestData("Delete_BloodLevel");
		
		if (dateNtime.equalsIgnoreCase("today")){
			DateFormat dateformat = new SimpleDateFormat("MM/dd/yy");
			Date date = new Date();
			tmpdate=dateformat.format(date);
			dateNtime= Integer.toString(Integer.parseInt(tmpdate.split("/")[0]))+"/"+Integer.toString(Integer.parseInt(tmpdate.split("/")[1]))+"/"+tmpdate.split("/")[2];
		}
		
		BRIJ_Reporter.logINFO("Date to delete is ", dateNtime + " , Value " +Value );
		
		//System.out.println("FUNCTION ** delete_RecentEntries_LevelPage \n");
		clickSideBarLink("Levels");
		boolean flgDataFound;
		List<WebElement> headerPanel_db,reportLink;
		headerPanel_db=sweetyLoginPage.panelHeading;
		for(WebElement ele:headerPanel_db){
			if (ele.getText().contains("Recent Entries")){
				reportLink=ele.findElement(By.xpath("..")).findElement(By.xpath("..")).findElements(By.tagName("table"));		
				if (reportLink.size()>0 && reportLink!=null){
					//System.out.println("Recent Entries, Table data is displayed below");
					flgDataFound=deleteLevelEntries(reportLink.get(0),dateNtime,Value);				
					if (Value.equals("") && dateNtime.length()<=8 && flgDataFound){
						delete_RecentEntries_LevelPage();
					}else{
						break;
					}
				}else{
					//System.out.println("Recent Entries {Data not avaialble}");
					BRIJ_Reporter.logFAIL("Recent Entries Data", "Data is NOT Available");
				}
			}			
		}
	}
	

	//call from verify_RecentEntries_LevelPage
	/** deleteLevelEntries
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 08, 2016 
	 * @Discription: deleteLevelEntries,details needed
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static boolean deleteLevelEntries(WebElement levelTbl,String dateNtime, String Level){
		//System.out.println("FUNCTION ** deleteLevelEntries \n");
		boolean deleteRow=false;
		List<WebElement> rows=levelTbl.findElements(By.tagName("tr"));

		if(rows.size()>0){
			for (int crow=1; crow<=rows.size()-1;crow++){
				String rowVal=rows.get(crow).getText();
				//System.out.println("\n check delete check 3 \n" + rowVal);
				if (Level.equals("") && rowVal.contains(dateNtime)){
					//Delete row
					deleteRow=true;
				}else if(rowVal.contains(dateNtime) && rowVal.contains(Level)){
					//Delete row
					deleteRow=true;
				}else{
					//Do not Delete row
				}
			
				if (deleteRow){
					rows.get(crow).findElement(By.linkText("Delete")).click();
					putWAIT(500);
					try{
						if(wait.until(ExpectedConditions.alertIsPresent())==null){
							//System.out.println("\n alert is not present");
							BRIJ_Reporter.logFAIL("Delete Alert Message", "Alert Message is NOT Displayed");
						}else{
							 Alert alert=driver.switchTo().alert();
							 if (alert.getText().equalsIgnoreCase("Are you sure?")){
								 alert.accept();
								 //System.out.println(dateNtime + ", " + Level + " mg/dl: is deleted "  );
								 BRIJ_Reporter.logPASS("Delete Entry for " + dateNtime, Level + ", Entry Deleted");
								 break;
							 }else{
								 //System.out.println("Proper alert message is not displayed");
								 BRIJ_Reporter.logFAIL("Delete Alert Message", "Expected Alert Message is NOT Displayed");
								 alert.accept();
								 break;
							 }
						}
					}catch(NoAlertPresentException e){
						//System.out.println("Alert exception");
						BRIJ_Reporter.logFAIL("Delete Alert Message", "Exception at alert " + e.getMessage());
					}
					
				}
			}
		}else{
			System.out.println("\n rows not found \n");
		}
		return deleteRow;
	}
	
	
	//add new entries
	/** addNewEntry
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 09, 2016 
	 * @Discription: addNewEntry, Provide username and password to perform login
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void addNewEntry(){
		String dataNTime,levelData,check;
		dataNTime	=BRIJ_ExcelUtil.BPS_GetTestData("DateAndTime");
		levelData	=BRIJ_ExcelUtil.BPS_GetTestData("BooldGlucoseData");
		check		=BRIJ_ExcelUtil.BPS_GetTestData("error_BooldGlucoseData");
		
		//System.out.println("FUNCTION ** addNewEntry \n ");
		boolean btFound=false;
		clickSideBarLink("Levels");
		List<WebElement> addNewBtn =sweetyLoginPage.addNewBtn;//driver.findElements(By.linkText("Add new"));
		if (addNewBtn.size()==1){
			addNewBtn.get(0).click();
			putWAIT(1500);
			btFound=true;
			BRIJ_Reporter.logINFO("Add new Entry", "Add New Button is CLICKED");
		}else{
			BRIJ_Reporter.logFAIL("Add new Entry", "Add New Button is NOT FOUND");
		}
		
		
		if (btFound){
			String month,day,year,hour,min,Tformate;
			if (dataNTime.equalsIgnoreCase("today")){
				DateFormat dateformat = new SimpleDateFormat("MM/dd/yy '@' HH:mm a");
				Date date = new Date();
				dataNTime=dateformat.format(date);
			}			
			month	= dataNTime.split(" ")[0].split("/")[0].replaceFirst("^0*", "");
			day		= dataNTime.split(" ")[0].split("/")[1].replaceFirst("^0*", "");
			year	= "20"+dataNTime.split(" ")[0].split("/")[2];
			
			hour 	= dataNTime.split(" ")[2].split(":")[0];
			min 	= dataNTime.split(" ")[2].split(":")[1];
			
			Tformate = dataNTime.split(" ")[3];
			
			if (Tformate.equalsIgnoreCase("PM") && Integer.parseInt(hour)<=12){
				hour= Integer.toString((Integer.parseInt(hour)+12));
			}
			
			boolean err=false;
			
			if (fun_selectAndEnterData("Year",year))	err=true;
			if (fun_selectAndEnterData("Month",month)) 	err=true;
			if (fun_selectAndEnterData("Day",day)) 		err=true;
			if (fun_selectAndEnterData("Hour",hour)) 	err=true;
			if (fun_selectAndEnterData("Min",min)) 		err=true;
			
			//save to data table
			if(Integer.parseInt(hour)>12){
				hour= Integer.toString((Integer.parseInt(hour)-12));
			}else if(hour.equals("00")){
				hour="12";
			}
			BRIJ_ExcelUtil.BPS_SetTestData("DateAndTime", month +"/"+ day +"/"+ year.substring(2)  +" @ "+ Integer.toString(Integer.parseInt(hour)) +":"+ min +" " + Tformate );	
			
			WebElement levelInput = sweetyLoginPage.levelInput;
			levelInput.sendKeys(levelData);
			WebElement submitBtn = sweetyLoginPage.commitBtn;
			submitBtn.click();
			putWAIT(2000);
			if (!err){
				BRIJ_Reporter.logPASS("New Blood Glucose Level Entry", dataNTime + " and " + levelData + " ,Values are Entered" );
			}
			List<WebElement> error=null;
			for(int loopc=0;loopc<20;loopc++){
				try{
					error = sweetyLoginPage.errorMsg;//driver.findElements(By.xpath("//*[@class='alert alert-warning fade in']"));
					break;
				}catch(StaleElementReferenceException e){
					System.out.println("\nStale exception detect  " + e.getLocalizedMessage() + "\n trying again to find object");
					putWAIT(200);
				}	
			}
			
			if (error.size()>0 || error==null){
				if (error.get(0).getText().equalsIgnoreCase("Maximum entries reached for this date.")){
					if (check.equalsIgnoreCase("false")){
						BRIJ_Reporter.logPASS("Validate max 4 Entries for " + dataNTime, "4 Entries are MAx out");
					}else{
						BRIJ_Reporter.logFAIL("Validate Entries for " + dataNTime, "4 Entries are MAx out");
					}
					clickSideBarLink("Levels");
				}
			}else{
				BRIJ_Reporter.logINFO("Error message error", "Error message object not Detected");
			}
			
		}
	}
	
	/** fun_selectAndEnterData
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 09, 2016 
	 * @Discription: fun_selectAndEnterData, 
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static boolean fun_selectAndEnterData(String fieldName, String value){
		String id;
		boolean err=false;
		switch(fieldName) {
			case "Year" 	: id="entry_recorded_at_1i";	break;						 
			case "Month" 	: id="entry_recorded_at_2i";	break;							
			case "Day" 		: id="entry_recorded_at_3i"; 	break;			 
			case "Hour" 	: id="entry_recorded_at_4i"; 	break;
			case "Min" 		: id="entry_recorded_at_5i"; 	break;
			default			: id="NILL";					break;				
		}
		
		if (!id.equals("NILL")){
			WebElement ele = wait.until(ExpectedConditions.elementToBeClickable(By.id(id)));
			Select select = new Select(ele);
			select.selectByValue(value);
		}else{
			err=true;
		}
		
		if (!err){
			//BRIJ_Reporter.logPASS(fieldName + " Value Entered " , "as [" + value + "]" );
		}else{
			BRIJ_Reporter.logFAIL("Values is not Entered", fieldName + " , and " + value );
		}	
		return err;
	}

	//click on sidebar link
	public static void clickSideBarLink(String linkName){
		WebElement sideLink = driver.findElement(By.linkText(linkName));
		sideLink.click();
		BRIJ_Reporter.logINFO("Sidebar Item Selection", "    ["+linkName + "] is CLICKED");
		//System.out.println("SideBar link [" + linkName + "] is clicked");
		putWAIT(2000);
	}
	
	//VALIDAT Recent entries
	/** validateRecentEntries
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 03, 2016 
	 * @Discription: validateRecentEntries, this method is under development and need to be evolve
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void validateRecentEntries(){
		clickSideBarLink("Levels");
		
		String dateNtime,Level;
		dateNtime	=BRIJ_ExcelUtil.BPS_GetTestData("DateAndTime");
		Level		=BRIJ_ExcelUtil.BPS_GetTestData("BooldGlucoseData");
		if (Level.indexOf(".")>0){
			Level=Level.substring(0, Level.indexOf("."));	
		}
		
		List<WebElement> headerPanel_db=null;
		
		//System.out.println(" check ppoint 1");
		for (int loop=0;loop<20;loop++){
			try{
				headerPanel_db=sweetyLoginPage.panelHeading;//driver.findElements(By.tagName("h3"));
				break;
			}catch(StaleElementReferenceException e){
				//System.out.println("\n stale exception detected " + e.getLocalizedMessage() + "\n trying again \n");
				putWAIT(200);
			}
		}
		//System.out.println(" check ppoint 2");
		List<WebElement> ReportsTbl=null;
		if (headerPanel_db!=null && headerPanel_db.size()>0){
			for(WebElement ele:headerPanel_db){
				if (ele.getText().equalsIgnoreCase("Recent Entries")){
					for (int loop=0;loop<20;loop++){
						try{
							ReportsTbl=ele.findElement(By.xpath("..")).findElement(By.xpath("..")).findElements(By.tagName("table"));
							break;
						}catch(StaleElementReferenceException e){
							//System.out.println("\n stale exception detected " + e.getLocalizedMessage() + "\n trying again \n");
							putWAIT(200);
						}
					}
					if (ReportsTbl.size()>0 && ReportsTbl!=null){
						validateRecentEntriesData(ReportsTbl.get(0),dateNtime,Level);
					}else{
						//System.out.println("Recent Entries data is not available");
						BRIJ_Reporter.logFAIL("Recent Entries Table", "Table Not FOUND");
					}	
					break;
				}			
			}
		}
		
	}
	

	//call from validateRecentEntries
	/** validateRecentEntriesData
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 03, 2016 
	 * @Discription: validateRecentEntriesData, this method is under development and need to be evolve
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void validateRecentEntriesData(WebElement recentTable, String dateNtime, String Level){
		String rowVal="";
		boolean flgFound=false;
		
		List<WebElement> rows=recentTable.findElements(By.tagName("tr"));
		for (int crow=0; crow<=rows.size()-1;crow++){
			rowVal = rows.get(crow).getText();
			if (rowVal.contains(dateNtime + " " + Level)){
				//System.out.println("Validated: " + dateNtime + " " + Level);
				BRIJ_Reporter.logPASS("Recent Entry Validated: " + dateNtime,  " Level Value " + Level);
				flgFound=true;
				break;
			}	
		}
		if (!flgFound){
			//System.out.println("Not Validated: Expected" + dateNtime + " " + Level + " , Actual: " + rowVal);
			BRIJ_Reporter.logFAIL("Recent Entry Not Validated: Expected " + dateNtime + " " + Level , "Please Investigate");
		}
	}
	

	//GENERIC function to VALIDATE the report data
	/** validateReport
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 10, 2016 
	 * @Discription: validateReport, this method is under development and need to be evolve
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void validateReport(){
		String reportName,dateToVal,entriesTime,bgLevel, minToVal, maxToVal, avgToVal;
		List<WebElement> headerPanel_db=null,reportLink;	
		boolean flagReportFound=false;
		
		clickSideBarLink("Reports");
		
		//validation data, date, min, max and average
		reportName	=BRIJ_ExcelUtil.BPS_GetTestData("ReportName");
		dateToVal	=BRIJ_ExcelUtil.BPS_GetTestData("ValReportDate");
		entriesTime =BRIJ_ExcelUtil.BPS_GetTestData("ValEntries_Time");
		bgLevel		=BRIJ_ExcelUtil.BPS_GetTestData("Val_Level");
		minToVal	=BRIJ_ExcelUtil.BPS_GetTestData("ValMinVal");
		maxToVal	=BRIJ_ExcelUtil.BPS_GetTestData("ValMaxVal");
		avgToVal	=BRIJ_ExcelUtil.BPS_GetTestData("ValAvgVal");
			
		if (bgLevel.indexOf(".")>0) 	bgLevel=bgLevel.substring(0, bgLevel.indexOf("."));	
		if (minToVal.indexOf(".")>0)	minToVal=minToVal.substring(0, minToVal.indexOf("."));	
		if (maxToVal.indexOf(".")>0)	maxToVal=maxToVal.substring(0, maxToVal.indexOf("."));	
		if (avgToVal.indexOf(".")>0)	avgToVal=avgToVal.substring(0, avgToVal.indexOf("."));	
		
		DateFormat dateformat = new SimpleDateFormat("yyyy-MM-dd");
		DateFormat dateformatMonth = new SimpleDateFormat("yyyy-MMM");
		Date date = new Date();
				
		if (reportName.isEmpty() || reportName.toString().equalsIgnoreCase("Default")){
			reportName="Daily Report as of " + dateformat.format(date);
			BRIJ_Reporter.logINFO("Report Validation", "Report Page default report will be validated, Roport Name is [" + reportName + "]");
		}else if(reportName.toString().equalsIgnoreCase("Month to Date")){
			clickReportType(reportName);
		}else{
			clickReportType(reportName.split(" ")[0]);
		}
		
		if (dateToVal.equalsIgnoreCase("today") && reportName.split(" ")[0].toString().equalsIgnoreCase("Daily")){
			dateToVal= dateformat.format(date);
		}
		if (dateToVal.equalsIgnoreCase("today") && reportName.split(" ")[0].toString().equalsIgnoreCase("Monthly")){
			dateToVal= dateformatMonth.format(date);
		}
		
		boolean pnlFound=false;
		for(int loop=0;loop<20;loop++){
			try{
				headerPanel_db =sweetyLoginPage.panelHeading;//driver.findElements(By.tagName("h3"));
				pnlFound=true;
				break;
			}catch(StaleElementReferenceException e){
				System.out.println("\nStale exception error " + e.getLocalizedMessage() + "\n trying again\n");
				putWAIT(200);
			}
		}
		
		if (pnlFound && headerPanel_db!=null){
			for(WebElement ele:headerPanel_db){
				if (ele.getText().contains(reportName)){
					//Validate report based on Report Name
					reportLink=ele.findElement(By.xpath("..")).findElement(By.xpath("..")).findElements(By.tagName("table"));
					if (reportLink.size()>0){
						//System.out.println(ele.getText() + ", Table data is displayed below");
						validateReportData(reportLink.get(0),dateToVal,entriesTime,bgLevel,minToVal,maxToVal,avgToVal);
					}else{
						//System.out.println(ele.getText() + " {Data not avaialble}");
						BRIJ_Reporter.logFATAL(ele.getText() + " Verification", "Data is not Available");;
					}
					flagReportFound=true;
					break;
				}			
			}
		}
		
		if(!flagReportFound){
			BRIJ_Reporter.logFATAL(reportName, "NOT FOUND");
		}
	}
	
	//call from validateReport
	/** validateReportData
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 10, 2016 
	 * @Discription: validateReportData, this method is under development and need to be evolve
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void validateReportData(WebElement reportTable,String dateToVal,String entriesTime,String bgLevel,String minToVal,String maxToVal,String avgToVal){
		List<WebElement> rows=reportTable.findElements(By.tagName("tr"));
		int dateIndex=-1, entrisIndex=-1, levelIndex=-1, minIndex=-1, maxIndex=-1, avgIndex=-1 ;
		String columnName="";
		boolean dateFlg=false, entryflg=false,bglevelFlg=false, minFlg=false, maxFlg=false, avgFlg=false;
		
		for (int crow=0; crow<=rows.size()-1;crow++){
			List<WebElement> th = rows.get(crow).findElements(By.tagName("th"));
			if(th.size()>0){
				for(int hcount=0; hcount<= th.size()-1;hcount++){
					//System.out.println(th.get(hcount).getText());
					columnName=th.get(hcount).getText().toString();
					switch (columnName){
						case "Date": 	dateIndex=	hcount;
							break;
						case "Entries": entrisIndex=hcount;
							break;
						case "Level": 	levelIndex=	hcount;
							break;
						case "Min": 	minIndex=	hcount;
							break;
						case "Max": 	maxIndex=	hcount;
							break;
						case "Avg": 	avgIndex=	hcount;
							break;
						default: BRIJ_Reporter.logFATAL("Report Data Validate", "Table column does not match with either of theese Date,Entries,Level,Min,Max,Avg, but it is [" + columnName +"]" );
							break;
					}	
				}
			}
			
			if (!(crow==0)){
				List<WebElement> td = rows.get(crow).findElements(By.tagName("td"));
				//System.out.println(td.size());
				if (!td.get(dateIndex).getText().toString().isEmpty() && dateFlg){
					break;
				}
				
				if (td.get(dateIndex).getText().toString().equalsIgnoreCase(dateToVal)){
					BRIJ_Reporter.logINFO("Report Validation Date", dateToVal);
					dateFlg=true;
				}
				if (td.get(entrisIndex).getText().toString().equalsIgnoreCase(entriesTime)){
					//BRIJ_Reporter.logINFO("Report Validation Entries", entriesTime);
					entryflg=true;
				}
				if (td.get(levelIndex).getText().toString().equalsIgnoreCase(bgLevel) && !bgLevel.isEmpty()){
					//BRIJ_Reporter.logINFO("Report Validation Level", bgLevel);
					bglevelFlg=true;
				}
				if (td.get(minIndex).getText().toString().equalsIgnoreCase(minToVal) && !minToVal.isEmpty()){
					BRIJ_Reporter.logINFO("Report Validation Min", minToVal);
					minFlg=true;
				}
				if (td.get(maxIndex).getText().toString().equalsIgnoreCase(maxToVal) && !maxToVal.isEmpty()){
					BRIJ_Reporter.logINFO("Report Validation Max", maxToVal);
					maxFlg=true;
				}
				if (td.get(avgIndex).getText().toString().equalsIgnoreCase(avgToVal) && !avgToVal.isEmpty()){
					BRIJ_Reporter.logINFO("Report Validation Avg", avgToVal);
					avgFlg=true;
				}
			}
			if (entryflg && bglevelFlg) BRIJ_Reporter.logINFO("Report Validation Entry "+entriesTime , "Level "+bgLevel );
			
			if (entryflg && bglevelFlg && dateFlg) break;
		}
		
		if(minToVal.isEmpty() && maxToVal.isEmpty() && avgToVal.isEmpty()){
			minFlg=true;maxFlg=true;avgFlg=true;
		}
		
		if (dateFlg && entryflg && bglevelFlg && minFlg && maxFlg && avgFlg){
			BRIJ_Reporter.logPASS("Report Validation Passed", "All Values Matched");
		}else{
			BRIJ_Reporter.logFAIL("Report Validation FAILED", "Some values Does not match");
		}

	}
	
	//GENERIC function to click report type in Level Reports Page
	/** clickReportType
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 10, 2016 
	 * @Discription: clickReportType, this method is under development and need to be evolve
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void clickReportType(String reportType){
		boolean flg=false, reportPresent=false, pnlheadFlg=false;
		List<WebElement> headerPanel_report=null,ReportTypeList;
		//clickReportLink("view");
		
		for(int loop=0;loop<20;loop++){
			try{
				headerPanel_report =sweetyLoginPage.panelHeading;//driver.findElements(By.tagName("h3"));
				pnlheadFlg=true;
				break;
			}catch(StaleElementReferenceException e){
				System.out.println("\n Stale exception detected " + e.getLocalizedMessage() +"\n trying again");
				putWAIT(200);
			}
		}
		if (pnlheadFlg && headerPanel_report!=null){
			for(WebElement ele:headerPanel_report){
				if(ele.getText().equalsIgnoreCase("Reports")){
					ReportTypeList =ele.findElement(By.xpath("..")).findElements(By.tagName("a"));
					//3 Click Daily/Monthly Report
					for (WebElement list : ReportTypeList){					
						if(list.getText().equalsIgnoreCase(reportType)){
							list.click();
							//System.out.println(reportType + " is CLICKED");
							BRIJ_Reporter.logINFO("Open Report "+ reportType, " Report is open");
							putWAIT(2000);
							flg=true;
							break;
						}
					}
				}
				if (flg){
					break;
				}
			}
		}
		if(!flg){
			BRIJ_Reporter.logFAIL("Open Report " + reportType, " Report is Not Found");
		}
	}
	
	
	//Click Report link
	/** clickReportLink
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : Oct 10, 2016 
	 * @Discription: clickReportLink, this method is under development and need to be evolve
	 * -----------------------------------------------------------------------------------------------------
	 */
	public static void clickReportLink(String reportLinkName){
		boolean flg=false,pnlheadFlg=false;
		List<WebElement> headerPanel_db=null ,reportLink;
		for(int loop=0;loop<20;loop++){
			try{
				headerPanel_db =sweetyLoginPage.panelHeading;//driver.findElements(By.tagName("h3"));
				pnlheadFlg=true;
				break;
			}catch(StaleElementReferenceException e){
				System.out.println("\n Stale exception detected " + e.getLocalizedMessage() +"\n trying again");
				putWAIT(200);
			}
		}
		
		if (pnlheadFlg && headerPanel_db!=null){
			for(WebElement ele:headerPanel_db){
				if (ele.getText().equalsIgnoreCase("Reports")){
					reportLink=ele.findElement(By.xpath("..")).findElement(By.xpath("..")).findElements(By.tagName("a"));
					for(WebElement link: reportLink){
						if (link.getText().equalsIgnoreCase(reportLinkName)){
							link.click();
							//System.out.println(reportLinkName + " LINK clicked on Welcome to sweety page");
							BRIJ_Reporter.logINFO("click on view Report link", "LINK clicked on Welcome to sweety page");
							putWAIT(3000);
							flg=true;
							break;
						}
					}
				}
				if (flg){
					break;
				}
			}
		}
		
		if(!flg){
			BRIJ_Reporter.logFATAL("click on view Report link", "LINK is not clicked on Welcome to sweety page");
		}
	}
		
	//*****************************************************************************************************
	/**----------------------------------------------------------------------------------------------------
	 *					Generic functions
	 -----------------------------------------------------------------------------------------------------*/
	
	/** Validate
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 03, 2016 
	 * @Discription: Validate, this method is under development and need to be evolve
	 * -----------------------------------------------------------------------------------------------------
	 */
	public void Validate(String actual, String compareWith){
		if (actual.equalsIgnoreCase(compareWith)){
			BRIJ_Reporter.logPASS(actual + " is compared with :", compareWith);
		}else{		
			BRIJ_Reporter.logFAIL( actual + " is compared with :" ,compareWith );
		}
	}
	
	public static WebDriver getdriver(){
		return driver;
	}
	

	/**3. appNavigateBack
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 03, 2016 
	 * @Discription: appNavigateBack, print news headers from specific class to logFile
	 * -----------------------------------------------------------------------------------------------------
	 */
	public void appNavigateBack(){
		driver.navigate().back();
		BRIJ_Reporter.logPASS("Open Previous Page", "Done");
	}
	
	/**4. QuitApp
	 * ----------------------------------------------------------------------------------------------------
	 * @author: Brijendra Singh
	 * @Date  : May 03, 2016 
	 * @Discription: QuitApp, close the browser
	 * -----------------------------------------------------------------------------------------------------
	 */
	public void QuitApp(){
		try{
			BRIJ_BrowserSetup.getMainDriver().close();
			BRIJ_BrowserSetup.getMainDriver().quit();
			BRIJ_Reporter.logINFO("Close Browser" , "Done");
			driver = null;
		}catch (Throwable e){
			BRIJ_Reporter.logERROR("Close Browser error", e.getMessage());
		}
	}
	
	public static void putWAIT(int wait){
		try {
			Thread.sleep(wait);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println("\n wait not inserted \n");
		}
	}
}
