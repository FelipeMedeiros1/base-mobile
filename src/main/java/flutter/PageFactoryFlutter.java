package flutter;

import io.github.ashwith.flutter.FlutterElement;
import io.github.ashwith.flutter.FlutterFinder;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class PageFactoryFlutter {

    public static void initElements(RemoteWebDriver driver, Object pageObject){
        Class<?> pageClass = pageObject.getClass();
        Field[] fields = pageClass.getDeclaredFields();

        for(Field field : fields){
            FlutterFindBy flutterFindBy = field.getAnnotation(FlutterFindBy.class);
            if(flutterFindBy != null){
                assertValidFindByFlutter(flutterFindBy);
                if(field.getType() == FlutterElement.class || field.getType() == WebElement.class){
                    FlutterElement flutterElement = buildFindByFlutter(flutterFindBy, driver);
                    field.setAccessible(true);
                    try {
                        field.set(pageObject, flutterElement);
                    }catch (Exception e){
                        throw new RuntimeException(e.getMessage());
                    }
                }
            }
        }
    }

    private static void assertValidFindByFlutter(FlutterFindBy findByFlutter){
        Set<String> finders = new HashSet<>();
        if (!"".equals(findByFlutter.key())) finders.add("key: " + findByFlutter.key());
        if (!"".equals(findByFlutter.type())) finders.add("type: " + findByFlutter.type());
        if (!"".equals(findByFlutter.toolTip())) finders.add("toolTip: " + findByFlutter.toolTip());
        if (!"".equals(findByFlutter.text())) finders.add("text: " + findByFlutter.text());
        if (!"".equals(findByFlutter.semanticsLabel())) finders.add("semanticsLabel: " + findByFlutter.semanticsLabel());
        if (!"".equals(findByFlutter.semanticsLabelPattern())) finders.add("semanticsLabelPattern" + findByFlutter.semanticsLabelPattern());

        if(finders.size() == 0)
            throw new IllegalArgumentException("Não foi especificada uma estratégia de localização para a anotação @FindByFlutter");

        if(finders.size() > 1) {
            throw new IllegalArgumentException(
                    String.format("Deve ser especificado no máximo uma estratégia de localização para a anotação @FindByFlutter." +
                            "Número encontrado: %d (%s)", finders.size(), finders.toString()));
        }
    }

    private static FlutterElement buildFindByFlutter(FlutterFindBy findByFlutter, RemoteWebDriver driver){
        FlutterFinder flutterFinder = new FlutterFinder(driver);

        if (!"".equals(findByFlutter.key())){
            return flutterFinder.byValueKey(findByFlutter.key());
        }
        if (!"".equals(findByFlutter.type())){
            return flutterFinder.byType(findByFlutter.type());
        }
        if (!"".equals(findByFlutter.toolTip())){
            return flutterFinder.byToolTip(findByFlutter.toolTip());
        }
        if (!"".equals(findByFlutter.text())) {
            return flutterFinder.byText(findByFlutter.text());
        }
        if (!"".equals(findByFlutter.semanticsLabel())){
            return flutterFinder.bySemanticsLabel(findByFlutter.semanticsLabel());
        }
        if (!"".equals(findByFlutter.semanticsLabelPattern())) {
            Pattern pattern = Pattern.compile(findByFlutter.semanticsLabelPattern());
            return flutterFinder.bySemanticsLabel(pattern);
        }

        return null;
    }
}
