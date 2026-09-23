package id.seria.gacha.lib.book;

import id.seria.gacha.lib.book.internals.BookHelper;
import id.seria.gacha.lib.book.internals.NmsBookHelper;
import id.seria.gacha.lib.book.internals.SpigotBookHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.BookMeta.Generation;

public final class BookUtil {
   private static final boolean canTranslateDirectly;
   private static final BookHelper helper;

   public BookUtil() {
   }

   public static void openPlayer(Player p, ItemStack book) {
      CustomBookOpenEvent event = new CustomBookOpenEvent(p, book, false);
      Bukkit.getPluginManager().callEvent(event);
      if (!event.isCancelled()) {
         p.closeInventory();
         ItemStack hand = p.getItemInHand();
         p.setItemInHand(event.getBook());
         p.updateInventory();
         helper.openBook(p, event.getBook());
         p.setItemInHand(hand);
         p.updateInventory();
      }
   }

   public static BookUtil.BookBuilder writtenBook() {
      return new BookUtil.BookBuilder(new ItemStack(Material.WRITTEN_BOOK));
   }

   static {
      boolean success = true;

      try {
         ChatColor.BLACK.asBungee();
      } catch (NoSuchMethodError e) {
         success = false;
      }

      canTranslateDirectly = success;
      if (SpigotBookHelper.isSupported()) {
         helper = new SpigotBookHelper();
      } else {
         helper = new NmsBookHelper();
      }
   }

   public static class BookBuilder {
      private final BookMeta meta;
      private final ItemStack book;

      public BookBuilder(ItemStack book) {
         this.book = book;
         this.meta = (BookMeta)book.getItemMeta();
      }

      public BookUtil.BookBuilder title(String title) {
         if (title.length() > 32) {
            throw new IllegalArgumentException("The book title must be at most 32 characters");
         }

         this.meta.setTitle(title);
         return this;
      }

      public BookUtil.BookBuilder author(String author) {
         this.meta.setAuthor(author);
         return this;
      }

      public BookUtil.BookBuilder pagesRaw(String... pages) {
         this.meta.setPages(pages);
         return this;
      }

      public BookUtil.BookBuilder pagesRaw(List<String> pages) {
         this.meta.setPages(pages);
         return this;
      }

      public BookUtil.BookBuilder pages(BaseComponent[]... pages) {
         BookUtil.helper.setPages(this.meta, pages);
         return this;
      }

      public BookUtil.BookBuilder pages(List<BaseComponent[]> pages) {
         BookUtil.helper.setPages(this.meta, pages.toArray(new BaseComponent[0][]));
         return this;
      }

      public BookUtil.BookBuilder generation(Generation generation) {
         this.meta.setGeneration(generation);
         return this;
      }

      public ItemStack build() {
         if (!this.meta.hasAuthor()) {
            this.meta.setAuthor("");
         }

         if (!this.meta.hasTitle()) {
            this.meta.setTitle("");
         }

         if (!this.meta.hasPages()) {
            this.pages(new BaseComponent[0]);
         }

         this.book.setItemMeta(this.meta);
         return this.book;
      }
   }

   public interface ClickAction {
      Action action();

      String value();

      static BookUtil.ClickAction runCommand(String command) {
         return new BookUtil.ClickAction.SimpleClickAction(Action.RUN_COMMAND, command);
      }

      @Deprecated
      static BookUtil.ClickAction suggestCommand(String command) {
         return new BookUtil.ClickAction.SimpleClickAction(Action.SUGGEST_COMMAND, command);
      }

