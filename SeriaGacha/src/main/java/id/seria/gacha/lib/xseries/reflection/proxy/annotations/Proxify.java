package id.seria.gacha.lib.xseries.reflection.proxy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Proxify {
   Class<?> target() default void.class;

   String packageName() default "";

   boolean ignoreCurrentName() default false;
}
