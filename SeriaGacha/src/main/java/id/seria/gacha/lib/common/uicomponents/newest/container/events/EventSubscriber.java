package id.seria.gacha.lib.common.uicomponents.newest.container.events;

@FunctionalInterface
public interface EventSubscriber<T> {
   void onEvent(T var1);
}
