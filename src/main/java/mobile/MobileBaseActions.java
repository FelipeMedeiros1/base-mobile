package mobile;

import java.awt.AWTException;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.imageio.ImageIO;

import exceptions.AutomationException;
import flutter.PageFactoryFlutter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import io.appium.java_client.pagefactory.AppiumFieldDecorator;
import net.sourceforge.tess4j.ITessAPI;
import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import net.sourceforge.tess4j.Word;
import utils.Utils;

/**
 * Centraliza os comportamentos comuns e cuida de variáveis relacionadas a telas android Mobile.
 * Métodos 'protected' foram utilizados para que não possam ser manipulados fora de um 'PageObject' definido para representar uma Screen.
 */
public abstract class MobileBaseActions {

    static final Logger logger = LogManager.getLogger(MobileBaseActions.class);

    public static final Boolean NOT_EXCEPTION = false;
    public static final Boolean EXCEPTION = true;
    protected static short tries = 0;
    private Duration timeSlice = Duration.ofSeconds(20);
    private Duration fixTimeSlice = Duration.ofSeconds(20);

    public MobileBaseActions() {
        updateDriver();
    }

    public MobileBaseActions(Duration timeSlice) {
        this.fixTimeSlice = timeSlice;
        this.timeSlice = timeSlice;
        updateDriver();
    }

    /**
     * Metodo para atualizar o PageFactory com novos parâmetros do AppiumDriver<MobileElement>,
     * valores de timeout e setar o driver que está sendo utilizado no momento, para controle de evidencia.
     */
    private void updateDriver() {
        PageFactory.initElements(new AppiumFieldDecorator(Mobile.getDriver(), timeSlice), this);
        PageFactoryFlutter.initElements(Mobile.getDriver(), this);

    }

    /**
     * Contrato entre a tela e a MobileActions a fim de formar uma obrigatoriedade
     * para saber se a tela carregada está realmente correta com a tela apresentada.
     * @return boolean
     */
    public abstract boolean isView();

    /**
     * Sobrecarga do metodo isView()
     *
     * @param element Elemento para ser validado se está visível
     * @return boolean
     */
    public boolean isView(WebElement element) {
        return isView(element, "");
    }

    /**
     * Sobrecarga do metodo isView()
     *
     * @param element Elemento para ser validado se está visível
     * @param text    Texto que contém no elemento para ser validado, se o elemento
     *                não possui texto enviar string vazia
     * @return boolean
     */
    protected boolean isView(WebElement element, String text) {
        return isView(element, text, EXCEPTION);
    }

    /**
     * Metodo para validar se o elemento está visível e se o elemento contém o mesmo
     * texto passado no parâmetro.
     *
     * @param element      Elemento para ser validado se está visível
     * @param text         Texto que contém no elemento para ser validado, se o
     *                     elemento não possui texto enviar string vazia
     * @param hasException Define se o método irá lançar uma exceção se o elemento
     *                     não estiver visível
     * @return boolean
     */
    protected boolean isView(WebElement element, String text, Boolean hasException) {
        try {
            if (isFlutterElement(element)) {
                Mobile.getDriver().executeScript("flutter:waitFor", element, timeSlice.getSeconds() * 1000);
                return true;
            }
            WebDriverWait wait = new WebDriverWait(Mobile.getDriver(), timeSlice);
            wait.until(ExpectedConditions.visibilityOf(element));
            return element.isDisplayed() && element.getText().contains(text);
        } catch (Exception e) {
            if (!hasException) {
                logger.warn(e.getMessage());
                return false;
            }
            if (++tries >= 3) {
                tries = 0;
                throw new AutomationException("Não foi possível localizar a tela - %s", e.getMessage());
            }
            return isView(element, text, hasException);
        } finally {
            this.timeSlice = fixTimeSlice;
        }
    }

