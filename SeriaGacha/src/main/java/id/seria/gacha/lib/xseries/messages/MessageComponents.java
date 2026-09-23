package id.seria.gacha.lib.xseries.messages;

import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftPackage;
import java.lang.invoke.MethodHandle;
import java.util.Objects;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.jetbrains.annotations.ApiStatus.Experimental;

public final class MessageComponents {
   private static final MethodHandle CraftChatMessage_fromJson;

   public MessageComponents() {
   }

   public static MessageComponents.MessageTextString ofNullable(String text) {
      return text == null ? null : new MessageComponents.MessageTextString(text);
   }

   public static MessageComponents.MessageTextComponent ofNullable(BaseComponent text) {
      return text == null ? null : new MessageComponents.MessageTextComponent(text);
   }

   @Experimental
   public static Object bungeeToVanilla(BaseComponent component) throws Throwable {
      String json = ComponentSerializer.toString(component);
      return (Object)CraftChatMessage_fromJson.invoke((String)json);
   }

   public static BaseComponent fromLegacy(String message) {
      return new TextComponent(TextComponent.fromLegacyText(message));
   }

   static {
      MinecraftClassHandle IChatBaseComponentClass = XReflection.ofMinecraft().inPackage(MinecraftPackage.NMS, "network.chat").named("IChatBaseComponent");

      MethodHandle fromJson;
      try {
         fromJson = XReflection.ofMinecraft()
            .inPackage(MinecraftPackage.CB, "util")
            .named("CraftChatMessage")
            .method("public static IChatBaseComponent fromJSON(String jsonMessage)")
            .returns(IChatBaseComponentClass)
            .reflect();
      } catch (Throwable ex) {
         fromJson = null;
      }

      CraftChatMessage_fromJson = fromJson;
   }

   public interface MessageText {
      String asString();

      BaseComponent asComponent();
   }

   public static final class MessageTextComponent implements MessageComponents.MessageText {
      private final BaseComponent component;

      public MessageTextComponent(BaseComponent component) {
         this.component = Objects.requireNonNull(component, "Message cannot be null");
      }

      @Override
      public String toString() {
         return this.getClass().getSimpleName() + '(' + this.component + ')';
      }

      @Override
      public String asString() {
         return this.component.toLegacyText();
      }

      @Override
      public BaseComponent asComponent() {
         return this.component;
      }
   }

   public static final class MessageTextString implements MessageComponents.MessageText {
      private final String string;

      public MessageTextString(String string) {
         this.string = Objects.requireNonNull(string, "Message cannot be null");
      }

      @Override
      public String toString() {
         return this.getClass().getSimpleName() + '(' + this.string + ')';
      }

      @Override
      public String asString() {
         return this.string;
      }

      @Override
      public BaseComponent asComponent() {
         return MessageComponents.fromLegacy(this.string);
      }
   }
}
