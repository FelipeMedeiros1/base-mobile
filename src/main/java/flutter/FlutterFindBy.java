package flutter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface FlutterFindBy {

    String key() default "";

    String type() default "";

    String toolTip() default "";

    String text() default "";

    String semanticsLabel() default "";

    String semanticsLabelPattern() default "";

}
