package id.seria.gacha.lib.xseries.reflection.jvm.classes;

import id.seria.gacha.lib.xseries.reflection.ReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.ReflectiveNamespace;
import id.seria.gacha.lib.xseries.reflection.constraint.ReflectiveConstraint;
import id.seria.gacha.lib.xseries.reflection.constraint.ReflectiveConstraintException;
import id.seria.gacha.lib.xseries.reflection.jvm.ConstructorMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.EnumMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.FieldMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MethodMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.NamedReflectiveHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObject;
import id.seria.gacha.lib.xseries.reflection.jvm.objects.ReflectedObjectHandle;
import id.seria.gacha.lib.xseries.reflection.parser.ReflectionParser;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;
import org.jetbrains.annotations.ApiStatus.Experimental;

public abstract class ClassHandle implements ReflectiveHandle<Class<?>>, NamedReflectiveHandle {
   protected final ReflectiveNamespace namespace;
   private final Map<Class<ReflectiveConstraint>, ReflectiveConstraint> constraints = new IdentityHashMap<>();

   protected ClassHandle(@NotNull ReflectiveNamespace namespace) {
      this.namespace = namespace;
      namespace.link(this);
   }

   @Experimental
   @Contract(value = "_ -> this", mutates = "this")
   public ClassHandle constraint(@NotNull ReflectiveConstraint constraint) {
      this.constraints.put((Class<ReflectiveConstraint>)constraint.getClass(), constraint);
      return this;
   }

   protected <T extends Class<?>> T checkConstraints(T jvm) {
      for (ReflectiveConstraint constraint : this.constraints.values()) {
         ReflectiveConstraint.Result result = constraint.appliesTo(this, jvm);
         if (result != ReflectiveConstraint.Result.MATCHED) {
            throw ReflectiveConstraintException.create(constraint, result, this, jvm);
         }
      }

      return jvm;
   }

   @NotNull
   @Contract("_ -> new")
   public abstract ClassHandle asArray(@Range(from = 1L, to = 2147483647L) int var1);

   @NotNull
   @Contract("-> new")
   public final ClassHandle asArray() {
      return this.asArray(1);
   }

   @Contract(pure = true)
   public abstract boolean isArray();

   @NotNull
   @Contract("_ -> new")
   public DynamicClassHandle inner(@Language(value = "Java", suffix = "{}") String declaration) {
      return this.inner(this.namespace.classHandle(declaration));
   }

   @NotNull
   @Contract("_ -> param1")
   public <T extends DynamicClassHandle> T inner(@NotNull T handle) {
      Objects.requireNonNull(handle, "Inner handle is null");
      if (this == handle) {
         throw new IllegalArgumentException("Same instance: " + this);
      }

      handle.parent = this;
      this.namespace.link(this);
      return handle;
   }

   public @Range(from = -1L, to = 2147483647L) int getDimensionCount() {
      int count = -1;
      Class<?> clazz = this.reflectOrNull();
      if (clazz == null) {
         return count;
      }

      do {
         clazz = clazz.getComponentType();
         count++;
      } while (clazz != null);

      return count;
   }

   @Contract(pure = true)
   public ReflectiveNamespace getNamespace() {
      return this.namespace;
   }

   @Contract(value = "-> new", pure = true)
   public MethodMemberHandle method() {
      return new MethodMemberHandle(this);
   }

   @Contract(value = "_ -> new", pure = true)
   public MethodMemberHandle method(@Language(value = "Java", suffix = ";") String declaration) {
      return this.createParser(declaration).parseMethod(this.method());
   }

   @Contract(value = "-> new", pure = true)
   public EnumMemberHandle enums() {
      return new EnumMemberHandle(this);
   }

   @Contract(value = "-> new", pure = true)
   public FieldMemberHandle field() {
      return new FieldMemberHandle(this);
   }

   @Contract(value = "_ -> new", pure = true)
   public FieldMemberHandle field(@Language(value = "Java", suffix = ";") String declaration) {
      return this.createParser(declaration).parseField(this.field());
   }

   @Contract(value = "_ -> new", pure = true)
   public ConstructorMemberHandle constructor(@Language(value = "Java", suffix = ";") String declaration) {
      return this.createParser(declaration).parseConstructor(this.constructor());
   }

   @Contract(value = "-> new", pure = true)
   public ConstructorMemberHandle constructor() {
      return new ConstructorMemberHandle(this);
   }

   @Contract(value = "_ -> new", pure = true)
   public ConstructorMemberHandle constructor(Class<?>... parameters) {
      return this.constructor().parameters(parameters);
   }

   @Contract(value = "_ -> new", pure = true)
   public ConstructorMemberHandle constructor(ClassHandle... parameters) {
      return this.constructor().parameters(parameters);
   }

   @Contract(value = "_ -> new", pure = true)
   private ReflectionParser createParser(@Language("Java") String declaration) {
      return new ReflectionParser(declaration).imports(this.namespace);
   }

   public abstract ClassHandle copy();

   @NotNull
   @Override
   public ReflectiveHandle<ReflectedObject> jvm() {
      return new ReflectedObjectHandle(() -> ReflectedObject.of(this.reflect()));
   }
}
