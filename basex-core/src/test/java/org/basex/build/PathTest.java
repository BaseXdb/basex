package org.basex.build;

import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.MainOptions.MainParser;
import org.basex.core.cmd.*;
import org.basex.io.*;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests queries with path in it on collections.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Andreas Weiler
 */
public final class PathTest extends SandboxTest {
  /** Test database name. */
  private static final String INPUT = Util.className(PathTest.class);
  /** Test database name. */
  private static final String WEEK1 = Util.className(PathTest.class) + '2';
  /** Test database name. */
  private static final String WEEK2 = Util.className(PathTest.class) + '3';
  /** Test file. */
  private static final String INPUTF = "src/test/resources/input.xml";
  /** Test file. */
  private static final String WEEK = "src/test/resources/week.zip";

  /**
   * Creates initial databases.
   */
  @BeforeAll public static void before() {
    execute(new CreateDB(INPUT));
    execute(new Add("input", INPUTF));
    execute(new Add("input2", INPUTF));
    execute(new CreateDB(WEEK1, WEEK));
    execute(new CreateDB(WEEK2, WEEK));
    execute(new Close());
  }

  /**
   * Drops the initial databases.
   */
  @AfterAll public static void after() {
    execute(new DropDB(INPUT));
    execute(new DropDB(WEEK1));
    execute(new DropDB(WEEK2));
  }

  /**
   * Checks the number of documents under the specified path.
   */
  @Test public void documentTestInput() {
    assertEquals("1", query("count(collection('" + INPUT + "/input'))"));
  }

  /**
   * Checks the number of documents under the specified path.
   */
  @Test public void documentTestWeek() {
    assertEquals("3", query("count(collection('" + WEEK1 + "/week/monday'))"));
  }

  /**
   * Checks the results of the query with index access.
   */
  @Test public void withIndexTest() {
    weekTest();
    weekTest2();
  }

  /**
   * #905: Ensure that parser options will not affect doc() and collection().
   * May be moved to a separate test class in future.
   */
  @Test public void docParsing() {
    final IOFile path = new IOFile(sandbox(), "doc.xml");
    write(path, "<a/>");
    set(MainOptions.PARSER, MainParser.JSON);
    assertEquals("<a/>", query("doc('" + path + "')"));
  }

  /**
   * Checks the results of the query without index access.
   */
  @Test public void withoutIndexTest() {
    execute(new Open(WEEK1));
    execute(new DropIndex("text"));
    execute(new Open(WEEK2));
    execute(new DropIndex("text"));
    weekTest();
    weekTest2();
  }

  /** Checks the results of the queries with the db week.
   */
  private static void weekTest() {
    assertEquals("3", query("count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text'])"));
    // cross-check
    assertEquals("4", query("count(collection('" + WEEK1 +
      "/week')/root/monday/text[text() = 'text'])"));
  }

  /** Checks the results of the queries with the db week.
   */
  private static void weekTest2() {
    assertEquals("3,1", query("count(collection('" + WEEK1 +
      "/week/monday')/root/monday/text[text() = 'text']) || ',' ||" +
      " count(collection('" + WEEK2 +
      "/week/tuesday')/root/monday/text[text() = 'text']) "));
  }
}
