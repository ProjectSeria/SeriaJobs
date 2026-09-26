package id.seria.jobs.model;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Mendefinisikan tipe pekerjaan dan binding skill AuraSkills terkait.
 * Bersifat dinamis dan tidak hardcoded, dimuat langsung dari file konfigurasi
 * pada folder jobs/ berdasarkan Job ID dan parameter pekerjaan yang ada.
 */
public class JobType {

    private static final Map<String, JobType> REGISTRY = new ConcurrentHashMap<>();

    private final String id;
    private final String displayName;
    private final String auraSkillName;

    public JobType(@NotNull String id, @NotNull String displayName, @Nullable String auraSkillName) {
        this.id = id.toUpperCase();
        this.displayName = displayName;
        this.auraSkillName = (auraSkillName != null && !auraSkillName.isBlank()) ? auraSkillName : id.toLowerCase();
    }

    @NotNull
    public String getId() {
        return id;
    }

    @NotNull
    public String getDisplayName() {
        return displayName;
    }

    @NotNull
    public String getAuraSkillName() {
        return auraSkillName;
    }

    public static JobType of(@NotNull String id, @NotNull String displayName, @Nullable String auraSkillName) {
        JobType type = new JobType(id, displayName, auraSkillName);
        REGISTRY.put(id.toUpperCase(), type);
        return type;
    }

    @Nullable
    public static JobType get(@Nullable String id) {
        if (id == null) return null;
        return REGISTRY.get(id.trim().toUpperCase());
    }

    @Nullable
    public static JobType fromId(@Nullable String id) {
        return get(id);
    }

    @NotNull
    public static JobType getOrCreate(@NotNull String id, @NotNull String displayName, @Nullable String auraSkillName) {
        return REGISTRY.computeIfAbsent(id.trim().toUpperCase(), k -> new JobType(k, displayName, auraSkillName));
    }

    @NotNull
    public static Collection<JobType> values() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    public static void register(@NotNull JobType type) {
        REGISTRY.put(type.getId(), type);
    }

    public static void clear() {
        REGISTRY.clear();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JobType jobType = (JobType) o;
        return id.equalsIgnoreCase(jobType.id);
    }

    @Override
    public int hashCode() {
        return id.toLowerCase().hashCode();
    }

    @Override
    public String toString() {
        return id;
    }
}
