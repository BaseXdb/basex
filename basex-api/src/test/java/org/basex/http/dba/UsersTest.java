package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the DBA users view.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UsersTest extends DBATest {
  /** Test user. */
  private static final String USER = "dba-junit-user";
  /** Name the test user is renamed to. */
  private static final String OTHER = USER + "-renamed";

  /**
   * Drops the test users after each test.
   * @throws IOException I/O exception
   */
  @AfterEach public void cleanup() throws IOException {
    post("users/drop", Map.of("name", USER));
    post("users/drop", Map.of("name", OTHER));
  }

  /**
   * Tests a create/list/inspect/drop round-trip.
   * @throws IOException I/O exception
   */
  @Test public void createAndDrop() throws IOException {
    assertTrue(create("read").contains("was created"), "user not created");
    assertTrue(get("users").contains(USER), "user missing from list");
    assertTrue(get("users?name=" + USER).contains("User: " + USER), "user panel not served");
    post("users/drop", Map.of("name", USER));
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
   * The user panel is only shown for a user that exists.
   * @throws IOException I/O exception
   */
  @Test public void noSelection() throws IOException {
    final String page = get("users");
    assertTrue(page.contains("class=\"panel hidden\" data-label=\"User\""),
        "user panel not hidden");
    assertTrue(page.contains("class=\"panel hidden\" data-label=\"Permissions\""),
        "permissions panel not hidden");
  }

  /**
   * Renames a user and changes its permission; the selection follows the new name.
   * @throws IOException I/O exception
   */
  @Test public void update() throws IOException {
    create("read");
    assertTrue(update(USER, OTHER, "write").contains("was updated"), "user not updated");
    final String page = get("users?name=" + OTHER);
    assertTrue(page.contains("User: " + OTHER), "renamed user not shown");
    assertTrue(page.contains("<option selected=\"\">write</option>"), "permission not changed");
  }

  /**
   * A name that is taken is rejected, and the panel keeps showing the edited user.
   * @throws IOException I/O exception
   */
  @Test public void updateToExistingName() throws IOException {
    create("read");
    final String page = update(USER, "admin", "read");
    assertTrue(page.contains("User already exists."), "duplicate name not rejected");
    assertTrue(page.contains("User: " + USER), "selection did not stay on the edited user");
  }

  /**
   * The password is not carried back into the address, the history and the log.
   * @throws IOException I/O exception
   */
  @Test public void passwordIsNotEchoed() throws IOException {
    create("read");
    // an update that fails is the case that used to carry the entered values back
    final String page = update(USER, "admin", "read", "topsecret");
    assertTrue(page.contains("User already exists."), "test needs an update that fails");
    assertFalse(page.contains("topsecret"), "password was echoed into the response");
  }

  /**
   * Adds and drops a local permission.
   * @throws IOException I/O exception
   */
  @Test public void patterns() throws IOException {
    create("read");
    assertTrue(post("users/pattern-add",
        Map.of("name", USER, "pattern", "unit*", "perm", "write")).contains("was created"),
        "pattern not added");
    assertTrue(get("users?name=" + USER).contains("unit*"), "pattern not listed");
    assertTrue(post("users/pattern-drop", Map.of("name", USER, "pattern", "unit*")).
        contains("was dropped"), "pattern not dropped");
  }

  /**
   * The admin has no local permissions to assign.
   * @throws IOException I/O exception
   */
  @Test public void adminHasNoPatterns() throws IOException {
    assertFalse(get("users?name=admin").contains("Local Permissions"), "panel shown for admin");
  }

  /**
   * Updates the information that belongs to no user in particular.
   * @throws IOException I/O exception
   */
  @Test public void globalInformation() throws IOException {
    assertTrue(post("users/info", Map.of("info", "<info><junit/></info>")).
        contains("User information was updated."), "information not updated");
    assertTrue(get("users").contains("junit"), "information not shown");
    post("users/info", Map.of("info", ""));
  }

  /**
   * Information that is not an {@code info} element is rejected.
   * @throws IOException I/O exception
   */
  @Test public void invalidInformation() throws IOException {
    assertTrue(post("users/info", Map.of("info", "<other/>")).
        contains("\"info\" root element"), "invalid information not rejected");
  }

  /**
   * Creates the test user with the given permission.
   * @param perm permission
   * @return response body
   * @throws IOException I/O exception
   */
  private static String create(final String perm) throws IOException {
    return post("users/create", Map.of("name", USER, "pw", "secret", "perm", perm));
  }

  /**
   * Updates the test user, leaving its password as it is.
   * @param name current name
   * @param newname new name
   * @param perm permission
   * @return response body
   * @throws IOException I/O exception
   */
  private static String update(final String name, final String newname, final String perm)
      throws IOException {
    return update(name, newname, perm, "");
  }

  /**
   * Updates the test user.
   * @param name current name
   * @param newname new name
   * @param perm permission
   * @param pw password (empty string: unchanged)
   * @return response body
   * @throws IOException I/O exception
   */
  private static String update(final String name, final String newname, final String perm,
      final String pw) throws IOException {
    return post("users/update", Map.of("name", name, "newname", newname, "pw", pw,
        "perm", perm, "info", ""));
  }
}
