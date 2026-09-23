package id.seria.gacha.lib.common.utils.configuration.adapter.defaults;

import com.google.common.primitives.Ints;
import id.seria.gacha.lib.common.utils.configuration.adapter.ConfigAdapter;
import id.seria.gacha.lib.common.utils.configuration.exceptions.ErrorException;
import id.seria.gacha.lib.common.utils.configuration.exceptions.WarningException;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationReader;
import id.seria.gacha.lib.common.utils.configuration.handlers.ConfigurationWriter;
import id.seria.gacha.lib.common.utils.configuration.objects.SlotCompound;
import id.seria.gacha.lib.common.utils.utility.StringUtil;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SlotCompoundAdapter extends ConfigAdapter<SlotCompound, String> {
   public SlotCompoundAdapter() {
   }

   public SlotCompound deserialize(ConfigurationReader reader, Type type, Object object) throws ErrorException, WarningException {
      if (!(object instanceof String) && !(object instanceof Integer) && !(object instanceof List)) {
         Class<?> c = object.getClass();
         String className = c.getSimpleName();
         if (className.isEmpty()) {
            className = c.getName();
         }

         throw new WarningException("Expected \"String\" or \"Integer\"  or \"List\", found \"" + className + "\"");
      } else {
         if (object instanceof String && ((String)object).isEmpty()) {
            return new SlotCompound(new int[0]);
         }

         Object[] rawSlots;
         if (object instanceof List) {
            rawSlots = ((List)object).toArray();
         } else {
            rawSlots = object.toString().replace(" ", "").split(",");
         }

         List<Integer> slots = new ArrayList<>();

         for (Object rawSlot : rawSlots) {
            String stringRawSlot = rawSlot.toString();
            if (!stringRawSlot.isEmpty()) {
               String[] range = stringRawSlot.split("-");
               if (range.length == 1) {
                  if (!StringUtil.isInteger(range[0])) {
                     throw new WarningException("Expected \"Integer\", founded \"" + range[0] + "\"");
                  }

                  int slot = Integer.valueOf(range[0]);
                  if (slot < 0) {
                     slot = -1;
                  }

                  slots.add(slot);
               } else {
                  if (range.length != 2) {
                     throw new WarningException("Expected \"Range\" or \"Integer\", founded \"" + stringRawSlot + "\"");
                  }

                  if (!StringUtil.isInteger(range[0])) {
                     throw new WarningException("Expected \"Integer\", founded \"" + range[0] + "\"");
                  }

                  if (!StringUtil.isInteger(range[1])) {
                     throw new WarningException("Expected \"Integer\", founded \"" + range[1] + "\"");
                  }

                  int start = Integer.valueOf(range[0]);
                  int end = Integer.valueOf(range[1]);
                  start = Math.min(start, end);
                  end = Math.max(start, end);
                  if (start >= 0 && end >= 0) {
                     for (int i = start; i <= end; i++) {
                        slots.add(i);
                     }
                  } else {
                     slots.add(-1);
                  }
               }
            }
         }

         return new SlotCompound(Ints.toArray(slots));
      }
   }

   @Deprecated
   public String serialize(ConfigurationWriter writer, SlotCompound compound) {
      int[] slots = compound.asArray();
      if (slots.length == 0) {
         return "";
      }

      Arrays.sort(slots);
      List<String> serializedSlots = new ArrayList<>();
      int start = slots[0];
      int end = slots[0];

      for (int i = 1; i < slots.length; i++) {
         if (slots[i] == end + 1) {
            end = slots[i];
         } else {
            if (start == end) {
               serializedSlots.add(Integer.toString(start));
            } else {
               serializedSlots.add(start + "-" + end);
            }

            start = slots[i];
            end = slots[i];
         }
      }

      if (start == end) {
         serializedSlots.add(Integer.toString(start));
      } else {
         serializedSlots.add(start + "-" + end);
      }

      return String.join(", ", serializedSlots);
   }
}
