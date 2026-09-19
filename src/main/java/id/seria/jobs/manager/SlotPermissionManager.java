package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.JobProfile;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class SlotPermissionManager {

    private final SeriaJobs plugin;

    public SlotPermissionManager(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    /**
     * Calculates the maximum jobs a player can join concurrently.
     * Returns -1 for unlimited (e.g. seriajobs.slots.* or seriajobs.slots.all).
     */
    public int getMaxSlots(Player player) {
        if (player == null) return 1;

        if (player.hasPermission("seriajobs.slots.*") || player.hasPermission("seriajobs.slots.all") || player.isOp()) {
            return -1;
        }

        if (!plugin.getConfigManager().isPermissionSlotsEnabled()) {
            return plugin.getConfigManager().getDefaultSlots();
        }

        int highestSlot = plugin.getConfigManager().getDefaultSlots();

        for (PermissionAttachmentInfo pai : player.getEffectivePermissions()) {
            String perm = pai.getPermission().toLowerCase();
            if (perm.startsWith("seriajobs.slots.") && pai.getValue()) {
                String val = perm.substring("seriajobs.slots.".length());
                try {
                    int slotVal = Integer.parseInt(val);
                    if (slotVal > highestSlot) {
                        highestSlot = slotVal;
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        return highestSlot;
    }

    public boolean canJoinJob(Player player, JobProfile profile) {
        int max = getMaxSlots(player);
        if (max == -1) return true;
        return profile.getActiveJobs().size() < max;
    }
}
