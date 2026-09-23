package id.seria.gacha.lib.xseries.reflection.proxy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(ReflectNames.class)
public @interface ReflectName {
   String[] value();

   String version() default "";
}
