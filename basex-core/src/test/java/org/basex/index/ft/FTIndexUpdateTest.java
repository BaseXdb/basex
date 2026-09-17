package org.basex.index.ft;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.io.in.DataInput;
import org.basex.util.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Tests the incremental updates of the full-text index.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FTIndexUpdateTest extends SandboxTest {
  /** Segment threshold of the tests. */
  private static final int THRESHOLD = 20;
  /** Default segment threshold. */
  private static final int DEFAULT = FTIndex.threshold;
  /** Words that fill the buffer. */
  private static final String WORDS =
      " x1 x2 x3 x4 x5 x6 x7 x8 x9 x10 x11 x12 x13 x14 x15 x16 x17 x18 x19 x20";

  /** Lowers the segment threshold. */
  @BeforeAll public static void start() {
    FTIndex.threshold = THRESHOLD;
  }

  /** Restores the segment threshold. */
  @AfterAll public static void stop() {
    FTIndex.threshold = DEFAULT;
  }

  /** Prepares a test. */
  @BeforeEach public void before() {
    set(MainOptions.UPDINDEX, true);
    set(MainOptions.FTINDEX, true);
  }

  /** Finalizes a test. */
  @AfterEach public void after() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.FTINDEX, false);
    set(MainOptions.FTMIXED, false);
    set(MainOptions.FTINCLUDE, "");
    set(MainOptions.STOPWORDS, "");
    set(MainOptions.AUTOFLUSH, true);
    set(MainOptions.AUTOOPTIMIZE, false);
    FTBuilder.splitTokens = 0;
  }

  /**
   * Text changes, insertions, deletions and renames.
   * @param mixed mixed-content flag
   */
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  public void updates(final boolean mixed) {
    set(MainOptions.FTMIXED, mixed);
    set(MainOptions.FTINCLUDE, mixed ? "a,b" : "");
    execute(new CreateDB(NAME, "<x><a>first entry</a><b>second entry</b><c>third one</c></x>"));
    search("entry", "first entry", "second entry");
    ftindex(true);

    query("replace value of node " + _DB_GET.args(NAME) + "//b with 'new entry'");
    ftindex(true);
    search("entry", "first entry", "new entry");
    search("second");
    check("entry");

    query("insert node <b>fourth entry here</b> into " + _DB_GET.args(NAME) + "/x");
    search("fourth", "fourth entry here");
    check("entry");

    query("delete node " + _DB_GET.args(NAME) + "//a");
    search("first");
    search("entry", "new entry", "fourth entry here");
    check("entry");

    // rename out of the inclusion, and back (all names are included if not mixed)
    query("rename node " + _DB_GET.args(NAME) + "//b[. = 'new entry'] as 'c'");
    search("new", mixed ? new String[0] : new String[] { "new entry" });
    query("rename node " + _DB_GET.args(NAME) + "//c[. = 'third one'] as 'a'");
    search("third", "third one");
    check("entry");
    check("third");

    execute(new Optimize());
    search("entry", mixed ? new String[] { "fourth entry here" } :
      new String[] { "new entry", "fourth entry here" });
    check("entry");
    ftindex(true);
  }

  /**
   * Text nodes without an included parent are excluded.
   */
  @Test public void include() {
    set(MainOptions.FTINCLUDE, "a");
    execute(new CreateDB(NAME, "<x><a>one</a><b>two</b></x>"));
    search("one", "one");
    search("two");
    query("rename node " + _DB_GET.args(NAME) + "//a as 'b'");
    query("rename node " + _DB_GET.args(NAME) + "//b[. = 'two'] as 'a'");
    search("one");
    search("two", "two");
    query("replace value of node " + _DB_GET.args(NAME) + "//b with 'three'");
    search("three");
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'four'");
    search("four", "four");
    check("four");
  }

  /**
   * Changes below an included element are reflected in its string value.
   */
  @Test public void mixed() {
    set(MainOptions.FTMIXED, true);
    set(MainOptions.FTINCLUDE, "p");
    execute(new CreateDB(NAME, "<x><p>one <b>two</b> three</p><q>four</q></x>"));
    search("two", "one two three");
    search("four");

    query("replace value of node " + _DB_GET.args(NAME) + "//b with 'five'");
    search("two");
    search("five", "one five three");
    query("insert node <i> six</i> into " + _DB_GET.args(NAME) + "//b");
    search("six", "one five six three");
    query("delete node " + _DB_GET.args(NAME) + "//b");
    search("five");
    search("six");
    search("three", "one  three");
    // nested included elements are units of their own
    query("insert node <p> seven</p> into " + _DB_GET.args(NAME) + "//p");
    search("seven", "one  three seven", " seven");
    query("rename node " + _DB_GET.args(NAME) + "/x/p as 'q'");
    search("three");
    search("seven", " seven");
    check("seven");
    // a text node inserted next to an included element
    query("insert node 'eight' after " + _DB_GET.args(NAME) + "//p");
    search("eight");
    query("insert node 'nine' into " + _DB_GET.args(NAME) + "//p");
    search("nine");
    search("sevennine", " sevennine");
  }

  /**
   * A unit that is changed twice, or changed and deleted, in one transaction.
   */
  @Test public void sameTransaction() {
    execute(new CreateDB(NAME, "<x><a>one</a><b>two</b></x>"));
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'three', " +
        "delete node " + _DB_GET.args(NAME) + "//b");
    search("one");
    search("two");
    search("three", "three");
    query("let $a := " + _DB_GET.args(NAME) + "//a return (" +
        "replace value of node $a with 'four', insert node <c>five</c> into $a/..)");
    search("three");
    search("four", "four");
    search("five", "five");
    // a text node changed and its parent deleted, and a subtree inserted and changed
    query("let $c := " + _DB_GET.args(NAME) + "//c return (" +
        "replace value of node $c with 'six', delete node $c, " +
        "insert node <d>seven</d> into $c/..)");
    search("five");
    search("six");
    search("seven", "seven");
    check("seven");
  }

  /**
   * The same document is replaced repeatedly: segments are written and merged, and the log
   * does not grow.
   */
  @Test public void replaceRepeatedly() {
    execute(new CreateDB(NAME));
    int max = 0;
    for(int r = 0; r < 60; r++) {
      // vary the document: replacing an identical document is a no-op
      final StringBuilder doc = new StringBuilder("<x><a>run" + r + "</a>");
      for(int i = 0; i < 12; i++) doc.append("<a>word").append(i).append("</a>");
      doc.append("</x>");
      execute(new Put("doc", doc.toString()));
      search("word5", "word5");
      check("word5");
      max = Math.max(max, segments());
      assertTrue(max <= 9, "Segments: " + max);
      assertTrue(file(FTIndex.LOG).length() < 2000, "Log grows");
    }
    assertTrue(max > 1, "Segments: " + max);
    execute(new Optimize());
    assertEquals(1, segments());
    search("word5", "word5");
    // dead references are gone
    tokens("word5", 1);
  }

  /**
   * A unit is superseded across segments, and segments are merged.
   */
  @Test public void supersede() {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    for(int r = 0; r < 40; r++) {
      final String word = "word" + r;
      query("replace value of node " + _DB_GET.args(NAME) + "//a with '" + word + "'");
      // fill the buffer, so that the change ends up in a new segment
      query("insert node <b>" + word + WORDS + "</b> into " + _DB_GET.args(NAME) + "/x");
      search(word, word, word + WORDS);
      if(r > 0) search("word" + (r - 1), "word" + (r - 1) + WORDS);
      check("x1");
      check(word);
      assertTrue(segments() <= 9, "Segments: " + segments());
    }
    // superseded references are counted until the segments are merged
    tokens("word38", 2);
    execute(new Optimize());
    assertEquals(1, segments());
    search("word39", "word39", "word39" + WORDS);
    tokens("word38", 1);
    tokens("x1", 40);
  }

  /**
   * Random updates, compared with a scan.
   */
  @Test public void random() {
    execute(new CreateDB(NAME, "<x/>"));
    final Random rnd = new Random(1);
    final String[] words = { "alpha", "beta", "gamma", "delta", "epsilon", "zeta", "eta" };
    for(int r = 0; r < 200; r++) {
      final String word = words[rnd.nextInt(words.length)];
      final String db = _DB_GET.args(NAME);
      switch(rnd.nextInt(5)) {
        case 0 -> query("insert node <a>" + word + " " + words[rnd.nextInt(words.length)] +
            "</a> into (" + db + "//*)[" + (rnd.nextInt(20) + 1) + "]");
        case 1 -> query("delete node (" + db + "//a)[" + (rnd.nextInt(20) + 1) + "]");
        case 2 -> query("replace value of node (" + db + "//a)[" + (rnd.nextInt(20) + 1) +
            "] with '" + word + "'");
        case 3 -> query("rename node (" + db + "//a)[" + (rnd.nextInt(20) + 1) + "] as 'b'");
        default -> query("insert node <a><a>" + word + "</a><b>" + word + "</b></a> into " +
            db + "/x");
      }
      check(word);
      check(words[rnd.nextInt(words.length)]);
      if(r % 50 == 49) {
        execute(new Close());
        execute(new Open(NAME));
      }
    }
    execute(new Optimize());
    for(final String word : words) check(word);
  }

  /**
   * Reopens the database with buffered references.
   * @param autoflush autoflush flag
   */
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  public void reopen(final boolean autoflush) {
    set(MainOptions.AUTOFLUSH, autoflush);
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'two'");
    query("insert node <a>three</a> into " + _DB_GET.args(NAME) + "/x");
    execute(new Close());
    execute(new Open(NAME));
    search("one");
    search("two", "two");
    search("three", "three");
    query("replace value of node " + _DB_GET.args(NAME) + "//a[. = 'two'] with 'four'");
    execute(new Close());
    search("two");
    search("four", "four");
    ftindex(true);
  }

  /**
   * Updates a database without accessing the buffer, and discards uncommitted log records.
   * @throws IOException I/O exception
   */
  @Test public void reopenUncommitted() throws IOException {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    execute(new Close());
    query("insert node <a>two</a> into " + _DB_GET.args(NAME) + "/x");
    query("insert node <a>three</a> into " + _DB_GET.args(NAME) + "/x");
    final IOFile log = file(FTIndex.LOG);
    final long length = log.length();
    // complete record that was written after the last commit: node 5, token 'bad' at position 0
    try(FileOutputStream out = new FileOutputStream(log.file(), true)) {
      out.write(new byte[] { 5, 1, 3, 'b', 'a', 'd', 0 });
    }
    search("bad");
    assertEquals(length, log.length());
    search("two", "two");
    search("three", "three");
    check("one");
  }

  /**
   * Discards an incomplete last record if the log is shorter than its committed length.
   * @param cut number of bytes cut off the log
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(ints = { 1, 3 })
  public void incompleteRecord(final int cut) throws IOException {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    query("insert node <a>two</a> into " + _DB_GET.args(NAME) + "/x");
    query("insert node <a>three</a> into " + _DB_GET.args(NAME) + "/x");
    execute(new Close());
    final IOFile log = file(FTIndex.LOG);
    final byte[] bytes = log.read();
    log.write(Arrays.copyOf(bytes, bytes.length - cut));
    search("two", "two");
    search("three");
    // record of the last unit: id, count, token length, token, position
    assertEquals(bytes.length - 9, log.length());
  }

  /**
   * Inserted units supersede no references of older segments.
   */
  @Test public void insertSupersedesNothing() {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    for(int r = 0; r < 3; r++) {
      query("insert node <b>word" + r + WORDS + "</b> into " + _DB_GET.args(NAME) + "/x");
    }
    assertEquals(4, segments());
    assertEquals(0, supersedeFiles());
    query("replace value of node " + _DB_GET.args(NAME) + "//b[1]/text() with 'two" + WORDS + "'");
    assertEquals(5, segments());
    assertEquals(1, supersedeFiles());
    search("word0");
    search("two", "two" + WORDS);
    check("x1");
  }

  /**
   * Files that are not listed in the meta data are removed.
   * @throws IOException I/O exception
   */
  @Test public void orphans() throws IOException {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    execute(new Close());
    final IOFile orphan = file("ftx99x");
    orphan.write(new byte[] { 0 });
    final IOFile tmp = file("ftxtmp0y");
    tmp.write(new byte[] { 0 });
    execute(new Open(NAME));
    assertFalse(orphan.exists());
    assertFalse(tmp.exists());
    search("one", "one");
  }

  /**
   * Optimizes the database in the same transaction as a text change.
   */
  @Test public void optimizeInTransaction() {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'two', " +
        _DB_OPTIMIZE.args(NAME));
    search("one");
    search("two", "two");
    ftindex(true);
    assertEquals(1, segments());
  }

  /**
   * Optimizes an unchanged index.
   */
  @Test public void optimizeUnchanged() {
    execute(new CreateDB(NAME, "<x><a>one</a><a>two</a><a>three</a></x>"));
    query("delete node " + _DB_GET.args(NAME) + "//a[. = 'two']");
    query("replace value of node " + _DB_GET.args(NAME) + "//a[. = 'one'] with 'four'");
    execute(new Optimize());
    final String segments = context.data().meta.ftsegments;
    assertEquals(1, segments());
    // attribute changes do not affect the full-text index
    query("insert node attribute b { 'c' } into " + _DB_GET.args(NAME) + "//a[1]");
    execute(new Optimize());
    assertEquals(segments, context.data().meta.ftsegments);
    execute(new Close());
    execute(new Open(NAME));
    execute(new Optimize());
    assertEquals(segments, context.data().meta.ftsegments);
    search("four", "four");
    search("two");
    check("three");
  }

  /**
   * Optimizes an index without segments.
   */
  @Test public void optimizeEmpty() {
    execute(new CreateDB(NAME, "<x/>"));
    query("insert node <a/> into " + _DB_GET.args(NAME) + "/x");
    assertEquals(0, segments());
    execute(new Optimize());
    assertTrue(file("ftxx").exists());
    ftindex(true);
    query("insert node <a>one</a> into " + _DB_GET.args(NAME) + "/x");
    search("one", "one");
    check("one");
  }

  /**
   * Automatic optimization merges the segments after each update.
   */
  @Test public void autooptimize() {
    set(MainOptions.AUTOOPTIMIZE, true);
    final StringBuilder doc = new StringBuilder("<x>");
    for(int i = 0; i < 200; i++) doc.append("<a>one</a>");
    doc.append("</x>");
    execute(new CreateDB(NAME, doc.toString()));
    for(int r = 0; r < 3; r++) {
      query("insert node <a>word" + r + WORDS + "</a> into " + _DB_GET.args(NAME) + "/x");
      check("x1");
      assertEquals(1, segments());
    }
    query("for $a in (" + _DB_GET.args(NAME) + "//a)[position() <= 50] " +
        "return replace value of node $a with 'two'");
    assertEquals(1, segments());
    check("x1");
    check("two");
    ftindex(true);
  }

  /**
   * A large document is indexed by the builder.
   */
  @Test public void largeInsert() {
    execute(new CreateDB(NAME, "<x><a>one</a></x>"));
    final StringBuilder doc = new StringBuilder("<y>");
    for(int i = 0; i < 100; i++) doc.append("<a>word").append(i).append(" one</a>");
    doc.append("</y>");
    query(_DB_ADD.args(NAME, " " + doc, "doc.xml"));
    assertEquals(2, segments());
    search("word7", "word7 one");
    check("one");
    query("replace value of node " + _DB_GET.args(NAME) + "//a[. = 'word7 one'] with 'two'");
    search("word7");
    search("two", "two");
    check("one");
    execute(new Optimize());
    assertEquals(1, segments());
    check("one");
  }

  /**
   * A large unit is indexed by the builder.
   */
  @Test public void largeUnit() {
    set(MainOptions.FTMIXED, true);
    set(MainOptions.FTINCLUDE, "p");
    final StringBuilder doc = new StringBuilder("<x><p>");
    for(int i = 0; i < 100; i++) doc.append("<a>word").append(i).append(" </a>");
    doc.append("</p><q>one</q></x>");
    execute(new CreateDB(NAME, doc.toString()));
    query("replace value of node " + _DB_GET.args(NAME) + "//a[. = 'word7 '] with 'two '");
    assertEquals(2, segments());
    search("word7");
    query(_FT_SEARCH.args(NAME, "two") + "/a[2]/string()", "word1 ");
    query("replace value of node " + _DB_GET.args(NAME) + "//a[. = 'word8 '] with 'three '");
    query(_FT_SEARCH.args(NAME, "two three") + "/a[2]/string()", "word1 ");
    query(_FT_SEARCH.args(NAME, "word8") + "/a[2]/string()", "");
    // the unit was indexed three times; superseded references are counted until the merge
    assertEquals(3, segments());
    tokens("word9", 3);
    execute(new Optimize());
    assertEquals(1, segments());
    tokens("word9", 1);
    query(_FT_SEARCH.args(NAME, "two three") + "/a[2]/string()", "word1 ");
  }

  /**
   * Many changes below one included element index it once.
   */
  @Test public void manyChanges() {
    set(MainOptions.FTMIXED, true);
    set(MainOptions.FTINCLUDE, "p");
    final StringBuilder doc = new StringBuilder("<x><p>");
    for(int i = 0; i < 1000; i++) doc.append("<a>w").append(i).append(" </a>");
    doc.append("</p></x>");
    execute(new CreateDB(NAME, doc.toString()));
    query("for $a in " + _DB_GET.args(NAME) + "//a return replace value of node $a with 'v '");
    // the unit was indexed once, in a single new segment
    assertEquals(2, segments());
    assertTrue(execute(new InfoIndex(CmdIndexInfo.FULLTEXT)).contains("Buffered: 0"));
    tokens("v", 1000);
    query(_FT_SEARCH.args(NAME, "v") + "/a[1]/string()", "v ");
    query(_FT_SEARCH.args(NAME, "w1") + "/a[1]/string()", "");
  }

  /**
   * The index is dropped if the lexer cannot be created.
   * @throws IOException I/O exception
   */
  @Test public void stopwords() throws IOException {
    final IOFile sw = new IOFile(sandbox(), "stopwords.txt");
    sw.write("one");
    set(MainOptions.STOPWORDS, sw.path());
    execute(new CreateDB(NAME, "<x><a>one two</a></x>"));
    search("one");
    search("two", "one two");
    assertTrue(sw.delete());
    // the update succeeds, the index is dropped
    query("replace value of node " + _DB_GET.args(NAME) + "//a with 'three'");
    ftindex(false);
    query(_DB_GET.args(NAME) + "//a[text() contains text 'three']/string()", "three");
    assertEquals(0, segments());
  }

  /**
   * Indexes are built from partial indexes.
   * @param updindex updindex flag
   */
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  public void split(final boolean updindex) {
    set(MainOptions.UPDINDEX, updindex);
    FTBuilder.splitTokens = 7;
    final StringBuilder doc = new StringBuilder("<x>");
    for(int i = 0; i < 50; i++) doc.append("<a>word").append(i % 10).append(" one</a>");
    doc.append("</x>");
    execute(new CreateDB(NAME, doc.toString()));
    assertEquals(0, tmpFiles());
    tokens("one", 50);
    tokens("word3", 5);
    check("one");
    check("word3");
    if(!updindex) return;

    // inserted subtree, indexed in a new segment
    query("insert node " + doc + " as first into " + _DB_GET.args(NAME) + "/x");
    assertEquals(0, tmpFiles());
    tokens("one", 100);
    check("word3");
    // rebuild: node IDs are no longer ordered by PRE value
    execute(new DropIndex(CmdIndex.FULLTEXT));
    execute(new CreateIndex(CmdIndex.FULLTEXT));
    assertEquals(0, tmpFiles());
    tokens("one", 100);
    check("one");
    check("word3");
    query("replace value of node (" + _DB_GET.args(NAME) + "//a)[1] with 'two'");
    execute(new Optimize());
    tokens("one", 99);
    check("one");
    check("two");
  }

  /**
   * Fuzzy and wildcard queries over segments and the buffer.
   */
  @Test public void fuzzyWildcards() {
    execute(new CreateDB(NAME, "<x><a>house</a><a>mouse</a></x>"));
    query("insert node (<a>houses</a>, <a>louse</a>) into " + _DB_GET.args(NAME) + "/x");
    final String db = _DB_GET.args(NAME);
    query(db + "//a[text() contains text 'house' using fuzzy] ! string()",
        "house\nmouse\nhouses\nlouse");
    query(db + "//a[text() contains text 'hous.*' using wildcards] ! string()",
        "house\nhouses");
    query(db + "//a[text() contains text '.ouse' using wildcards] ! string()",
        "house\nmouse\nlouse");
    query(_FT_TOKENS.args(NAME) + " ! string()", "house\nlouse\nmouse\nhouses");
    query(_FT_TOKENS.args(NAME, "house", " { 'fuzzy': true() }") + " ! string()",
        "house\nlouse\nmouse\nhouses");
    execute(new Optimize());
    query(db + "//a[text() contains text 'house' using fuzzy] ! string()",
        "house\nmouse\nhouses\nlouse");
    query(_FT_TOKENS.args(NAME) + " ! string()", "house\nlouse\nmouse\nhouses");
  }

  /**
   * A freshly built index keeps the unsegmented layout of older versions until the first update,
   * which adopts it as first segment.
   * @param mixed mixed-content flag
   */
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  public void adopt(final boolean mixed) {
    set(MainOptions.FTMIXED, mixed);
    set(MainOptions.FTINCLUDE, mixed ? "a" : "");
    execute(new CreateDB(NAME, "<x><a b='c'>one</a><a>two</a></x>"));
    final IOFile old = file("ftxx"), seg = file("ftx0x");
    // string values of elements are never written in the old layout
    assertEquals(!mixed, old.exists());
    assertEquals(mixed, seg.exists());
    search("one", "one");

    // an update that leaves the index valid for older versions keeps the old layout
    query("replace value of node " + _DB_GET.args(NAME) + "//@b with 'd'");
    ftindex(true);
    assertEquals(!mixed, old.exists());
    search("one", "one");
    // an update that shifts PRE values adopts it
    query("delete node " + _DB_GET.args(NAME) + "//@b");
    ftindex(true);
    assertFalse(old.exists());
    assertTrue(seg.exists());
    search("one", "one");
    query("replace value of node " + _DB_GET.args(NAME) + "//a[. = 'one'] with 'three'");
    search("one");
    search("three", "three");
    execute(new Close());
    search("three", "three");
    check("three");

    // a full rebuild returns to the old layout
    query(_DB_OPTIMIZE.args(NAME, true));
    assertEquals(!mixed, old.exists());
    search("three", "three");
  }

  /**
   * A database created with initial documents ends up with a single segment.
   * @param mixed mixed-content flag
   */
  @ParameterizedTest
  @ValueSource(booleans = { false, true })
  public void createWithInput(final boolean mixed) {
    query(_DB_CREATE.args(NAME, " <xml>test</xml>", "test.xml",
      " { 'ftindex': true(), 'updindex': true(), 'ftmixed': " + mixed + "(), " +
      "'ftinclude': 'xml' }"));
    final IOFile old = file("ftxx"), seg = file("ftx0x");
    // string values of elements are never stored in the layout of older versions
    assertEquals(!mixed, old.exists());
    assertEquals(mixed, seg.exists());
    assertEquals(1, segments());
    search("test", "test");
    if(mixed) {
      query("replace value of node " + _DB_GET.args(NAME) + "/xml with 'other'");
      search("other", "other");
      query(_DB_OPTIMIZE.args(NAME));
      assertEquals(1, segments());
      search("other", "other");
      return;
    }
    query("replace value of node " + _DB_GET.args(NAME) + "/xml with 'other'");
    assertFalse(old.exists());
    assertTrue(seg.exists());
    search("test");
    search("other", "other");
    // optimization returns to the old layout as long as all IDs equal their PRE values
    query(_DB_OPTIMIZE.args(NAME));
    assertTrue(old.exists());
    assertFalse(seg.exists());
    search("other", "other");
    query("delete node " + _DB_GET.args(NAME) + "/xml");
    query(_DB_OPTIMIZE.args(NAME));
    assertFalse(old.exists());
    assertEquals(1, segments());
    search("other");
  }

  /**
   * A mixed-content index is never stored in the format of older versions.
   * @throws IOException I/O exception
   */
  @Test public void mixedStorage() throws IOException {
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.FTINCLUDE, "xml");
    execute(new CreateDB(NAME, "<xml>test</xml>"));
    execute(new Close());
    assertEquals(DataText.OLDSTORAGE, storage());
    set(MainOptions.FTMIXED, true);
    execute(new CreateDB(NAME, "<xml>test</xml>"));
    execute(new Close());
    assertEquals(DataText.STORAGE, storage());
  }

  /**
   * Returns the storage version of the test database.
   * @return storage version
   * @throws IOException I/O exception
   */
  private static String storage() throws IOException {
    final MetaData meta = new MetaData(NAME, context.options, context.soptions);
    try(DataInput in = new DataInput(meta.dbFile(DataText.DATAINF))) {
      while(true) {
        final String key = Token.string(in.readToken()), value = Token.string(in.readToken());
        if(key.equals(DataText.DBSTR)) return value;
        if(key.isEmpty()) return "";
      }
    }
  }

  /**
   * Checks the results of a full-text search.
   * @param token token
   * @param results expected results
   */
  private static void search(final String token, final String... results) {
    query(_FT_SEARCH.args(NAME, token) + " ! string()", String.join("\n", results));
  }

  /**
   * Compares the index access with a scan.
   * @param token token
   */
  private static void check(final String token) {
    queryIndexScan("//*[text() contains text '" + token + "']");
  }

  /**
   * Checks the number of references of an index token.
   * @param token token
   * @param expected expected number
   */
  private static void tokens(final String token, final int expected) {
    query(_FT_TOKENS.args(NAME, token) + "[. = '" + token + "']/@count/string()", expected);
  }

  /**
   * Returns a file of the test database.
   * @param name file name without suffix
   * @return file
   */
  private static IOFile file(final String name) {
    return new IOFile(context.soptions.dbPath(NAME), name + IO.BASEXSUFFIX);
  }

  /**
   * Checks the full-text index flag.
   * @param exists expected flag
   */
  private static void ftindex(final boolean exists) {
    query(_DB_INFO.args(NAME) + "//ftindex/text()", exists);
  }

  /**
   * Returns the number of full-text index segments on disk.
   * @return number of segments
   */
  private static int segments() {
    return context.soptions.dbPath(NAME).children("ftx\\d*x\\.basex").length;
  }

  /**
   * Returns the number of temporary index files on disk.
   * @return number of files
   */
  private static int tmpFiles() {
    return context.soptions.dbPath(NAME).children("ftxtmp.*").length;
  }

  /**
   * Returns the number of supersede sets on disk.
   * @return number of files
   */
  private static int supersedeFiles() {
    return context.soptions.dbPath(NAME).children("ftx\\d+s\\.basex").length;
  }
}
