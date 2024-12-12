package mobile;

import com.google.common.base.Strings;

import data.InternalPropertiesLoader;
import exceptions.AutomationException;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import io.appium.java_client.service.local.flags.GeneralServerFlag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

public class AppiumServer {

    static final Logger logger = LogManager.getLogger(AppiumServer.class);
    private static InternalPropertiesLoader pLoader = new InternalPropertiesLoader("configuration_core.properties");

    private static String isConsoleLog = pLoader.getValue("mobile.appium.server.consolelog");

    static AppiumDriverLocalService service;
    static String host = pLoader.getValue("mobile.appium.server.host");
    static int port = Integer.parseInt(pLoader.getValue("mobile.appium.server.port"));

    private static final String appiumUrl = new InternalPropertiesLoader("configuration_core.properties").getValue("mobile.appium.url");

    static AppiumDriverLocalService getInstance() {
        if (service == null) {
            setInstance();
            if (!Boolean.parseBoolean(isConsoleLog)) service.clearOutPutStreams();
        }
        return service;
    }

    public static void start() {
        logger.info("Iniciando o Appium Server " + String.format("(%s:%s)", host, port));
        if (isPortAvailable(port)) {
            validateEnvironment();
            getInstance().start();
        } else {
            logger.warn("Foi identificado que já existe uma execução do Appium no seguinte endereço: " + String.format("%s:%s", host, port));
        }
    }

    private static void validateEnvironment() {
        String nullVariables = "";

        if (Strings.isNullOrEmpty(System.getenv("APPIUMJS_PATH")))
            nullVariables = nullVariables.concat("{APPIUMJS_PATH} ");

        if (Strings.isNullOrEmpty(System.getenv("NODE_PATH"))) nullVariables = nullVariables.concat("{NODE_PATH}");

        if (!Strings.isNullOrEmpty(nullVariables))
            throw new AutomationException("Favor definir a(s) variável(eis) de ambiente '%s'. Consulte a documentação do Framework.", nullVariables);
    }

    static void setInstance() {
        String nodeFile = System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "\\node.exe" : "/node";
        String nodeJsFile = System.getProperty("os.name").toUpperCase().contains("WINDOWS") ? "\\main.js" : "/main.js";

        AppiumServiceBuilder builder = new AppiumServiceBuilder();
        builder.withAppiumJS(new File(System.getenv("APPIUMJS_PATH").concat(nodeJsFile))).usingDriverExecutable(new File(System.getenv("NODE_PATH").concat(nodeFile))).usingPort(port).withIPAddress(host).withArgument(GeneralServerFlag.LOCAL_TIMEZONE).withLogFile(new File("appiumLog.txt"));

        if (appiumUrl.contains("/wd/hub")) builder.withArgument(GeneralServerFlag.BASEPATH, "/wd/hub");

        service = AppiumDriverLocalService.buildService(builder);
    }

    private static boolean isPortAvailable(int port) {
        try (ServerSocket serverSocket = new ServerSocket()) {
            serverSocket.setReuseAddress(false);
            serverSocket.bind(new InetSocketAddress(InetAddress.getByName(host), port), 1);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void stop() {
        if (service != null) {
            service.stop();
            logger.info("Appium Server finalizado!");
        }
    }

}
