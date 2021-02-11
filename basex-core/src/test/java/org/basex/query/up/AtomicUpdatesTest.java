package org.basex.query.up;

import static org.basex.util.Token.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.*;
import org.basex.build.*;
import org.basex.data.*;
import org.basex.io.*;
import org.basex.query.up.atomic.*;
import org.basex.util.*;
import org.junit.jupiter.api.Test;

/**
 * Test the {@link AtomicUpdateCache}.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Lukas Kircher
 */
public final class AtomicUpdatesTest extends SandboxTest {
  /**
   * Basic Lazy Replace tests.
   */
  @Test public void lazyReplace() {
    // IDENTICAL: 0 value updates
    query(transform("<doc><tree/></doc>",
        "replace node $input//tree with <tree/>"),
        "<doc>\n<tree/>\n</doc>");
    // FAIL: different size of trees
    query(transform("<doc><tree><n/></tree></doc>",
        "replace node $input//tree with <tree/>"),
        "<doc>\n<tree/>\n</doc>");
    // FAIL: kind
    query(transform("<doc><tree><n/></tree></doc>",
        "replace node $input//tree with <tree>text</tree>"),
        "<doc>\n<tree>text</tree>\n</doc>");
    // FAIL: distance (size would've already failed on ancestor axis)
    query(transform("<doc><tree><n/></tree></doc>",
        "replace node $input//tree with <tree/>"),
        "<doc>\n<tree/>\n</doc>");
    // FAIL: replace attribute w/ sequence of attributes
    query(transform("<doc><tree id=\"0\"/></doc>",
        "replace node $input//@id with (attribute id {\"1\"}, attribute id2 {\"2\"})"),
        "<doc>\n<tree id=\"1\" id2=\"2\"/>\n</doc>");
    // LAZY REPLACE: 8 value updates -> element, attribute, text, comment, processing instruction
    query(transform("<doc><tree1 a='0'>text1<a/><!--comm1--><a/><?p1 i1?><?p11?></tree1></doc> ",
        "replace node $input//tree1 with " +
        "<tree2 b='1'>text2<a/><!--comm2--><a/><?p2 i2?><?p22?></tree2>"),
        "<doc>\n<tree2 b=\"1\">text2<a/>\n<!--comm2-->\n<a/>\n" +
        "<?p2 i2?>\n<?p22 ?>\n</tree2>\n</doc>");
    // LAZY REPLACE: 2 value updates -> single attribute
    query(transform("<doc><tree id1=\"0\"/></doc>",
        "replace node $input//@id1 with attribute id2 {\"1\"}"),
        "<doc>\n<tree id2=\"1\"/>\n</doc>");
  }

  /**
   * Basic test for tree-aware updates.
   */
  @Test public void treeAwareUpdates0() {
    final String doc = "<n1>" + "<n2 att3='0'><n4/><n5><n6/></n5></n2>" + "</n1>";
    final AtomicUpdateCache auc = atomics(doc);
    final MemData md = new MemData(context.options);
    final DataClip ins = elemClip(md, "<d/>", false);
    auc.addDelete(2);
    auc.addInsert(3, 2, ins);
    auc.addInsert(6, 5, ins);
    auc.addReplace(6, ins);
    auc.addInsert(7, 6, ins);
    assertEquals(1, auc.updatesSize());

    auc.clear();
    auc.addInsert(2, 2, ins);
    auc.addDelete(3);
    auc.addInsert(4, 2, ins);
    auc.addInsert(6, 5, ins);
    auc.addReplace(6, ins);
    auc.addInsert(7, 6, ins);
    auc.addInsert(7, 5, ins);
    assertEquals(6, auc.updatesSize());
  }

