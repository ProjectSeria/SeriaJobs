package id.seria.gacha.lib.xseries.reflection.jvm.objects;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;

abstract class AbstractReflectedObject implements ReflectedObject {
   AbstractReflectedObject() {
   }

   public abstract AnnotatedElement unreflect();

   @Override
   public final <A extends Annotation> A getAnnotation(Class<A> annotationClass) {
      return this.unreflect().getAnnotation(annotationClass);
   }

   @Override
   public final boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
      return this.unreflect().isAnnotationPresent(annotationClass);
   }

   @Override
   public final <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationClass) {
      return this.unreflect().getAnnotationsByType(annotationClass);
   }

   @Override
   public final Annotation[] getAnnotations() {
      return this.unreflect().getAnnotations();
   }

   @Override
   public final <A extends Annotation> A getDeclaredAnnotation(Class<A> annotationClass) {
      return this.unreflect().getDeclaredAnnotation(annotationClass);
   }

   @Override
   public final <A extends Annotation> A[] getDeclaredAnnotationsByType(Class<A> annotationClass) {
      return this.unreflect().getDeclaredAnnotationsByType(annotationClass);
   }

   @Override
   public final Annotation[] getDeclaredAnnotations() {
      return this.unreflect().getDeclaredAnnotations();
   }

   @Override
   public final boolean equals(Object obj) {
      if (this == obj) {
         return true;
      } else if (obj == null) {
         return false;
      } else {
         return obj instanceof ReflectedObject ? this.unreflect().equals(((ReflectedObject)obj).unreflect()) : this.unreflect().equals(obj);
      }
   }

   @Override
   public final int hashCode() {
      return this.unreflect().hashCode();
   }

   @Override
   public final String toString() {
      return this.getClass().getSimpleName() + '(' + this.unreflect() + ')';
   }
}
