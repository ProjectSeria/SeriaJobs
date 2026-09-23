package id.seria.gacha.lib.xseries.profiles.mojang;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.time.Duration;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jetbrains.annotations.Unmodifiable;
import org.jetbrains.annotations.ApiStatus.Internal;

@Internal
public final class RateLimiter {
   private final ConcurrentLinkedQueue<Long> requests = new ConcurrentLinkedQueue<>();
   private final int maxRequests;
   private final long per;

   RateLimiter(int maxRequests, Duration per) {
      this.maxRequests = maxRequests;
      this.per = per.toMillis();
   }

   @CanIgnoreReturnValue
   private @Unmodifiable ConcurrentLinkedQueue<Long> getRequests() {
      if (this.requests.isEmpty()) {
         return this.requests;
      }

      long now = System.currentTimeMillis();
      Iterator<Long> iter = this.requests.iterator();

      while (iter.hasNext()) {
         long requestedAt = iter.next();
         long diff = now - requestedAt;
         if (diff <= this.per) {
            break;
         }

         iter.remove();
      }

      return this.requests;
   }

   public int getRemainingRequests() {
      return Math.max(0, this.maxRequests - this.getRequests().size());
   }

   public int getEffectiveRequestsCount() {
      return this.getRequests().size();
   }

   public void instantRateLimit() {
      long now = System.currentTimeMillis();

      for (int i = 0; i < this.getRemainingRequests(); i++) {
         this.requests.add(now);
      }
   }

   public boolean acquire() {
      if (this.getRemainingRequests() <= 0) {
         return false;
      }

      this.requests.add(System.currentTimeMillis());
      return true;
   }

   public Duration timeUntilNextFreeRequest() {
      if (this.getRemainingRequests() == 0) {
         long now = System.currentTimeMillis();
         long oldestRequestedAt = this.requests.peek();
         long diff = now - oldestRequestedAt;
         return Duration.ofMillis(this.per - diff);
      } else {
         return Duration.ZERO;
      }
   }

   public synchronized void acquireOrWait() {
      long sleepUntil = this.timeUntilNextFreeRequest().toMillis();
      if (sleepUntil != 0L) {
         try {
            Thread.sleep(sleepUntil);
         } catch (InterruptedException e) {
            throw new IllegalStateException("RateLimiter lock was interrupted unexpectedly", e);
         }
      }
   }

   @Override
   public String toString() {
      return this.getClass().getSimpleName()
         + "[total="
         + this.getRequests().size()
         + ", remaining="
         + this.getRemainingRequests()
         + ", maxRequests="
         + this.maxRequests
         + ", per="
         + this.per
         + ']';
   }
}
