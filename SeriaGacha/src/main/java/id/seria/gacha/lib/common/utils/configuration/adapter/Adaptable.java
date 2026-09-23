package id.seria.gacha.lib.common.utils.configuration.adapter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Adaptable {
   Class<? extends ConfigAdapter> adapter();
}
