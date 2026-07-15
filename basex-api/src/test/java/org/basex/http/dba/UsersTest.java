package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the DBA user pages.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UsersTest extends DBATest {
  /** Test user. */
  private static final String USER = "dba-junit-user";

  /**
   * Drops the test user after each test.
   * @throws IOException I/O exception
   */
  @AfterEach public void cleanup() throws IOException {
    post("user-drop", Map.of("name", USER));
  }

  /**
   * Tests a create/list/inspect/drop round-trip.
   * @throws IOException I/O exception
   */
  @Test public void createAndDrop() throws IOException {
    assertTrue(create("read").contains("User was created."), "user not created");
    assertTrue(get("users").contains(USER), "user missing from list");
    assertTrue(get("user?name=" + USER).contains(USER), "user page not served");
    post("user-drop", Map.of("name", USER));
    assertFalse(get("users").contains(USER), "user still listed after drop");
  }

  /**
   * Tests that creating an existing user reports an error instead of a redirect.
   * @throws IOException I/O exception
   */
  @Test public void duplicate() throws IOException {
    create("read");
    assertTrue(create("read").contains("User already exists."), "duplicate not rejected");
  }

  /**
   * Creates the test user with the given permission.
   * @param perm permission
   * @return response body
   * @throws IOException I/O exception
   */
  private static String create(final String perm) throws IOException {
    return post("user-create", Map.of("do", "do", "name", USER, "pw", "secret", "perm", perm));
  }
}
