package id.seria.gacha.lib.xseries.reflection.proxy.annotations;

import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftMapping;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(MappedMinecraftNames.class)
public @interface MappedMinecraftName {
   MinecraftMapping mapping();

   ReflectName[] names();
}
