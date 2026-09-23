package id.seria.gacha.lib.slf4j.event;

import id.seria.gacha.lib.slf4j.Marker;

public interface LoggingEvent {
   Level getLevel();

   Marker getMarker();

   String getLoggerName();

   String getMessage();

   String getThreadName();

   Object[] getArgumentArray();

   long getTimeStamp();

   Throwable getThrowable();
}
