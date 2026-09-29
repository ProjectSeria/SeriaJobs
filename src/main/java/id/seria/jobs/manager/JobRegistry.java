package id.seria.jobs.manager;

import id.seria.jobs.SeriaJobs;
import id.seria.jobs.model.Job;
import id.seria.jobs.model.JobOrder;
import id.seria.jobs.model.reward.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class JobRegistry {

    private final SeriaJobs plugin;
    private final Map<String, Job> jobs = new LinkedHashMap<>();

    public JobRegistry(SeriaJobs plugin) {
        this.plugin = plugin;
    }

    /** File job bawaan yang disalin ke plugins/SeriaJobs/jobs/ saat folder masih kosong. */
    private static final String[] DEFAULT_JOBS = {"miner.yml", "farmer.yml", "fisherman.yml", "alchemist.yml"};

    /**
     * Muat ulang semua job dari plugins/SeriaJobs/jobs/*.yml.
     * Bila folder belum ada / kosong, semua job bawaan disalin dulu.
     */
    public void load() {
        jobs.clear();
        id.seria.jobs.model.JobType.clear();
        File jobsFolder = new File(plugin.getDataFolder(), "jobs");
        if (!jobsFolder.exists()) {
            jobsFolder.mkdirs();
            saveDefaultJobs();
        }

        File[] files = jobsFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null || files.length == 0) {
            saveDefaultJobs();
            files = jobsFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        }

        if (files != null) {
            for (File file : files) {
                try {
                    Job job = loadJobFromFile(file);
                    if (job != null) {
                        jobs.put(job.getId().toLowerCase(), job);
                        plugin.getLogger().info("Loaded job: " + job.getId() + " (" + job.getOrdersPool().size() + " orders, " + job.getLevelRewards().size() + " level reward milestones)");
                        warnIfNoOrders(job, file);
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to load job file " + file.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
        plugin.getLogger().info("Successfully loaded " + jobs.size() + " total job(s) from registry.");
    }

    /**
     * Job tanpa orders-pool tidak akan pernah punya Work Order. Biasanya ini terjadi karena
     * file memakai skema lama {@code actions:} (gaya Jobs Reborn) yang tidak dibaca plugin ini.
     */
    private void warnIfNoOrders(Job job, File file) {
        if (!job.getOrdersPool().isEmpty()) return;
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String hint = config.isConfigurationSection("actions")
                ? " File ini memakai skema 'actions:' yang tidak didukung; pindahkan ke 'orders-pool:'."
                : " Tambahkan section 'orders-pool:'.";
        plugin.getLogger().warning("Job '" + job.getId() + "' (" + file.getName() + ") tidak punya Work Order." + hint);
    }

    private void saveDefaultJobs() {
        for (String fileName : DEFAULT_JOBS) {
            saveDefaultJob(fileName);
        }
    }

    private void saveDefaultJob(String fileName) {
        File target = new File(plugin.getDataFolder(), "jobs/" + fileName);
        if (!target.exists()) {
            plugin.saveResource("jobs/" + fileName, false);
        }
    }

    private Job loadJobFromFile(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String id = config.getString("job-name", config.getString("id"));
        if (id == null || id.isEmpty()) {
            id = file.getName().replace(".yml", "").replace(".yaml", "");
        }

        String displayName = config.getString("job-display-name", config.getString("display-name", id));
        String iconName = config.getString("icon", "STONE");
        Material icon = Material.matchMaterial(iconName);
        if (icon == null) icon = Material.BOOK;

        List<String> description = config.getStringList("description");

        int maxLevel = config.getInt("max-level", config.getInt("leveling.max-level", 100));
        int baseXp = config.getInt("leveling.base-xp", 100);
        double multiplier = config.getDouble("leveling.multiplier", 1.25);

        String levelingEquation = config.getString("leveling-progression-equation", null);
        String incomeEquation = config.getString("income-progression-equation", null);
        String pointsEquation = config.getString("points-progression-equation", null);
        String expEquation = config.getString("experience-progression-equation", null);

        int activeOrdersCount = config.getInt("active-orders-count", 3);

        // Parse orders pool
        Map<String, JobOrder> ordersMap = new LinkedHashMap<>();
        ConfigurationSection poolSection = config.getConfigurationSection("orders-pool");

        if (poolSection != null) {
            for (String orderKey : poolSection.getKeys(false)) {
                ConfigurationSection sec = poolSection.getConfigurationSection(orderKey);
                if (sec == null) continue;

                String orderDisplayName = sec.getString("display-name", orderKey);
                List<String> orderLore = sec.getStringList("lore");

                String source = sec.getString("required-item.source", "VANILLA").toUpperCase();
                String matStr = sec.getString("required-item.material");
                Material mat = matStr != null ? Material.matchMaterial(matStr) : null;
                int amount = sec.getInt("required-item.amount", 1);
                boolean strictNbt = sec.getBoolean("required-item.strict-nbt", false);
                Integer customModelData = sec.contains("required-item.custom-model-data")
                        ? sec.getInt("required-item.custom-model-data")
                        : null;

                String mmoType = sec.getString("required-item.mmoitem-type");
                String mmoId = sec.getString("required-item.mmoitem-id");

                String customItemId = sec.getString("required-item.custom-item-id");
                if (customItemId == null) customItemId = sec.getString("required-item.mythic-id");
                if (customItemId == null) customItemId = sec.getString("required-item.fishing-id");
                if (customItemId == null) customItemId = sec.getString("required-item.bundle-id");
                if (customItemId == null) customItemId = sec.getString("required-item.crafting-id");
                if (customItemId == null) customItemId = sec.getString("required-item.item-id");

                List<String> customItemIds = new ArrayList<>();
                if (sec.contains("required-item.custom-item-ids")) {
                    customItemIds.addAll(sec.getStringList("required-item.custom-item-ids"));
                } else if (sec.contains("required-item.fishing-ids")) {
                    customItemIds.addAll(sec.getStringList("required-item.fishing-ids"));
                } else if (sec.contains("required-item.item-ids")) {
                    customItemIds.addAll(sec.getStringList("required-item.item-ids"));
                } else if (sec.contains("required-item.ids")) {
                    customItemIds.addAll(sec.getStringList("required-item.ids"));
                } else if (sec.contains("required-item.mythic-ids")) {
                    customItemIds.addAll(sec.getStringList("required-item.mythic-ids"));
                } else if (sec.contains("required-item.bundle-ids")) {
                    customItemIds.addAll(sec.getStringList("required-item.bundle-ids"));
                } else if (sec.contains("required-item.crafting-ids")) {
                    customItemIds.addAll(sec.getStringList("required-item.crafting-ids"));
                }

                if (customItemIds.isEmpty() && customItemId != null && !customItemId.isEmpty()) {
                    customItemIds.add(customItemId);
                } else if (!customItemIds.isEmpty() && customItemId == null) {
                    customItemId = customItemIds.get(0);
                }

                List<Reward> rewards = parseRewardsList(sec.getMapList("rewards"));
                List<String> customRewardsDisplay = sec.getStringList("display-rewards");
                if (customRewardsDisplay.isEmpty()) {
                    customRewardsDisplay = sec.getStringList("rewards-display");
                }

                int minRep = sec.getInt("min-reputation-amount", sec.getInt("min-reputation", 0));
                int maxRep = sec.getInt("max-reputation-amount", sec.getInt("max-reputation", 0));
                double rewardGins = sec.getDouble("reward-gins", sec.getDouble("gins", 0.0));
                int rewardJobXp = sec.getInt("reward-job-xp", sec.getInt("job-xp", 0));
                double rewardAuraXp = sec.getDouble("reward-aura-xp", sec.getDouble("aura-xp", 0.0));
                double rareDropChance = sec.getDouble("rare-drop-chance", sec.getDouble("rare-chance", 0.0));
                List<String> rewardActions = sec.getStringList("reward-actions");
                if (rewardActions.isEmpty()) {
                    rewardActions = sec.getStringList("actions");
                }

                JobOrder order = new JobOrder(orderKey, id, orderDisplayName, orderLore, source, mat,
                        customModelData, strictNbt, mmoType, mmoId, customItemId, customItemIds, amount, rewards, customRewardsDisplay,
                        minRep, maxRep, rewardGins, rewardJobXp, rewardAuraXp, rareDropChance, rewardActions);
                ordersMap.put(orderKey.toLowerCase(), order);
            }
        }

        // Parse level progression milestone rewards (supports both level-rewards and reward-levels)
        Map<Integer, List<Reward>> levelRewards = new LinkedHashMap<>();
        Map<Integer, List<String>> levelDisplayRewards = new LinkedHashMap<>();
        Map<Integer, ConfigurationSection> levelIcons = new LinkedHashMap<>();

        ConfigurationSection rewardsSec = config.getConfigurationSection("reward-levels");
        if (rewardsSec == null) {
            rewardsSec = config.getConfigurationSection("level-rewards");
        }

        if (rewardsSec != null) {
            for (String lvlKey : rewardsSec.getKeys(false)) {
                try {
                    int lvl = Integer.parseInt(lvlKey);
                    List<Reward> rList = new ArrayList<>();

                    if (rewardsSec.isConfigurationSection(lvlKey)) {
                        ConfigurationSection singleLvl = rewardsSec.getConfigurationSection(lvlKey);
                        if (singleLvl != null) {
                            if (singleLvl.isConfigurationSection("icon")) {
                                levelIcons.put(lvl, singleLvl.getConfigurationSection("icon"));
                            } else if (singleLvl.isConfigurationSection("item")) {
                                levelIcons.put(lvl, singleLvl.getConfigurationSection("item"));
                            }

                            if (singleLvl.contains("display-rewards")) {
                                levelDisplayRewards.put(lvl, singleLvl.getStringList("display-rewards"));
                            }
                            if (singleLvl.contains("commands")) {
                                for (String cmd : singleLvl.getStringList("commands")) {
                                    rList.add(new CommandReward("CONSOLE", cmd));
                                }
                            }
                            if (singleLvl.contains("rewards")) {
                                rList.addAll(parseRewardsList(singleLvl.getMapList("rewards")));
                            }
                        }
                    } else if (rewardsSec.isList(lvlKey)) {
                        rList = parseRewardsList(rewardsSec.getMapList(lvlKey));
                    }

                    if (!rList.isEmpty()) {
                        levelRewards.put(lvl, rList);
                    }
                } catch (NumberFormatException ignored) {}
            }
        }

        String auraSkill = config.getString("aura-skill", config.getString("auraskill", config.getString("aura_skill", config.getString("skill-binding"))));

        return new Job(id, displayName, icon, description, maxLevel, baseXp, multiplier,
                levelingEquation, incomeEquation, pointsEquation, expEquation,
                activeOrdersCount, ordersMap, levelRewards, levelDisplayRewards, levelIcons, auraSkill);
    }

    public List<Reward> parseRewardsList(List<Map<?, ?>> rewardList) {
        List<Reward> rewards = new ArrayList<>();
        if (rewardList == null) return rewards;

        for (Map<?, ?> rMap : rewardList) {
            String type = String.valueOf(rMap.get("type")).toUpperCase();
            String customDisplay = null;
            if (rMap.containsKey("display")) {
                customDisplay = String.valueOf(rMap.get("display"));
            } else if (rMap.containsKey("display-name")) {
                customDisplay = String.valueOf(rMap.get("display-name"));
            } else if (rMap.containsKey("text")) {
                customDisplay = String.valueOf(rMap.get("text"));
            } else if (rMap.containsKey("name")) {
                customDisplay = String.valueOf(rMap.get("name"));
            } else if (rMap.containsKey("lore")) {
                customDisplay = String.valueOf(rMap.get("lore"));
            }

            switch (type) {
                case "VAULT":
                    double vAmount = Double.parseDouble(String.valueOf(rMap.get("amount")));
                    rewards.add(new VaultReward(vAmount, customDisplay));
                    break;
                case "EXCELLENT_ECONOMY":
                case "EXCELLENTECONOMY":
                case "EXCELLENT":
                case "CURRENCY":
                case "COINSENGINE":
                    String currId = rMap.containsKey("currency") ? String.valueOf(rMap.get("currency")) : null;
                    double eAmount = Double.parseDouble(String.valueOf(rMap.get("amount")));
                    rewards.add(new ExcellentEconomyReward(currId, eAmount, customDisplay));
                    break;
                case "JOB_XP":
                    int jXp = Integer.parseInt(String.valueOf(rMap.get("amount")));
                    rewards.add(new JobXPReward(jXp, customDisplay));
                    break;
                case "AURASKILLS_XP":
                    String skill = String.valueOf(rMap.get("skill"));
                    double aXp = Double.parseDouble(String.valueOf(rMap.get("amount")));
                    rewards.add(new AuraSkillsXPReward(skill, aXp, customDisplay));
                    break;
                case "ITEM":
                case "VANILLA":
                    String rMatStr = String.valueOf(rMap.get("material"));
                    Material rMat = Material.matchMaterial(rMatStr);
                    int rAmount = Integer.parseInt(String.valueOf(rMap.get("amount")));
                    String extraMetaKey = null;
                    if (rMap.containsKey("luckperms-bonus-meta")) {
                        extraMetaKey = String.valueOf(rMap.get("luckperms-bonus-meta"));
                    } else if (rMap.containsKey("extra-meta")) {
                        extraMetaKey = String.valueOf(rMap.get("extra-meta"));
                    }
                    rewards.add(new VanillaItemReward(rMat, rAmount, extraMetaKey, customDisplay));
                    break;
                case "MMOITEM":
                    String rMmoType = String.valueOf(rMap.get("mmoitem-type"));
                    String rMmoId = String.valueOf(rMap.get("mmoitem-id"));
                    int rMmoAmount = Integer.parseInt(String.valueOf(rMap.get("amount")));
                    rewards.add(new MMOItemReward(rMmoType, rMmoId, rMmoAmount, customDisplay));
                    break;
                case "MYTHICITEM":
                case "MYTHICMOBS":
                case "MYTHIC": {
                    Object idObj = rMap.get("item-id");
                    if (idObj == null) idObj = rMap.get("mythic-id");
                    if (idObj == null) idObj = rMap.get("id");
                    String itemId = String.valueOf(idObj);
                    Object amtObj = rMap.get("amount");
                    int rAmt = amtObj != null ? Integer.parseInt(String.valueOf(amtObj)) : 1;
                    rewards.add(new MythicItemReward(itemId, rAmt, customDisplay));
                    break;
                }
                case "CUSTOMFISHING":
                case "FISHING": {
                    Object idObj = rMap.get("item-id");
                    if (idObj == null) idObj = rMap.get("fishing-id");
                    if (idObj == null) idObj = rMap.get("id");
                    String itemId = String.valueOf(idObj);
                    Object amtObj = rMap.get("amount");
                    int rAmt = amtObj != null ? Integer.parseInt(String.valueOf(amtObj)) : 1;
                    rewards.add(new CustomFishingReward(itemId, rAmt, customDisplay));
                    break;
                }
                case "SERIAMAGICBUNDLE":
                case "MAGICBUNDLE":
                case "BUNDLE": {
                    Object idObj = rMap.get("item-id");
                    if (idObj == null) idObj = rMap.get("bundle-id");
                    if (idObj == null) idObj = rMap.get("id");
                    String itemId = String.valueOf(idObj);
                    Object amtObj = rMap.get("amount");
                    int rAmt = amtObj != null ? Integer.parseInt(String.valueOf(amtObj)) : 1;
                    rewards.add(new SeriaMagicBundleReward(itemId, rAmt, customDisplay));
                    break;
                }
                case "SERIACRAFTING":
                case "CRAFTING": {
                    Object idObj = rMap.get("item-id");
                    if (idObj == null) idObj = rMap.get("crafting-id");
                    if (idObj == null) idObj = rMap.get("id");
                    String itemId = String.valueOf(idObj);
                    Object amtObj = rMap.get("amount");
                    int rAmt = amtObj != null ? Integer.parseInt(String.valueOf(amtObj)) : 1;
                    rewards.add(new SeriaCraftingReward(itemId, rAmt, customDisplay));
                    break;
                }
                case "COMMAND":
                    String exec = rMap.containsKey("executor") ? String.valueOf(rMap.get("executor")) : "CONSOLE";
                    String cmd = String.valueOf(rMap.get("command"));
                    rewards.add(new CommandReward(exec, cmd, customDisplay));
                    break;
            }
        }
        return rewards;
    }

    public Collection<Job> getJobs() {
        return jobs.values();
    }

    public Collection<Job> getAllJobs() {
        return jobs.values();
    }

    public Job getJob(String id) {
        if (id == null) return null;
        return jobs.get(id.toLowerCase());
    }

    public boolean hasJob(String id) {
        if (id == null) return false;
        return jobs.containsKey(id.toLowerCase());
    }
}
