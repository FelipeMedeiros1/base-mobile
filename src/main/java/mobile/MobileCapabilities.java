package mobile;


import data.InternalPropertiesLoader;
import data.PropertiesLoader;

public class MobileCapabilities {

    private String platformName = getInternalProperty(String.format("mobile.appium.capability.%s.platformName", Mobile.getPlatformName().toString().toLowerCase()));
    private String automationName = getInternalProperty(String.format("mobile.appium.capability.%s.automationName", Mobile.getPlatformName().toString().toLowerCase()));
    private String platformVersion = getExternalProperty(String.format("mobile.appium.capability.%s.platformVersion", Mobile.getPlatformName().toString().toLowerCase()));
    private String deviceName = getExternalProperty(String.format("mobile.appium.capability.%s.deviceName", Mobile.getPlatformName().toString().toLowerCase()));
    private String appPackage = getExternalProperty("mobile.appium.capability.appPackage");
    private String appActivity = getExternalProperty("mobile.appium.capability.appActivity");
    private String noReset = getInternalProperty("mobile.appium.capability.noReset");
    private String fullReset = getInternalProperty("mobile.appium.capability.fullReset");
    private String resetKeyboard = getInternalProperty("mobile.appium.capability.resetKeyboard");
    private String unicodeKeyboard = getInternalProperty("mobile.appium.capability.unicodeKeyboard");
    private String newCommandTimeout = getInternalProperty("mobile.appium.capability.newCommandTimeout");
    private String udid = getExternalProperty("mobile.appium.capability.udid");
    private String bundleId = getExternalProperty("mobile.appium.capability.bundleId");
    private String autoGrantPermissions = getInternalProperty("mobile.appium.capability.autoGrantPermissions");
    private String autoAcceptAlerts = getInternalProperty("mobile.appium.capability.autoAcceptAlerts");
    private String app = getExternalProperty(String.format("mobile.appium.capability.%s.app", Mobile.getPlatformName().toString().toLowerCase()));

    private static MobileCapabilities instance;

    private MobileCapabilities() {
    }

    public static MobileCapabilities getInstance() {
        if (instance == null) {
            instance = new MobileCapabilities();
        }
        return instance;
    }

    public String getPlatformName() {
        return platformName;
    }

    public String getAutomationName() {
        return automationName;
    }

    public String getPlatformVersion() {
        return platformVersion;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getAppPackage() {
        return appPackage;
    }

    public String getAppActivity() {
        return appActivity;
    }

    public String getNoReset() {
        return noReset;
    }

    public void setNoReset(boolean noReset) {
        this.noReset = String.valueOf(noReset);
    }

    public String getFullReset() {
        return fullReset;
    }

    public String getResetKeyboard() {
        return resetKeyboard;
    }

    public String getUnicodeKeyboard() {
        return unicodeKeyboard;
    }

    public String getApp() {
        return app;
    }

    public void setApp(String app) {
        this.app = app;
    }

    public String getNewCommandTimeout() {
        return newCommandTimeout;
    }

    public String getUdid() {
        return udid;
    }

    public String getBundleId() {
        return bundleId;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public void setPlatformVersion(String platformVersion) {
        this.platformVersion = platformVersion;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public void setAppPackage(String appPackage) {
        this.appPackage = appPackage;
    }

    public void setAppActivity(String appActivity) {
        this.appActivity = appActivity;
    }

    public void setBundleId(String bundleId) {
        this.bundleId = bundleId;
    }

    public String getAutoGrantPermissions() {
        return autoGrantPermissions;
    }

    public String getAutoAcceptAlerts() {
        return autoAcceptAlerts;
    }

    private String getInternalProperty(String key) {
        try {
            return new InternalPropertiesLoader("configuration_core.properties").getValue(key);
        } catch (Exception e) {
            return null;
        }
    }

    private String getExternalProperty(String key) {
        return new PropertiesLoader("configuration.properties").getValue(key);
    }

}
