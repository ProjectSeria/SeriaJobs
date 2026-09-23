package id.seria.gacha.lib.common.utils.objects;

import java.util.ArrayList;
import java.util.List;
import lombok.Generated;
import org.bukkit.Bukkit;

public class BasicLogger {
   private final List<String> messages = new ArrayList<>();
   private int flushedCount;
   private boolean silent;

   public BasicLogger() {
   }

   public void push(String message) throws Exception {
      synchronized (this.messages) {
         this.messages.add(message);
      }
   }

   public void pushAndFlush(String message) throws Exception {
      this.push(message);
      this.flush();
   }

   public void pop() {
      synchronized (this.messages) {
         this.messages.remove(this.messages.size() - 1);
      }
   }

   public void flush() {
      synchronized (this.messages) {
         for (String message : this.messages) {
            this.log(message);
         }

         this.messages.clear();
      }
   }

   public void log(String message) {
      this.flushedCount++;
      if (!this.silent) {
         Bukkit.getConsoleSender().sendMessage(message);
      }
   }

   public int countQueue() {
      synchronized (this.messages) {
         return this.messages.size();
      }
   }

   public int countFlushed() {
      synchronized (this.messages) {
         return this.flushedCount;
      }
   }

   public void clear() {
      synchronized (this.messages) {
         this.messages.clear();
      }
   }

   public void reset() {
      synchronized (this.messages) {
         this.clear();
         this.flushedCount = 0;
      }
   }

   @Generated
   public boolean isSilent() {
      return this.silent;
   }

   @Generated
   public void setSilent(boolean silent) {
      this.silent = silent;
   }
}
