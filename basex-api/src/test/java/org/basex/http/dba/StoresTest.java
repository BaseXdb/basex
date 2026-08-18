package org.basex.http.dba;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;
import java.util.regex.*;

import org.junit.jupiter.api.*;

/**
 * Tests for the DBA stores view.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class StoresTest extends DBATest {
  /** Test store. */
  private static final String STORE = "dba-junit-store";
  /** Test store whose name follows the first one, ignoring case. */
  private static final String OTHER = "DBA-JUNIT-ZOO";
  /** Link of the child that is looked at. */
  private static final Pattern SELECTED =
      Pattern.compile("data-step=\"([^\"]*)\"[^>]*class=\"selected\"");

  /**
   * Deletes the test stores after each test.
   * @throws IOException I/O exception
   */
  @AfterEach public void cleanup() throws IOException {
    post("stores/delete", Map.of("name", STORE));
    post("stores/delete", Map.of("name", OTHER));
  }

  /**
   * The view opens on the default store and offers to add an entry.
   * @throws IOException I/O exception
   */
  @Test public void storesPage() throws IOException {
    final String page = get("stores");
    assertTrue(page.contains("(default)"), "default store not listed");
    assertTrue(page.contains(">Add…<"), "add button not offered");
  }

  /**
   * An entry is added to a store and removed again.
   * @throws IOException I/O exception
   */
  @Test public void addAndRemove() throws IOException {
    assertTrue(add("", false, "'key'", "'value'").contains("Entry \"key\" was added."),
        "entry not added");
    assertTrue(get("stores?name=" + STORE).contains("data-step=\"key\""),
        "entry missing from the list");
    assertTrue(post("stores/remove", Map.of("name", STORE, "path", "", "step", "key")).
        contains("Entry \"key\" was removed."), "entry not removed");
    assertFalse(get("stores?name=" + STORE).contains("data-step=\"key\""),
        "entry still listed after removal");
  }

  /**
   * An entry that exists is not replaced.
   * @throws IOException I/O exception
   */
  @Test public void rejectExisting() throws IOException {
    add("", false, "'key'", "'value'");
    assertTrue(add("", false, "'key'", "'other'").contains("Entry already exists: key."),
        "existing entry not rejected");
    assertTrue(get("stores?name=" + STORE + "&key=key").contains(">\"value\"</textarea>"),
        "value of the existing entry was replaced");
  }

  /**
   * A map key is stored as a string: a key of another type could only be addressed by its
   * position, which would make its value read-only.
   * @throws IOException I/O exception
   */
  @Test public void stringKey() throws IOException {
    add("", false, "'map'", "{ 'a': 1 }");
    assertTrue(add("map", false, "'abcde'", "'text'").contains("Entry \"abcde\" was added."),
        "child not added");

    final String level = get("stores?name=" + STORE + "&path=map");
    assertTrue(level.contains("data-step=\"abcde\""), "key not addressed by itself");
    assertFalse(level.contains("{&quot;pos&quot;"), "key addressed by its position");

    final String value = get("stores?name=" + STORE + "&path=map.abcde");
    assertTrue(value.contains(">\"text\"</textarea>"), "value not shown in the editor");
    assertFalse(value.contains("Read-only"), "value reported as read-only");
  }

  /**
   * A child of a sequence is appended: its position is not asked for.
   * @throws IOException I/O exception
   */
  @Test public void appendToSequence() throws IOException {
    add("", false, "'seq'", "(1, 2, 3)");
    final String level = get("stores?name=" + STORE + "&path=seq");
    assertFalse(level.contains("Index:"), "position asked for");
    assertTrue(level.contains("name=\"step\" value=\"4\""), "position to append at not offered");

    assertTrue(add("seq", true, "4", "4").contains("Entry \"4\" was added."), "child not added");
    assertTrue(get("stores?name=" + STORE + "&path=seq").
        contains("name=\"step\" value=\"5\""), "child not appended");
  }

  /**
   * A member of an array is appended.
   * @throws IOException I/O exception
   */
  @Test public void appendToArray() throws IOException {
    add("", false, "'array'", "[ 'a' ]");
    assertTrue(get("stores?name=" + STORE + "&path=array").
        contains("name=\"step\" value=\"2\""), "position to append at not offered");

    assertTrue(add("array", true, "2", "'b'").contains("Entry \"2\" was added."),
        "member not added");
    assertTrue(get("stores?name=" + STORE + "&path=array").
        contains("name=\"step\" value=\"3\""), "member not appended");
  }

  /**
   * The store opens on the entry that its first row shows, which is sorted ignoring case.
   * @throws IOException I/O exception
   */
  @Test public void firstEntry() throws IOException {
    add("", false, "'Zoo'", "'z'");
    add("", false, "'apple'", "'a'");

    final String page = get("stores?name=" + STORE);
    assertTrue(page.indexOf("data-step=\"apple\"") < page.indexOf("data-step=\"Zoo\""),
        "entries not sorted, ignoring case");
    final Matcher matcher = SELECTED.matcher(page);
    assertTrue(matcher.find(), "no entry is looked at");
    assertEquals("apple", matcher.group(1), "entry of the first row is not the one looked at");
  }

  /**
   * The stores are listed in the order the table sorts them, ignoring case.
   * @throws IOException I/O exception
   */
  @Test public void storeOrder() throws IOException {
    add("", false, "'key'", "1");
    post("stores/add", Map.of("name", OTHER, "path", "", "index", "false", "step", "'key'",
        "value", "1"));

    final String page = get("stores");
    assertTrue(page.indexOf(STORE) < page.indexOf(OTHER), "stores not sorted, ignoring case");
  }

  /**
   * A store is written to disk, read again and closed.
   * @throws IOException I/O exception
   */
  @Test public void writeReadClose() throws IOException {
    add("", false, "'key'", "'value'");
    final Map<String, String> store = Map.of("name", STORE);
    assertTrue(post("stores/write", store).contains("was written to disk"), "store not written");
    assertTrue(post("stores/read", store).contains("was read from disk"), "store not read");
    assertTrue(post("stores/close", store).contains("was closed"), "store not closed");
  }

  /**
   * A key is an expression: what it yields is the key, with its type.
   * @throws IOException I/O exception
   */
  @Test public void typedKey() throws IOException {
    add("", false, "'map'", "{}");
    assertTrue(add("map", false, "1", "'int'").contains("Entry \"1\" was added."),
        "child not added");
    assertTrue(get("stores?name=" + STORE + "&path=map.1").contains(">\"int\"</textarea>"),
        "integer key not addressed by its value");

    assertTrue(add("map", false, "'1'", "'string'").contains("was added"),
        "string key rejected as the integer one");
    assertTrue(add("map", false, "1", "'other'").contains("Entry already exists: 1."),
        "integer key not rejected as existing");
  }

  /**
   * A key that is not a single atomic value is rejected, and an entry of a store is named by
   * a string.
   * @throws IOException I/O exception
   */
  @Test public void invalidKey() throws IOException {
    assertTrue(add("", false, "(1, 2)", "'x'").contains("must be a single atomic value"),
        "sequence accepted as a key");
    assertTrue(add("", false, "1", "'x'").contains("named by a string"),
        "integer accepted as the name of an entry");
  }

  /**
   * Adds a value to a level of the test store.
   * @param path path of the level
   * @param index whether the children of the level are addressed by position
   * @param step key or position of the new child
   * @param value expression that yields the value
   * @return response body
   * @throws IOException I/O exception
   */
  private static String add(final String path, final boolean index, final String step,
      final String value) throws IOException {
    return post("stores/add", Map.of("name", STORE, "path", path, "index",
        String.valueOf(index), "step", step, "value", value));
  }
}