    /**
     * Sobrecarga para executar um 'click' em um objeto especificado a partir do
     * parâmetro. Obs.: a definição de isException é feita na construção da isView.
     *
     * @param clickable Elemento que irá receber o click
     */
    protected void click(WebElement clickable) {
        click(clickable, MobileBaseActions.EXCEPTION);
    }

    /**
     * Metodo para executar um 'click' em um objeto especificado a partir do
     * parâmetro, com a especificação de exceção.
     *
     * @param clickable    Elemento que irá receber o click
     * @param hasException Define se o método irá lançar uma exceção se o elemento
     *                     não estiver visível
     */
    protected void click(WebElement clickable, Boolean hasException) {
        try {
            if (isView(clickable, "", hasException)) {
                if (isFlutterElement(clickable))
                    clickable.click();
                else
                    new Actions(Mobile.getDriver()).click(clickable).build().perform();
            } else {
                logger.info("Erro ao clicar no item - ".concat(clickable.getTagName()));
            }
        } catch (NoSuchElementException e) {
            if (!hasException) {
                logger.error(e.getMessage());
                return;
            }
            if (++tries >= 3) {
                tries = 0;
                throw new AutomationException("Erro ao clicar no item - ".concat(e.getMessage()));
            }
            click(clickable, hasException);
        }
    }

    /**
     * Sobrecarga do metodo setText()
     *
     * @param text      texto a ser inserido no campo
     * @param textField campo onde será clicado para inserir o texto
     */
    protected void setText(String text, WebElement textField) {
        this.setText(text, textField, MobileBaseActions.EXCEPTION);
    }

    /**
     * Metodo para preencher um elemento editável com um texto específico, passando
     * o texto e o elemento que deseja manipular, com o adicional de execução de
     * exceção.
     *
     * @param text         Texto a ser inserido no campo
     * @param textField    Campo onde será clicado para inserir o texto.
     * @param hasException Define se o método irá lançar uma exceção se o elemento
     *                     não estiver visível
     */
    protected void setText(String text, WebElement textField, Boolean hasException) {
        click(textField, hasException);

        if (!isFlutterElement(textField)) {
            List<WebElement> listEditText = textField.findElements(By.className("android.widget.EditText"));
            if (listEditText.size() > 0) {
                WebElement textBox = listEditText.get(0);
                isView(textBox);
                textBox.sendKeys(text);
                return;
            }
        }

        textField.sendKeys(text);

        try {
            if (Mobile.getPlatformName() == MobilePlatform.IOS) {
                Mobile.setDriverContext(DriverContext.NATIVE_APP);
                Mobile.getDriver().switchTo().activeElement().sendKeys(Keys.RETURN);
                Utils.wait(1);
            }
        } catch (Exception e) {
            logger.info("Teclado do dispositivo iOS não foi ocultado. Teclado iOS não localizado.");
        }
    }

    /**
     * Recuperar o texto de um Web Element específico, ou de um campo de texto
     * específico a partir do atributo text. É possível recuperar a partir do objeto
     * pai.
     *
     * @param webElement Elemento que possui o texto para ser recuperado
     * @return String
     */
    protected String getText(WebElement webElement) {
        String text = null;
        try {
            if (isView(webElement, "", true))
                text = webElement.getText();
        } catch (Exception e) {
            throw new AutomationException("Falha ao capturar o texto do elemento [%s]", e.getMessage());
        }
        return text;
    }

    /**
     * Recuperar o texto de um campo de texto específico a partir do atributo text.
     * É possível recuperar a partir do objeto pai.
     *
     * @param fieldElement Elemento que possui o campo de texto
     * @return String
     */
    protected String getTextField(WebElement fieldElement) {
        if (isFlutterElement(fieldElement))
            throw new AutomationException(
                    "O método 'getTextField' não está implementado para elementos Flutter. Utilize o método 'getText'");

        String text = null;
        try {
            if (isView(fieldElement, "", true)) {
                text = fieldElement.findElement(By.className("android.widget.EditText")).getAttribute("text");
            }
        } catch (Exception e) {
            throw new AutomationException("Falha ao capturar o texto do elemento [%s]", e.getMessage());
        }
        return text;
    }

