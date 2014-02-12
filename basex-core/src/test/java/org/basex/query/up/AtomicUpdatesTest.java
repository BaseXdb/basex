package org.basex.query.up;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.rules.*;

/**
 * Test the {@link AtomicUpdateCache}.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Lukas Kircher
 */
public final class AtomicUpdatesTest extends AdvancedQueryTest {
  /** Expected exception. */
  @Rule
  public final ExpectedException thrown = ExpectedException.none();

  /**
   * Basic Lazy Replace tests.
   */
  @Test
  public void lazyReplace() {
    // IDENTICAL: 0 value updates
    query(transform("<doc><tree/></doc>",
            "replace node $input//tree with <tree/>"),
        "<doc><tree/></doc>");
    // FAIL: different size of trees
    query(transform("<doc><tree><n/></tree></doc>",
            "replace node $input//tree with <tree/>"),
        "<doc><tree/></doc>");
    // FAIL: kind
    query(transform("<doc><tree><n/></tree></doc>",
            "replace node $input//tree with <tree>text</tree>"),
        "<doc><tree>text</tree></doc>");
    // FAIL: distance (size would've already failed on ancestor axis)
    query(transform("<doc><tree><n/></tree></doc>",
            "replace node $input//tree with <tree/>"),
        "<doc><tree/></doc>");
    // FAIL: replace attribute w/ sequence of attributes
    query(transform("<doc><tree id=\"0\"/></doc>",
        "replace node $input//@id with (attribute id {\"1\"}, attribute id2 {\"2\"})"),
    "<doc><tree id=\"1\" id2=\"2\"/></doc>");
    // LAZY REPLACE: 8 value updates -> element, attribute, text, comment, processing instruction
    query(transform("<doc><tree1 a='0'>text1<a/><!--comm1--><a/><?p1 i1?><?p11?></tree1></doc>",
            "replace node $input//tree1 with " +
            "<tree2 b='1'>text2<a/><!--comm2--><a/><?p2 i2?><?p22?></tree2>"),
        "<doc><tree2 b=\"1\">text2<a/><!--comm2--><a/><?p2 i2?><?p22 ?></tree2></doc>");
    // LAZY REPLACE: 2 value updates -> single attribute
    query(transform("<doc><tree id1=\"0\"/></doc>",
            "replace node $input//@id1 with attribute id2 {\"1\"}"),
        "<doc><tree id2=\"1\"/></doc>");
  }

  /**
   * Basic test for tree-aware updates.
   */
  @Test
  public void treeAwareUpdates0() {
    final String doc = "<n1>" + "<n2 att3='0'><n4/><n5><n6/></n5></n2>" + "</n1>";
    final AtomicUpdateCache l = atomics(doc);
    final MemData m = new MemData(l.data);
    final DataClip ins = clipE(m, "<d/>", false);
    l.addDelete(2);
    l.addInsert(3, 2, ins, true);
    l.addInsert(6, 5, ins, false);
    l.addReplace(6, ins);
    l.addInsert(7, 6, ins, false);
    assertEquals(1, l.updatesSize());

    l.clear();
    l.addInsert(2, 2, ins, true);
    l.addDelete(3);
    l.addInsert(4, 2, ins, true);
    l.addInsert(6, 5, ins, false);
    l.addReplace(6, ins);
    l.addInsert(7, 6, ins, false);
    l.addInsert(7, 5, ins, false);
    assertEquals(6, l.updatesSize());
  }

