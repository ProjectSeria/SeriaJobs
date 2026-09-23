package id.seria.gacha.lib.common.utils.configuration.adapter;

import com.google.common.base.Preconditions;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public abstract class ConfigAdapter<T, K> {
   public ConfigAdapter() {
   }

   public T deserialize(ConfigAdapter.ReaderContext context) throws ErrorException, WarningException {
      return this.deserialize(context.getReader(), context.getType(), context.getObject());
   }

   @Deprecated
   public T deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      throw new UnsupportedOperationException("Missing deserialize implementation for adapter " + this.getClass().getName());
   }

   public K serialize(ConfigAdapter.WriterContext<T> context) throws ErrorException {
      return this.serialize(context.getWriter(), context.getObject());
   }

   @Deprecated
   public K serialize(ConfigurationWriter writer, T object) throws ErrorException {
      throw new UnsupportedOperationException("Missing serialize implementation for adapter " + this.getClass().getName());
   }

   public Type getInputType() {
      Type superclass = this.getClass().getGenericSuperclass();
      Preconditions.checkArgument(superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
      return ((ParameterizedType)superclass).getActualTypeArguments()[0];
   }

   public Type getOutputType() {
      Type superclass = this.getClass().getGenericSuperclass();
      Preconditions.checkArgument(superclass instanceof ParameterizedType, "%s isn't parameterized", superclass);
      return ((ParameterizedType)superclass).getActualTypeArguments()[1];
   }

   public static class ReaderContext {
      private final ConfigurationReader reader;
      private final Type type;
      private final Object object;
      private final Object rootObject;

      public ReaderContext(ConfigurationReader reader, Type type, Object object, Object rootObject) {
         this.reader = reader;
         this.type = type;
         this.object = object;
         this.rootObject = rootObject;
      }

      public ConfigurationReader getReader() {
         return this.reader;
      }

      public Type getType() {
         return this.type;
      }

      public Object getObject() {
         return this.object;
      }

      public Object getRootObject() {
         return this.rootObject;
      }
   }

   public static class WriterContext<T> {
      private final ConfigurationWriter writer;
      private final Type type;
      private final T object;
      private final Object rootObject;

      public WriterContext(ConfigurationWriter writer, Type type, T object, Object rootObject) {
         this.writer = writer;
         this.type = type;
         this.object = object;
         this.rootObject = rootObject;
      }

      public ConfigurationWriter getWriter() {
         return this.writer;
      }

      public Type getType() {
         return this.type;
      }

      public T getObject() {
         return this.object;
      }

      public Object getRootObject() {
         return this.rootObject;
      }
   }
}
