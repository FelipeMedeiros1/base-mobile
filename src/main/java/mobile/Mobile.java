package mobile;

import com.google.common.base.Strings;


import data.InternalPropertiesLoader;
import exceptions.AutomationException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.InteractsWithApps;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.NoSuchContextException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.net.URL;
import java.time.Duration;

/**
 * Esta classe tem o objetivo centralizar as variáveis
 * de controle de driver Mobile Appium, e realizar a manutenção
 * das mesmas.
 */
public class Mobile {

    static final Logger logger = LogManager.getLogger(Mobile.class);

    private static AppiumDriver appiumDriver;
    private static DeviceFarm deviceFarm;
    private static DesiredCapabilities cap;
    private static String appiumUrl = new InternalPropertiesLoader("configuration_core.properties").getValue("mobile.appium.url");
    private static Boolean isDeviceFarmActive = Boolean.valueOf(new InternalPropertiesLoader("configuration_core.properties").getValue("mobile.devicefarm.android.isActive"));
    private static MobilePlatform platformName;

    /**
     * Retorna o Driver Appium já iniciado e conectado com o device.
     *
     * @return appiumDriver
     */
    public static AppiumDriver getDriver() {
        if (appiumDriver == null) {
            return createDriver();
        }
        appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        return appiumDriver;
    }

    /**
     * Metodo privado para construição do objeto appiumDriver, e execução da
     * conexão com o device
     *
     * @return appiumDriver
     */
    private static AppiumDriver createDriver() {
        try {
            if (cap == null) {
                cap = setCapability();
            }

            logger.info("Iniciando processo de instancia do Appium Driver para a plataforma {}", Mobile.platformName.toString());

            switch (Mobile.platformName) {
                case ANDROID:
                    appiumDriver = new AndroidDriver(new URL(appiumUrl), cap);
                    break;
                case IOS:
                    appiumDriver = new IOSDriver(new URL(appiumUrl), cap);
                    break;
            }

            logger.info("Appium Driver instanciado com sucesso.");

        } catch (Exception e) {
            throw new AutomationException("Falha ao instanciar o Appium Driver = [%s]", e.getMessage());
        }
        appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
        return appiumDriver;
    }

    /**
     * Método utilizado para abrir um novo aplicativo no device
     *
     * @param cap - DesiredCapabilities do aplicativo que sera aberto
     */
    public static void abrirNovoApp(DesiredCapabilities cap) {
        try {
            logger.info("Iniciando nova aplicação.");

            switch (Mobile.platformName) {
                case ANDROID:
                    appiumDriver = new AndroidDriver(new URL(appiumUrl), cap);
                    break;
                case IOS:
                    appiumDriver = new IOSDriver(new URL(appiumUrl), cap);
                    break;
            }
            logger.info("Aplicação iniciada com sucesso.");

        } catch (Exception e) {
            throw new AutomationException("Falha ao iniciar nova aplicação = [%s]", e.getMessage());
        }
        appiumDriver.manage().timeouts().implicitlyWait(Duration.ofSeconds(1));
    }

    /**
     * Responsável por limpar o objeto appium driver e desconectar do device
     */
    public static void closeDriver() {
        if (appiumDriver != null) {
            ((InteractsWithApps) appiumDriver).terminateApp(getAppPackageOrBundleId());
            appiumDriver.quit();
            appiumDriver = null;
            cap = null;
            logger.info("Appium Driver finalizado!");
        }
    }

    /**
     * Responsável por retornar uma execução de elementos em JavaScript do appiumDriver
     *
     * @return (JavascriptExecutor) appiumDriver
     */
    public static JavascriptExecutor getJsExecutor() {
        return (JavascriptExecutor) appiumDriver;
    }

    /**
     * Para reiniciar a aplicação deve-se utilizar este metodo para que não se perca a
     * conexão entre o device e o Appium Driver
     *
     * @throws InterruptedException Exceção lançada se o driver não conseguir fechar e abrir o app
     */
    public static void restartApplication() throws InterruptedException {
        logger.info("Reiniciando aplicativo");
        String appPackageOrBundleId = getAppPackageOrBundleId();
        ((InteractsWithApps) appiumDriver).terminateApp(appPackageOrBundleId);
        ((InteractsWithApps) appiumDriver).activateApp(appPackageOrBundleId);
    }

    /**
     * Responsável por desconectar um dispositivo do Device Farm
     */
    public static void disconnectDevice() {
        if ((isDeviceFarmActive || isJenkinsEnvironment()) && deviceFarm != null && deviceFarm.getDevice() != null) {
            deviceFarm.disconnect();
        }
    }