    /**
     * Metodo para aguardar um elemento desaparecer da tela
     *
     * @param elementToWait elemento para aguardar o desaparecimento da tela
     * @param timeSlice     tempo em segundos a ser esperado o desaparecimento do
     *                      elemento
     * @param hasException  lançar uma exceção caso não desapareça após o término do
     *                      tempo de espera
     */
    protected void waitDisappear(WebElement elementToWait, Duration timeSlice, Boolean hasException) {
        try {
            if (isFlutterElement(elementToWait)) {
                Mobile.getDriver().executeScript("flutter:waitForAbsent", elementToWait, timeSlice.getSeconds() * 1000);
            } else {
                WebDriverWait wait = new WebDriverWait(Mobile.getDriver(), timeSlice);
                wait.until(ExpectedConditions.not(ExpectedConditions.visibilityOfAllElements(elementToWait)));
            }
        } catch (Exception e) {
            if (!hasException) {
                logger.error(e.getMessage());
                return;
            }
            if (++tries >= 3) {
                tries = 0;
                throw new AutomationException(
                        "Após aguardar %s segundos o desaparecimento do elemento, o mesmo não desapareceu da tela [%s]",
                        timeSlice.getSeconds(), e.getMessage());
            }
            this.waitDisappear(elementToWait, timeSlice, hasException);
        }
    }

