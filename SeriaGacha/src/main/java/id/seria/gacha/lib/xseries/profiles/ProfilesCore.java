package id.seria.gacha.lib.xseries.profiles;

import com.google.common.cache.LoadingCache;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import id.seria.gacha.lib.xseries.reflection.ReflectiveNamespace;
import id.seria.gacha.lib.xseries.reflection.XReflection;
import id.seria.gacha.lib.xseries.reflection.jvm.FieldMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.FlaggedNamedMemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MemberHandle;
import id.seria.gacha.lib.xseries.reflection.jvm.MethodMemberHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftClassHandle;
import id.seria.gacha.lib.xseries.reflection.minecraft.MinecraftMapping;
import java.lang.invoke.MethodHandle;
import java.net.Proxy;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class ProfilesCore {
   public static final Object USER_CACHE;
   public static final Object MINECRAFT_SESSION_SERVICE;
   public static final Proxy PROXY;
   public static final LoadingCache<Object, Object> YggdrasilMinecraftSessionService_insecureProfiles;
   public static final boolean SUPPORTS_BUKKIT_PlayerProfile;
   public static final Map<String, Object> UserCache_profilesByName;
   public static final Map<UUID, Object> UserCache_profilesByUUID;
   public static final MethodHandle MinecraftSessionService_fillProfileProperties;
   public static final MethodHandle GameProfileCache_get$profileByName$;
   public static final MethodHandle GameProfileCache_get$profileByUUID$;
   public static final MethodHandle CachedUserNameToIdResolver_add;
   public static final MethodHandle CraftMetaSkull_profile$getter;
   public static final MethodHandle CraftMetaSkull_profile$setter;
   public static final MethodHandle CraftSkull_profile$setter;
   public static final MethodHandle CraftSkull_profile$getter;
   public static final MethodHandle Property_getValue;
   public static final MethodHandle UserCache_getNextOperation;
   public static final MethodHandle GameProfileInfo_getProfile;
   public static final MethodHandle GameProfileInfo_setLastAccess;
   public static final MethodHandle GameProfileInfo_nameAndId;
   public static final MethodHandle NameAndId_id;
   public static final MethodHandle NameAndId_name;
   public static final MethodHandle NameAndId$ctor_GameProfile;
   public static final MethodHandle ResolvableProfile$constructor;
   public static final MethodHandle ResolvableProfile_gameProfile;
   public static final boolean ResolvableProfile$bukkitSupports;
   public static final boolean NULLABILITY_RECORD_UPDATE = XReflection.supports(1, 20, 2);

   public ProfilesCore() {
   }

   static {
      Object insecureProfiles = null;
      MethodHandle fillProfileProperties = null;
      MethodHandle newResolvableProfile = null;
      MethodHandle $ResolvableProfile_gameProfile = null;
      MethodHandle NameAndId$ctor = null;
      boolean bukkitUsesResolvableProfile = false;
      ReflectiveNamespace ns = XReflection.namespaced().imports(GameProfile.class, MinecraftSessionService.class, LoadingCache.class);
      MinecraftClassHandle CachedUserNameToIdResolver = ns.ofMinecraft("package nms.server.players; public class CachedUserNameToIdResolver")
         .map(MinecraftMapping.MOJANG, XReflection.v(21, 9, "CachedUserNameToIdResolver").orElse("GameProfileCache"))
         .map(MinecraftMapping.SPIGOT, "UserCache");
      SUPPORTS_BUKKIT_PlayerProfile = ns.ofMinecraft("package org.bukkit; public interface OfflinePlayer")
         .method("org.bukkit.profile.PlayerProfile getPlayerProfile()")
         .exists();
      MinecraftClassHandle GameProfileInfo = CachedUserNameToIdResolver.inner("private static class GameProfileInfo")
         .map(MinecraftMapping.SPIGOT, XReflection.v(21, 9, "a").orElse("UserCacheEntry"))
         .map(MinecraftMapping.OBFUSCATED, "bay$a");
      MinecraftClassHandle NameAndId = ns.ofMinecraft("package nms.server.players; public class NameAndId").map(MinecraftMapping.OBFUSCATED, "bbb");

      Object userCache;
      Object minecraftSessionService;
      Proxy proxy;
      MethodHandle getProfileByName;
      MethodHandle getProfileByUUID;
      MethodHandle cacheProfile;
      MethodHandle profileSetterMeta;
      MethodHandle profileGetterMeta;
      try {
         MinecraftClassHandle CraftMetaSkull = ns.ofMinecraft("package cb.inventory; class CraftMetaSkull extends CraftMetaItem implements SkullMeta");
         MinecraftClassHandle ResolvableProfile = ns.ofMinecraft("package nms.world.item.component; public class ResolvableProfile");
         if (ResolvableProfile.exists()) {
            newResolvableProfile = XReflection.<MethodHandle, MemberHandle>any(
                  ResolvableProfile.method("public static ResolvableProfile createResolved(GameProfile gameProfile)").map(MinecraftMapping.OBFUSCATED, "a"),
                  ResolvableProfile.constructor("public ResolvableProfile(GameProfile gameProfile)")
               )
               .reflect();
            $ResolvableProfile_gameProfile = ResolvableProfile.method("public GameProfile partialProfile()")
               .map(MinecraftMapping.MOJANG, XReflection.v(21, 9, "partialProfile").orElse("gameProfile"))
               .map(MinecraftMapping.OBFUSCATED, XReflection.v(21, 9, "b").v(21, 6, "g").orElse("f"))
               .reflect();
            bukkitUsesResolvableProfile = CraftMetaSkull.field("private ResolvableProfile profile").exists();
         }

         profileGetterMeta = XReflection.<MethodHandle, FieldMemberHandle>any(
               CraftMetaSkull.field("private ResolvableProfile profile"), CraftMetaSkull.field("private GameProfile       profile")
            )
            .modify(FieldMemberHandle::getter)
            .reflect();
         profileSetterMeta = XReflection.<MethodHandle, FlaggedNamedMemberHandle>any(
               CraftMetaSkull.method("private void setProfile(ResolvableProfile profile)"),
               CraftMetaSkull.method("private void setProfile(GameProfile       profile)"),
               CraftMetaSkull.field("private                 GameProfile       profile ").setter()
            )
            .reflect();
         MinecraftClassHandle MinecraftServer = ns.ofMinecraft("package nms.server; public abstract class MinecraftServer");
         Object minecraftServer = (Object)MinecraftServer.method("public static MinecraftServer getServer()").reflect().invoke();
         MinecraftClassHandle Services = ns.ofMinecraft("package nms.server; public class Services");
         boolean usesServices = XReflection.supports(1, 21, 9) && Services.exists();
         Object services = null;
         if (usesServices) {
            services = (Object)MinecraftServer.method("public Services services()")
               .map(MinecraftMapping.OBFUSCATED, XReflection.v(21, 11, "ar").orElse("av"))
               .reflect()
               .invoke((Object)minecraftServer);
            minecraftSessionService = (Object)Services.method("public com.mojang.authlib.minecraft.MinecraftSessionService sessionService()")
               .map(MinecraftMapping.OBFUSCATED, "c")
               .reflect()
               .invoke((Object)services);
         } else {
            minecraftSessionService = (Object)MinecraftServer.method("public MinecraftSessionService getSessionService()")
               .named("aq", "ay", "getMinecraftSessionService", "az", "ao", "am", "aD", "ar", "ap")
               .reflect()
               .invoke((Object)minecraftServer);
         }

         MinecraftClassHandle yggdrasilService = ns.ofMinecraft(
            "package com.mojang.authlib.yggdrasil;public class YggdrasilMinecraftSessionService implements MinecraftSessionService"
         );
         FieldMemberHandle yggdrasilField = yggdrasilService.field().getter();
         if (NULLABILITY_RECORD_UPDATE) {
            yggdrasilField.signature("private final LoadingCache<UUID, Optional<ProfileResult>> insecureProfiles");
         } else {
            yggdrasilField.signature("private final LoadingCache<GameProfile, GameProfile> insecureProfiles");
         }

         MethodHandle insecureProfilesField = yggdrasilField.reflectOrNull();
         if (insecureProfilesField != null) {
            insecureProfiles = (Object)insecureProfilesField.invoke((Object)minecraftSessionService);
         }

         if (usesServices) {
            ns.ofMinecraft("package nms.server.players; public interface UserNameToIdResolver").map(MinecraftMapping.OBFUSCATED, "bbm");
            userCache = (Object)Services.method("public UserNameToIdResolver nameToIdCache()")
               .map(MinecraftMapping.OBFUSCATED, "f")
               .reflect()
               .invoke((Object)services);
         } else {
            userCache = (Object)MinecraftServer.method("public CachedUserNameToIdResolver getProfileCache()")
               .named("at", "ar", "ao", "ap", "au")
               .map(MinecraftMapping.OBFUSCATED, "getUserCache")
               .reflect()
               .invoke((Object)minecraftServer);
         }

         if (!NULLABILITY_RECORD_UPDATE) {
            fillProfileProperties = ns.of(MinecraftSessionService.class)
               .method("public GameProfile fillProfileProperties(GameProfile profile, boolean flag)")
               .reflect();
         }

         UserCache_getNextOperation = CachedUserNameToIdResolver.method("private long getNextOperation()")
            .map(MinecraftMapping.OBFUSCATED, XReflection.v(21, "e").v(16, "d").orElse("d"))
            .reflectOrNull();
         MethodMemberHandle profileByName = CachedUserNameToIdResolver.method().named("getProfile", "a");
         MethodMemberHandle profileByUUID = CachedUserNameToIdResolver.method().named("getProfile", "a");
         getProfileByName = (MethodHandle)XReflection.anyOf(
               () -> profileByName.signature("public          GameProfile  get(String username)"),
               () -> profileByName.signature("public Optional<GameProfile> get(String username)")
            )
            .reflect();
         getProfileByUUID = (MethodHandle)XReflection.anyOf(
               () -> profileByUUID.signature("public          GameProfile  get(UUID id)"),
               () -> profileByUUID.signature("public Optional<GameProfile> get(UUID id)")
            )
            .reflect();
         if (usesServices) {
            NameAndId$ctor = NameAndId.constructor("public NameAndId(GameProfile profile)").reflect();
            cacheProfile = CachedUserNameToIdResolver.method("private void add(NameAndId nameAndId)")
               .parameters(NameAndId)
               .map(MinecraftMapping.OBFUSCATED, "a")
               .reflect();
         } else {
            cacheProfile = CachedUserNameToIdResolver.method("public void add(GameProfile profile)").map(MinecraftMapping.OBFUSCATED, "a").reflect();
         }

         try {
            proxy = (Proxy)MinecraftServer.field("protected final java.net.Proxy proxy")
               .getter()
               .map(
                  MinecraftMapping.OBFUSCATED,
                  XReflection.v(21, 9, "i").v(20, 5, "h").v(20, 3, "i").v(19, "j").v(18, 2, "n").v(18, "o").v(17, "m").v(14, "proxy").v(13, "c").orElse("e")
               )
               .reflect()
               .invoke((Object)minecraftServer);
         } catch (Throwable ex) {
            ProfileLogger.LOGGER.error("Failed to initialize server proxy settings", ex);
            proxy = null;
         }
      } catch (Throwable throwable) {
         throw XReflection.throwCheckedException(throwable);
      }

      MinecraftClassHandle CraftSkull = ns.ofMinecraft("package cb.block; public class CraftSkull extends CraftBlockEntityState implements Skull");
      FieldMemberHandle CraftSkull_profile = XReflection.<MethodHandle, FieldMemberHandle>any(
            CraftSkull.field("private ResolvableProfile profile"), CraftSkull.field("private GameProfile profile")
         )
         .getHandle();
      Property_getValue = NULLABILITY_RECORD_UPDATE ? null : ns.of(Property.class).method("public String getValue()").unreflect();
      PROXY = proxy;
      USER_CACHE = userCache;
      CachedUserNameToIdResolver_add = cacheProfile;
      MINECRAFT_SESSION_SERVICE = minecraftSessionService;
      NameAndId$ctor_GameProfile = NameAndId$ctor;
      Objects.requireNonNull(insecureProfiles, () -> "Couldn't find Mojang's insecureProfiles cache " + XReflection.getVersionInformation());
      YggdrasilMinecraftSessionService_insecureProfiles = (LoadingCache<Object, Object>)insecureProfiles;
      MinecraftSessionService_fillProfileProperties = fillProfileProperties;
      GameProfileCache_get$profileByName$ = getProfileByName;
      GameProfileCache_get$profileByUUID$ = getProfileByUUID;
      CraftMetaSkull_profile$setter = profileSetterMeta;
      CraftMetaSkull_profile$getter = profileGetterMeta;
      CraftSkull_profile$setter = CraftSkull_profile.setter().unreflect();
      CraftSkull_profile$getter = CraftSkull_profile.getter().unreflect();
      ResolvableProfile$constructor = newResolvableProfile;
      ResolvableProfile_gameProfile = $ResolvableProfile_gameProfile;
      ResolvableProfile$bukkitSupports = bukkitUsesResolvableProfile;
      GameProfileInfo_getProfile = GameProfileInfo.method("public GameProfile getProfile()")
         .map(MinecraftMapping.OBFUSCATED, "a")
         .makeAccessible()
         .unwrap()
         .reflectOrNull();
      if (GameProfileInfo_getProfile == null) {
         GameProfileInfo_nameAndId = GameProfileInfo.method("public NameAndId nameAndId()")
            .map(MinecraftMapping.OBFUSCATED, "a")
            .makeAccessible()
            .unwrap()
            .unreflect();
         NameAndId_id = NameAndId.method("public UUID id()").map(MinecraftMapping.OBFUSCATED, "a").makeAccessible().unwrap().unreflect();
         NameAndId_name = NameAndId.method("public String name()").map(MinecraftMapping.OBFUSCATED, "b").makeAccessible().unwrap().unreflect();
      } else {
         GameProfileInfo_nameAndId = null;
         NameAndId_id = null;
         NameAndId_name = null;
      }

      GameProfileInfo_setLastAccess = GameProfileInfo.method("public void setLastAccess(long lastAccess)")
         .map(MinecraftMapping.OBFUSCATED, "a")
         .reflectOrNull();

      try {
         UserCache_profilesByName = (Map)CachedUserNameToIdResolver.field("private final Map<String, UserCache.UserCacheEntry> profilesByName")
            .getter()
            .map(MinecraftMapping.OBFUSCATED, XReflection.v(17, "e").v(16, 2, "c").v(9, "d").orElse("c"))
            .reflect()
            .invoke((Object)userCache);
         UserCache_profilesByUUID = (Map)CachedUserNameToIdResolver.field("private final Map<UUID, UserCache.UserCacheEntry> profilesByUUID")
            .getter()
            .map(MinecraftMapping.OBFUSCATED, XReflection.v(17, "f").v(16, 2, "d").v(9, "e").orElse("d"))
            .reflect()
            .invoke((Object)userCache);
      } catch (Throwable e) {
         throw new IllegalStateException("Failed to initialize ProfilesCore", e);
      }
   }
}
