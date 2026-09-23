package id.seria.gacha.lib.common.nms.v26_R2.wrappers;

import id.seria.gacha.lib.common.nms.interfaces.NMSFactory;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.BossBarWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarColorWrapper;
import id.seria.gacha.lib.common.nms.interfaces.wrappers.enums.BarStyleWrapper;
import java.util.List;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class BossBarWrapperImpl implements BossBarWrapper {
   private final NMSFactory factory;
   private final BossBar bossbar;

   public BossBarWrapperImpl(NMSFactory factory, BossBar bossbar) {
      this.factory = factory;
      this.bossbar = bossbar;
   }

   @Override
   public NMSFactory getNMSFactory() {
      return this.factory;
   }

   @Override
   public String getTitle() {
      return this.bossbar.getTitle();
   }

   @Override
   public void setTitle(String title) {
      this.bossbar.setTitle(title);
   }

   @Override
   public BarColorWrapper getColor() {
      return BarColorWrapper.valueOf(this.bossbar.getColor().name());
   }

   @Override
   public void setColor(BarColorWrapper color) {
      this.bossbar.setColor(BarColor.valueOf(color.name()));
   }

   @Override
   public BarStyleWrapper getStyle() {
      return BarStyleWrapper.valueOf(this.bossbar.getStyle().name());
   }

   @Override
   public void setStyle(BarStyleWrapper style) {
      this.bossbar.setStyle(BarStyle.valueOf(style.name()));
   }

   @Override
   public void setProgress(double progress) {
      this.bossbar.setProgress(progress);
   }

   @Override
   public double getProgress() {
      return this.bossbar.getProgress();
   }

   @Override
   public void addPlayer(Player player) {
      this.bossbar.addPlayer(player);
   }

   @Override
   public void removePlayer(Player player) {
      this.bossbar.removePlayer(player);
   }

   @Override
   public void removeAll() {
      this.bossbar.removeAll();
   }

   @Override
   public List<Player> getPlayers() {
      return this.bossbar.getPlayers();
   }

   @Override
   public void setVisible(boolean visible) {
      this.bossbar.setVisible(visible);
   }

   @Override
   public boolean isVisible() {
      return this.bossbar.isVisible();
   }
}
