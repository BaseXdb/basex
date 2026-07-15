package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the DBA database pages.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DatabasesTest extends DBATest {
  /** Test database. */
  private static final String DB = "dba-junit-test";

  /**
   * Drops the test database after each test.
   * @throws IOException I/O exception
   */
  @AfterEach public void cleanup() throws IOException {
    post("dbs-drop", Map.of("name", DB));
  }

  /**
   * Tests that the databases overview page is served.
   * @throws IOException I/O exception
   */
  @Test public void listPage() throws IOException {
    assertTrue(get("databases").contains("<html"), "expected an HTML page");
  }

  /**
   * Tests a create/list/drop round-trip.
   * @throws IOException I/O exception
   */
  @Test public void createAndDrop() throws IOException {
    assertTrue(post("db-create", Map.of("name", DB, "do", "do", "lang", "en")).contains(DB),
        "new database not shown");
    assertTrue(get("databases").contains(DB), "database missing from list");
    post("dbs-drop", Map.of("name", DB));
    assertFalse(get("databases").contains(DB), "database still listed after drop");
  }
}
