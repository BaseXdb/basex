package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the DBA databases view.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class DatabasesTest extends DBATest {
  /** Test database. */
  private static final String DB = "dba-junit-test";
  /** Name the test database is renamed or copied to. */
  private static final String OTHER = DB + "-other";

  /**
   * Drops the test databases after each test.
   * @throws IOException I/O exception
   */
  @AfterEach public void cleanup() throws IOException {
    post("databases/drop", Map.of("name", DB));
    post("databases/drop", Map.of("name", OTHER));
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
    assertTrue(create().contains("was created"), "new database not reported");
    assertTrue(get("databases").contains(DB), "database missing from list");
    post("databases/drop", Map.of("name", DB));
    assertFalse(get("databases").contains(DB + "<"), "database still listed after drop");
  }

  /**
   * Tests that creating an existing database reports an error.
   * @throws IOException I/O exception
   */
  @Test public void duplicate() throws IOException {
    create();
    assertTrue(create().contains("Database already exists."), "duplicate not rejected");
  }

  /**
   * The index options of the create dialog reach the new database: the optimize dialog offers
   * what the database really has, which is not what the create dialog defaults to.
   * @throws IOException I/O exception
   */
  @Test public void createWithOptions() throws IOException {
    post("databases/create", Map.of("name", DB, "opts", "textindex", "lang", "en"));
    final String dialog = dialog(get("databases?name=" + DB), "optimize");
    assertTrue(dialog.contains("value=\"textindex\" checked"), "text index not enabled");
    assertFalse(dialog.contains("value=\"attrindex\" checked"), "attribute index not disabled");
  }

  /**
   * Without a selection, only the lists are shown.
   * @throws IOException I/O exception
   */
  @Test public void noSelection() throws IOException {
    final String page = get("databases");
    assertTrue(page.contains("hidden\" data-label=\"Database\""), "database panel not hidden");
    assertTrue(page.contains("hidden\" data-label=\"Resource\""), "resource panel not hidden");
  }

  /**
   * Renames a database; the selection follows the new name.
   * @throws IOException I/O exception
   */
  @Test public void rename() throws IOException {
    create();
    assertTrue(post("databases/rename", Map.of("name", DB, "newname", OTHER)).
        contains("was renamed"), "database not renamed");
    assertTrue(get("databases?name=" + OTHER).contains("Database: "), "renamed database not shown");
    assertFalse(get("databases").contains(DB + "<"), "old name still listed");
  }

  /**
   * A name that is taken is rejected, and the selection stays on the edited database.
   * @throws IOException I/O exception
   */
  @Test public void renameToExistingName() throws IOException {
    create();
    post("databases/create", Map.of("name", OTHER));
    final String page = post("databases/rename", Map.of("name", DB, "newname", OTHER));
    assertTrue(page.contains("Database already exists."), "duplicate name not rejected");
    assertTrue(page.contains(">" + DB + "</a>"), "selection did not stay on the database");
  }

  /**
   * Copies a database; both names remain.
   * @throws IOException I/O exception
   */
  @Test public void copy() throws IOException {
    create();
    assertTrue(post("databases/copy", Map.of("name", DB, "newname", OTHER)).
        contains("was copied"), "database not copied");
    final String page = get("databases");
    assertTrue(page.contains(DB), "source missing");
    assertTrue(page.contains(OTHER), "copy missing");
  }

  /**
   * Creates a backup, finds it in the panel and drops it again.
   * @throws IOException I/O exception
   */
  @Test public void backups() throws IOException {
    create();
    assertTrue(post("databases/backup-create",
        Map.of("name", DB, "comment", "junit", "compress", "true")).
        contains("was backed up"), "backup not created");

    final String page = get("databases?name=" + DB);
    assertTrue(page.contains("<td>junit</td>"), "comment of the backup not listed");
    final Matcher m = Pattern.compile("name=\"backup\" value=\"([^\"]+)\"").matcher(page);
    assertTrue(m.find(), "backup not listed");

    assertTrue(post("databases/backup-drop", Map.of("name", DB, "backup", m.group(1))).
        contains("was dropped"), "backup not dropped");
  }

  /**
   * An unknown action is rejected.
   * @throws IOException I/O exception
   */
  @Test public void unknownAction() throws IOException {
    send(404, "POST", "databases/no-such-action", "name=" + DB,
        "application/x-www-form-urlencoded");
  }

  /**
   * Creates the test database with the default options.
   * @return response body
   * @throws IOException I/O exception
   */
  private static String create() throws IOException {
    return post("databases/create", Map.of("name", DB));
  }

  /**
   * Returns the markup of a dialog of a page.
   * @param page page
   * @param id id of the dialog, without its suffix
   * @return markup
   */
  private static String dialog(final String page, final String id) {
    final String start = "<dialog id=\"" + id + "-dialog\">";
    final int s = page.indexOf(start);
    assertTrue(s != -1, id + " dialog not found");
    return page.substring(s, page.indexOf("</dialog>", s));
  }
}
