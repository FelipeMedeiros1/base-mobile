package hooks;

import data.InternalPropertiesLoader;
import mobile.AppiumServer;
import mobile.Mobile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import utils.TestInfoGeneral;

public class Hooks {

    static final Logger logger = LogManager.getLogger(Hooks.class);
    private static boolean isTestPlanBeginning = true;
    private static String isAppiumServer = new InternalPropertiesLoader("configuration_core.properties")
            .getValue("mobile.appium.server");
    private static String versaoFramework = new InternalPropertiesLoader("configuration_core.properties")
            .getValue("project.version");

    private static InternalPropertiesLoader pLoader = new InternalPropertiesLoader("configuration_core.properties");

    private static String versaoSistema = "";


    @Before(order = 01, value = "@Mobile")
    public void beforeMobile(Scenario scenario) {
        logger.info("Iniciando Objetos para Mobile");
        TestInfoGeneral.setEnviroment("Mobile");
        TestInfoGeneral.setPlatformNameByTag(scenario);
        if (Boolean.parseBoolean(isAppiumServer))
            AppiumServer.start();

        Mobile.getDriver();
    }

    @After(order = 0, value = "@Mobile")
    public void afterMobile(Scenario scenario) {
        String sistema = pLoader.getValue("project.system");
        if (sistema.toLowerCase().contains("mobile")) {
            String versaoAutomacao = pLoader.getValue("project.system.version");
        }
        logger.info("Finalizando Objetos para Mobile");
        Mobile.closeDriver();
    }
}