  /**
   * Tests tree-aware updates algorithm. - Delete and Insert on the single child of a node
   */
  @Test public void treeAwareUpdates1() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache auc = atomics(doc);
    final MemData md = new MemData(context.options);
    auc.addDelete(2);
    auc.addInsert(3, 2, elemClip(md, "<c/>", false));
    assertEquals(1, auc.updatesSize());
    query(transform(doc, "insert node <c/> into $input/b, delete node $input/b"), "<a/>");
  }

  /**
   * Tests tree-aware updates algorithm. - Insert on a target T - Delete and Insert on the
   * single child of T
   */
  @Test public void treeAwareUpdates2() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache auc = atomics(doc);
    final MemData md = new MemData(context.options);
    auc.addDelete(2);
    auc.addInsert(3, 2, elemClip(md, "<c/>", false));
    auc.addInsert(3, 1, elemClip(md, "<d/>", false));
    assertEquals(1, auc.updatesSize());
    query(transform(doc, "insert node <d/> into $input, insert node <c/> into $input/b,"
        + "delete node $input/b"), "<a>\n<d/>\n</a>");
  }

  /**
   * Tests tree-aware updates algorithm. - Insert on a target T - Replace on child of T
   */
  @Test public void treeAwareUpdates3() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache auc = atomics(doc);
    final MemData md = new MemData(context.options);
    auc.addReplace(2, elemClip(md, "<newb/>", false));
    auc.addInsert(3, 1, elemClip(md, "<d/>", false));
    assertEquals(2, auc.updatesSize());
    query(transform(doc, "insert node <d/> into $input,"
        + "replace node $input/b with <newb/>"), "<a>\n<newb/>\n<d/>\n</a>");
  }

  /**
   * Tests tree-aware updates algorithm. - Replace and InsertInto on the single child of a
   * node
   */
  @Test public void treeAwareUpdates4() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache auc = atomics(doc);
    final MemData md = new MemData(context.options);
    auc.addReplace(2, elemClip(md, "<newb/>", false));
    auc.addInsert(3, 2, elemClip(md, "<c/>", false));
    assertEquals(1, auc.updatesSize());
    query(transform(doc, "insert node <c/> into $input/b,"
        + "replace node $input/b with <newb/>"), "<a>\n<newb/>\n</a>");
  }

  /**
   * Tests tree-aware updates algorithm with value updates.
   */
  @Test public void treeAwareUpdates5() {
    final String doc = "<a><b/></a>";
    final AtomicUpdateCache auc = atomics(doc);
    auc.addDelete(2);
    auc.addRename(2, token("bb"), EMPTY);
    assertEquals(1, auc.updatesSize());
  }

  /**
   * Tests tree-aware updates algorithm with attribute updates.
   */
  @Test public void treeAwareUpdates6() {
    final String doc = "<a><b id='1'/></a>";
    final AtomicUpdateCache auc = atomics(doc);
    auc.addDelete(3);
    auc.addRename(3, token("idx"), EMPTY);
    assertEquals(1, auc.updatesSize());
  }

  /**
   * Merge atomic updates A,B with A being an insert atomic.
   */
  @Test public void mergeSequence01() {
    // a=1, b=2
    final String doc = "<a><b/></a>";

    // two inserts cannot be merged!
    AtomicUpdateCache auc = atomics(doc);
    final MemData md = new MemData(context.options);
    auc.addInsert(3, 2, elemClip(md, "<c/>", false));
    auc.addInsert(3, 2, elemClip(md, "<d/>", false));
    assertEquals(2, auc.updatesSize());
    query(transform(doc, "insert node <c/> into $input/b,insert node <d/> into $input/b"),
        "<a>\n<b>\n<c/>\n<d/>\n</b>\n</a>");

    // delete(x) -> insert(x+1)
    auc = atomics(doc);
    auc.addDelete(2);
    auc.addInsert(3, 1, elemClip(md, "<d/>", false));
    assertEquals(1, auc.updatesSize());
    query(transform(doc, "insert node <d/> into $input,delete node $input/b"),
        "<a>\n<d/>\n</a>");

    // delete(x) -> insert(x+1) with affecting insert before x
    auc = atomics("<a><b/><c/></a>");
    auc.addDelete(2);
    auc.addDelete(3);
    auc.addInsert(4, 1, elemClip(md, "<d/>", false));
    assertEquals(2, auc.updatesSize());
    query(transform("<a><b/><c/></a>", "insert node <d/> into $input," +
        "delete node $input/c,delete node $input/b"), "<a>\n<d/>\n</a>");

    // insert(x) <- delete(x)
    auc = atomics(doc);
    auc.addInsert(2, 1, elemClip(md, "<d/>", false));
    auc.addDelete(2);
    assertEquals(1, auc.updatesSize());
    query(transform(doc, "insert node <d/> into $input,delete node $input/b"), "<a>\n<d/>\n</a>");
  }

  /**
   * Multiple destructive operations on same node.
   */
  @Test public void updateSequence01() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      auc.addDelete(2);
      auc.addDelete(2);
    }, "Multiple deletes/replaces on node");
  }

  /**
   * Multiple destructive operations on same node.
   */
  @Test public void updateSequence02() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      final MemData md = new MemData(context.options);
      auc.addDelete(2);
      auc.addReplace(2, elemClip(md, "<newb/>", false));
    }, "Multiple deletes/replaces on node");
  }

  /**
   * Multiple destructive operations on same node.
   */
  @Test public void updateSequence03() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      final MemData md = new MemData(context.options);
      auc.addReplace(2, elemClip(md, "<newb/>", false));
      auc.addReplace(2, elemClip(md, "<newb/>", false));
    }, "Multiple deletes/replaces on node");
  }

  /**
   * Multiple renames/updates on same node.
   */
  @Test public void updateSequence04() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      auc.addRename(2, token("foo"), EMPTY);
      auc.addRename(2, token("foo2"), EMPTY);
    }, "Multiple renames on node");
  }

  /**
   * Multiple renames/updates on same node.
   */
  @Test public void updateSequence05() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      auc.addUpdateValue(2, token("foo"));
      auc.addUpdateValue(2, token("foo"));
    }, "Multiple updates on node");
  }

  /**
   * Order of updates.
   */
  @Test public void updateSequence06() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      auc.addDelete(3);
      auc.addDelete(2);
    }, "Invalid order at location");
  }

  /**
   * Order of updates.
   */
  @Test public void updateSequence07() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      auc.addRename(2, token("bb"), EMPTY);
      auc.addDelete(2);
    }, "Invalid sequence of value update and destructive update at location 2");
  }

  /**
   * Order of updates.
   */
  @Test public void updateSequence08() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      final MemData md = new MemData(context.options);
      auc.addDelete(2);
      auc.addInsert(2, 1, elemClip(md, "<dummy/>", false));
    }, "Invalid sequence of delete, insert at location 2");
  }

  /**
   * Order of updates.
   */
  @Test public void updateSequence09() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b id='0'/></a>");
      final MemData md = new MemData(context.options);
      auc.addDelete(3);
      auc.addInsert(3, 2, attrClip(md, "id", "1"));
    }, "Invalid sequence of delete, insert at location 3");
  }

  /**
   * Order of updates.
   */
  @Test public void updateSequence10() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b/></a>");
      final MemData md = new MemData(context.options);
      auc.addReplace(2, elemClip(md, "<bb/>", false));
      auc.addInsert(2, 1, elemClip(md, "<dummy/>", false));
    }, "Invalid sequence of replace, insert at location 2");
  }

  /**
   * Order of updates.
   */
  @Test public void updateSequence11() {
    assertThrows(RuntimeException.class, () -> {
      final AtomicUpdateCache auc = atomics("<a><b id='0'/></a>");
      final MemData md = new MemData(context.options);
      auc.addReplace(3, attrClip(md, "id", "11"));
      auc.addInsert(3, 2, attrClip(md, "id", "1"));
    }, "Invalid sequence of replace, insert at location 3");
  }

  /**
   * Tests if PRE values are correctly calculated before/after updates.
   */
  @Test public void calculatePreValues() {
    final String doc =
      "<n1>" +
        "<n2/><n3/><n4/><n5/><n6/><n7/><n8/><n9/><n10/><n11/>" +
      "</n1>";
    AtomicUpdateCache auc = atomics(doc);
    auc.addDelete(3);
    auc.applyUpdates();
    assertEquals(2, auc.calculatePreValue(2, false));
    assertEquals(3, auc.calculatePreValue(3, false));
    assertEquals(3, auc.calculatePreValue(4, false));
    assertEquals(4, auc.calculatePreValue(5, false));
    assertEquals(2, auc.calculatePreValue(2, true));
    assertEquals(4, auc.calculatePreValue(3, true));
    assertEquals(5, auc.calculatePreValue(4, true));
    assertEquals(6, auc.calculatePreValue(5, true));

    auc = atomics(doc);
    auc.addDelete(3);
    auc.addDelete(5);
    auc.addDelete(7);
    auc.applyUpdates();
    assertEquals(2, auc.calculatePreValue(2, false));
    assertEquals(3, auc.calculatePreValue(3, false));
    assertEquals(3, auc.calculatePreValue(4, false));
    assertEquals(4, auc.calculatePreValue(5, false));
    assertEquals(4, auc.calculatePreValue(6, false));
    assertEquals(5, auc.calculatePreValue(7, false));
    assertEquals(5, auc.calculatePreValue(8, false));
    assertEquals(2, auc.calculatePreValue(2, true));
    assertEquals(4, auc.calculatePreValue(3, true));
    assertEquals(6, auc.calculatePreValue(4, true));
    assertEquals(8, auc.calculatePreValue(5, true));
    assertEquals(9, auc.calculatePreValue(6, true));
    assertEquals(10, auc.calculatePreValue(7, true));
    assertEquals(11, auc.calculatePreValue(8, true));

    auc = atomics(doc);
    // mind that dummy insert data instance size==2!
    final MemData md = new MemData(context.options);
    auc.addInsert(3, 1, elemClip(md, "<dummy3/>", true));
    auc.addInsert(3, 1, elemClip(md, "<dummy4/>", true));
    auc.addDelete(3);
    auc.applyUpdates();
    assertEquals(1, auc.calculatePreValue(1, false));
    assertEquals(2, auc.calculatePreValue(2, false));
    assertEquals(5, auc.calculatePreValue(3, false));
    assertEquals(7, auc.calculatePreValue(4, false));
    assertEquals(8, auc.calculatePreValue(5, false));
    assertEquals(9, auc.calculatePreValue(6, false));
    assertEquals(1, auc.calculatePreValue(1, true));
    assertEquals(2, auc.calculatePreValue(2, true));
    // nodes like 3,4 that have not existed prior to insert are not recalculated
    assertEquals(3, auc.calculatePreValue(3, true));
    assertEquals(4, auc.calculatePreValue(4, true));
    assertEquals(3, auc.calculatePreValue(5, true));
    assertEquals(4, auc.calculatePreValue(6, true));
  }

  /**
   * Tests if distances are updated correctly.
   */
  @Test public void distanceCaching() {
    final String doc = "<n1>" + "<n2>T3</n2>T4<n5/>T6<n7/>"
        + "<n8><n9><n10><n11/><n12/></n10></n9></n8><n13/><n14/>" + "</n1>";
    final AtomicUpdateCache auc = atomics(doc);
    final MemData md = new MemData(context.options);
    auc.addDelete(3);
    auc.addReplace(5, elemClip(md, "dummy1", true));
    auc.addInsert(11, 10, elemClip(md, "dummy2", true));
    auc.addInsert(13, 10, elemClip(md, "dummy3", true));
    auc.addDelete(13);
    auc.execute(true);
    checkDistances(auc.data, new int[][] { new int[] { 17, 1}, new int[] { 16, 15},
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
  @Test public void textMerging() {
    final String doc = "<n1>" + "<n2>T3</n2>T4<n5/>T6<n7/>" + "</n1>";
    final AtomicUpdateCache auc = atomics(doc);
    // MemData needed to build valid DataClip object
    final MemData md = new MemData(context.options);
    auc.addInsert(3, 2, textClip(md, "Tx0"));
    auc.addDelete(3);
    auc.addInsert(4, 2, textClip(md, "Tx01"));
    auc.addDelete(5);
    auc.addReplace(6, textClip(md, "T6new"));
    auc.addInsert(8, 7, textClip(md, "Tx1"));
    auc.addInsert(8, 7, textClip(md, "Tx2"));
    auc.addInsert(8, 1, textClip(md, "Tx3"));
    auc.execute(true);
    checkTextAdjacency(auc.data, new byte[][] { token("Tx0Tx01"), token("T4T6new"),
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
  private static DataClip textClip(final Data d, final String text) {
    final int s = d.meta.size;
    d.text(s + 1, token(text), Data.TEXT);
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
  private static DataClip attrClip(final Data d, final String name, final String value) {
    final int s = d.meta.size;
    d.attr(s + 1, d.attrNames.put(token(name)), token(value), -1);
    d.insert(s);
    return new DataClip(d, s, d.meta.size);
  }

  /**
   * Creates a small insertion sequence data containing 2 nodes.
   * @param d parent data instance
   * @param n name of the elements to be inserted
   * @param b add tree w/ size==2 if false add tree w/ size==1
   * @return insertion sequence data instance
   */
  private static DataClip elemClip(final Data d, final String n, final boolean b) {
    final int s = d.meta.size;
    d.elem(s + 1, d.elemNames.put(token(n)), 1, b ? 2 : 1, 0, false);
    d.insert(s);
    if(b) {
      d.elem(1, d.elemNames.put(token(n)), 1, 1, 0, false);
      d.insert(s + 1);
    }
    return new DataClip(d, s, d.meta.size);
  }

  /**
   * Creates an {@link AtomicUpdateCache} for the given XML fragment.
   * @param doc XML fragment string
   * @return atomic update list or {@code null}
   */
  private static AtomicUpdateCache atomics(final String doc) {
    try {
      return new AtomicUpdateCache(MemBuilder.build(new IOContent(doc)));
    } catch(final IOException ex) {
      fail(Util.message(ex));
      return null;
    }
  }
}
