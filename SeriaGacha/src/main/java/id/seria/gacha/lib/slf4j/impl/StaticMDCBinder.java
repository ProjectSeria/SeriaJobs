package id.seria.gacha.lib.slf4j.impl;

import id.seria.gacha.lib.slf4j.helpers.NOPMDCAdapter;
import id.seria.gacha.lib.slf4j.spi.MDCAdapter;

public class StaticMDCBinder {
   public static final StaticMDCBinder SINGLETON = new StaticMDCBinder();
   public static StaticMDCBinder getSingleton() {
      return SINGLETON;
   }

   private StaticMDCBinder() {
   }

   public MDCAdapter getMDCA() {
      return new NOPMDCAdapter();
   }

   public String getMDCAdapterClassStr() {
      return NOPMDCAdapter.class.getName();
   }
}
