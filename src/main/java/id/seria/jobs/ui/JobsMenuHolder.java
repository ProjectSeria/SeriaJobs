package id.seria.jobs.ui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Pembeda instance GUI Jobs dari inventory container standar atau plugin lain.
 */
public class JobsMenuHolder implements InventoryHolder {

    private final UUID playerUuid;
    private final String jobId;
    private Inventory inventory;

    public JobsMenuHolder(@NotNull UUID playerUuid, @NotNull String jobId) {
        this.playerUuid = playerUuid;
        this.jobId = jobId;
    }

    public JobsMenuHolder(@NotNull UUID playerUuid) {
        this(playerUuid, "");
    }

    @NotNull
    public UUID getPlayerUuid() {
        return playerUuid;
    }

    @NotNull
    public String getJobId() {
        return jobId;
    }

    public void setInventory(@NotNull Inventory inventory) {
        this.inventory = inventory;
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }
}
