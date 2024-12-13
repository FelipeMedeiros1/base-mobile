package utils;

import data.InternalPropertiesLoader;
import exceptions.AutomationException;
import io.cucumber.java.Scenario;
import io.cucumber.java.Status;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import mobile.Mobile;
import mobile.MobilePlatform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.common.base.Strings;


import io.cucumber.junit.CucumberOptions;

public final class TestInfoGeneral {

	static final Logger logger = LogManager.getLogger(TestInfoGeneral.class);
	private static ArrayList<Boolean> totalStatus = new ArrayList<Boolean>();
	private static LocalDateTime startTest;
	private static String environ;
	private static InternalPropertiesLoader pLoader = new InternalPropertiesLoader("configuration_core.properties");
	private static String urlSistema;
	private static String versaoSistema;

	public static List<Boolean> getTotalStatus() {
		return totalStatus;
	}

	public static void addStatus(Status status) {
		TestInfoGeneral.totalStatus.add(status.equals(Status.PASSED));
	}

	public static void updateStatusToSkipped(Status status) {
		TestInfoGeneral.totalStatus.add(status.equals(Status.SKIPPED));
	}

	public static Integer getTotalScenarios() {
		return Integer.valueOf(TestInfoGeneral.totalStatus.size());
	}

	public static Integer getTotalPassed() {
		Integer cont = 0;
		for (Boolean status : totalStatus) {
			if (status)
				cont += 1;
		}

		return cont;
	}

	public static Integer getTotalFailed() {
		Integer cont = 0;
		for (Boolean status : totalStatus) {
			if (!status)
				cont += 1;
		}

		return cont;
	}

	public static void startTest() {
		startTest = LocalDateTime.now();
	}

	public static Integer getDuration() {
		LocalDateTime endTest = LocalDateTime.now();
		Duration duration = Duration.between(startTest, endTest);

		return Integer.valueOf(String.valueOf(duration.toMillis()));
	}

	public static String formattedTestDurtation(int duration) {
		duration = duration/1000;
		int days = duration / 86400;
		int hours = (duration % 86400) / 3600;
		int minutes = ((duration % 86400) % 3600) / 60;
		int seconds = ((duration % 86400) % 3600) % 60;

		String formattedTestDurtation = hours + ":" + minutes + ":" + seconds + "h";
		return formattedTestDurtation;
	}

	public static void setTagsFromRunTest(Collection<String> scenarioTags) {
		logger.info("Tags from RunTest", scenarioTags);
	}

	public static void setEnviroment(String enviroment) {
		environ = enviroment;
	}

	public static String getEnviroment() {
		return environ;
	}

	public static String getAmbienteURL() {
		return pLoader.getValue("web.url");
	}
	
	public static String getURLSistema() {
		return urlSistema;
	}
	
	public static void setURLSistema(String url) {
		urlSistema = url;
	}
	
	public static String getVersaoSistema() {
		return versaoSistema;
	}
	
	public static void setVersaoSistema(String versao) {
		versaoSistema = versao;
	}
	
	public static void setPlatformNameByTag(Scenario scenario) {

		if (Mobile.getPlatformName() != null) {
			return;
		}

		if (verifyAndroidOrIOSTag(List.copyOf(scenario.getSourceTagNames()), "Cenario")) {
			return;
		}

		String systemProperty = System.getProperty("cucumber.filter.tags");
		systemProperty = Strings.isNullOrEmpty(systemProperty) ? new String() : systemProperty;
		if (verifyAndroidOrIOSTag(Arrays.asList(systemProperty.split(" ")), "cucumber.filter.tags")) {
			return;
		}

		Class<?> clazz = Reflections.findClassByName("RunTest");
		if (clazz.isAnnotationPresent(CucumberOptions.class)) {
			CucumberOptions cucumberOptions = clazz.getAnnotation(CucumberOptions.class);
			if (verifyAndroidOrIOSTag(Arrays.asList(cucumberOptions.tags().split(" ")), "RunTest")) {
				return;
			}
		}

		throw new AutomationException(
				"Verifique as Tags @Android e @IOS elas podem estar ausentes, ou se estiverem simultaneamente no cenario é necessario informar a Tag referente a plataforma Mobile. Verifique a documentação do NaaS");

	}

	public static void setWebPlatformNameByProperties() {
		if (Browser.getPlatformName() == null) {

			String platformName = pLoader.getValue("web.drivers.set");

			if (platformName.equalsIgnoreCase("Chrome")) {
				Browser.setPlatformName(Browser.WebPlatform.CHROME);
				return;
			}

			if (platformName.equalsIgnoreCase("Edge")) {
				Browser.setPlatformName(Browser.WebPlatform.EDGE);
				return;
			}

			if (platformName.equalsIgnoreCase("Safari")) {
				Browser.setPlatformName(Browser.WebPlatform.SAFARI);
				return;
			}

			throw new AutomationException(
					String.format("Plataforma web %s não é suportada. Verifique a documentação do NaaS", platformName));
		}
	}

	private static boolean verifyAndroidOrIOSTag(List<String> tags, String origin) {
		List<String> platforms = filterAndroidOrIOSTag(tags);
		if (platforms.size() != 1) {
			return false;
		}

		logger.info(String.format("Localizado a Tag %s para a Plataforma Mobile em %s", platforms.get(0), origin));

		Mobile.setPlatformName(
				platforms.get(0).equalsIgnoreCase("android") ? MobilePlatform.ANDROID : MobilePlatform.IOS);
		return true;

	}

	private static List<String> filterAndroidOrIOSTag(List<String> tags) {
		List<String> platforms = new ArrayList<>();
		for (String tag : tags) {
			tag = tag.replace("@", "");
			if (tag.equalsIgnoreCase("android") || tag.equalsIgnoreCase("ios"))
				platforms.add(tag);
		}
		return platforms;
	}

}
