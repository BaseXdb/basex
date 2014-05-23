package org.basex.data;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the stability of the database text store.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class StoreTest extends SandboxTest {
  /** Number of runs per client. */
  private static final int NQUERIES = 100;

  /**
   * Initializes the test.
   * @throws Exception exception
   */
  @BeforeClass
  public static void init() throws Exception {
    // speed up updates and create initial database
    run(new Set(MainOptions.TEXTINDEX, false));
    run(new Set(MainOptions.ATTRINDEX, false));
    run(new Set(MainOptions.AUTOFLUSH, false));
  }

  /**
   * Finishes the test.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    run(new DropDB(NAME));
    run(new Set(MainOptions.TEXTINDEX, true));
    run(new Set(MainOptions.ATTRINDEX, true));
    run(new Set(MainOptions.AUTOFLUSH, true));
    run(new Set(MainOptions.UPDINDEX, false));
  }

  /**
   * Replaces text nodes with random double values.
   * @throws BaseXException database exception
   */
  @Test
  public void replace() throws BaseXException {
    run(new CreateDB(NAME, "<X><A>q</A><A>q</A></X>"));
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();
    for(int n = 0; n < NQUERIES; n++) {
      final String qu =
          "for $a in //text() " +
          "let $d := random:double() " +
          "return replace node $a with $d";
      run(new XQuery(qu));
    }
    check(size);
  }

  /**
   * Replaces two text nodes with random integer values.
   * @throws BaseXException database exception
   */
  @Test
  public void deleteInsertTwo() throws BaseXException {
    run(new CreateDB(NAME, "<X><A>q</A><A>q</A></X>"));
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();

    for(int n = 0; n < NQUERIES; n++) {
      String qu = "for $a in //text() return delete node $a";
      run(new XQuery(qu));
      qu = "for $a in //text() " +
          "let $d := random:integer(" + Integer.MAX_VALUE + ") " +
          "return insert node $a into $d";
      run(new XQuery(qu));
    }
    check(size);
  }

  /**
   * Deletes and inserts a text multiple times.
   * @throws BaseXException database exception
   */
  @Test
  public void deleteInsert() throws BaseXException {
    run(new CreateDB(NAME, "<X>abc</X>"));
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();

    for(int i = 0; i < NQUERIES; i++) {
      run(new XQuery("delete node //text()"));
      run(new XQuery("insert node 'abc' into /X"));
    }
    check(size);
  }

  /**
   * Tests the {@link MainOptions#UPDINDEX} and {@link MainOptions#AUTOFLUSH} flags in
   * combination. Reaction on a bug (incremental value index was not correctly closed)
   * @throws BaseXException database exception
   */
  @Test
  public void updIndexFlush() throws BaseXException {
    run(new Set(MainOptions.TEXTINDEX, true));
    run(new Set(MainOptions.AUTOFLUSH, false));
    run(new Set(MainOptions.UPDINDEX, true));
    run(new CreateDB(NAME));
    final String input = "<a>0</a>";
    run(new Add("a.xml", input));
    final String query = "doc('" + NAME + "')//*[text()='0']";
    assertEquals(input, run(new XQuery(query)));
    run(new Close());
    assertEquals(input, run(new XQuery(query)));
  }

  /**
   * Tests if the size of the text store has not changed.
   * @param old old size
   */
  private static void check(final long old) {
    assertEquals(old, context.data().meta.dbfile(DataText.DATATXT).length());
  }

  /**
   * Runs the specified command.
   * @param cmd command to be run
   * @return string result
   * @throws BaseXException database exception
   */
  private static String run(final Command cmd) throws BaseXException {
    return cmd.execute(context);
  }
}
