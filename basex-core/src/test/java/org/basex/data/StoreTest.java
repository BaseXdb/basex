package org.basex.data;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * This class tests the stability of the database store.
 *
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class StoreTest extends SandboxTest {
  /** Test file. */
  private static final String GH1711 = "src/test/resources/gh1711.xml";

  /**
   * Initializes the tests.
   */
  @BeforeAll public static void init() {
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
    final long size = context.data().meta.dbFile(DataText.DATATXT).length();
    for(int n = 0; n < 500; n++) {
      query("for $a in //text() return replace node $a with " + _RANDOM_DOUBLE.args());
    }
    assertEquals(size, context.data().meta.dbFile(DataText.DATATXT).length());
  }

  /**
   * Replaces two text nodes with random integer values.
   */
  @Test public void deleteInsertTwo() {
    execute(new CreateDB(NAME, "<X><A>q</A><A>q</A></X>"));
    final long size = context.data().meta.dbFile(DataText.DATATXT).length();

    for(int n = 0; n < 500; n++) {
      String qu = "for $a in //text() return delete node $a";
      query(qu);
      qu = "for $a in //text() " +
          "let $d := " + _RANDOM_INTEGER.args(Integer.MAX_VALUE) +
          "return insert node $a into $d";
      query(qu);
    }
    assertEquals(size, context.data().meta.dbFile(DataText.DATATXT).length());
  }

  /**
   * Deletes and inserts a text multiple times.
   */
  @Test public void deleteInsert() {
    execute(new CreateDB(NAME, "<X>abc</X>"));
    final long size = context.data().meta.dbFile(DataText.DATATXT).length();

    for(int i = 0; i < 500; i++) {
      query("delete node //text()");
      query("insert node 'abc' into /X");
    }
    assertEquals(size, context.data().meta.dbFile(DataText.DATATXT).length());
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
          final String query = _DB_GET.args(NAME) + "//*[text()='0']";
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
   * Add, delete and replace documents in an empty database.
   */
  @Test public void gh1662() {
    final String[] adds = {
      _DB_ADD.args(NAME, " element x { (1 to 254) ! element y { } }", "a"),
      _DB_ADD.args(NAME, " element x { }", "b")
    };
    final String[] deletes = {
      _DB_DELETE.args(NAME, "a"),
      _DB_DELETE.args(NAME, "b")
    };

    for(int i = 0; i < 4; i++) {
      execute(new CreateDB(NAME));
      query(adds[i / 2]);
      query(adds[1 - i / 2]);
      query("count(//y)", 254);
      query(deletes[i % 2]);
      query(deletes[1 - i % 2]);
      query(_DB_ADD.args(NAME, " element x { (1 to 256) ! element y { } }", "a"));
      query("count(//y)", 256);
      assertTrue(execute(new Inspect()).contains("No inconsistencies found."));
      execute(new Close());
    }
  }

  /**
   * Add, delete and replace document with namespace in an empty database.
   */
  @Test public void gh1711() {
    query(_DB_CREATE.args(NAME, GH1711));
    query(_DB_PUT.args(NAME, GH1711, "/"));
    query(_DB_GET.args(NAME));
  }

  /**
   * Update, replace nodes: AIOOB.
   */
  @Test public void gh2208() {
    query(_DB_CREATE.args(NAME, " <a xmlns:a='a'>{ (1 to 4095) ! <b/> }</a>", "x.xml"));
    query(_DB_GET.args(NAME) +
        " update { replace node /a with <c>{ (1 to 4096) ! <d/> }</c> }");

    query(_DB_CREATE.args(NAME, " <a xmlns:a='a'>{ (1 to 4095) ! <b/> }</a>", "x.xml"));
    query(_DB_GET.args(NAME) +
        " update { replace node /a with <c xmlns:c='c'>{ (1 to 4096) ! <d/> }</c> }");

    query(_DB_CREATE.args(NAME, " <a xmlns:a='a'>{ (1 to 4095) ! <b/> }</a>", "x.xml"));
    query(_DB_GET.args(NAME) +
        " update { replace node /a with <c xmlns:c='c'>{ (1 to 4096) ! <d/> }</c> }");

    query(_DB_CREATE.args(NAME, " <a>{ (1 to 40950) ! <b/> }</a>", "x.xml"));
    query(_DB_GET.args(NAME) +
        " update { replace node /a with <c xmlns:c='c'>{ (1 to 40960) ! <d/> }</c> }");

    query(_DB_CREATE.args(NAME, " <a><b/></a>", "x.xml"));
    query(_DB_GET.args(NAME) +
        " update { replace node /a with <c xmlns:c='c'>{ (1 to 40960) ! <d/> }</c> }");
  }
}