      static BookUtil.ClickAction openUrl(String url) {
         if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Invalid url: \"" + url + "\", it should start with http:// or https://");
         } else {
            return new BookUtil.ClickAction.SimpleClickAction(Action.OPEN_URL, url);
         }
      }

      static BookUtil.ClickAction changePage(int page) {
         return new BookUtil.ClickAction.SimpleClickAction(Action.CHANGE_PAGE, Integer.toString(page));
      }

      class SimpleClickAction implements BookUtil.ClickAction {
         private final Action action;
         private final String value;

         @Override
         public Action action() {
            return this.action;
         }

         @Override
         public String value() {
            return this.value;
         }

         public SimpleClickAction(Action action, String value) {
            this.action = action;
            this.value = value;
         }
      }
   }

   public interface HoverAction {
      net.md_5.bungee.api.chat.HoverEvent.Action action();

      BaseComponent[] value();

      static BookUtil.HoverAction showText(BaseComponent... text) {
         return new BookUtil.HoverAction.SimpleHoverAction(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, text);
      }

      static BookUtil.HoverAction showText(String text) {
         return new BookUtil.HoverAction.SimpleHoverAction(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new TextComponent(text));
      }

      static BookUtil.HoverAction showItem(BaseComponent... item) {
         return new BookUtil.HoverAction.SimpleHoverAction(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM, item);
      }

      static BookUtil.HoverAction showItem(ItemStack item) {
         return new BookUtil.HoverAction.SimpleHoverAction(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ITEM, BookUtil.helper.itemToComponents(item));
      }

      static BookUtil.HoverAction showEntity(BaseComponent... entity) {
         return new BookUtil.HoverAction.SimpleHoverAction(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ENTITY, entity);
      }

      static BookUtil.HoverAction showEntity(UUID uuid, String type, String name) {
         return new BookUtil.HoverAction.SimpleHoverAction(
            net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ENTITY, new TextComponent("{id:\"" + uuid + "\",type:\"" + type + "\"name:\"" + name + "\"}")
         );
      }

      static BookUtil.HoverAction showEntity(Entity entity) {
         return showEntity(entity.getUniqueId(), entity.getType().getName(), entity.getName());
      }

      static BookUtil.HoverAction showAchievement(String achievementId) {
         return new BookUtil.HoverAction.SimpleHoverAction(
            net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponent("achievement." + achievementId)
         );
      }

      static BookUtil.HoverAction showStatistic(String statisticId) {
         return new BookUtil.HoverAction.SimpleHoverAction(
            net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_ACHIEVEMENT, new TextComponent("statistic." + statisticId)
         );
      }

      class SimpleHoverAction implements BookUtil.HoverAction {
         private final net.md_5.bungee.api.chat.HoverEvent.Action action;
         private final BaseComponent[] value;

         public SimpleHoverAction(net.md_5.bungee.api.chat.HoverEvent.Action action, BaseComponent... value) {
            this.action = action;
            this.value = value;
         }

         @Override
         public net.md_5.bungee.api.chat.HoverEvent.Action action() {
            return this.action;
         }

         @Override
         public BaseComponent[] value() {
            return this.value;
         }
      }
   }

   public static class PageBuilder {
      private List<BaseComponent> text = new ArrayList<>();

      public PageBuilder() {
      }

      public BookUtil.PageBuilder add(String text) {
         this.text.add(BookUtil.TextBuilder.of(text).build());
         return this;
      }

      public BookUtil.PageBuilder add(BaseComponent component) {
         this.text.add(component);
         return this;
      }

      public BookUtil.PageBuilder add(BaseComponent... components) {
         this.text.addAll(Arrays.asList(components));
         return this;
      }

      public BookUtil.PageBuilder add(Collection<BaseComponent> components) {
         this.text.addAll(components);
         return this;
      }

      public BookUtil.PageBuilder newLine() {
         this.text.add(new TextComponent("\n"));
         return this;
      }

      public BaseComponent[] build() {
         return this.text.toArray(new BaseComponent[0]);
      }

      public static BookUtil.PageBuilder of(String text) {
         return new BookUtil.PageBuilder().add(text);
      }

      public static BookUtil.PageBuilder of(BaseComponent text) {
         return new BookUtil.PageBuilder().add(text);
      }

      public static BookUtil.PageBuilder of(BaseComponent... text) {
         BookUtil.PageBuilder res = new BookUtil.PageBuilder();

         for (BaseComponent b : text) {
            res.add(b);
         }

         return res;
      }
   }

   public static class TextBuilder {
      private String text = "";
      private BookUtil.ClickAction onClick = null;
      private BookUtil.HoverAction onHover = null;
      private ChatColor color = ChatColor.BLACK;
      private ChatColor[] style;

      public TextBuilder() {
      }

      public BookUtil.TextBuilder color(ChatColor color) {
         if (color != null && !color.isColor()) {
            throw new IllegalArgumentException("Argument isn't a color!");
         }

         this.color = color;
         return this;
      }

      public BookUtil.TextBuilder style(ChatColor... style) {
         for (ChatColor c : style) {
            if (!c.isFormat()) {
               throw new IllegalArgumentException("Argument isn't a style!");
            }
         }

         this.style = style;
         return this;
      }

      public BaseComponent build() {
         TextComponent res = new TextComponent(this.text);
         if (this.onClick != null) {
            res.setClickEvent(new ClickEvent(this.onClick.action(), this.onClick.value()));
         }

         if (this.onHover != null) {
            res.setHoverEvent(new HoverEvent(this.onHover.action(), this.onHover.value()));
         }

         if (this.color != null) {
            if (BookUtil.canTranslateDirectly) {
               res.setColor(this.color.asBungee());
            } else {
               res.setColor(net.md_5.bungee.api.ChatColor.getByChar(this.color.getChar()));
            }
         }

         if (this.style != null) {
            for (ChatColor c : this.style) {
               switch (c) {
                  case MAGIC:
                     res.setObfuscated(true);
                     break;
                  case BOLD:
                     res.setBold(true);
                     break;
                  case STRIKETHROUGH:
                     res.setStrikethrough(true);
                     break;
                  case UNDERLINE:
                     res.setUnderlined(true);
                     break;
                  case ITALIC:
                     res.setItalic(true);
               }
            }
         }

         return res;
      }

      public static BookUtil.TextBuilder of(String text) {
         return new BookUtil.TextBuilder().text(text);
      }

      public BookUtil.TextBuilder text(String text) {
         this.text = text;
         return this;
      }

      public BookUtil.TextBuilder onClick(BookUtil.ClickAction onClick) {
         this.onClick = onClick;
         return this;
      }

      public BookUtil.TextBuilder onHover(BookUtil.HoverAction onHover) {
         this.onHover = onHover;
         return this;
      }

      public String text() {
         return this.text;
      }

      public BookUtil.ClickAction onClick() {
         return this.onClick;
      }

      public BookUtil.HoverAction onHover() {
         return this.onHover;
      }

      public ChatColor color() {
         return this.color;
      }
   }
}