    /**
     * Método responsável por iniciar a construção das capabilities
     *
     * @return DesiredCapabilities
     */
    private static DesiredCapabilities setCapability() {
        if ((isDeviceFarmActive || isJenkinsEnvironment()) && Mobile.getPlatformName() == MobilePlatform.ANDROID)
            setCapabilityDeviceFarm();
        logger.info("DeviceFarm = (mobile.devicefarm.android.isActive = {})", isDeviceFarmActive);
        return getCapability();
    }

    /**
     * Metodo interno reponsável por setar os capabilities para a conexão com o device do Device Farm.
     */
    private static void setCapabilityDeviceFarm() {
        deviceFarm = DeviceFarm.getInstance();
        Device device = deviceFarm.getDevice();
        MobileCapabilities mobileCapabilities = MobileCapabilities.getInstance();
        mobileCapabilities.setPlatformName(device.getPlatform());
        mobileCapabilities.setDeviceName(device.getRemoteConnectUrl());
        mobileCapabilities.setPlatformVersion(device.getPlatformVersion());
    }

    /**
     * Metodo interno reponsável por construir os capabilities para a conexão com o device.
     *
     * @return DesiredCapabilities
     */
    private static DesiredCapabilities getCapability() {
        MobileCapabilities mobileCapabilities = MobileCapabilities.getInstance();
        DesiredCapabilities cap = new DesiredCapabilities();
        cap.setCapability("platformName", mobileCapabilities.getPlatformName());
        cap.setCapability("deviceName", mobileCapabilities.getDeviceName());
        cap.setCapability("appium:version", mobileCapabilities.getPlatformVersion());
        cap.setCapability("automationName", mobileCapabilities.getAutomationName());
        cap.setCapability("fullReset", mobileCapabilities.getFullReset());
        cap.setCapability("newCommandTimeout", mobileCapabilities.getNewCommandTimeout());
        new AppInstall().setAppCapability(cap);

        switch (Mobile.platformName) {
            case ANDROID:
                cap.setCapability("noReset", mobileCapabilities.getNoReset());
                cap.setCapability("appium:appPackage", mobileCapabilities.getAppPackage());
                cap.setCapability("appium:appActivity", mobileCapabilities.getAppActivity());
                cap.setCapability("appium:unicodeKeyboard", mobileCapabilities.getUnicodeKeyboard());
                cap.setCapability("appium:resetKeyboard", mobileCapabilities.getResetKeyboard());
                cap.setCapability("appium.autoGrantPermissions", mobileCapabilities.getAutoGrantPermissions());
                cap.setCapability("appium:uiautomator2ServerInstallTimeout", 60000);
                break;

            case IOS:
                cap.setCapability("udid", mobileCapabilities.getUdid());
                cap.setCapability("appium:bundleId", mobileCapabilities.getBundleId());
                cap.setCapability("appium.autoAcceptAlerts", mobileCapabilities.getAutoAcceptAlerts());
                break;
        }
        return cap;
    }

    /**
     * Metodo para retornar uma array de byte que representa a imagem da tela
     *
     * @return byte[]
     */
    public static byte[] getScreenShot() {
        return ((TakesScreenshot) appiumDriver).getScreenshotAs(OutputType.BYTES);
    }

    /**
     * Metodo que verifica se a execução está ocorrendo em um ambiente Jenkins
     *
     * @return boolean
     */
    public static boolean isJenkinsEnvironment() {
        return !Strings.isNullOrEmpty(System.getenv("JENKINS_HOME"));
    }

    /**
     * Metodo que retorna as DesiredCapabilities sendo utilizadas
     *
     * @return boolean
     */
    public static DesiredCapabilities getDesiredCapabilities() {
        return cap;
    }

    public static void setPlatformName(MobilePlatform platformName) {
        Mobile.platformName = platformName;
    }

    public static MobilePlatform getPlatformName() {
        return Mobile.platformName;
    }

    private static String getAppPackageOrBundleId() {
        MobileCapabilities mobileCapabilities = MobileCapabilities.getInstance();
        return getPlatformName() == MobilePlatform.ANDROID ? mobileCapabilities.getAppPackage() : mobileCapabilities.getBundleId();
    }

    /**
     * Metodo que altera o contexto do driver mobile
     *
     * @param driverContext - Enum informando o contexto do driver mobile
     */
    public static void setDriverContext(DriverContext driverContext) {
        String currentDriverContext = ((SupportsContextSwitching) Mobile.getDriver()).getContext();
        try {
            if (!currentDriverContext.equals(driverContext.toString())) {
                ((SupportsContextSwitching) Mobile.getDriver()).context(driverContext.toString());
                logger.debug("Contexto do driver alterado: {}", driverContext.toString());
            }
        } catch (NoSuchContextException e) {
            String validContexts = ((SupportsContextSwitching) appiumDriver).getContextHandles().toString();
            throw new NoSuchContextException(
                    String.format("O contexto '%s' não é válido para o driver em execução, contextos válidos: %s - [%s]"
                            , driverContext.toString(), validContexts, e.getMessage())
            );
        }

    }
}
