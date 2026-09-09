package org.basex.data;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.core.cmd.*;
import org.basex.core.parse.Commands.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

/**
 * Tests for the metadata that is preserved or updated by database updates.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class MetaUpdateTest extends SandboxTest {
  /** Query for the documents of the database. */
  private static final String DOC = _DB_GET.args(NAME);

  /** Creates a database. */
  @BeforeEach public void setUp() {
    execute(new CreateDB(NAME, "<x><a id='1'>1</a><a id='2'>2</a></x>"));
  }

  /** Drops the database. */
  @AfterEach public void tearDown() {
    execute(new DropDB(NAME));
    set(MainOptions.UPDINDEX, false);
    set(MainOptions.MAINMEM, false);
  }

  /** Insertions keep all metadata accurate. */
  @Test public void insert() {
    query("insert node <b>3</b> into " + DOC + "/x");
    query(_DB_PROPERTY.args(NAME, "uptodate"), true);
  }

  /** Element names that are added by an insertion are found. */
  @Test public void insertName() {
    query("insert node <b>3</b> into " + DOC + "/x");
    query(DOC + "//b", "<b>3</b>");
    query("count(" + DOC + "//b)", 1);
    query("insert node <c/> into " + DOC + "/x/b");
    query(DOC + "//c", "<c/>");
    query(DOC + "/x/b/c", "<c/>");
  }

  /** Values that are added by an insertion are reflected by the statistics. */
  @Test public void insertValue() {
    query("insert node <a id='3'>3</a> into " + DOC + "/x");
    query("sort(distinct-values(" + DOC + "/x/a))", "1\n2\n3");
    query("min(" + DOC + "/x/a)", 1);
    query("max(" + DOC + "/x/a/@id)", 3);
    query("count(" + DOC + "/x/a)", 3);
  }

  /** Attributes that are added by an insertion are found. */
  @Test public void insertAttribute() {
    query("insert node attribute lang { 'de' } into " + DOC + "/x/a[1]");
    query(DOC + "//@lang/string()", "de");
    query("count(" + DOC + "//@lang)", 1);
  }

  /** More attributes are added than are addressed by the distance of a table entry. */
  @Test public void insertManyAttributes() {
    query("insert node (for $i in 1 to 40 return attribute { 'a' || $i } { $i }) into " +
        DOC + "/x/a[1]");
    query("count(" + DOC + "/x/a[1]/@*)", 41);
    query(DOC + "/x/a[1]/@a40/string()", 40);
    assertRebuilt();
  }

  /** The data type of statistics is widened by insertions. */
  @Test public void insertType() {
    query("insert node <a>x</a> into " + DOC + "/x");
    query("sort(distinct-values(" + DOC + "/x/a))", "1\n2\nx");
    // values are no longer numeric
    error("min(" + DOC + "/x/a)", FUNCCAST_X_X);
  }

  /** The numeric range of statistics is widened by insertions. */
  @Test public void insertRange() {
    set(MainOptions.UPDINDEX, true);
    execute(new CreateDB(NAME, "<x><a>1</a><a>2</a></x>"));
    query("insert node <a>99</a> into " + DOC + "/x");
    query(DOC + "/x/a[. >= 50]", "<a>99</a>");
    query(DOC + "/x/a[. > 0][. < 2]", "<a>1</a>");
    query("max(" + DOC + "/x/a)", 99);
  }

  /** Nodes are inserted at all possible positions. */
  @Test public void insertPositions() {
    query("insert node <b/> before " + DOC + "/x/a[1]");
    query("insert node <c/> after " + DOC + "/x/a[2]");
    query("insert node <d/> as first into " + DOC + "/x");
    query("insert node <e/> as last into " + DOC + "/x");
    query("insert node <f><g><h>1</h></g></f> into " + DOC + "/x/a[1]");
    query("insert node (<i/>, <j>2</j>, <!-- c -->, <?pi v?>) into " + DOC + "/x/a[2]");
    query("count(" + DOC + "//*)", 12);
    assertRebuilt();
  }

  /** Comments and processing instructions are inserted next to the root element. */
  @Test public void insertDocumentLevel() {
    query("insert node <!-- c --> before " + DOC + "/x");
    query("insert node <?pi v?> after " + DOC + "/x");
    query("count(" + DOC + "/node())", 3);
    assertRebuilt();
  }

  /** Adjacent text nodes are merged, which invalidates the statistics. */
  @Test public void insertText() {
    query("insert node text { '3' } into " + DOC + "/x/a[1]");
    query(DOC + "/x/a[1]/string()", 13);
    // the merge is applied as an insertion and a deletion
    query(_DB_PROPERTY.args(NAME, "uptodate"), false);
    assertFlags(true, false);
    query("sort(distinct-values(" + DOC + "/x/a))", "13\n2");
  }

  /** Documents are added, replaced and deleted. */
  @Test public void documents() {
    query(_DB_ADD.args(NAME, " <y><b>3</b></y>", "b.xml"));
    query(_DB_PROPERTY.args(NAME, "uptodate"), true);
    query("count(" + DOC + ")", 2);
    query(DOC + "//b", "<b>3</b>");
    assertRebuilt();

    // identical structure: the document is replaced by value updates
    query(_DB_PUT.args(NAME, " <y><b>4</b></y>", "b.xml"));
    query(DOC + "//b", "<b>4</b>");
    assertFlags(true, true);

    // different structure: the document is replaced, and statistics counts get outdated
    query(_DB_PUT.args(NAME, " <y><b>5</b><b>6</b></y>", "b.xml"));
    query("count(" + DOC + "//b)", 2);
    assertFlags(true, false);

    // identical structure, different names: the replacement is applied as a renaming
    query(_DB_PUT.args(NAME, " <y><c>7</c><c>8</c></y>", "b.xml"));
    query("count(" + DOC + "//c)", 2);
    assertFlags(false, false);

    execute(new Optimize());
    query(_DB_DELETE.args(NAME, "b.xml"));
    query("count(" + DOC + ")", 1);
    assertFlags(true, false);
  }

  /** Elements without a text node child contribute an empty string value. */
  @Test public void insertEmpty() {
    execute(new CreateDB(NAME, "<x><g/></x>"));
    query("insert node <g>0</g> into " + DOC + "/x");
    query("count(distinct-values(" + DOC + "/x/g))", 2);
    assertRebuilt();
  }

  /** Elements without a text node child are inserted after elements with a text node child. */
  @Test public void insertEmptyReverse() {
    execute(new CreateDB(NAME, "<x><g>0</g></x>"));
    query("insert node <g/> into " + DOC + "/x");
    query("count(distinct-values(" + DOC + "/x/g))", 2);
    assertRebuilt();
  }

  /** Statistics that were read from disk are extended by numeric values. */
  @Test public void reopenNumeric() {
    execute(new CreateDB(NAME, "<x><b/></x>"));
    execute(new Close());
    execute(new Open(NAME));
    query("insert node <b>500</b> into " + DOC + "/x");
    query("insert node <b>600</b> into " + DOC + "/x");
    assertRebuilt();
  }

  /** Updates on a main-memory database. */
  @Test public void mainmem() {
    set(MainOptions.MAINMEM, true);
    execute(new CreateDB(NAME, "<x><a id='1'>1</a><a id='2'>2</a></x>"));
    query("insert node <b><c>3</c></b> into " + DOC + "/x");
    query(DOC + "//c", "<c>3</c>");
    query("count(" + DOC + "//*)", 5);
    set(MainOptions.MAINMEM, false);
  }

  /** Flags that are assigned by the different update operations. */
  @Test public void flags() {
    assertFlags(true, true);
    query("insert node <b/> into " + DOC + "/x");
    assertFlags(true, true);
    query(_DB_PROPERTY.args(NAME, "uptodate"), true);

    query("replace value of node " + DOC + "/x/a[1] with '9'");
    assertFlags(true, true);
    query(_DB_PROPERTY.args(NAME, "uptodate"), false);

    query("delete node " + DOC + "/x/a[1]");
    assertFlags(true, false);

    query("rename node " + DOC + "/x/a[1] as 'z'");
    assertFlags(false, false);

    // insertions do not recover the accuracy of the metadata
    query("insert node <b/> into " + DOC + "/x");
    assertFlags(false, false);
    execute(new Optimize());
    assertFlags(true, true);
  }

  /**
   * Incrementally updated metadata is identical to rebuilt metadata.
   * @param updindex incremental value indexes
   */
  @ParameterizedTest
  @ValueSource(booleans = { true, false })
  public void rebuild(final boolean updindex) {
    set(MainOptions.UPDINDEX, updindex);
    execute(new CreateDB(NAME, "<x><a id='1'>1</a><a id='2'>2</a></x>"));

    query("insert node <a id='3'><c/></a> into " + DOC + "/x");
    query("insert node attribute lang { 'de' } into " + DOC + "/x/a[1]");
    query("insert node <e/> into " + DOC + "/x");
    query("insert node <a>4</a> into " + DOC + "/x");
    query("insert node <!-- c --> into " + DOC + "/x");
    query("insert node <?pi v?> into " + DOC + "/x");
    // prefixed names, and more distinct values than categories are cached for
    query("insert node <n:z xmlns:n='u'>v</n:z> into " + DOC + "/x");
    query("insert node (for $i in 1 to 150 return <v>{ $i }</v>) into " + DOC + "/x");
    query(_DB_ADD.args(NAME, " <y><a id='5'>5</a></y>", "second.xml"));
    query(_DB_PROPERTY.args(NAME, "uptodate"), true);

    final String[] indexes = indexes();
    execute(new OptimizeAll());
    query(_DB_PROPERTY.args(NAME, "uptodate"), true);
    assertArrayEquals(indexes, indexes());
  }

  /** Deletions keep the path and name indexes complete. */
  @Test public void delete() {
    query("delete node " + DOC + "/x/a[1]");
    query(_DB_PROPERTY.args(NAME, "uptodate"), false);
    // statistics are outdated, but the path index still discards non-existing paths
    check(DOC + "//a/a", "", root(Empty.class));
    query(DOC + "//a", "<a id=\"2\">2</a>");
  }

  /** Value updates keep the statistics counts exact. */
  @Test public void replaceValue() {
    query("replace value of node " + DOC + "/x/a[1] with '9'");
    query(_DB_PROPERTY.args(NAME, "uptodate"), false);
    // counts are still exact: the path is pre-evaluated
    check("count(" + DOC + "/x/a)", 2, root(Itr.class));
    query("sort(distinct-values(" + DOC + "/x/a))", "2\n9");
    query("min(" + DOC + "/x/a)", 2);
  }

  /** Renamed nodes are found. */
  @Test public void rename() {
    query("rename node " + DOC + "/x/a[1] as 'b'");
    query(_DB_PROPERTY.args(NAME, "uptodate"), false);
    query(DOC + "//b", "<b id=\"1\">1</b>");
    query(DOC + "/x/b", "<b id=\"1\">1</b>");
  }

  /** Replaced nodes are found. */
  @Test public void replaceNode() {
    query("replace node " + DOC + "/x/a[1] with <b><c/></b>");
    query(DOC + "//b", "<b><c/></b>");
    query(DOC + "//c", "<c/>");
    query(DOC + "/x/b/c", "<c/>");
  }

  /** The accuracy of the metadata is persisted. */
  @Test public void persist() {
    query("delete node " + DOC + "/x/a[1]");
    execute(new Close());
    execute(new Open(NAME));
    query(_DB_PROPERTY.args(NAME, "uptodate"), false);
    // path index is still complete
    check(DOC + "//a/a", "", root(Empty.class));

    query("rename node " + DOC + "/x/a[1] as 'b'");
    execute(new Close());
    execute(new Open(NAME));
    // path index is incomplete
    check(DOC + "//b/b", "", empty(Empty.class));
  }

  /** Random insertions yield the same metadata as a rebuild. */
  @Test public void randomInsertions() {
    final Random rnd = new Random(0x5EED);
    for(int i = 0; i < 100; i++) {
      insert(rnd, i);
      // reopen the database to exercise the metadata that is read from disk
      if(rnd.nextInt(10) == 0) {
        execute(new Close());
        execute(new Open(NAME));
      }
    }
    // insertions keep all metadata exact
    query(_DB_PROPERTY.args(NAME, "uptodate"), true);
    assertRebuilt();
  }

  /**
   * Runs a random insertion.
   * @param rnd random number generator
   * @param i number of the operation
   */
  private static void insert(final Random rnd, final int i) {
    // operations on document level
    switch(rnd.nextInt(12)) {
      case 0 -> {
        query(_DB_ADD.args(NAME, " <z><y>" + rnd.nextInt(9) + "</y></z>", "d" + i + ".xml"));
        return;
      }
      case 1 -> {
        update(nodes(DOC + "/*", rnd), "insert node (<!-- c -->, <?pi v?>) before $n");
        return;
      }
      case 2 -> {
        update(nodes(DOC + "/*", rnd), "insert node <?pi v?> after $n");
        return;
      }
      case 3 -> {
        update(element(rnd), "insert node (for $j in 1 to " + (rnd.nextInt(40) + 1) +
            " return attribute { 'z' || " + i + " || $j } { $j }) into $n");
        return;
      }
      default -> { }
    }
    final String fragment = switch(rnd.nextInt(7)) {
      case 0 -> "<b/>";
      case 1 -> "<b>" + rnd.nextInt(1000) + "</b>";
      case 2 -> "<c d='" + rnd.nextInt(10) + "'>x</c>";
      case 3 -> "<e><f><g>" + rnd.nextInt(5) + "</g></f></e>";
      case 4 -> "(<h/>, <i>" + rnd.nextInt(3) + "</i>)";
      case 5 -> "<l xmlns:n='u'><n:m>" + rnd.nextInt(3) + "</n:m></l>";
      default -> "attribute k" + i + " { '" + rnd.nextInt(9) + "' }";
    };
    update(element(rnd), "insert node " + fragment + " into $n");
  }

  /** Random updates keep the path and name indexes complete. */
  @Test public void randomUpdates() {
    final Random rnd = new Random(0xC0FFEE);
    for(int i = 0; i < 100; i++) {
      switch(rnd.nextInt(6)) {
        case 0, 1 -> insert(rnd, i);
        case 2 -> update(child(rnd), "delete node $n");
        case 3 -> update(element(rnd), "replace value of node $n with '" + rnd.nextInt(99) + '\'');
        case 4 -> update(element(rnd), "insert node (<!-- c -->, <?pi v?>, text { '" +
            rnd.nextInt(9) + "' }) into $n");
        default -> update(child(rnd), "replace node $n with <p><q>" + rnd.nextInt(9) + "</q></p>");
      }
    }
    // statistics are outdated, but no path or name is missing
    query(_DB_PROPERTY.args(NAME, "uptodate"), false);
    assertFlags(true, false);
    assertComplete();
  }

  /**
   * Runs an update on the addressed nodes. Nothing happens if no node is addressed.
   * @param nodes query that addresses the target nodes
   * @param update update expression, referring to the target as {@code $n}
   */
  private static void update(final String nodes, final String update) {
    query("for $n in " + nodes + " return " + update);
  }

  /**
   * Returns a query that addresses an arbitrary element of the database.
   * @param rnd random number generator
   * @return query
   */
  private static String element(final Random rnd) {
    return nodes(DOC + "//*", rnd);
  }

  /**
   * Returns a query that addresses an arbitrary element that is not a root element.
   * @param rnd random number generator
   * @return query
   */
  private static String child(final Random rnd) {
    return nodes(DOC + "//*[parent::*]", rnd);
  }

  /**
   * Returns a query that addresses at most one of the specified nodes.
   * @param nodes query that addresses the candidates
   * @param rnd random number generator
   * @return query
   */
  private static String nodes(final String nodes, final Random rnd) {
    // position 0 addresses no node: the update will be skipped
    return '(' + nodes + ")[" + rnd.nextInt(9999) + " mod (count(" + nodes + ") + 1)]";
  }

  /**
   * Checks that the incrementally updated metadata equals rebuilt metadata.
   * The database will be optimized.
   */
  private static void assertRebuilt() {
    final String[] indexes = indexes();
    execute(new OptimizeAll());
    assertArrayEquals(indexes, indexes());
  }

  /**
   * Checks that the path index contains all paths of a rebuilt index.
   * The database will be optimized.
   */
  private static void assertComplete() {
    final Map<String, Integer> paths = paths();
    execute(new OptimizeAll());
    for(final Map.Entry<String, Integer> entry : paths().entrySet()) {
      final String path = entry.getKey();
      assertTrue(paths.getOrDefault(path, 0) >= entry.getValue(), "Missing path: " + path);
    }
  }

  /**
   * Checks the accuracy flags of the metadata.
   * @param complete complete path and name indexes
   * @param counts exact statistics counts
   */
  private static void assertFlags(final boolean complete, final boolean counts) {
    final MetaData meta = context.data().meta;
    assertEquals(complete, meta.complete, "Complete flag");
    assertEquals(counts, meta.counts, "Counts flag");
  }

  /**
   * Returns the entries of the path index, without their statistics.
   * @return paths and their number of occurrences
   */
  private static Map<String, Integer> paths() {
    final Map<String, Integer> paths = new HashMap<>();
    for(final String line : execute(new InfoIndex(CmdIndexInfo.PATH)).split("\r?\n")) {
      final int i = line.lastIndexOf(": ");
      if(i != -1) paths.merge(line.substring(0, i), 1, Integer::sum);
    }
    return paths;
  }

  /**
   * Returns the sorted contents of the structural index structures.
   * @return index information
   */
  private static String[] indexes() {
    final String info = execute(new InfoIndex(CmdIndexInfo.PATH)) +
        execute(new InfoIndex(CmdIndexInfo.ELEMNAME)) +
        execute(new InfoIndex(CmdIndexInfo.ATTRNAME));
    // child nodes are appended in insertion order: compare the entries, not their order
    final String[] lines = info.split("\r?\n");
    Arrays.sort(lines);
    return lines;
  }
}
