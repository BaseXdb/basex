package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests queries on collections.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Michael Seiferle
 */
public final class CollectionPathTest extends SandboxTest {
  /** Test files directory. */
  private static final String DIR = "src/test/resources/";
  /** Test files. */
  private static final String[] FILES = {
    DIR + "input.xml", DIR + "xmark.xml", DIR + "test.xml"
  };
  /** Test ZIP. */
  private static final String ZIP = DIR + "xml.zip";

  /**
   * Creates an initial database.
   */
  @BeforeAll public static void before() {
    execute(new CreateDB(NAME));
    for(final String file : FILES) execute(new Add(DIR, file));
    execute(new Add("test/zipped", ZIP));
  }

  /**
   * Drops the initial collection.
   */
  @AfterAll public static void after() {
    execute(new DropDB(NAME));
  }

  /**
   * Finds single doc.
   */
  @Test public void findDoc() {
    assertEquals("1", query(
      "count(for $x in collection('" + NAME + '/' + DIR + "xmark.xml') " +
      "where $x//location contains text 'uzbekistan' " +
      "return $x)"));
  }

  /**
   * Finds documents in path.
   */
  @Test public void findDocs() {
    assertEquals("4", query("count(collection('" + NAME + "/test/zipped'))"));
  }

  /**
   * Checks if the constructed base-uri matches the base-uri of added documents.
   */
  @Test public void baseUri() {
    assertEquals('/' + NAME + '/' + FILES[1],
        query("base-uri(collection('" + NAME + '/' + DIR + "xmark.xml'))"));
  }
}
