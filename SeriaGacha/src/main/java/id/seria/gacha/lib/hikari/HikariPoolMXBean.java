package id.seria.gacha.lib.hikari;

public interface HikariPoolMXBean {
   int getIdleConnections();

   int getActiveConnections();

   int getTotalConnections();

   int getThreadsAwaitingConnection();

   void softEvictConnections();

   void suspendPool();

   void resumePool();
}
