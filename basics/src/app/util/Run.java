package app.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Run {
    boolean active() default true;

    String id() default "";

    boolean print() default false;

    boolean timer() default false;

    String timeIdentifier() default "it is";

    boolean showStacktrace() default false;

    boolean showError() default false;

    boolean throwing() default false;


}