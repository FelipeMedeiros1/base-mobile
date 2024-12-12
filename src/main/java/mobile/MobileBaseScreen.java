package mobile;

import flutter.PageFactoryFlutter;
import org.openqa.selenium.support.PageFactory;



import io.appium.java_client.pagefactory.AppiumFieldDecorator;

public abstract class MobileBaseScreen {

	public MobileBaseScreen() {
		updateDriver();
	}

	/**
	 * Metodo para atualizar o PageFactory com novas parametros do
	 * AppiumDriver<MobileElement>, valores de timeout e setar o driver que está
	 * sendo utilizado no momento, para controle de evidencia.
	 */
	private void updateDriver() {
		PageFactory.initElements(new AppiumFieldDecorator(Mobile.getDriver()), this);
		PageFactoryFlutter.initElements(Mobile.getDriver(), this);

	}

}
