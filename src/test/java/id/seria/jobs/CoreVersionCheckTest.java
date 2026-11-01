package id.seria.jobs;

import id.seria.jobs.utils.CoreVersionCheck;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreVersionCheckTest {

    @Test
    void sameOrNewerVersionPasses() {
        assertTrue(CoreVersionCheck.isAtLeast("2.4.0", "2.4.0"));
        assertTrue(CoreVersionCheck.isAtLeast("2.4.1", "2.4.0"));
        assertTrue(CoreVersionCheck.isAtLeast("2.10.0", "2.4.0"));
        assertTrue(CoreVersionCheck.isAtLeast("3.0.0", "2.4.0"));
    }

    @Test
    void snapshotSuffixIgnored() {
        assertTrue(CoreVersionCheck.isAtLeast("2.4.0-SNAPSHOT", "2.4.0"));
        assertTrue(CoreVersionCheck.isAtLeast("2.4.0", "2.4.0-SNAPSHOT"));
    }

    @Test
    void olderVersionFails() {
        // Jar lama dengan plugin.yml hardcode '2.0.0'
        assertFalse(CoreVersionCheck.isAtLeast("2.0.0", "2.4.0"));
        assertFalse(CoreVersionCheck.isAtLeast("2.3.1", "2.4.0"));
        assertFalse(CoreVersionCheck.isAtLeast("1.9.9", "2.4.0"));
    }

    @Test
    void shortVersionsTreatedAsZeroPadded() {
        assertTrue(CoreVersionCheck.isAtLeast("2.4", "2.4.0"));
        assertFalse(CoreVersionCheck.isAtLeast("2", "2.4.0"));
    }
}
