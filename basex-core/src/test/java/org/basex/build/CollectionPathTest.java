package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import java.util.function.*;

import org.basex.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests queries on collections.
 *
 * @author BaseX Team, BSD License
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
  /** Paths with characters that are invalid or special in URIs. */
  private static final String[] SPECIAL = {
    "50%.xml", "[x].xml", "a#b#c.xml", "a#b.xml", "a b.xml", "%41.xml", "ä{}.xml", "c d.xml"
  };

  /**
   * Creates an initial database.
   */
  @BeforeAll public static void before() {
    execute(new CreateDB(NAME));
    for(final String file : FILES) execute(new Add(DIR, file));
    execute(new Add("test/zipped", ZIP));
    for(final String path : SPECIAL) execute(new Add("special/" + path, "<a/>"));
    execute(new Add("special/c%20d.xml", "<b/>"));
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

  /**
   * Checks if the URIs of documents with special characters can be reused (GH-1172).
   */
  @Test public void reuseUris() {
    final String docs = "collection('" + NAME + "/special')";
    assertEquals(String.valueOf(SPECIAL.length + 1), query("count(" + docs + " ! base-uri(.))"));
    assertEquals("true", query("every $d in " + docs + " satisfies ("
        + "doc-available(document-uri($d)) and doc(base-uri($d)) is $d and "
        + "collection(document-uri($d)) is $d)"));
  }

  /**
   * Resolves percent-encoded references against database URIs (GH-1172).
   */
  @Test public void encodedUris() {
    final String base = "'/" + NAME + "/special/x.xml'";
    final Function<String, String> doc = href ->
      query("name(doc(resolve-uri('" + href + "', " + base + "))/*)");
    // literal path is favored, decoded path is the fallback
    assertEquals("a", doc.apply("c d.xml"));
    assertEquals("b", doc.apply("c%20d.xml"));
    assertEquals("a", doc.apply("a%20b.xml"));
    assertEquals("a", doc.apply("%C3%A4%7B%7D.xml"));
    assertEquals("a", doc.apply("../special/%41.xml"));
    assertEquals("1", query("count(collection(resolve-uri('a%20b.xml', " + base + ")))"));
    assertEquals("true", query("doc-available(resolve-uri('50%25.xml', " + base + "))"));
    assertEquals("false", query("doc-available(resolve-uri('%41%41.xml', " + base + "))"));
  }
}