    /**
     * Metodo para realizar a ação de mover um elemento (source) para o ponto da
     * tela onde está o outro elemento (target). Esse movimento é feito de cima para
     * baixo ou de baixo para cima.
     *
     * @param source elemento inicial que será pressionado antes de realizar a ação
     *               de rolagem
     * @param target elemento final onde irá finalizar a ação de rolagem
     */
    protected void scrollMoveTo(WebElement source, WebElement target) {
        if (!isFlutterElement(source) && !isFlutterElement(target)) {
            try {
                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

                Sequence scroll = new Sequence(finger, 1);
                scroll.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(),
                        source.getLocation().getX(), source.getLocation().getY()));
                scroll.addAction(finger.createPointerDown(1));
                scroll.addAction(finger.createPointerMove(Duration.ofMillis(700), PointerInput.Origin.viewport(),
                        source.getLocation().getX(), target.getLocation().getY()));
                scroll.addAction(finger.createPointerUp(1));

                Mobile.getDriver().perform(Arrays.asList(scroll));
            } catch (Exception e) {
                throw new AutomationException("Elemento para realizar o scroll não encontrado: [%s]", e.getMessage());
            }
        } else {
            throw new AutomationException("O método 'scrollMoveTo' não está implementado para elementos Flutter");
        }
    }

    /**
     * Metodo para realizar a ação de mover um elemento (source) para o ponto da
     * tela onde está o outro elemento (target). Esse movimento é feito para os
     * lados.
     *
     * @param source elemento inicial que será pressionado antes de realizar a ação
     *               de rolagem
     * @param target elemento final onde irá finalizar a ação de rolagem
     */
    protected void scrollMoveToHorizontal(WebElement source, WebElement target) {
        if (!isFlutterElement(source) && !isFlutterElement(target)) {
            try {
                PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

                Sequence scroll = new Sequence(finger, 1);
                scroll.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(),
                        source.getLocation().getX(), source.getLocation().getY()));
                scroll.addAction(finger.createPointerDown(1));
                scroll.addAction(finger.createPointerMove(Duration.ofMillis(700), PointerInput.Origin.viewport(),
                        target.getLocation().getX(), source.getLocation().getY()));
                scroll.addAction(finger.createPointerUp(1));

                Mobile.getDriver().perform(Arrays.asList(scroll));
            } catch (Exception e) {
                throw new AutomationException("Elemento para realizar o scroll não encontrado: [%s]", e.getMessage());
            }
        } else {
            throw new AutomationException(
                    "O método 'scrollMoveToHorizontal' não está implementado para elementos Flutter");
        }
    }

    /**
     * Metodo para realizar a ação de rolagem para baixo até localizar o elemento
     *
     * @param element elemento a ser localizado
     */
    public void scrollToElement(WebElement element) {
        this.scrollToElement(element, 0.80, 0.20, 10);
    }

    /**
     * Metodo para realizar a ação de rolagem para baixo até localizar o elemento,
     * podendo escolher o número máximo de rolagens
     *
     * @param element               elemento a ser localizado
     * @param maximumScrollAttempts número máximo de rolagens a ser feitas para
     *                              localizar o elemento
     */
    public void scrollToElement(WebElement element, int maximumScrollAttempts) {
        this.scrollToElement(element, 0.80, 0.20, maximumScrollAttempts);
    }

    /**
     * Metodo para realizar a ação de rolagem (com escolha de direção) até localizar
     * o elemento
     *
     * @param element    elemento a ser localizado
     * @param isScrollUp definição se a rolagem deve ser realizada para cima ou para
     *                   baixo
     */
    public void scrollToElement(WebElement element, boolean isScrollUp) {
        this.scrollToElement(element, 0.80, 0.20, isScrollUp, 10);
    }

    /**
     * Metodo para realizar a ação de rolagem (com escolha de direção) até localizar
     * o elemento, podendo escolher o número máximo de rolagens
     *
     * @param element               elemento a ser localizado
     * @param isScrollUp            definição se a rolagem deve ser realizada para
     *                              cima ou para baixo
     * @param maximumScrollAttempts número máximo de rolagens a ser feitas para
     *                              localizar o elemento
     */
    public void scrollToElement(WebElement element, boolean isScrollUp, int maximumScrollAttempts) {
        this.scrollToElement(element, 0.80, 0.20, isScrollUp, maximumScrollAttempts);
    }

    /**
     * Metodo para realizar a ação de rolagem para baixo até um elemento específico,
     * podendo escolher a porcentagem de rolagem da tela, do ponto inicial para o
     * ponto final.
     *
     * @param element               elemento a ser localizado
     * @param startPointYValue      ponto inicial de rolagem
     * @param endPointYValue        ponto final da rolagem
     * @param maximumScrollAttempts número máximo de rolagens a ser feitas para
     *                              localizar o elemento
     */
    public void scrollToElement(WebElement element, double startPointYValue, double endPointYValue,
                                int maximumScrollAttempts) {
        this.scrollToElement(element, startPointYValue, endPointYValue, false, maximumScrollAttempts);
    }

    /**
     * Metodo para realizar a ação de rolagem até um elemento específico. Podendo
     * escolher a porcentagem de rolagem da tela, do ponto inicial para o ponto final
     * e também podendo escolher a direção da rolagem.
     *
     * @param element               elemento a ser localizado
     * @param startPointYValue      ponto inicial de rolagem
     * @param endPointYValue        ponto final da rolagem
     * @param isScrollUp            definição se a rolagem deve ser realizada para
     *                              cima ou para baixo
     * @param maximumScrollAttempts número máximo de rolagens a ser feitas para
     *                              localizar o elemento
     */
    public void scrollToElement(WebElement element, double startPointYValue, double endPointYValue, boolean isScrollUp,
                                int maximumScrollAttempts) {
        Duration timeSlice = Duration.ofSeconds(1);

        setTimeSlice(timeSlice);
        while (!isView(element, "", false) && maximumScrollAttempts > 0) {
            scroll(startPointYValue, endPointYValue, isScrollUp);
            maximumScrollAttempts--;
            setTimeSlice(timeSlice);
        }

        setTimeSlice(timeSlice);
        if (!isView(element, "", false)) {
            throw new AutomationException(
                    String.format("Após realizar a rolagem na tela por %s vezes, o elemento não foi localizado.",
                            maximumScrollAttempts));
        } else {
            scrollElementToMiddleOfScreen(element);
        }
    }

    /**
     * Metodo para centralizar um elemento visível na tela
     *
     * @param element elemento visível a ser centralizado
     */
    public void scrollElementToMiddleOfScreen(WebElement element) {
        if (isFlutterElement(element))
            scrollElementToMiddleOfScreenFlutter(element);
        else {
            Dimension screenSize = Mobile.getDriver().manage().window().getSize();
            int screenX = screenSize.width / 2;
            int screenY = screenSize.height / 2;
            int screenHeight = screenSize.height - 10;

            int elementY = element.getLocation().getY();

            PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

            Sequence scroll = new Sequence(finger, 1);
            scroll.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), screenX,
                    Math.min(elementY, screenHeight)));
            scroll.addAction(finger.createPointerDown(1));
            scroll.addAction(
                    finger.createPointerMove(Duration.ofMillis(700), PointerInput.Origin.viewport(), screenX, screenY));
            scroll.addAction(finger.createPointerUp(1));

            Mobile.getDriver().perform(Arrays.asList(scroll));
        }
    }

    private void scrollElementToMiddleOfScreenFlutter(WebElement element) {
        HashMap<String, Object> param = new HashMap<String, Object>();
        param.put("alignment", 0.5);
        Mobile.getDriver().executeScript("flutter:scrollIntoView", element, param);
    }

    /**
     * Metodo para realizar a ação de rolagem para baixo ou para cima
     *
     * @param isScrollUp TRUE = realizar rolagem para cima. FALSE = realizar rolagem
     *                   para baixo
     */
    public void scroll(boolean isScrollUp) {
        scroll(0.80, 0.35, isScrollUp);
    }

    /**
     * Metodo para realizar a ação de rolagem para baixo ou para cima, podendo
     * escolher a porcentagem de rolagem da tela, do ponto inicial para o final.
     *
     * @param startPointYValue ponto inicial de rolagem
     * @param endPointYValue   ponto final da rolagem
     * @param isScrollUp       TRUE = realizar rolagem para cima. FALSE = realizar
     *                         rolagem para baixo
     */
    public void scroll(double startPointYValue, double endPointYValue, boolean isScrollUp) {
        Mobile.setDriverContext(DriverContext.NATIVE_APP);
        Dimension windowSize = Mobile.getDriver().manage().window().getSize();
        int pointX = windowSize.width / 2;
        int startPointY = (int) ((windowSize.height - 10) * startPointYValue); // 0.80
        int endPointY = (int) ((windowSize.height - 10) * endPointYValue); // 0.40

        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");

        Sequence scroll = new Sequence(finger, 1);
        scroll.addAction(finger.createPointerMove(Duration.ofMillis(0), PointerInput.Origin.viewport(), pointX,
                isScrollUp ? endPointY : startPointY));
        scroll.addAction(finger.createPointerDown(1));
        scroll.addAction(finger.createPointerMove(Duration.ofMillis(700), PointerInput.Origin.viewport(), pointX,
                isScrollUp ? startPointY : endPointY));
        scroll.addAction(finger.createPointerUp(1));

        Mobile.getDriver().perform(Arrays.asList(scroll));
    }

    /**
     * Metodo para definir o timeout ou alterar em tempo de execução.
     *
     * @param timeSlice tempo de espera em segundos
     */
    public void setTimeSlice(Duration timeSlice) {
        this.timeSlice = timeSlice;
        updateDriver();
    }

    /**
     * Metodo para limpar um campo de texto.
     *
     * @param fieldElement elemento que contém o campo de texto que será limpo
     */
    protected void clearField(WebElement fieldElement) {
        if (isView(fieldElement, "")) {
            fieldElement.findElement(By.className("android.widget.EditText")).clear();
        }
    }

    protected boolean isFlutterElement(WebElement element) {
        Class<?> clazz = element.getClass();
        if (clazz.getName().contains("FlutterElement")) {
            Mobile.setDriverContext(DriverContext.FLUTTER);
            return true;
        }
        Mobile.setDriverContext(DriverContext.NATIVE_APP);
        return false;
    }

    /**
     * Pressiona a tecla TAB um número específico de vezes.
     *
     * @param times o número de vezes que a tecla TAB deve ser pressionada.
     */
    protected void pressTab(int times) {
        try {
            Robot robot = new Robot();
            for (int i = 0; i < times; i++) {
                robot.keyPress(KeyEvent.VK_TAB);
                robot.keyRelease(KeyEvent.VK_TAB);
                robot.delay(100);
            }
        } catch (Exception e) {
            throw new AutomationException("Erro ao pressionar a tecla TAB - ".concat(e.getMessage()));
        }
    }

    /**
     * Envia um texto caractere por caractere usando a classe Actions.
     *
     * @param text o texto a ser enviado.
     */
    protected void sendTextUsingActions(String text) {
        try {
            Actions actions = new Actions(Mobile.getDriver());
            for (char c : text.toCharArray()) {
                actions.sendKeys(String.valueOf(c)).perform();
            }
        } catch (Exception e) {
            throw new AutomationException("Erro ao enviar texto usando Actions - ".concat(e.getMessage()));
        }
    }

    /**
     * Realiza OCR em uma captura de tela e verifica se um texto específico está
     * presente.
     *
     * @param expectedText  o texto que se espera encontrar na captura de tela.
     * @param shouldContain indica se o texto deve ou não estar presente na captura
     *                      de tela.
     * @return true se o texto esperado estiver presente (ou ausente, conforme
     * especificado), false caso contrário.
     * @throws AutomationException se ocorrer um erro durante o processo de OCR.
     */
    public boolean verifyTextInScreenshot(String expectedText, boolean shouldContain) {
        byte[] screenshotBytes = Mobile.getScreenShot();
        BufferedImage fullImage;
        try {
            fullImage = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
        } catch (IOException e) {
            throw new AutomationException("Erro ao ler a imagem da captura de tela: " + e.getMessage());
        }

        BufferedImage preprocessedImage = preprocessImage(fullImage);

        if (tryVerifyTextInScreenshot(preprocessedImage, expectedText, shouldContain)) {
            return true;
        }

        int width = preprocessedImage.getWidth();
        int height = preprocessedImage.getHeight();
        BufferedImage[] subImages = new BufferedImage[4];
        subImages[0] = preprocessedImage.getSubimage(0, 0, width / 2, height / 2);
        subImages[1] = preprocessedImage.getSubimage(width / 2, 0, width / 2, height / 2);
        subImages[2] = preprocessedImage.getSubimage(0, height / 2, width / 2, height / 2);
        subImages[3] = preprocessedImage.getSubimage(width / 2, height / 2, width / 2, height / 2);

        for (BufferedImage subImage : subImages) {
            if (tryVerifyTextInScreenshot(subImage, expectedText, shouldContain)) {
                return true;
            }
        }

        if (!shouldContain) {
            return true;
        }

        throw new AutomationException(
                "Texto não encontrado na captura de tela mesmo após 5 tentativas: " + expectedText);
    }

    /**
     * Tenta verificar o texto em uma captura de tela.
     *
     * @param image         a imagem da captura de tela.
     * @param expectedText  o texto que se espera encontrar na captura de tela.
     * @param shouldContain indica se o texto deve ou não estar presente na captura
     *                      de tela.
     * @return true se o texto esperado estiver presente (ou ausente, conforme
     * especificado), false caso contrário.
     * @throws AutomationException se ocorrer um erro durante o processo de OCR.
     */
    private boolean tryVerifyTextInScreenshot(BufferedImage image, String expectedText, boolean shouldContain) {
        String ocrResult = performOCROnScreenshot(image);
        boolean containsText = ocrResult != null && ocrResult.contains(expectedText);

        if (shouldContain) {
            return containsText;
        } else {
            if (containsText) {
                throw new AutomationException(
                        "Texto encontrado na captura de tela quando não deveria: " + expectedText);
            } else {
                return true;
            }
        }
    }

    /**
     * Realiza OCR em uma imagem de captura de tela.
     *
     * @param image a imagem da captura de tela.
     * @return o texto extraído da imagem usando OCR.
     * @throws AutomationException se ocorrer um erro durante o processo de OCR.
     */
    private String performOCROnScreenshot(BufferedImage image) {
        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/drivers/tessdata");
        tesseract.setLanguage("por");

        try {
            return tesseract.doOCR(image);
        } catch (TesseractException e) {
            throw new AutomationException("Erro ao realizar OCR: " + e.getMessage());
        }
    }

    /**
     * Realiza OCR em uma captura de tela e retorna a posição do texto encontrado.
     *
     * @param text o texto que se espera encontrar na captura de tela.
     * @return o ponto (x, y) da posição do texto encontrado.
     * @throws AutomationException se ocorrer um erro durante o processo de OCR ou
     *                             se o texto não for encontrado.
     */
    public Point findTextPositionInScreenshot(String text) {
        byte[] screenshotBytes = Mobile.getScreenShot();
        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(screenshotBytes));
            image = preprocessImage(image);
        } catch (IOException e) {
            throw new AutomationException("Erro ao ler a imagem da captura de tela: " + e.getMessage());
        }

        ITesseract tesseract = new Tesseract();
        tesseract.setDatapath("src/main/resources/drivers/tessdata");
        tesseract.setLanguage("por");

        List<Word> words;
        try {
            words = tesseract.getWords(image, ITessAPI.TessPageIteratorLevel.RIL_WORD);
        } catch (Exception e) {
            throw new AutomationException("Erro ao realizar OCR: " + e.getMessage());
        }

        for (Word word : words) {
            if (word.getText().equalsIgnoreCase(text)) {
                Rectangle rect = word.getBoundingBox();
                return new Point(rect.x, rect.y);
            }
        }

        throw new AutomationException("Texto não encontrado na captura de tela: " + text);
    }

    /**
     * Realiza um duplo clique na posição do texto encontrado na captura de tela.
     *
     * @param text o texto que se espera encontrar na captura de tela para clicar.
     * @throws AutomationException se ocorrer um erro durante o processo.
     */
    public void doubleClickOnText(String text) {
        Point point = findTextPositionInScreenshot(text);
        try {
            Robot robot = new Robot();
            robot.mouseMove(point.x, point.y);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
        } catch (AWTException e) {
            throw new AutomationException("Erro ao realizar o duplo clique: " + e.getMessage());
        }
    }

    /**
     * Preprocessa a imagem para melhorar a precisão do OCR.
     *
     * @param image a imagem original.
     * @return a imagem preprocessada.
     */
    private BufferedImage preprocessImage(BufferedImage image) {
        BufferedImage grayscaleImage = new BufferedImage(image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D graphics = grayscaleImage.createGraphics();
        graphics.drawImage(image, 0, 0, null);
        graphics.dispose();

        RescaleOp rescaleOp = new RescaleOp(1.5f, 0, null);
        rescaleOp.filter(grayscaleImage, grayscaleImage);

        for (int y = 0; y < grayscaleImage.getHeight(); y++) {
            for (int x = 0; x < grayscaleImage.getWidth(); x++) {
                int rgb = grayscaleImage.getRGB(x, y);
                int gray = (rgb >> 16) & 0xff;
                gray = gray > 128 ? 255 : 0;
                rgb = (gray << 16) | (gray << 8) | gray;
                grayscaleImage.setRGB(x, y, rgb);
            }
        }

        return grayscaleImage;
    }
}
