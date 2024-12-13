package utils;

import com.google.common.base.Strings;


import data.InternalPropertiesLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;

public class Browser {
	
	static final Logger logger = LogManager.getLogger(Browser.class);

	private static WebDriver driver;

	private static WebPlatform platformName;
	
	public static void SetDriver(WebDriver webDriver) {
		close();
		driver = webDriver;
		driver.manage().window().maximize();
	}

	public static WebDriver getDriver() {
		return driver;
	}
	
	public static void close() {
		String webdriverName = Strings.isNullOrEmpty(System.getProperty("web.drivers.set")) ? new InternalPropertiesLoader("configuration_core.properties").getValue("web.drivers.set") : System.getProperty("web.drivers.set");
		if(driver != null) {
			driver.quit();
			driver = null;
		}
	}
	
	public static void openUrl(String url) {
		driver.get(url);
	}
	
	public static void switchToTab(int tabIdentifier) {
		ArrayList<String> tabsList = new ArrayList<String>(driver.getWindowHandles());
		try {
			driver.switchTo().window(tabsList.get(tabIdentifier - 1));
		}catch(NoSuchWindowException e) {
			logger.error("Erro ao tentar trocar para a aba " + tabIdentifier);
		}
	}
	
	public static byte[] getScreenshot() {
		TakesScreenshot takesScreenshot = ((TakesScreenshot) driver);
		return takesScreenshot.getScreenshotAs(OutputType.BYTES);
	}

	public static void setPlatformName(WebPlatform platformName) {
		Browser.platformName = platformName;
	}

	public static WebPlatform getPlatformName() {
		return Browser.platformName;
	}

	public enum WebPlatform {
		CHROME,
		EDGE,
		SAFARI
	}
}
