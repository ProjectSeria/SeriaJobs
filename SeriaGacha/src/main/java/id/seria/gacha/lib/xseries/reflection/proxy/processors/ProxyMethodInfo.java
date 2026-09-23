package id.seria.gacha.lib.xseries.reflection.proxy.processors;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import java.lang.reflect.Method;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public class ProxyMethodInfo {
   public final ReflectiveHandle<?> handle;
   public final Method interfaceMethod;
   public final MappedType rType;
   public final MappedType[] pTypes;

   public ProxyMethodInfo(ReflectiveHandle<?> handle, Method interfaceMethod, MappedType rType, MappedType[] pTypes) {
      this.handle = handle;
      this.interfaceMethod = interfaceMethod;
      this.rType = rType;
      this.pTypes = pTypes;
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName() + '(' + this.interfaceMethod + ')';
   }
}
