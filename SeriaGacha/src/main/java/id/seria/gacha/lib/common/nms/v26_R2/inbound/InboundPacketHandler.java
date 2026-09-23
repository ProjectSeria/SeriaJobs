package id.seria.gacha.lib.common.nms.v26_R2.inbound;

import id.seria.gacha.lib.common.nms.interfaces.events.AsyncPlayerInteractFakeEntityEvent;
import id.seria.gacha.sdk.platforms.server.ServerPlugin;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.minecraft.network.protocol.game.ServerboundInteractPacket;
import net.minecraft.world.InteractionHand;
import org.bukkit.entity.Player;

public class InboundPacketHandler extends ChannelInboundHandlerAdapter {
   public static final String HANDLER_NAME = "phoenix_common_listener";
   private final ServerPlugin plugin;
   private final Player player;

   public InboundPacketHandler(ServerPlugin plugin, Player player) {
      this.player = player;
      this.plugin = plugin;
   }

   public void channelRead(ChannelHandlerContext context, Object object) throws Exception {
      try {
         if (this.plugin.isEnabled() && object instanceof ServerboundInteractPacket packet) {
            int entityId = packet.getEntityId();
            InteractionHand[] handHolder = new InteractionHand[1];
            packet.dispatch(new ServerboundInteractPacket.Handler() {
               @Override
               public void onInteraction(InteractionHand interactionHand) {
                  handHolder[0] = interactionHand;
               }
               @Override
               public void onInteraction(InteractionHand interactionHand, net.minecraft.world.phys.Vec3 vec3) {
                  handHolder[0] = interactionHand;
               }
               @Override
               public void onAttack() {
                  handHolder[0] = null;
               }
            });
            InteractionHand hand = handHolder[0];
            AsyncPlayerInteractFakeEntityEvent event;
            if (hand == null) {
               event = new AsyncPlayerInteractFakeEntityEvent(
                  this.player, entityId, AsyncPlayerInteractFakeEntityEvent.FakeInteract.ATTACK, AsyncPlayerInteractFakeEntityEvent.FakeHand.MAIN_HAND
               );
            } else {
               event = new AsyncPlayerInteractFakeEntityEvent(
                  this.player,
                  entityId,
                  AsyncPlayerInteractFakeEntityEvent.FakeInteract.INTERACT,
                  AsyncPlayerInteractFakeEntityEvent.FakeHand.valueOf(hand.name())
               );
            }

            event.callEvent(this.plugin);
         }
      } catch (Throwable t) {
         t.printStackTrace();
      }

      super.channelRead(context, object);
   }
}
