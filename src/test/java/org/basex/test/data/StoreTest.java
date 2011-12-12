package org.basex.test.data;

import static org.junit.Assert.*;

import org.basex.core.BaseXException;
import org.basex.core.Context;
import org.basex.core.Prop;
import org.basex.core.cmd.CreateDB;
import org.basex.core.cmd.DropDB;
import org.basex.core.cmd.Set;
import org.basex.core.cmd.XQuery;
import org.basex.data.DataText;
import org.basex.util.Util;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This class tests the stability of the database text store.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class StoreTest {
  /** Test database name. */
  private static final String DB = Util.name(StoreTest.class);
  /** Global context. */
  private static final Context CONTEXT = new Context();
  /** Number of runs per client. */
  private static final int NQUERIES = 100;

  /**
   * Initializes the test.
   * @throws Exception exception
   */
  @BeforeClass
  public static void init() throws Exception {
    // speed up updates and create initial database
    new Set(Prop.TEXTINDEX, false).execute(CONTEXT);
    new Set(Prop.ATTRINDEX, false).execute(CONTEXT);
    new Set(Prop.AUTOFLUSH, false).execute(CONTEXT);
  }

  /**
   * Finishes the test.
   * @throws BaseXException database exception
   */
  @AfterClass
  public static void finish() throws BaseXException {
    new DropDB(DB).execute(CONTEXT);
  }

  /**
   * Replaces text nodes with random double values.
   * @throws BaseXException database exception
   */
  @Test
  public void replace() throws BaseXException {
    new CreateDB(DB, "<X><A>q</A><A>q</A></X>").execute(CONTEXT);
    final long size = CONTEXT.data().meta.dbfile(DataText.DATATXT).length();
    for(int n = 0; n < NQUERIES; n++) {
      final String qu =
          "for $a in //text() " +
          "let $d := math:random() " +
          "return replace node $a with $d";
      new XQuery(qu).execute(CONTEXT);
    }
    check(size);
  }

  /**
   * Replaces two text nodes with random integer values.
   * @throws BaseXException database exception
   */
  @Test
  public void deleteInsertTwo() throws BaseXException {
    new CreateDB(DB, "<X><A>q</A><A>q</A></X>").execute(CONTEXT);
    final long size = CONTEXT.data().meta.dbfile(DataText.DATATXT).length();

    for(int n = 0; n < NQUERIES; n++) {
      String qu = "for $a in //text() return delete node $a";
      new XQuery(qu).execute(CONTEXT);
      qu = "for $a in //text() " +
          "let $d := xs:integer(math:random() * " + Integer.MAX_VALUE + ") " +
          "return insert node $a into $d";
      new XQuery(qu).execute(CONTEXT);
    }
    check(size);
  }

  /**
   * Deletes and inserts a text multiple times.
   * @throws BaseXException database exception
   */
  @Test
  public void deleteInsert() throws BaseXException {
    new CreateDB(DB, "<X>abc</X>").execute(CONTEXT);
    final long size = CONTEXT.data().meta.dbfile(DataText.DATATXT).length();

    for(int i = 0; i < NQUERIES; i++) {
      new XQuery("delete node //text()").execute(CONTEXT);
      new XQuery("insert node 'abc' into /X").execute(CONTEXT);
    }
    check(size);
  }

  /**
   * Tests if the size of the text store has not changed.
   * @param old old size
   */
  private void check(final long old) {
    assertEquals(old, CONTEXT.data().meta.dbfile(DataText.DATATXT).length());
  }
}
