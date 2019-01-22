package org.basex.data;

import static org.junit.Assert.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.query.func.*;
import org.junit.*;
import org.junit.Test;

/**
 * This class tests the stability of the database text store.
 *
 * @author BaseX Team 2005-19, BSD License
 * @author Christian Gruen
 */
public final class StoreTest extends SandboxTest {
  /** Number of runs per client. */
  private static final int NQUERIES = 100;

  /**
   * Initializes the tests.
   */
  @BeforeClass public static void init() {
    // speed up updates and create initial database
    set(MainOptions.TEXTINDEX, false);
    set(MainOptions.ATTRINDEX, false);
    set(MainOptions.AUTOFLUSH, false);
  }

  /**
   * Replaces text nodes with random double values.
   */
  @Test public void replace() {
    execute(new CreateDB(NAME, "<X><A>q</A><A>q</A></X>"));
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();
    for(int n = 0; n < NQUERIES; n++) {
      query("for $a in //text() return replace node $a with random:double()");
    }
    check(size);
  }

  /**
   * Replaces two text nodes with random integer values.
   */
  @Test public void deleteInsertTwo() {
    execute(new CreateDB(NAME, "<X><A>q</A><A>q</A></X>"));
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();

    for(int n = 0; n < NQUERIES; n++) {
      String qu = "for $a in //text() return delete node $a";
      query(qu);
      qu = "for $a in //text() " +
          "let $d := random:integer(" + Integer.MAX_VALUE + ") " +
          "return insert node $a into $d";
      query(qu);
    }
    check(size);
  }

  /**
   * Deletes and inserts a text multiple times.
   */
  @Test public void deleteInsert() {
    execute(new CreateDB(NAME, "<X>abc</X>"));
    final long size = context.data().meta.dbfile(DataText.DATATXT).length();

    for(int i = 0; i < NQUERIES; i++) {
      query("delete node //text()");
      query("insert node 'abc' into /X");
    }
    check(size);
  }

  /**
   * Tests the {@link MainOptions#UPDINDEX} and {@link MainOptions#AUTOFLUSH} flags in combination.
   * Reaction on a bug (incremental value index was not correctly closed)
   */
  @Test public void updIndexFlush() {
    try {
      for(int a = 0; a < 2; a++) {
        for(int b = 0; b < 2; b++) {
          set(MainOptions.TEXTINDEX, a == 0);
          set(MainOptions.UPDINDEX, b == 0);
          execute(new CreateDB(NAME));
          final String input = "<a>0</a>";
          execute(new Add("a.xml", input));
          final String query = Function._DB_OPEN.args(NAME) + "//*[text()='0']";
          assertEquals(input, query(query));
          execute(new Close());
          assertEquals(input, query(query));
        }
      }
    } finally {
      set(MainOptions.TEXTINDEX, false);
      set(MainOptions.UPDINDEX, false);
    }
  }

  /**
   * Tests if the size of the text store has not changed.
   * @param old old size
   */
  private static void check(final long old) {
    assertEquals(old, context.data().meta.dbfile(DataText.DATATXT).length());
  }
}
