package id.seria.gacha.listeners;

import com.google.common.collect.Lists;
import id.seria.gacha.Crates;
import id.seria.gacha.api.crate.events.CratePlaceEvent;
import id.seria.gacha.api.crate.events.CratePreviewOpenEvent;
import id.seria.gacha.api.crate.exceptions.CrateOpeningException;
import id.seria.gacha.api.crate.exceptions.CratePreviewException;
import id.seria.gacha.configurations.crates.CrateLocation;
import id.seria.gacha.facades.CrateFacade;
import id.seria.gacha.facades.KeyFacade;
import id.seria.gacha.internal.Addons;
import id.seria.gacha.internal.Constants;
import id.seria.gacha.internal.FacingDirection;
import id.seria.gacha.internal.Translations;
import id.seria.gacha.lib.common.modelengine.interfaces.event.MegBaseEntityInteractEvent;
import id.seria.gacha.lib.common.nms.interfaces.events.LegacyPlayerInteractEvent;
import id.seria.gacha.lib.common.utils.Utilities;
import id.seria.gacha.lib.common.utils.cooldown.Cooldowns;
import id.seria.gacha.lib.common.utils.inventory.ItemBuilder;
import id.seria.gacha.lib.common.utils.utility.FaceUtil;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import id.seria.gacha.lib.xseries.XMaterial;
import id.seria.gacha.managers.CratesManager;
import id.seria.gacha.managers.crates.Crate;
import id.seria.gacha.managers.crates.CrateType;
import id.seria.gacha.managers.crates.engine.modelengine.ModelEngineDisplayComponent;
import id.seria.gacha.managers.crates.engine.vanilla_block.VanillaBlockDisplayComponent;
import id.seria.gacha.managers.crates.session.CrateOpeningSession;
import id.seria.gacha.registries.InformationRegistry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.inventory.FurnaceBurnEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.event.inventory.PrepareGrindstoneEvent;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;

public class CratesListener implements Listener {
   private final CratesManager manager;
   @Deprecated
   private final Cooldowns previewOpenCooldown = new Cooldowns();

   public CratesListener(CratesManager manager) {
      this.manager = manager;
   }

   @Deprecated
   private boolean shouldUseSubCardinalDirections(CrateType type) {
      return type.getEngineType().getName().equalsIgnoreCase("VANILLA")
         ? !ArrayUtils.contains(Constants.CHEST_MATERIALS, type.getBlockType().getMaterial())
         : true;
   }

   @EventHandler
   protected void onWorldLoadEvent(WorldLoadEvent event) {
      for (Entry<String, List<CrateLocation>> entry : this.manager.getLocationsConfiguration().getLocations().entrySet()) {
         CrateType type = this.manager.getTypeByIdentifier(entry.getKey());
         if (type == null) {
            Crates.getLogger().warn("There was an attempt to add crates with an invalid type: \"" + entry.getKey() + "\"!");
         } else if (type.isEnabled()) {
            try {
               for (CrateLocation serializedLocation : entry.getValue()) {
                  World world = Bukkit.getWorld(serializedLocation.getWorld());
                  if (world != null && world.equals(event.getWorld()) && !this.manager.getCrates().containsKey(serializedLocation)) {
                     Location location = new Location(world, serializedLocation.getX(), serializedLocation.getY(), serializedLocation.getZ());
                     Crate crate = new Crate(this.manager, type, location, serializedLocation.getFace());
                     crate.load(false);
                     this.manager.getCrates().put(serializedLocation, crate);
                  }
               }
            } catch (Exception ex) {
               Crates.getLogger().error("Error loading crate", ex);
            }
         }
      }
   }

   @EventHandler
   protected void onWorldUnloadEvent(WorldUnloadEvent event) {
      for (Crate crate : new ArrayList<>(this.manager.getCrates().values())) {
         if (crate.getWorld().equals(event.getWorld())) {
            crate.unload(false);
            this.manager.getCrates().remove(CrateLocation.from(crate.getLocation()));
         }
      }
   }

