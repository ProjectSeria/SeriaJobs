package id.seria.gacha.lib.common.utils.objects;

import lombok.Generated;

public class Pair<A, B> {
   public A first;
   public B second;

   public static <P, Q> Pair<P, Q> of(P p, Q q) {
      return new Pair<>(p, q);
   }

   public Pair(A first, B second) {
      this.first = first;
      this.second = second;
   }

   @Generated
   public void setFirst(A first) {
      this.first = first;
   }

   @Generated
   public void setSecond(B second) {
      this.second = second;
   }

   @Generated
   public A getFirst() {
      return this.first;
   }

   @Generated
   public B getSecond() {
      return this.second;
   }
}
