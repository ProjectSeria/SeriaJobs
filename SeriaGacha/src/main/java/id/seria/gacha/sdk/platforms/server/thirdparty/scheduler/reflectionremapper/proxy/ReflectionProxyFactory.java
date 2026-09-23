package id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.reflectionremapper.proxy;

import id.seria.gacha.sdk.platforms.server.thirdparty.scheduler.reflectionremapper.ReflectionRemapper;
import java.lang.reflect.Proxy;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.framework.qual.DefaultQualifier;

@DefaultQualifier(NonNull.class)
public final class ReflectionProxyFactory {
   private final ReflectionRemapper reflectionRemapper;
   private final ClassLoader classLoader;

   private ReflectionProxyFactory(final ReflectionRemapper reflectionRemapper, final ClassLoader classLoader) {
      this.reflectionRemapper = reflectionRemapper;
      this.classLoader = classLoader;
   }

   public <I> I reflectionProxy(final Class<I> proxyInterface) {
      return (I)Proxy.newProxyInstance(
         this.classLoader, new Class[]{proxyInterface}, new ReflectionProxyInvocationHandler<>(proxyInterface, this.reflectionRemapper)
      );
   }

   public static ReflectionProxyFactory create(final ReflectionRemapper reflectionRemapper, final ClassLoader classLoader) {
      return new ReflectionProxyFactory(reflectionRemapper, classLoader);
   }
}
