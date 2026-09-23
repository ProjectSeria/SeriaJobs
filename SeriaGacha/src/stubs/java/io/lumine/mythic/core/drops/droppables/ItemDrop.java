package io.lumine.mythic.core.drops.droppables;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.drops.DropMetadata;
public class ItemDrop {
    public ItemDrop(String line, MythicLineConfig config) {}
    public int rollBonuses(DropMetadata meta, int amount) { return amount; }
}