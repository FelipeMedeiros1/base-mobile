package mobile;

import com.google.common.base.Strings;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.remote.DesiredCapabilities;

import java.io.File;

public class AppInstall {

    static final Logger logger = LogManager.getLogger(AppInstall.class);

    private String appPath;
    private File appFile;

    public AppInstall() {
        appPath = MobileCapabilities.getInstance().getApp();
        validateAppFile();
    }

    private void validateAppFile() {
        if (!Strings.isNullOrEmpty(appPath)) {
            File appFile = new File(appPath);
            if (!appFile.exists()) {
                throw new RuntimeException(String.format("Não foi possível localizar o arquivo do aplicativo a ser instalado no dispositivo. " +
                        "Verifique se a propriedade 'mobile.appium.capability.%s.app' está com o valor correto: %s", Mobile.getPlatformName().toString(), appPath));
            }
            this.appFile = appFile;
        }
    }

    public void setAppCapability(DesiredCapabilities cap) {
        if (appFile != null) {
            cap.setCapability("app", appFile.getAbsolutePath());
            cap.setCapability("appium:enforceAppInstall", true);
            MobileCapabilities.getInstance().setNoReset(false);
            logger.info("Capability 'appium:app' definida para o aplicativo ser instalado no dispositivo. " +
                    "Arquivo do aplicativo que será instalado: " + appFile.getAbsolutePath());
        }
    }
}
