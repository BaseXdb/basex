package org.basex.test.data;

import static org.junit.Assert.*;

import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.test.*;
import org.junit.*;

/**
 * This class tests the stability of the database text store.
 *
 * @author BaseX Team 2005-12, BSD License
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
    new Set(Prop.TEXTINDEX, false).execute(context);
    new Set(Prop.ATTRINDEX, false).execute(context);
    new Set(Prop.AUTOFLUSH, false).execute(context);
  }

  /**
   * Finishes the test.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(NAME).execute(context);
  }

  /**
   * Replaces text nodes with random double values.
   * @throws BaseXException database exception
   */
  @Test
  public void replace() throws BaseXException {
    new CreateDB(NAME, "<X><A>q</A><A>q</A></X>").execute(context);
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();
    for(int n = 0; n < NQUERIES; n++) {
      final String qu =
          "for $a in //text() " +
          "let $d := math:random() " +
          "return replace node $a with $d";
      new XQuery(qu).execute(context);
    }
    check(size);
  }

  /**
   * Replaces two text nodes with random integer values.
   * @throws BaseXException database exception
   */
  @Test
  public void deleteInsertTwo() throws BaseXException {
    new CreateDB(NAME, "<X><A>q</A><A>q</A></X>").execute(context);
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();

    for(int n = 0; n < NQUERIES; n++) {
      String qu = "for $a in //text() return delete node $a";
      new XQuery(qu).execute(context);
      qu = "for $a in //text() " +
          "let $d := xs:integer(math:random() * " + Integer.MAX_VALUE + ") " +
          "return insert node $a into $d";
      new XQuery(qu).execute(context);
    }
    check(size);
  }

  /**
   * Deletes and inserts a text multiple times.
   * @throws BaseXException database exception
   */
  @Test
  public void deleteInsert() throws BaseXException {
    new CreateDB(NAME, "<X>abc</X>").execute(context);
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();

    for(int i = 0; i < NQUERIES; i++) {
      new XQuery("delete node //text()").execute(context);
      new XQuery("insert node 'abc' into /X").execute(context);
    }
    check(size);
  }

  /**
   * Tests if the size of the text store has not changed.
   * @param old old size
   */
  private static void check(final long old) {
    assertEquals(old, context.data().meta.dbfile(DataText.DATATXT).length());
  }
}
