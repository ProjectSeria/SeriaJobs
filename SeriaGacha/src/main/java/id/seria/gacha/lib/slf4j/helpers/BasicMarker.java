package id.seria.gacha.lib.slf4j.helpers;

import id.seria.gacha.lib.slf4j.Marker;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

public class BasicMarker implements Marker {
   private static final long serialVersionUID = 1803952589649545191L;
   private final String name;
   private List<Marker> referenceList;
   private static String OPEN = "[ ";
   private static String CLOSE = " ]";
   private static String SEP = ", ";

   BasicMarker(String name) {
      if (name == null) {
         throw new IllegalArgumentException("A marker name cannot be null");
      }

      this.name = name;
   }

   @Override
   public String getName() {
      return this.name;
   }

   @Override
   public synchronized void add(Marker reference) {
      if (reference == null) {
         throw new IllegalArgumentException("A null value cannot be added to a Marker as reference.");
      }

      if (!this.contains(reference)) {
         if (!reference.contains(this)) {
            if (this.referenceList == null) {
               this.referenceList = new Vector<>();
            }

            this.referenceList.add(reference);
         }
      }
   }

   @Override
   public synchronized boolean hasReferences() {
      return this.referenceList != null && this.referenceList.size() > 0;
   }

   @Override
   public boolean hasChildren() {
      return this.hasReferences();
   }

   @Override
   public synchronized Iterator<Marker> iterator() {
      if (this.referenceList != null) {
         return this.referenceList.iterator();
      }

      List<Marker> emptyList = Collections.emptyList();
      return emptyList.iterator();
   }

   @Override
   public synchronized boolean remove(Marker referenceToRemove) {
      if (this.referenceList == null) {
         return false;
      }

      int size = this.referenceList.size();

      for (int i = 0; i < size; i++) {
         Marker m = this.referenceList.get(i);
         if (referenceToRemove.equals(m)) {
            this.referenceList.remove(i);
            return true;
         }
      }

      return false;
   }

   @Override
   public boolean contains(Marker other) {
      if (other == null) {
         throw new IllegalArgumentException("Other cannot be null");
      }

      if (this.equals(other)) {
         return true;
      }

      if (this.hasReferences()) {
         for (Marker ref : this.referenceList) {
            if (ref.contains(other)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean contains(String name) {
      if (name == null) {
         throw new IllegalArgumentException("Other cannot be null");
      }

      if (this.name.equals(name)) {
         return true;
      }

      if (this.hasReferences()) {
         for (Marker ref : this.referenceList) {
            if (ref.contains(name)) {
               return true;
            }
         }
      }

      return false;
   }

   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (!(obj instanceof Marker)) {
         return false;
      }

      Marker other = (Marker)obj;
      return this.name.equals(other.getName());
   }

   @Override
   public int hashCode() {
      return this.name.hashCode();
   }

   @Override
   public String toString() {
      if (!this.hasReferences()) {
         return this.getName();
      }

      Iterator<Marker> it = this.iterator();
      StringBuilder sb = new StringBuilder(this.getName());
      sb.append(' ').append(OPEN);

      while (it.hasNext()) {
         Marker reference = it.next();
         sb.append(reference.getName());
         if (it.hasNext()) {
            sb.append(SEP);
         }
      }

      sb.append(CLOSE);
      return sb.toString();
   }
}