   @EventHandler
   protected void onCratePreviewOpenEvent(CratePreviewOpenEvent event) {
      Player player = event.getPlayer();
      if (!this.previewOpenCooldown.checkAndUseCooldown(player, "spamming", 0.1)) {
         event.setCancelled(true);
      }

      if (player.hasPermission("phoenixcrates.admin") && this.previewOpenCooldown.checkAndUseCooldown(player, "removal", 3.0)) {
         Utilities.getNMSFactory().sendActionBarPacket(Arrays.asList(player), "\u00a77(Shift + Left-click this crate to remove it)");
      }
   }

   @EventHandler
   protected void onPlayerPlaceCrate(BlockPlaceEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      ItemStack hand = event.getItemInHand();
      if (hand.getType() != Material.AIR) {
         String identifierString = ItemBuilder.of(hand).getNbtTagString("phoenixcrates:crate");
         if (identifierString != null) {
            event.setCancelled(true);
            this.registerPlacedCrate(player, block, identifierString);
         }
      }
   }

   private void registerPlacedCrate(Player player, Block block, String identifierString) {
      CrateType type = this.manager.getTypeByIdentifier(identifierString);
      if (type == null) {
         Translations.send(player, Translations.t("crate-type-dont-exist", "%identifier%", identifierString));
      } else if (!type.isEnabled()) {
         Translations.send(player, Translations.t("crate.disabled", "%identifier%", identifierString));
      } else if (this.manager.getCrateByBlock(block) != null) {
         Translations.send(player, Translations.t("crate.already-placed"));
      } else {
         CratePlaceEvent placeEvent = new CratePlaceEvent(type, player, block.getLocation());
         Bukkit.getPluginManager().callEvent(placeEvent);
         if (!placeEvent.isCancelled()) {
            if (!player.hasPermission("phoenixcrates.admin")) {
               Translations.send(player, Translations.t("no-permission", "%permission%", "phoenixcrates.admin"));
            } else {
               float playerYaw = player.getLocation().getYaw();
               boolean useSubCardinalDirections = this.shouldUseSubCardinalDirections(type);
               BlockFace blockFace = FaceUtil.yawToFace(playerYaw, useSubCardinalDirections);
               FacingDirection facingDirection = FacingDirection.fromBukkit(blockFace);
               Crates.getScheduler().runSync(block, () -> {
                  try {
                     Crate crate = new Crate(this.manager, type, block.getLocation(), facingDirection);
                     this.manager.createCrate(crate, true);
                     Translations.send(player, Translations.t("crate.successfully-placed"));
                  } catch (Exception e) {
                     Translations.error(player, e);
                  }
               }, 1L);
            }
         }
      }
   }

   @EventHandler
   protected void onPlayerBreakCrate(BlockBreakEvent event) {
      Player player = event.getPlayer();
      Block block = event.getBlock();
      Crate target = this.manager.getCrateByBlock(block);
      if (target != null) {
         event.setCancelled(true);
         if (!player.hasPermission("phoenixcrates.admin")) {
            if (!target.getType().isPreviewMenuEnabled() && !target.getType().hasPreviewActions()) {
               Translations.send(player, Translations.t("no-permission", "%permission%", "phoenixcrates.admin"));
            }
         } else if (!player.isSneaking()) {
            Translations.send(player, Translations.t("crate.need-crouched"));
         } else {
            try {
               this.manager.destroyCrate(target);
               Translations.send(player, Translations.t("crate.successfully-removed"));
               event.setCancelled(false);
            } catch (Exception e) {
               Translations.error(player, e);
            }
         }
      }
   }

