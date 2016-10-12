/**
 * 
 */
package pageobjects;

import java.util.List;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.CacheLookup;
import org.openqa.selenium.support.FindBy;

/**
 * @author Brijendra Singh
 *
 */
public class Sweety_LoginPage {
	WebDriver driver;
	
	//@@ Constructor to set the Webdriver
	public Sweety_LoginPage(WebDriver ldriver) {
		driver = ldriver;
	}
	
	@FindBy(id="user_email")
	public WebElement loginEmail;
	
	@FindBy(id="user_password")
	public WebElement loginPassword;
	
	@FindBy(xpath="//*[@class='actions']/input")
	public WebElement loginPBtn;
	
	@FindBy(xpath="//*[@id='page-content-wrapper']/div/div[1]")
	public WebElement loginError;
	
	@FindBy(tagName="h3")
	public List<WebElement> panelHeading;
	
	@FindBy(linkText="Add new")
	public List<WebElement> addNewBtn;
	
	@FindBy(id="entry_level")
	public WebElement levelInput;
	
	@FindBy(name="commit")
	public WebElement commitBtn;
	
	@FindBy(xpath="//*[@class='alert alert-warning fade in']")
	public List<WebElement> errorMsg;
}
