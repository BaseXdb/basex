package org.basex.index;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the deferred updates of the value indexes: additions and key removals are applied
 * once per transaction.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class UpdatableDiskValuesTest extends SandboxTest {
  /** Second database. */
  private static final String NAME2 = NAME + '2';

  /** Prepares a test. */
  @BeforeEach public void before() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, true);
  }

  /** Finalizes a test. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.AUTOFLUSH, true);
    set(MainOptions.AUTOOPTIMIZE, false);
  }

  /**
   * An entry that is changed twice in one transaction (a rename re-adds the entries of a
   * node): the second change discards the pending addition of the first one.
   */
  @Test public void changedTwice() {
    execute(new CreateDB(NAME, "<x><a>one</a><a b='p q'>two</a></x>"));
    final String db = _DB_GET.args(NAME);
    query("let $a := " + db + "//a[. = 'one'] return (" +
      "replace value of node $a with 'three', rename node $a as 'c')");
    text("one");
    text("three", "three");
    text("two", "two");
    // same for tokens
    query("let $b := " + db + "//@b return (" +
      "replace value of node $b with 'q r', rename node $b as 'd')");
    token("p");
    token("q", "q r");
    token("r", "q r");
    check("//c[text() = 'three']");
    check("//a[contains-token(@d, 'r')]");
  }

  /**
   * A key that is emptied and re-added in one transaction.
   */
  @Test public void emptiedAndReadded() {
    execute(new CreateDB(NAME, "<x><a>one</a><a>two</a><a>three</a></x>"));
    final String db = _DB_GET.args(NAME);
    // 'one' is emptied and re-added by another node, 'two' is emptied and stays empty
    query("replace value of node " + db + "//a[. = 'one'] with 'x', " +
      "replace value of node " + db + "//a[. = 'two'] with 'one'");
    text("one", "one");
    text("two");
    text("x", "x");
    text("three", "three");
    check("//a[text() = 'one']");
    // renaming an element and back yields the original state
    set(MainOptions.TEXTINCLUDE, "a");
    execute(new CreateDB(NAME, "<x><a>one</a><b>two</b></x>"));
    query("rename node " + db + "//a as 'c', rename node " + db + "//b as 'a'");
    text("one");
    text("two", "two");
    query("rename node " + db + "//c as 'a', rename node " + db + "//a[. = 'two'] as 'b'");
    text("one", "one");
    text("two");
    set(MainOptions.TEXTINCLUDE, "");
  }

  /**
   * Many nodes are inserted, changed and deleted in single transactions.
   */
  @Test public void bulk() {
    execute(new CreateDB(NAME, "<x/>"));
    final String db = _DB_GET.args(NAME);
    query("insert node (for $i in 1 to 500 return <a t='t{ $i } u'>v{ $i }</a>) into " +
      db + "/x");
    check("//a[text() = 'v250']");
    check("//a[contains-token(@t, 't250')]");
    query("for $a in " + db + "//a return replace value of node $a with 'w' || $a");
    text("v250");
    text("wv250", "wv250");
    check("//a[text() = 'wv250']");
    query("delete node " + db + "//a[position() mod 2 = 0]");
    check("//a[text() = 'wv250']");
    check("//a[text() = 'wv251']");
    check("//a[contains-token(@t, 'u')]");
    query("delete node " + db + "//a");
    text("wv251");
    token("u");
    query(_DB_INFO.args(NAME) + "//textindex/text()", true);
  }

  /**
   * Optimization compacts the index files and is skipped if the indexes are unchanged.
   * @throws IOException I/O exception
   */
  @Test public void optimize() throws IOException {
    execute(new CreateDB(NAME, "<x/>"));
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 100; i++) {
      query("insert node <a t='t" + i % 7 + " u'>v" + i % 5 + "</a> into " + db + "/x");
    }
    query("for $a in " + db + "//a[. = 'v1'] return replace value of node $a with 'v2'");
    final String[] files = { "txtl", "atvl", "tokl", "txtr" };
    final long[] sizes = sizes(files);

    execute(new Optimize());
    final MetaData meta = context.data().meta;
    assertEquals(EnumSet.of(IndexType.TEXT, IndexType.ATTRIBUTE, IndexType.TOKEN),
        meta.optimized);
    final long[] optimized = sizes(files);
    for(int f = 0; f < files.length; f++) assertTrue(optimized[f] < sizes[f], files[f]);
    text("v1");
    text("v2", Collections.nCopies(40, "v2").toArray(String[]::new));
    token("t3", Collections.nCopies(14, "t3 u").toArray(String[]::new));
    check("//a[text() = 'v2']");
    check("//a[@t = 't4 u']");
    check("//a[contains-token(@t, 't6')]");

    // the files are identical to those of a new database with the same node IDs
    final String doc = query(db + " => serialize()");
    execute(new CreateDB(NAME2, doc));
    for(final String file : files) {
      assertArrayEquals(file(NAME2, file).read(), file(NAME, file).read(), file);
    }
    execute(new DropDB(NAME2));

    // unchanged indexes are skipped, even if they could be compacted
    execute(new Open(NAME));
    query("insert node <a t='x'>y</a> into " + db + "/x");
    query("delete node " + db + "//a[. = 'y']");
    assertEquals(EnumSet.noneOf(IndexType.class), context.data().meta.optimized);
    context.data().meta.optimized.add(IndexType.TEXT);
    execute(new Optimize());
    final long[] skipped = sizes(files);
    assertTrue(skipped[0] > optimized[0]);
    assertEquals(optimized[1], skipped[1]);
    execute(new Close());
    execute(new Open(NAME));
    assertEquals(EnumSet.of(IndexType.TEXT, IndexType.ATTRIBUTE, IndexType.TOKEN),
        context.data().meta.optimized);
  }

  /**
   * Optimization compacts index files that span several blocks.
   */
  @Test public void optimizeBlocks() {
    execute(new CreateDB(NAME, "<x/>"));
    // closed database: free slots are dropped after each transaction
    execute(new Close());
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 300; i++) {
      query("insert node <a t='t" + i % 3 + "'>v" + i % 3 + "</a> into " + db + "/x");
    }
    final long size = sizes("txtl")[0];
    assertTrue(size > 3 * IO.BLOCKSIZE, Long.toString(size));
    execute(new Open(NAME));
    execute(new Optimize());
    assertTrue(sizes("txtl")[0] < IO.BLOCKSIZE);
    execute(new Close());
    execute(new Open(NAME));
    text("v1", Collections.nCopies(100, "v1").toArray(String[]::new));
    check("//a[text() = 'v2']");
    check("//a[@t = 't0']");
    query("insert node <a t='t0'>v0</a> into " + db + "/x");
    check("//a[text() = 'v0']");
    check("//a[contains-token(@t, 't0')]");
    query(_DB_INSPECT.args(NAME) + "?valid", true);
  }

  /**
   * Automatic optimization compacts index files.
   */
  @Test public void autoOptimize() {
    set(MainOptions.AUTOOPTIMIZE, true);
    execute(new CreateDB(NAME, "<x/>"));
    // closed database: free slots are dropped after each transaction
    execute(new Close());
    final String db = _DB_GET.args(NAME);
    final String[] files = { "txtl", "atvl", "tokl" };
    final long[] max = new long[files.length];
    for(int i = 0; i < 300; i++) {
      query("insert node <a t='t" + i % 3 + "'>v" + i % 3 + "</a> into " + db + "/x");
      final long[] sizes = sizes(files);
      for(int f = 0; f < files.length; f++) max[f] = Math.max(max[f], sizes[f]);
    }
    // without compaction, the text index file exceeds three blocks (see optimizeBlocks)
    for(int f = 0; f < files.length; f++) assertTrue(max[f] < IO.BLOCKSIZE, files[f]);

    execute(new Open(NAME));
    text("v1", Collections.nCopies(100, "v1").toArray(String[]::new));
    check("//a[text() = 'v2']");
    check("//a[@t = 't0']");
    check("//a[contains-token(@t, 't1')]");
    query(_DB_INSPECT.args(NAME) + "?valid", true);
  }

  /**
   * Pending changes are written when the database is closed without autoflush.
   */
  @Test public void closeWithoutFlush() {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    set(MainOptions.AUTOFLUSH, false);
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'two'");
    execute(new Close());
    text("one");
    text("two", "two");
    execute(new Close());
    execute(new Open(NAME));
    text("two", "two");
  }

  /**
   * Checks a text index lookup.
   * @param text text
   * @param results expected results
   */
  private static void text(final String text, final String... results) {
    query(_DB_TEXT.args(NAME, text) + " ! string()", String.join("\n", results));
  }

  /**
   * Checks a token index lookup.
   * @param token token
   * @param results expected results
   */
  private static void token(final String token, final String... results) {
    query(_DB_TOKEN.args(NAME, token) + " ! string()", String.join("\n", results));
  }

  /**
   * Returns the sizes of database files.
   * @param names file names
   * @return sizes
   */
  private static long[] sizes(final String... names) {
    return Arrays.stream(names).mapToLong(name -> file(NAME, name).length()).toArray();
  }

  /**
   * Returns a database file.
   * @param db database
   * @param name file name
   * @return file
   */
  private static IOFile file(final String db, final String name) {
    return new IOFile(context.soptions.dbPath(db), name + IO.BASEXSUFFIX);
  }

  /**
   * Compares index access with a scan.
   * @param path path
   */
  private static void check(final String path) {
    queryIndexScan(path);
  }
}
