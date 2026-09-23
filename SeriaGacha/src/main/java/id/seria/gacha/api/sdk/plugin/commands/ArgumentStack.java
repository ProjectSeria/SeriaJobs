package id.seria.gacha.api.sdk.plugin.commands;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.Generated;

public class ArgumentStack {
   private final LinkedList<String> arguments;
   private final LinkedList<String> popAudit;

   public ArgumentStack(List<String> args) {
      this.arguments = new LinkedList<>(args);
      this.popAudit = new LinkedList<>();
   }

   public String pop() {
      if (this.arguments.isEmpty()) {
         throw new NoSuchElementException("No more arguments to pop.");
      }

      String popped = this.arguments.pollFirst();
      this.popAudit.add(popped);
      return popped;
   }

   public String peek() {
      if (this.arguments.isEmpty()) {
         throw new NoSuchElementException("No arguments available to peek.");
      } else {
         return this.arguments.getFirst();
      }
   }

   public boolean hasNext() {
      return !this.arguments.isEmpty();
   }

   public List<String> getPopAudit() {
      return new LinkedList<>(this.popAudit);
   }

   public void clearPopAudit() {
      this.popAudit.clear();
   }

   @Generated
   public LinkedList<String> getArguments() {
      return this.arguments;
   }
}
