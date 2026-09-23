package id.seria.gacha.lib.xseries.reflection.jvm;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.XAccessFlag;
import id.seria.gacha.lib.xseries.reflection.jvm.classes.ClassHandle;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.EnumSet;
import java.util.Set;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.ApiStatus.Internal;

public abstract class MemberHandle implements ReflectiveHandle<MethodHandle> {
   protected Set<XAccessFlag> accessFlags = EnumSet.noneOf(XAccessFlag.class);
   protected final ClassHandle clazz;

   protected MemberHandle(ClassHandle clazz) {
      this.clazz = clazz;
   }

   @Internal
   public Set<XAccessFlag> getAccessFlags() {
      return this.accessFlags;
   }

   public ClassHandle getClassHandle() {
      return this.clazz;
   }

   public MemberHandle makeAccessible() {
      this.accessFlags.add(XAccessFlag.PRIVATE);
      return this;
   }

   public abstract MemberHandle signature(@Language("Java") String var1);

   public abstract MethodHandle reflect() throws ReflectiveOperationException;

   public abstract <T extends AccessibleObject & Member> T reflectJvm() throws ReflectiveOperationException;

   protected <T extends AccessibleObject & Member> T handleAccessible(T accessibleObject) throws ReflectiveOperationException {
      if (this.accessFlags.contains(XAccessFlag.PRIVATE) || Modifier.isPrivate(accessibleObject.getDeclaringClass().getModifiers())) {
         accessibleObject.setAccessible(true);
      }

      return accessibleObject;
   }

   public abstract MemberHandle copy();
}