   @EventHandler
   protected void onPlayerInteractBlock(LegacyPlayerInteractEvent event) {
      if (event.getAction() == Action.RIGHT_CLICK_BLOCK || event.getAction() == Action.LEFT_CLICK_BLOCK) {
         Player player = event.getPlayer();
         Block block = event.getClickedBlock();
         ItemStack item = event.getItem() == null ? new ItemStack(Material.AIR) : event.getItem();
         String crateKey = KeyFacade.extractKey(item);
         if (crateKey != null) {
            event.setCancelled(true);
         }

         Crate crate = this.manager.getCrateByBlock(block);
         if (crate == null) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK && !event.isBlockInHand()) {
               String identifierString = ItemBuilder.of(item).getNbtTagString("phoenixcrates:crate");
               if (identifierString != null) {
                  event.setCancelled(true);
                  Block target = block.getRelative(event.getBlockFace());
                  if (target.getType() == Material.AIR) {
                     this.registerPlacedCrate(player, target, identifierString);
                  }
               }
            }
         } else {
            event.setCancelled(true);
            if (!event.getHand().name().equalsIgnoreCase("OFF_HAND")) {
               if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                  if (!InformationRegistry.isEnabled(Addons.CLAIM) && !InventoryUtil.hasAvaliableSlot(player)) {
                     Translations.send(player, Translations.t("editor.errors.inventory-full"));
                     return;
                  }

                  try {
                     this.openCrateWithMoneyConfirmation(crate, player);
                  } catch (CrateOpeningException e) {
                     if (e.isKnockbackActive() && Crates.getConfiguration().getCratesKnockback() > 0.0) {
                        Location crateLocation = crate.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
                        player.setVelocity(
                           player.getLocation()
                              .subtract(crateLocation)
                              .toVector()
                              .normalize()
                              .setY(0.1)
                              .multiply(Crates.getConfiguration().getCratesKnockback())
                        );
                     }

                     Translations.send(player, e.getMessage());
                  } catch (CratePreviewException e) {
                     throw new RuntimeException(e);
                  }
               } else if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                  if (player.isSneaking()) {
                     event.setCancelled(false);
                     if (player.hasPermission("phoenixcrates.admin")) {
                        return;
                     }
                  }

                  if (crate.getType().isPreviewMenuEnabled() || crate.getType().hasPreviewActions()) {
                     if (crate.getType().isPreviewMenuEnabled() && crate.getType().getRegisteredRewards().isEmpty()) {
                        Translations.send(player, Translations.t("crate.no-rewards"));
                        return;
                     }

                     try {
                        CrateFacade.openPreviewMenu(crate, player);
                     } catch (CratePreviewException | CrateOpeningException e) {
                        Translations.send(player, e.getMessage());
                     }
                  }

                  if (player.getGameMode().equals(GameMode.CREATIVE)
                     && crate.getType().getBlockMaterial().equals(XMaterial.PLAYER_HEAD)
                     && crate.getDisplayComponent() instanceof VanillaBlockDisplayComponent) {
                     Crates.getScheduler().runSync(() -> crate.getDisplayComponent().show(Arrays.asList(player)), 2L);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   protected void onPlayerInteractModel(MegBaseEntityInteractEvent event) {
      if (!event.getSlot().name().equalsIgnoreCase("OFF_HAND")) {
         if (event.getAction() != MegBaseEntityInteractEvent.Action.INTERACT) {
            Player player = event.getPlayer();
            Crate crate = ModelEngineDisplayComponent.LOOKUP_MAP.get(event.getBaseEntity().getUniqueId());
            if (crate != null) {
               if (event.getAction() != MegBaseEntityInteractEvent.Action.ATTACK) {
                  if (!InformationRegistry.isEnabled(Addons.CLAIM) && !InventoryUtil.hasAvaliableSlot(player)) {
                     Translations.send(player, Translations.t("editor.errors.inventory-full"));
                     return;
                  }

                  try {
                     this.openCrateWithMoneyConfirmation(crate, player);
                  } catch (CrateOpeningException e) {
                     if (e.isKnockbackActive() && Crates.getConfiguration().getCratesKnockback() > 0.0) {
                        Location crateLocation = crate.getLocation().getBlock().getLocation().add(0.5, 0.5, 0.5);
                        player.setVelocity(
                           player.getLocation()
                              .subtract(crateLocation)
                              .toVector()
                              .normalize()
                              .setY(0.1)
                              .multiply(Crates.getConfiguration().getCratesKnockback())
                        );
                     }

                     Translations.send(player, e.getMessage());
                  } catch (CratePreviewException e) {
                     throw new RuntimeException(e);
                  }
               } else {
                  if (player.isSneaking() && player.hasPermission("phoenixcrates.admin")) {
                     try {
                        this.manager.destroyCrate(crate);
                        Translations.send(player, Translations.t("crate.successfully-removed"));
                     } catch (Exception e) {
                        Translations.error(player, e);
                     }

                     return;
                  }

                  if (!crate.getType().isPreviewMenuEnabled() && !crate.getType().hasPreviewActions()) {
                     return;
                  }

                  if (crate.getType().isPreviewMenuEnabled() && crate.getType().getRegisteredRewards().isEmpty()) {
                     Translations.send(player, Translations.t("crate.no-rewards"));
                     return;
                  }

                  try {
                     CrateFacade.openPreviewMenu(crate, player);
                  } catch (CratePreviewException | CrateOpeningException e) {
                     Translations.send(player, e.getMessage());
                  }
               }
            }
         }
      }
   }

   @EventHandler
   protected void onPlayerQuit(PlayerQuitEvent event) {
      Player player = event.getPlayer();
      List<CrateOpeningSession> sessions = this.manager.getPlayerOpeningSessions(player);
      sessions.forEach(session -> session.getCrate().unloadSession(session, true));
   }

   @EventHandler
   protected void onPlayerTeleport(PlayerTeleportEvent event) {
      if (event.getFrom().getWorld() != null && event.getTo().getWorld() != null) {
         if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            Player player = event.getPlayer();
            List<CrateOpeningSession> sessions = Crate.OPENING_SESSIONS.get(player.getUniqueId());
            if (sessions != null) {
               for (CrateOpeningSession session : Lists.newArrayList(sessions)) {
                  if (!session.isUnloaded() && !session.isWorldChangeAllowed()) {
                     session.getCrate().unloadSession(session, true);
                  }
               }
            }
         }
      }
   }

   @EventHandler
   public void onAnvilPrepare(PrepareAnvilEvent event) {
      ItemStack item = event.getInventory().getItem(0);
      if (item != null && item.getType() != Material.AIR) {
         if (KeyFacade.extractKey(item) != null) {
            event.setResult(null);
         }
      }
   }

   @EventHandler
   public void onCraftPrepare(PrepareItemCraftEvent event) {
      CraftingInventory craftingInventory = event.getInventory();

      for (ItemStack item : craftingInventory.getMatrix()) {
         if (item != null && item.getType() != Material.AIR) {
            String crateKey = KeyFacade.extractKey(item);
            if (crateKey != null) {
               event.getInventory().setResult(null);
               break;
            }
         }
      }
   }

   @EventHandler
   public void onGrindstonePrepare(PrepareGrindstoneEvent event) {
      for (ItemStack item : event.getInventory().getContents()) {
         if (item != null && item.getType() != Material.AIR && KeyFacade.extractKey(item) != null) {
            event.setResult(null);
            return;
         }
      }
   }

   @EventHandler
   public void onFurnaceBurn(FurnaceBurnEvent event) {
      if (KeyFacade.extractKey(event.getFuel()) != null) {
         event.setCancelled(true);
      }
   }

   @EventHandler
   public void onFurnaceSmelt(FurnaceSmeltEvent event) {
      if (KeyFacade.extractKey(event.getSource()) != null) {
         event.setCancelled(true);
      }
   }

   private void openCrateWithMoneyConfirmation(Crate crate, Player player) throws CrateOpeningException, CratePreviewException {
      CrateFacade.openCrateWithMoneyConfirmation(crate, player, () -> CrateFacade.openCrate(crate, player));
   }
}
