package io.github.udayhe.quicksilver;

import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

/**
 * Test suite to run all Quicksilver tests
 */
@Suite
@SelectPackages({
    "io.github.udayhe.quicksilver.db",
    "io.github.udayhe.quicksilver.command", 
    "io.github.udayhe.quicksilver.cluster",
    "io.github.udayhe.quicksilver.integration",
    "io.github.udayhe.quicksilver.performance"
})
public class AllTests {
    // This class serves as a test suite runner
    // It will automatically discover and run all tests in the specified packages
}