  /**
   * Tests tree-aware updates algorithm. - Delete and Insert on the single child of a node
   */
  @Test
  public void treeAwareUpdates1() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache l = atomics(doc);
    final MemData m = new MemData(l.data);
    l.addDelete(2);
    l.addInsert(3, 2, clipE(m, "<c/>", false), false);
    assertEquals(1, l.updatesSize());
    query(transform(doc, "insert node <c/> into $input/b, delete node $input/b"), "<a/>");
  }

  /**
   * Tests tree-aware updates algorithm. - Insert on a target T - Delete and Insert on the
   * single child of T
   */
  @Test
  public void treeAwareUpdates2() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache l = atomics(doc);
    final MemData m = new MemData(l.data);
    l.addDelete(2);
    l.addInsert(3, 2, clipE(m, "<c/>", false), false);
    l.addInsert(3, 1, clipE(m, "<d/>", false), false);
    assertEquals(1, l.updatesSize());
    query(transform(doc, "insert node <d/> into $input, insert node <c/> into $input/b,"
        + "delete node $input/b"), "<a><d/></a>");
  }

  /**
   * Tests tree-aware updates algorithm. - Insert on a target T - Replace on child of T
   */
  @Test
  public void treeAwareUpdates3() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache l = atomics(doc);
    final MemData m = new MemData(l.data);
    l.addReplace(2, clipE(m, "<newb/>", false));
    l.addInsert(3, 1, clipE(m, "<d/>", false), false);
    assertEquals(2, l.updatesSize());
    query(transform(doc, "insert node <d/> into $input,"
        + "replace node $input/b with <newb/>"), "<a><newb/><d/></a>");
  }

  /**
   * Tests tree-aware updates algorithm. - Replace and InsertInto on the single child of a
   * node
   */
  @Test
  public void treeAwareUpdates4() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache l = atomics(doc);
    final MemData m = new MemData(l.data);
    l.addReplace(2, clipE(m, "<newb/>", false));
    l.addInsert(3, 2, clipE(m, "<c/>", false), false);
    assertEquals(1, l.updatesSize());
    query(transform(doc, "insert node <c/> into $input/b,"
        + "replace node $input/b with <newb/>"), "<a><newb/></a>");
  }

  /**
   * Tests tree-aware updates algorithm with value updates.
   */
  @Test
  public void treeAwareUpdates5() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache l = atomics(doc);
    l.addDelete(2);
    l.addRename(2, token("bb"), EMPTY);
    assertEquals(1, l.updatesSize());
  }

  /**
   * Tests tree-aware updates algorithm with attribute updates.
   */
  @Test
  public void treeAwareUpdates6() {
    final String doc = "<a><b id='1'/></a>";
    final AtomicUpdateCache l = atomics(doc);
    l.addDelete(3);
    l.addRename(3, token("idx"), EMPTY);
    assertEquals(1, l.updatesSize());
  }

  /**
   * Merge atomic updates A,B with A being an insert atomic.
   */
  @Test
  public void mergeSequence01() {
    // a=1, b=2
    final String doc = "<a><b/></a>";

    // two inserts cannot be merged!
    AtomicUpdateCache l = atomics(doc);
    final MemData m = new MemData(l.data);
    l.addInsert(3, 2, clipE(m, "<c/>", false), false);
    l.addInsert(3, 2, clipE(m, "<d/>", false), false);
    assertEquals(2, l.updatesSize());
    query(transform(doc, "insert node <c/> into $input/b,insert node <d/> into $input/b"),
        "<a><b><c/><d/></b></a>");

    // delete(x) -> insert(x+1)
    l = atomics(doc);
    l.addDelete(2);
    l.addInsert(3, 1, clipE(m, "<d/>", false), false);
    assertEquals(1, l.updatesSize());
    query(transform(doc, "insert node <d/> into $input,delete node $input/b"),
        "<a><d/></a>");

    // delete(x) -> insert(x+1) with affecting insert before x
    l = atomics("<a><b/><c/></a>");
    l.addDelete(2);
    l.addDelete(3);
    l.addInsert(4, 1, clipE(m, "<d/>", false), false);
    assertEquals(2, l.updatesSize());
    query(transform("<a><b/><c/></a>", "insert node <d/> into $input," +
        "delete node $input/c,delete node $input/b"), "<a><d/></a>");

    // insert(x) <- delete(x)
    l = atomics(doc);
    l.addInsert(2, 1, clipE(m, "<d/>", false), false);
    l.addDelete(2);
    assertEquals(1, l.updatesSize());
    query(transform(doc, "insert node <d/> into $input,delete node $input/b"),
        "<a><d/></a>");
  }

  /**
   * Multiple destructive operations on same node.
   */
  @Test
  public void updateSequence01() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    l.addDelete(2);
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Multiple deletes/replaces on node");
    l.addDelete(2);
  }

  /**
   * Multiple destructive operations on same node.
   */
  @Test
  public void updateSequence02() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    final MemData m = new MemData(l.data);
    l.addDelete(2);
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Multiple deletes/replaces on node");
    l.addReplace(2, clipE(m, "<newb/>", false));
  }

  /**
   * Multiple destructive operations on same node.
   */
  @Test
  public void updateSequence03() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    final MemData m = new MemData(l.data);
    l.addReplace(2, clipE(m, "<newb/>", false));
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Multiple deletes/replaces on node");
    l.addReplace(2, clipE(m, "<newb/>", false));
  }

  /**
   * Multiple renames/updates on same node.
   */
  @Test
  public void updateSequence04() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    l.addRename(2, token("foo"), EMPTY);
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Multiple renames on node");
    l.addRename(2, token("foo2"), EMPTY);
  }

  /**
   * Multiple renames/updates on same node.
   */
  @Test
  public void updateSequence05() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    l.addUpdateValue(2, token("foo"));
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Multiple updates on node");
    l.addUpdateValue(2, token("foo"));
  }

  /**
   * Order of updates.
   */
  @Test
  public void updateSequence06() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    l.addDelete(3);
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Invalid order at location");
    l.addDelete(2);
  }

  /**
   * Order of updates.
   */
  @Test
  public void updateSequence07() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    l.addRename(2, token("bb"), EMPTY);
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Invalid sequence of value update and destructive update at " +
        "location 2");
    l.addDelete(2);
  }

  /**
   * Order of updates.
   */
  @Test
  public void updateSequence08() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    final MemData m = new MemData(l.data);
    l.addDelete(2);
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Invalid sequence of delete, insert at location 2");
    l.addInsert(2, 1, clipE(m, "<dummy/>", false), false);
  }

  /**
   * Order of updates.
   */
  @Test
  public void updateSequence09() {
    final AtomicUpdateCache l = atomics("<a><b id='0'/></a>");
    final MemData m = new MemData(l.data);
    l.addDelete(3);
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Invalid sequence of delete, insert at location 3");
    l.addInsert(3, 2, clipA(m, "id", "1"), true);
  }

  /**
   * Order of updates.
   */
  @Test
  public void updateSequence10() {
    final AtomicUpdateCache l = atomics("<a><b/></a>");
    final MemData m = new MemData(l.data);
    l.addReplace(2, clipE(m, "<bb/>", false));
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Invalid sequence of replace, insert at location 2");
    l.addInsert(2, 1, clipE(m, "<dummy/>", false), false);
  }

  /**
   * Order of updates.
   */
  @Test
  public void updateSequence11() {
    final AtomicUpdateCache l = atomics("<a><b id='0'/></a>");
    final MemData m = new MemData(l.data);
    l.addReplace(3, clipA(m, "id", "11"));
    thrown.expect(RuntimeException.class);
    thrown.expectMessage("Invalid sequence of replace, insert at location 3");
    l.addInsert(3, 2, clipA(m, "id", "1"), true);
  }

  /**
   * Tests if PRE values are correctly calculated before/after updates.
   */
  @Test
  public void calculatePreValues() {
    final String doc =
        "<n1>" +
            "<n2/><n3/><n4/><n5/><n6/><n7/><n8/><n9/><n10/><n11/>" +
        "</n1>";
    AtomicUpdateCache l = atomics(doc);
    l.addDelete(3);
    l.applyUpdates();
    assertEquals(2, l.calculatePreValue(2, false));
    assertEquals(3, l.calculatePreValue(3, false));
    assertEquals(3, l.calculatePreValue(4, false));
    assertEquals(4, l.calculatePreValue(5, false));
    assertEquals(2, l.calculatePreValue(2, true));
    assertEquals(4, l.calculatePreValue(3, true));
    assertEquals(5, l.calculatePreValue(4, true));
    assertEquals(6, l.calculatePreValue(5, true));

    l = atomics(doc);
    l.addDelete(3);
    l.addDelete(5);
    l.addDelete(7);
    l.applyUpdates();
    assertEquals(2, l.calculatePreValue(2, false));
    assertEquals(3, l.calculatePreValue(3, false));
    assertEquals(3, l.calculatePreValue(4, false));
    assertEquals(4, l.calculatePreValue(5, false));
    assertEquals(4, l.calculatePreValue(6, false));
    assertEquals(5, l.calculatePreValue(7, false));
    assertEquals(5, l.calculatePreValue(8, false));
    assertEquals(2, l.calculatePreValue(2, true));
    assertEquals(4, l.calculatePreValue(3, true));
    assertEquals(6, l.calculatePreValue(4, true));
    assertEquals(8, l.calculatePreValue(5, true));
    assertEquals(9, l.calculatePreValue(6, true));
    assertEquals(10, l.calculatePreValue(7, true));
    assertEquals(11, l.calculatePreValue(8, true));

    l = atomics(doc);
    // mind that dummy insert data instance size==2!
    final MemData m = new MemData(l.data);
    l.addInsert(3, 1, clipE(m, "<dummy3/>", true), false);
    l.addInsert(3, 1, clipE(m, "<dummy4/>", true), false);
    l.addDelete(3);
    l.applyUpdates();
    assertEquals(1, l.calculatePreValue(1, false));
    assertEquals(2, l.calculatePreValue(2, false));
    assertEquals(5, l.calculatePreValue(3, false));
    assertEquals(7, l.calculatePreValue(4, false));
    assertEquals(8, l.calculatePreValue(5, false));
    assertEquals(9, l.calculatePreValue(6, false));
    assertEquals(1, l.calculatePreValue(1, true));
    assertEquals(2, l.calculatePreValue(2, true));
    // nodes like 3,4 that have not existed prior to insert are not recalculated
    assertEquals(3, l.calculatePreValue(3, true));
    assertEquals(4, l.calculatePreValue(4, true));
    assertEquals(3, l.calculatePreValue(5, true));
    assertEquals(4, l.calculatePreValue(6, true));
  }

  /**
   * Tests if distances are updated correctly.
   */
  @Test
  public void distanceCaching() {
    final String doc = "<n1>" + "<n2>T3</n2>T4<n5/>T6<n7/>"
        + "<n8><n9><n10><n11/><n12/></n10></n9></n8><n13/><n14/>" + "</n1>";
    final AtomicUpdateCache l = atomics(doc);
    final MemData m = new MemData(l.data);
    l.addDelete(3);
    l.addReplace(5, clipE(m, "dummy1", true));
    l.addInsert(11, 10, clipE(m, "dummy2", true), false);
    l.addInsert(13, 10, clipE(m, "dummy3", true), false);
    l.addDelete(13);
    l.execute(true);
    checkDistances(l.data, new int[][] { new int[] { 17, 1}, new int[] { 16, 15},
        new int[] { 15, 10}, new int[] { 14, 10}, new int[] { 13, 10},
        new int[] { 12, 11}, new int[] { 11, 10}, new int[] { 10, 9}, new int[] { 9, 8},
        new int[] { 8, 1}, new int[] { 7, 1}, new int[] { 6, 1}, new int[] { 5, 4},
        new int[] { 4, 1}, new int[] { 3, 1}, new int[] { 2, 1}, new int[] { 1, 0}});
  }

  /**
   * Tests if the given child/parent PRE value pairs are still valid in the database.
   * @param d reference
   * @param pairs PRE value pairs
   */
  private static void checkDistances(final Data d, final int[][] pairs) {
    for(final int[] pair : pairs)
      if(d.parent(pair[0], d.kind(pair[0])) != pair[1]) fail("Wrong parent for pre="
          + pair[0]);
  }

  /**
   * Tests if text node adjacency is correctly resolved.
   */
  @Test
  public void textMerging() {
    final String doc = "<n1>" + "<n2>T3</n2>T4<n5/>T6<n7/>" + "</n1>";
    final AtomicUpdateCache l = atomics(doc);
    // MemData needed to build valid DataClip object
    final MemData m = new MemData(l.data);
    l.addInsert(3, 2, clipT(m, "Tx0"), false);
    l.addDelete(3);
    l.addInsert(4, 2, clipT(m, "Tx01"), false);
    l.addDelete(5);
    l.addReplace(6, clipT(m, "T6new"));
    l.addInsert(8, 7, clipT(m, "Tx1"), false);
    l.addInsert(8, 7, clipT(m, "Tx2"), false);
    l.addInsert(8, 1, clipT(m, "Tx3"), false);
    l.execute(true);
    checkTextAdjacency(l.data, new byte[][] { token("Tx0Tx01"), token("T4T6new"),
        token("Tx1Tx2"), token("Tx3")});
  }

  /**
   * Checks if text node adjacency has been correctly resolved for the given data
   * instance.
   * @param data reference
   * @param texts in the expected order of occurrence
   */
  private static void checkTextAdjacency(final Data data, final byte[][] texts) {
    int i = 0;
    // find adjacent text nodes
    while(i + 1 < data.meta.size) {
      final int a = i++;
      final int b = i;
      final int aKind = data.kind(a);
      final int bKind = data.kind(b);
      if(aKind == Data.TEXT && bKind == Data.TEXT
          && data.parent(a, aKind) == data.parent(b, bKind))
        fail("Adjacent text nodes at position "
          + i);
    }

    // check order of texts
    i = -1;
    int t = 0;
    while(++i < data.meta.size) {
      if(data.kind(i) == Data.TEXT && !eq(data.text(i, true), texts[t++]))
        fail("Invalid text node at position "
          + i);
    }
  }

  /**
   * Creates a Data instance that contains a text node with the given value.
   * @param d parent data instance
   * @param text for text node
   * @return data instance with text node
   */
  private static DataClip clipT(final Data d, final String text) {
    final int s = d.meta.size;
    d.text(s, s + 1, token(text), Data.TEXT);
    d.insert(s);
    return new DataClip(d, s, d.meta.size);
  }

  /**
   * Creates a DataClip instance that contains an attribute node with the given name and
   * value.
   * @param d parent data instance
   * @param name for attribute node
   * @param value for attribute node
   * @return data instance with text node
   */
  private static DataClip clipA(final Data d, final String name, final String value) {
    final int s = d.meta.size;
    d.attr(s, s + 1, d.atnindex.index(token(name), null, false), token(value), -1, false);
    d.insert(s);
    return new DataClip(d, s, d.meta.size);
  }

  /**
   * Creates a small insertion sequence data containing 2 nodes.
   * @param d parent data instance
   * @param n name of the elements to be inserted
   * @param b add tree w/ size==2, if false add tree w/ size==1
   * @return insertion sequence data instance
   */
  private static DataClip clipE(final Data d, final String n, final boolean b) {
    final int s = d.meta.size;
    d.elem(s + 1, d.tagindex.index(token(n), null, false), 1, b ? 2 : 1, 0, false);
    d.insert(s);
    if(b) {
      d.elem(1, d.tagindex.index(token(n), null, false), 1, 1, 0, false);
      d.insert(s + 1);
    }
    return new DataClip(d, s, d.meta.size);
  }

  /**
   * Creates an {@link AtomicUpdateCache} for the given XML fragment.
   * @param doc XML fragment string
   * @return atomic update list
   */
  private static AtomicUpdateCache atomics(final String doc) {
    Data d = null;
    try {
      d = CreateDB.mainMem(new IOContent(doc), context);
    } catch(final IOException ex) {
      fail(Util.message(ex));
    }
    return new AtomicUpdateCache(d);
  }
}
