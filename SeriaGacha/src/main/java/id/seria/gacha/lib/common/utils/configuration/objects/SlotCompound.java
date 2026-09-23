package id.seria.gacha.lib.common.utils.configuration.objects;

import com.google.common.primitives.Ints;
import id.seria.gacha.lib.common.utils.configuration.annotations.ConfigField;
import id.seria.gacha.lib.common.utils.configuration.annotations.Serializable;
import id.seria.gacha.lib.common.utils.utility.InventoryUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import lombok.Generated;
import org.jetbrains.annotations.NotNull;

@Serializable
public class SlotCompound implements Cloneable, Iterable<Integer> {
   @ConfigField
   private int[] slots = new int[0];

   public static SlotCompound fromGrid(int start, int rows, int cols) {
      int[] slots = new int[rows * cols];
      int[] xy = InventoryUtil.getXYPosition(start);
      int startX = xy[0];
      int startY = xy[1];
      int i = 0;

      for (int y = 0; y < rows; y++) {
         int yy = Math.min(startY + y, 6);

         for (int x = 0; x < cols; x++) {
            int xx = Math.min(startX + x, 9);
            slots[i] = InventoryUtil.getPositionRaw(xx, yy);
            i++;
         }
      }

      return new SlotCompound(slots);
   }

   public static SlotCompound from(int x, int y) {
      return new SlotCompound(InventoryUtil.getPositionRaw(x, y));
   }

   public static SlotCompound fromRange(int startSlot, int endSlot) {
      return new SlotCompound(startSlot, endSlot);
   }

   public static SlotCompound fromRange(int startX, int startY, int endX, int endY) {
      return new SlotCompound(InventoryUtil.getPositionRaw(startX, startY), InventoryUtil.getPositionRaw(endX, endY));
   }

   public SlotCompound(List<Integer> list) {
      this.slots = Ints.toArray(list);
   }

   public SlotCompound(int slot) {
      this.slots = new int[]{slot};
   }

   public SlotCompound(int start, int end) {
      this.slots = new int[end - start + 1];
      int i = 0;

      for (int s = start; s <= end; i++) {
         this.slots[i] = s++;
      }
   }

   public SlotCompound(int[] slots) {
      this.slots = Arrays.copyOf(slots, slots.length);
   }

   public int length() {
      return this.slots.length;
   }

   public int[] asArray() {
      return this.slots;
   }

   public SlotCompound with(SlotCompound compound) {
      Set<Integer> hash = new HashSet<>();

      for (int slot : this.slots) {
         hash.add(slot);
      }

      for (int slot : compound.slots) {
         hash.add(slot);
      }

      List<Integer> temp = new ArrayList<>(hash);
      Collections.sort(temp);
      this.slots = temp.stream().mapToInt(i -> i).toArray();
      return this;
   }

   public int getFirst() {
      return this.slots.length == 0 ? -1 : this.slots[0];
   }

   public int getLast() {
      return this.slots.length == 0 ? -1 : this.slots[this.slots.length - 1];
   }

   public List<int[]> horizontalGroups() {
      Arrays.sort(this.slots);
      List<int[]> consecutiveSlots = new ArrayList<>();
      List<Integer> currentRange = new ArrayList<>();
      int currentRow = -1;

      for (int slot : this.slots) {
         int row = slot / 9;
         if (currentRow != -1 && (row != currentRow || slot != currentRange.get(currentRange.size() - 1) + 1)) {
            consecutiveSlots.add(currentRange.stream().mapToInt(Integer::intValue).toArray());
            currentRange.clear();
            currentRange.add(slot);
         } else {
            currentRange.add(slot);
         }

         currentRow = row;
      }

      if (!currentRange.isEmpty()) {
         consecutiveSlots.add(currentRange.stream().mapToInt(Integer::intValue).toArray());
      }

      return consecutiveSlots;
   }

   public List<int[]> verticalGroups() {
      Arrays.sort(this.slots);
      List<Integer> sortedByColumn = new ArrayList<>(this.slots.length);

      for (int col = 0; col < 9; col++) {
         for (int slot : this.slots) {
            if (slot % 9 == col) {
               sortedByColumn.add(slot);
            }
         }
      }

      List<int[]> consecutiveSlots = new ArrayList<>();
      List<Integer> currentRange = new ArrayList<>();
      int currentColumn = -1;

      for (int slot : sortedByColumn) {
         int column = slot % 9;
         if (currentColumn != -1 && (column != currentColumn || slot != currentRange.get(currentRange.size() - 1) + 9)) {
            consecutiveSlots.add(currentRange.stream().mapToInt(Integer::intValue).toArray());
            currentRange.clear();
            currentRange.add(slot);
         } else {
            currentRange.add(slot);
         }

         currentColumn = column;
      }

      if (!currentRange.isEmpty()) {
         consecutiveSlots.add(currentRange.stream().mapToInt(Integer::intValue).toArray());
      }

      return consecutiveSlots;
   }

   public Size getSize() {
      int minX = Integer.MAX_VALUE;
      int minY = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int maxY = Integer.MIN_VALUE;

      for (int slot : this.slots) {
         int[] xy = InventoryUtil.getXYPosition(slot);
         minX = Math.min(minX, xy[0]);
         minY = Math.min(minY, xy[1]);
         maxX = Math.max(maxX, xy[0]);
         maxY = Math.max(maxY, xy[1]);
      }

      int rows = maxY - minY + 1;
      int columns = maxX - minX + 1;
      return new Size(columns, rows);
   }

   public boolean contains(int targetSlot) {
      for (int slot : this.slots) {
         if (slot == targetSlot) {
            return true;
         }
      }

      return false;
   }

   public SlotCompound clone() {
      return new SlotCompound((int[])this.slots.clone());
   }

   @NotNull
   @Override
   public Iterator<Integer> iterator() {
      return IntStream.of(this.slots).iterator();
   }

   @Generated
   @Override
   public String toString() {
      return "SlotCompound(slots=" + Arrays.toString(this.slots) + ")";
   }

   @Generated
   public void setSlots(int[] slots) {
      this.slots = slots;
   }

   @Generated
   public SlotCompound() {
   }
}
