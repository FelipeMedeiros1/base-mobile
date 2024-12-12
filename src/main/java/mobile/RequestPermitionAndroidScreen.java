package mobile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.appium.java_client.pagefactory.AndroidFindBy;
import org.openqa.selenium.WebElement;

import java.time.Duration;

/**
 * Classe que representa a PageObject global para permitir ou bloquear um recurso do sistema.
 */
public class RequestPermitionAndroidScreen extends MobileBaseActions {

    static final Logger logger = LogManager.getLogger(RequestPermitionAndroidScreen.class);

    @AndroidFindBy(id = "com.android.permissioncontroller:id/permission_deny_button")
    private WebElement btnPermissionDeny;

    @AndroidFindBy(id = "com.android.permissioncontroller:id/permission_allow_button")
    private WebElement btnPermissionAllow;

    @AndroidFindBy(id = "com.android.permissioncontroller:id/permission_allow_foreground_only_button")
    private WebElement btnPermissionAllowWhileUsingApp;

    // algumas versões do android mudam o pacote de com.android.permissioncontroller para com.android.packageinstaller
    @AndroidFindBy(id = "com.android.packageinstaller:id/permission_deny_button")
    private WebElement btnPermissionDeny2;

    @AndroidFindBy(id = "com.android.packageinstaller:id/permission_allow_button")
    private WebElement btnPermissionAllow2;

    public RequestPermitionAndroidScreen() {

    }

    public RequestPermitionAndroidScreen(Duration timeslice) {
        super(timeslice);
    }

    public boolean isShow() {
        return (isView(btnPermissionAllowWhileUsingApp, "", false) || isView(btnPermissionAllow, "", false)
                || isView(btnPermissionAllow2, "", false));
    }

    public void allow() {
        if (isView(btnPermissionAllow, "", false))
            click(btnPermissionAllow);
        else if (isView(btnPermissionAllowWhileUsingApp, "", false))
            click(btnPermissionAllowWhileUsingApp);
        else if (isView(btnPermissionAllow2, "", false))
            click(btnPermissionAllow2);
        else
            logger.error("Erro ao clicar no Botao [Aceitar] do popUp de permições.");
    }

    public void deny() {
        if (isView(btnPermissionDeny, "", false))
            click(btnPermissionDeny);
        else if (isView(btnPermissionDeny2, "", false))
            click(btnPermissionDeny2);
        else
            logger.error("Erro ao clicar no Botao [Recusar] do popUp de permições.");
    }

    @Override
    public boolean isView() {
        return isShow();
    }

}
