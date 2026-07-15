package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the DBA settings page.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SettingsTest extends DBATest {
  /** Default option values (a save persists every field, so all must be supplied). */
  private static final Map<String, String> DEFAULTS = Map.of(
      "timeout", "30", "memory", "1000", "maxchars", "1000000",
      "permission", "admin", "indent", "no", "maxrows", "1000", "ignore-logs", "");

  /**
   * Restores the default settings after each test.
   * @throws IOException I/O exception
   */
  @AfterEach public void restore() throws IOException {
    save(Map.of());
  }

  /**
   * Tests that the settings page is served with the current option values.
   * @throws IOException I/O exception
   */
  @Test public void settingsPage() throws IOException {
    assertTrue(get("settings").contains("<title>DBA"), "not a settings page");
  }

  /**
   * Tests that a changed option is saved and shown on reload.
   * @throws IOException I/O exception
   */
  @Test public void saveRoundTrip() throws IOException {
    assertTrue(save(Map.of("maxrows", "7")).contains("Settings were saved."), "settings not saved");
    assertTrue(get("settings").contains("value=\"7\""), "changed value not persisted");
  }

  /**
   * Saves the settings, overriding individual defaults.
   * @param overrides option values to override
   * @return response body
   * @throws IOException I/O exception
   */
  private static String save(final Map<String, String> overrides) throws IOException {
    final Map<String, String> form = new HashMap<>(DEFAULTS);
    form.putAll(overrides);
    return post("settings-save", form);
  }
}
