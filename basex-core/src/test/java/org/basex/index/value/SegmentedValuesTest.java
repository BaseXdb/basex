package org.basex.index.value;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.index.*;
import org.basex.io.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;

/**
 * Tests the incremental updates of the segmented value indexes.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class SegmentedValuesTest extends SandboxTest {
  /** Segment threshold of the tests. */
  private static final int THRESHOLD = 20;
  /** Default segment threshold. */
  private static final int DEFAULT = SegmentedValues.threshold;
  /** Second database. */
  private static final String NAME2 = NAME + '2';
  /** Value index types. */
  private static final IndexType[] TYPES = { IndexType.TEXT, IndexType.ATTRIBUTE,
    IndexType.TOKEN };

  /** Lowers the segment threshold. */
  @BeforeAll public static void start() {
    SegmentedValues.threshold = THRESHOLD;
  }

  /** Restores the segment threshold. */
  @AfterAll public static void stop() {
    SegmentedValues.threshold = DEFAULT;
  }

  /** Prepares a test. */
  @BeforeEach public void before() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.TOKENINDEX, true);
  }

  /** Finalizes a test. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    execute(new DropDB(NAME2));
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.TOKENINDEX, false);
    set(MainOptions.AUTOFLUSH, true);
    set(MainOptions.AUTOOPTIMIZE, false);
    set(MainOptions.TEXTINCLUDE, "");
  }

  /**
   * An entry that is changed twice in one transaction.
   */
  @Test public void changedTwice() {
    execute(new CreateDB(NAME, "<x><a>one</a><a b='p q'>two</a></x>"));
    final String db = _DB_GET.args(NAME);
    query("let $a := " + db + "//a[. = 'one'] return (" +
      "replace value of node $a with 'three', rename node $a as 'c')");
    text("one");
    text("three", "three");
    text("two", "two");
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
    inspect();
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
   * Segments are written and merged; the buffer is replayed after the database is reopened.
   */
  @Test public void segments() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Open(NAME));
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 40; i++) {
      query("insert node (for $j in 1 to 7 return <a t='t{ $j } u{ " + i + " }'>v{ " + i +
        " mod 9 }</a>) into " + db + "/x");
      if(i % 5 == 0) query("delete node (" + db + "//a)[1]");
    }
    final MetaData meta = context.data().meta;
    for(final IndexType type : TYPES) assertNotNull(meta.segments.get(type), type.name());
    final String info = execute(new InfoIndex());
    assertTrue(info.contains("Segments: "), info);
    checkAll();

    // the database exceeds the threshold: segments and buffer are kept on close
    execute(new Close());
    execute(new Open(NAME));
    assertNotNull(context.data().meta.segments.get(IndexType.TEXT));
    checkAll();
    inspect();

    // optimize: all segments are merged into the base structure
    execute(new Optimize());
    for(final IndexType type : TYPES) {
      assertNull(context.data().meta.segments.get(type), type.name());
      assertNull(context.data().meta.buffers.get(type), type.name());
    }
    checkAll();
    execute(new Close());
    assertTrue(Arrays.stream(files()).noneMatch(f -> f.matches("(txt|atv|tok)(\\d+.|b|p)")),
      Arrays.toString(files()));
  }

  /**
   * Keys of the base structure are pinned if their anchors are deleted or changed.
   */
  @Test public void pins() {
    final StringBuilder sb = new StringBuilder("<x>");
    for(int i = 0; i < THRESHOLD; i++) sb.append("<a t='t" + i % 3 + "'>v" + i % 4 + "</a>");
    execute(new CreateDB(NAME, sb.append("</x>").toString()));
    final String db = _DB_GET.args(NAME);
    // delete the first node of each value, change the second one
    query("delete node " + db + "//a[position() <= 4]");
    query("for $a in (" + db + "//a)[position() <= 4] return (" +
      "replace value of node $a with 'w', replace value of node $a/@t with 'changed')");
    execute(new Open(NAME));
    assertTrue(execute(new InfoIndex()).contains("Pinned: "));
    execute(new Close());
    for(int i = 0; i < 4; i++) check("//a[text() = 'v" + i + "']");
    check("//a[text() = 'w']");
    check("//a[@t = 't1']");
    check("//a[contains-token(@t, 'changed')]");
    entries();
    inspect();
    // reopened database: pins are persistent
    execute(new Close());
    for(int i = 0; i < 4; i++) check("//a[text() = 'v" + i + "']");
    entries();
  }

  /**
   * Range queries and index entries span all segments and the buffer.
   */
  @Test public void ranges() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Open(NAME));
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 30; i++) {
      query("insert node <a n='" + i * 7 % 30 + "'>" + i * 3 % 20 + "</a> into " + db + "/x");
      if(i % 6 == 0) query("delete node (" + db + "//a)[last()]");
      if(i % 7 == 0) query("replace value of node (" + db + "//a)[1] with '5'");
    }
    for(final String path : new String[] {
      "//a[text() >= '11' and text() <= '15']", "//a[text() > '1' and text() < '5']",
      "//a[@n >= 3 and @n <= 12]", "//a[@n > '2' and @n < '8']", "//a[text() = '5']"
    }) {
      check(path);
    }
    entries();
    query("count(" + _INDEX_TEXTS.args(NAME, "1") + ")",
        query("count(distinct-values(" + db + "//text()[starts-with(., '1')]))"));
    query("count(" + _INDEX_TEXTS.args(NAME, "15", true) + ")",
        query("count(distinct-values(" + db + "//text()[. >= '15']))"));
    query("count(" + _INDEX_TEXTS.args(NAME, "15", false) + ")",
        query("count(distinct-values(" + db + "//text()[. < '15']))"));
  }

  /**
   * Result sizes are only derived from exact counts.
   */
  @Test public void sizes() {
    // more references than are counted exactly
    execute(new CreateDB(NAME, "<x>" + "<a>v</a>".repeat(300) + "<a>w</a></x>"));
    execute(new Open(NAME));
    final String db = _DB_GET.args(NAME);
    // dead references remain in the base structure
    query("delete node " + db + "//a[position() < 300]");
    query("replace value of node " + db + "//a[. = 'w'] with 'v'");
    query("count(" + _DB_TEXT.args(NAME, "v") + ")", 2);
    query("exists(" + _DB_TEXT.args(NAME, "v") + ")", true);
    query(_DB_TEXT.args(NAME, "v") + "[2] ! string()", "v");
    query(_DB_TEXT.args(NAME, "v") + "[3] ! string()", "");
    query(_DB_TEXT.args(NAME, "w") + " ! string()", "");
    query("count(" + db + "//a[text() = 'v'])", 2);
    query("(" + db + "//a[text() = 'v'])[last()] ! string()", "v");
    query("exists(" + db + "//a[text() = 'w'])", false);
  }

  /**
   * Large inserted subtrees are written as segments of their own.
   */
  @Test public void largeSubtree() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Open(NAME));
    final String db = _DB_GET.args(NAME);
    query("insert node <y>{ (1 to 100) ! <a t='t{ . mod 5 } u'>v{ . mod 7 }</a> }</y> into " +
      db + "/x");
    query("replace value of node (" + db + "//a)[1] with 'v3'");
    checkAll();
    execute(new Close());
    checkAll();
  }

  /**
   * Optimization writes the same files as a new database with the same node IDs.
   * @throws IOException I/O exception
   */
  @Test public void optimize() throws IOException {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 100; i++) {
      query("insert node <a t='t" + i % 7 + " u'>v" + i % 5 + "</a> into " + db + "/x");
    }
    query("for $a in " + db + "//a[. = 'v1'] return replace value of node $a with 'v2'");
    execute(new Open(NAME));
    execute(new Optimize());
    assertEquals(EnumSet.of(IndexType.TEXT, IndexType.ATTRIBUTE, IndexType.TOKEN),
        context.data().meta.optimized);
    text("v1");
    text("v2", Collections.nCopies(40, "v2").toArray(String[]::new));
    token("t3", Collections.nCopies(14, "t3 u").toArray(String[]::new));
    check("//a[text() = 'v2']");
    check("//a[@t = 't4 u']");
    check("//a[contains-token(@t, 't6')]");

    final String doc = query(db + " => serialize()");
    execute(new CreateDB(NAME2, doc));
    for(final String file : new String[] { "txtl", "txtr", "atvl", "atvr", "tokl", "tokr" }) {
      assertArrayEquals(file(NAME2, file).read(), file(NAME, file).read(), file);
    }
  }

  /**
   * Partial index structures of the builder are merged into the same files as a single one.
   * @throws IOException I/O exception
   */
  @Test public void builderSplits() throws IOException {
    final String input =
      "<x>{ (1 to 5000) ! <a t='t{ . mod 13 } u{ . mod 7 }'>v{ . mod 101 }</a> }</x>";
    final String[] files = { "txtl", "txtr", "atvl", "atvr", "tokl", "tokr" };
    for(final boolean updindex : new boolean[] { false, true }) {
      set(MainOptions.UPDINDEX, updindex);
      execute(new CreateDB(NAME2));
      execute(new Close());
      query(_DB_ADD.args(NAME2, " " + input, "x.xml"));
      query(_DB_OPTIMIZE.args(NAME2, true));
      DiskValuesBuilder.splitKeys = 20;
      try {
        execute(new CreateDB(NAME));
        execute(new Close());
        query(_DB_ADD.args(NAME, " " + input, "x.xml"));
        query(_DB_OPTIMIZE.args(NAME, true));
      } finally {
        DiskValuesBuilder.splitKeys = 0;
      }
      for(final String file : files) {
        assertArrayEquals(file(NAME2, file).read(), file(NAME, file).read(), file);
      }
      checkAll();
    }
  }

  /**
   * Automatic optimization keeps the layout of older versions.
   */
  @Test public void autoOptimize() {
    set(MainOptions.AUTOOPTIMIZE, true);
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Open(NAME));
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 30; i++) {
      query("insert node <a t='t" + i % 3 + "'>v" + i % 3 + "</a> into " + db + "/x");
      assertTrue(context.data().meta.segments.isEmpty());
    }
    text("v1", Collections.nCopies(10, "v1").toArray(String[]::new));
    checkAll();
    inspect();
  }

  /**
   * A small database is written in the layout of older versions when it is closed.
   */
  @Test public void closeSmall() {
    execute(new CreateDB(NAME, "<x><a t='u'>v</a></x>"));
    execute(new Open(NAME));
    query("insert node <a t='w'>x</a> into " + _DB_GET.args(NAME) + "/x");
    assertFalse(context.data().meta.segments.isEmpty());
    execute(new Close());
    assertTrue(Arrays.stream(files()).noneMatch(f -> f.matches("(txt|atv|tok)(\\d+.|b|p)")),
      Arrays.toString(files()));
    text("x", "x");
    token("w", "w");
  }

  /**
   * Discards log records that were written after the last commit.
   * @throws IOException I/O exception
   */
  @Test public void reopenUncommitted() throws IOException {
    execute(new CreateDB(NAME, "<x><a>one</a>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Close());
    query("insert node <a>two</a> into " + _DB_GET.args(NAME) + "/x");
    query("insert node <a>three</a> into " + _DB_GET.args(NAME) + "/x");
    final IOFile log = file(NAME, "txtb");
    final long length = log.length();
    assertTrue(length > 0);
    // complete record that was written after the last commit: node 5, key 'bad' at position 0
    try(FileOutputStream out = new FileOutputStream(log.file(), true)) {
      out.write(new byte[] { 5, 1, 3, 'b', 'a', 'd', 0 });
    }
    text("bad");
    assertEquals(length, log.length());
    text("two", "two");
    text("three", "three");
    text("one", "one");
  }

  /**
   * Discards an incomplete last record if the log is shorter than its committed length.
   * @throws IOException I/O exception
   */
  @Test public void incompleteRecord() throws IOException {
    execute(new CreateDB(NAME, "<x><a>one</a>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Close());
    query("insert node <a>two</a> into " + _DB_GET.args(NAME) + "/x");
    query("insert node <a>three</a> into " + _DB_GET.args(NAME) + "/x");
    final IOFile log = file(NAME, "txtb");
    final byte[] bytes = log.read();
    log.write(Arrays.copyOf(bytes, bytes.length - 1));
    text("two", "two");
    text("three");
    // record of the last unit: id, count, key length, key, position
    assertEquals(bytes.length - 9, log.length());
  }

  /**
   * Files that are not listed in the meta data are removed.
   * @throws IOException I/O exception
   */
  @Test public void orphans() throws IOException {
    execute(new CreateDB(NAME, "<x><a>one</a>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Close());
    query("insert node <a>two</a> into " + _DB_GET.args(NAME) + "/x");
    final IOFile[] orphans = { file(NAME, "txt99l"), file(NAME, "atv7s"), file(NAME, "toktmp0t") };
    for(final IOFile orphan : orphans) orphan.write(new byte[] { 0 });
    execute(new Open(NAME));
    for(final IOFile orphan : orphans) assertFalse(orphan.exists(), orphan.name());
    text("one", "one");
    text("two", "two");
  }

  /**
   * Segments are written without autoflush; the database is consistent after it is closed.
   */
  @Test public void noAutoflush() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    set(MainOptions.AUTOFLUSH, false);
    execute(new Open(NAME));
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 30; i++) {
      query("insert node (1 to 5) ! <a t='t{ . } u" + i + "'>v" + i % 4 + "</a> into " +
        db + "/x");
      if(i % 4 == 0) query("delete node " + db + "//a[1]");
      if(i % 3 == 0) query("replace value of node " + db + "//a[last()] with 'v9'");
    }
    checkAll();
    execute(new Close());
    execute(new Open(NAME));
    checkAll();
    inspect();
  }

  /**
   * A database that is created without input.
   */
  @Test public void emptyDatabase() {
    execute(new CreateDB(NAME));
    execute(new Open(NAME));
    query(_DB_ADD.args(NAME, " <x>" + "<a t='u v'>w</a>".repeat(THRESHOLD) + "</x>", "x.xml"));
    text("w", Collections.nCopies(THRESHOLD, "w").toArray(String[]::new));
    execute(new Close());
    checkAll();
    query(_DB_DELETE.args(NAME, "x.xml"));
    text("w");
    token("u");
    inspect();
  }

  /**
   * Empty values, values exceeding the maximum length, and long tokens.
   */
  @Test public void specialValues() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Close());
    final String db = _DB_GET.args(NAME);
    final String longValue = "l".repeat(200), longToken = "k".repeat(150);
    for(int i = 0; i < 12; i++) {
      query("insert node <a t='' u='" + longValue + "' v='" + longToken + " s" + i % 3 +
        "'>" + (i % 2 == 0 ? longValue : "v" + i % 4) + "</a> into " + db + "/x");
    }
    query("replace value of node (" + db + "//a)[2] with '" + longValue + "'");
    query("replace value of node (" + db + "//a)[1] with 'v1'");
    query("replace value of node (" + db + "//a)[3]/@t with 'filled'");
    check("//a[@t = '']");
    check("//a[@t = 'filled']");
    check("//a[text() = 'v1']");
    check("//a[text() = '" + longValue + "']");
    check("//a[@u = '" + longValue + "']");
    check("//a[contains-token(@v, '" + longToken + "')]");
    check("//a[contains-token(@v, 's1')]");
    entries();
    execute(new Open(NAME));
    execute(new Optimize());
    execute(new Close());
    check("//a[@t = '']");
    check("//a[contains-token(@v, '" + longToken + "')]");
    entries();
  }

  /**
   * Values with multi-byte characters: keys are sorted by their bytes in all segments.
   */
  @Test public void unicode() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "<a>ä</a><a>z</a></x>"));
    execute(new Close());
    final String db = _DB_GET.args(NAME);
    final String[] values = { "é", "a", "😀", "Ω", "ab", "zz", "ä", "ß", "日本" };
    for(final String value : values) {
      query("insert node <a t='" + value + "'>" + value + "</a> into " + db + "/x");
      query("insert node <a>" + value + "x</a> into " + db + "/x");
    }
    query("delete node " + db + "//a[. = 'z']");
    for(final String value : values) check("//a[text() = '" + value + "']");
    check("//a[text() >= 'a' and text() <= 'zz']");
    check("//a[text() > 'ä' and text() < '😀']");
    check("//a[@t >= 'Ω']");
    entries();
    query("string-join(" + _INDEX_TEXTS.args(NAME, "ä") + ", ',')", "ä,äx");
  }

  /**
   * Many references of the same value in all segments.
   */
  @Test public void duplicates() {
    execute(new CreateDB(NAME, "<x>" + "<a>v</a>".repeat(400) + "</x>"));
    execute(new Close());
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 5; i++) {
      query("insert node (1 to 100) ! <a>v</a> into " + db + "/x");
      query("delete node " + db + "//a[position() mod 7 = " + i + "]");
      query("for $a in " + db + "//a[position() mod 11 = " + i + "] " +
        "return replace value of node $a with 'w'");
    }
    final String count = query("count(" + db + "//a[string() = 'v'])");
    query("count(" + _DB_TEXT.args(NAME, "v") + ")", count);
    query("count(" + db + "//a[text() = 'v'])", count);
    query("exists(" + _DB_TEXT.args(NAME, "v") + "[" + count + "])", true);
    query("exists(" + _DB_TEXT.args(NAME, "v") + "[" + count + " + 1])", false);
    entries();
  }

  /**
   * Indexes of a segmented database are dropped, created and rebuilt.
   */
  @Test public void dropCreate() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "</x>"));
    execute(new Close());
    final String db = _DB_GET.args(NAME);
    for(int i = 0; i < 10; i++) {
      query("insert node <a t='t" + i % 3 + "'>v" + i % 4 + "</a> into " + db + "/x");
    }
    execute(new Open(NAME));
    execute(new DropIndex(CmdIndex.TEXT));
    assertNull(context.data().meta.segments.get(IndexType.TEXT));
    assertFalse(Arrays.stream(files()).anyMatch(f -> f.matches("txt(\\d*[lrsp]|b)")),
      Arrays.toString(files()));
    execute(new CreateIndex(CmdIndex.TEXT));
    assertNull(context.data().meta.segments.get(IndexType.TEXT));
    execute(new Close());
    checkAll();

    // changed include option: indexes are rebuilt
    query(_DB_OPTIMIZE.args(NAME, false, " { 'attrinclude': 't' }"));
    checkAll();
    query("insert node <a t='t9'>v9</a> into " + db + "/x");
    checkAll();

    // full optimization: layout of older versions
    query(_DB_OPTIMIZE.args(NAME, true));
    assertTrue(Arrays.stream(files()).noneMatch(f -> f.matches("(txt|atv|tok)(\\d+.|b|p)")),
      Arrays.toString(files()));
    checkAll();

    // copied, backed up and restored databases
    query("insert node <a t='t8'>v8</a> into " + db + "/x");
    query(_DB_COPY.args(NAME, NAME2));
    query(_DB_CREATE_BACKUP.args(NAME));
    query("insert node <a t='t7'>v7</a> into " + db + "/x");
    query(_DB_RESTORE.args(NAME));
    text("v8", "v8");
    text("v7");
    query("count(" + _DB_TEXT.args(NAME2, "v8") + ")", 1);
    query(_DB_DROP_BACKUP.args(NAME));
  }

  /**
   * Nodes that are inserted and deleted, or changed repeatedly, in the same transaction.
   */
  @Test public void sameTransaction() {
    execute(new CreateDB(NAME, "<x>" + "<b/>".repeat(THRESHOLD) + "<a>one</a></x>"));
    execute(new Close());
    final String db = _DB_GET.args(NAME);
    query("insert node <a t='x'>two</a> into " + db + "/x, delete node " + db + "//a[. = 'one']");
    query("delete node " + db + "//a[. = 'two'], insert node <a>three</a> into " + db + "/x");
    for(int i = 0; i < 5; i++) {
      final String[] values = i % 2 == 0 ? new String[] { "three", "four" } :
        new String[] { "four", "three" };
      query("let $a := " + db + "//a[. = '" + values[0] + "'] return (" +
        "replace value of node $a with '" + values[1] + "', rename node $a as 'c')");
      query("rename node " + db + "//c as 'a'");
    }
    text("one");
    text("two");
    text("three");
    text("four", "four");
    checkAll();
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
   * Compares all text, attribute and token lookups with a scan.
   */
  private static void checkAll() {
    final String db = _DB_GET.args(NAME);
    for(final String value : query("distinct-values(" + db + "//a/text())").split("\n")) {
      if(!value.isEmpty()) check("//a[text() = '" + value + "']");
    }
    for(final String value : query("distinct-values(" + db + "//@t)").split("\n")) {
      if(!value.isEmpty()) check("//a[@t = '" + value + "']");
    }
    for(final String value : query("distinct-values(" + db + "//@t ! tokenize(.))").split("\n")) {
      if(!value.isEmpty()) check("//a[contains-token(@t, '" + value + "')]");
    }
    entries();
  }

  /**
   * Compares the index entries with the values of the database.
   */
  private static void entries() {
    final String db = _DB_GET.args(NAME);
    query("string-join(" + _INDEX_TEXTS.args(NAME) + " ! (. || ':' || @count), ' ')",
      query("string-join(for $t in " + db + "//text()[string-length() <= 96] " +
        "group by $v := string($t) " +
        "order by $v return $v || ':' || count($t), ' ')"));
    query("string-join(" + _INDEX_ATTRIBUTES.args(NAME) + " ! (. || ':' || @count), ' ')",
      query("string-join(for $t in " + db + "//@*[string-length() <= 96] " +
        "group by $v := string($t) " +
        "order by $v return $v || ':' || count($t), ' ')"));
  }

  /**
   * Checks that the inspection of the database finds no issues.
   */
  private static void inspect() {
    query(_DB_INSPECT.args(NAME) + "?issues ! (?check || ': ' || ?count)", "");
  }

  /**
   * Compares index access with a scan.
   * @param path path
   */
  private static void check(final String path) {
    queryIndexScan(path);
  }

  /**
   * Returns the names of the files of the test database.
   * @return file names without suffix
   */
  private static String[] files() {
    return Arrays.stream(context.soptions.dbPath(NAME).children()).
      map(f -> f.name().replace(IO.BASEXSUFFIX, "")).toArray(String[]::new);
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
}
