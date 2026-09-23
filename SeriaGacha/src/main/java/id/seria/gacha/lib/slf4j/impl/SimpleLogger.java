package id.seria.gacha.lib.slf4j.impl;

import id.seria.gacha.lib.slf4j.helpers.FormattingTuple;
import id.seria.gacha.lib.slf4j.helpers.MarkerIgnoringBase;
import id.seria.gacha.lib.slf4j.helpers.MessageFormatter;

public class SimpleLogger extends MarkerIgnoringBase {
   private static final long serialVersionUID = -6560244151660620173L;
   private static long startTime = System.currentTimeMillis();
   public static final String LINE_SEPARATOR = System.getProperty("line.separator");
   private static String INFO_STR = "INFO";
   private static String WARN_STR = "WARN";
   private static String ERROR_STR = "ERROR";

   SimpleLogger(String name) {
      this.name = name;
   }

   public boolean isTraceEnabled() {
      return false;
   }

   public void trace(String msg) {
   }

   public void trace(String format, Object param1) {
   }

   public void trace(String format, Object param1, Object param2) {
   }

   public void trace(String format, Object[] argArray) {
   }

   public void trace(String msg, Throwable t) {
   }

   public boolean isDebugEnabled() {
      return false;
   }

   public void debug(String msg) {
   }

   public void debug(String format, Object param1) {
   }

   public void debug(String format, Object param1, Object param2) {
   }

   public void debug(String format, Object[] argArray) {
   }

   public void debug(String msg, Throwable t) {
   }

   private void log(String level, String message, Throwable t) {
      StringBuffer buf = new StringBuffer();
      long millis = System.currentTimeMillis();
      buf.append(millis - startTime);
      buf.append(" [");
      buf.append(Thread.currentThread().getName());
      buf.append("] ");
      buf.append(level);
      buf.append(" ");
      buf.append(this.name);
      buf.append(" - ");
      buf.append(message);
      buf.append(LINE_SEPARATOR);
      System.err.print(buf.toString());
      if (t != null) {
         t.printStackTrace(System.err);
      }

      System.err.flush();
   }

   private void formatAndLog(String level, String format, Object arg1, Object arg2) {
      FormattingTuple tp = MessageFormatter.format(format, arg1, arg2);
      this.log(level, tp.getMessage(), tp.getThrowable());
   }

   private void formatAndLog(String level, String format, Object[] argArray) {
      FormattingTuple tp = MessageFormatter.arrayFormat(format, argArray);
      this.log(level, tp.getMessage(), tp.getThrowable());
   }

   public boolean isInfoEnabled() {
      return true;
   }

   public void info(String msg) {
      this.log(INFO_STR, msg, null);
   }

   public void info(String format, Object arg) {
      this.formatAndLog(INFO_STR, format, arg, null);
   }

   public void info(String format, Object arg1, Object arg2) {
      this.formatAndLog(INFO_STR, format, arg1, arg2);
   }

   public void info(String format, Object[] argArray) {
      this.formatAndLog(INFO_STR, format, argArray);
   }

   public void info(String msg, Throwable t) {
      this.log(INFO_STR, msg, t);
   }

   public boolean isWarnEnabled() {
      return true;
   }

   public void warn(String msg) {
      this.log(WARN_STR, msg, null);
   }

   public void warn(String format, Object arg) {
      this.formatAndLog(WARN_STR, format, arg, null);
   }

   public void warn(String format, Object arg1, Object arg2) {
      this.formatAndLog(WARN_STR, format, arg1, arg2);
   }

   public void warn(String format, Object[] argArray) {
      this.formatAndLog(WARN_STR, format, argArray);
   }

   public void warn(String msg, Throwable t) {
      this.log(WARN_STR, msg, t);
   }

   public boolean isErrorEnabled() {
      return true;
   }

   public void error(String msg) {
      this.log(ERROR_STR, msg, null);
   }

   public void error(String format, Object arg) {
      this.formatAndLog(ERROR_STR, format, arg, null);
   }

   public void error(String format, Object arg1, Object arg2) {
      this.formatAndLog(ERROR_STR, format, arg1, arg2);
   }

   public void error(String format, Object[] argArray) {
      this.formatAndLog(ERROR_STR, format, argArray);
   }

   public void error(String msg, Throwable t) {
      this.log(ERROR_STR, msg, t);
   }
}
