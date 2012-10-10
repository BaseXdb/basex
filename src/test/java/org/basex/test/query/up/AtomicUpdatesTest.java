package org.basex.test.query.up;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.core.cmd.*;
import org.basex.data.*;
import org.basex.data.atomic.*;
import org.basex.io.*;
import org.basex.test.query.*;
import org.basex.util.*;
import org.junit.*;
import org.junit.rules.*;

/**
 * Test the {@link AtomicUpdateList}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Lukas Kircher
 */
public final class AtomicUpdatesTest extends AdvancedQueryTest {
  /** Expected exception. */
  @Rule public ExpectedException thrown = ExpectedException.none();

  /**
   * Basic test for tree-aware updates.
   */
  @Test
  public void treeAwareUpdates() {
    final String doc =
        "<n1>" +
            "<n2 att3='0'><n4/><n5><n6/></n5></n2>" +
        "</n1>";
    final AtomicUpdateList l = atomics(doc);
    final Data ins = data("<d/>");
    l.addInsert(7, 6, ins, false);
    l.addReplace(6, ins);
    l.addInsert(6, 5, ins, false);
    l.addInsert(3, 2, ins, true);
    l.addDelete(2);
    optimize(l, 1);

    l.clear();
    l.addInsert(7, 5, ins, false);
    l.addInsert(7, 6, ins, false);
    l.addReplace(6, ins);
    l.addInsert(6, 5, ins, false);
    l.addInsert(4, 2, ins, true);
    l.addDelete(3);
    l.addInsert(2, 2, ins, true);
    optimize(l, 6);
  }

  /**
  * Tests tree-aware updates algorithm.
  * - Delete and Insert on the single child of a node
  */
 @Test
 public void treeAwareUpdates1() {
   final String doc = "<a><b/></a>";
   final AtomicUpdateList l = atomics(doc);
   l.addInsert(3, 2, data("<c/>"), false);
   l.addDelete(2);
   optimize(l, 1);
   query(transform(doc, "insert node <c/> into $input/b, delete node $input/b"), "<a/>");
 }

/**
  * Tests tree-aware updates algorithm.
  * - Insert on a target T
  * - Delete and Insert on the single child of T
  */
 @Test
 public void treeAwareUpdates2() {
   final String doc = "<a><b/></a>";
   final AtomicUpdateList l = atomics(doc);
   l.addInsert(3, 1, data("<d/>"), false);
   l.addInsert(3, 2, data("<c/>"), false);
   l.addDelete(2);
   optimize(l, 2);
   query(transform(doc, "insert node <d/> into $input, insert node <c/> into $input/b," +
       "delete node $input/b"), "<a><d/></a>");
 }

 /**
  * Tests tree-aware updates algorithm.
  * - Insert on a target T
  * - Replace on child of T
  */
 @Test
 public void treeAwareUpdates3() {
   final String doc = "<a><b/></a>";
   final AtomicUpdateList l = atomics(doc);
   l.addInsert(3, 1, data("<d/>"), false);
   l.addReplace(2, data("<newb/>"));
   optimize(l, 2);
   query(transform(doc, "insert node <d/> into $input," +
       "replace node $input/b with <newb/>"), "<a><newb/><d/></a>");
 }

 /**
  * Tests tree-aware updates algorithm.
  * - Replace and InsertInto on the single child of a node
  */
 @Test
 public void treeAwareUpdates4() {
   final String doc = "<a><b/></a>";
   final AtomicUpdateList l = atomics(doc);
   l.addInsert(3, 2, data("<c/>"), false);
   l.addReplace(2, data("<newb/>"));
   optimize(l, 1);
   query(transform(doc, "insert node <c/> into $input/b," +
       "replace node $input/b with <newb/>"), "<a><newb/></a>");
 }

 /**
  * Checks Constraints. A node can only be target of one 'destructive' operation like
  * replace and delete.
  */
 @Test
 public void noMultipleDeletesOnSameTarget() {
   final AtomicUpdateList l = atomics("<a><b/></a>");
   l.addDelete(2);
   l.addDelete(2);
   thrown.expect(RuntimeException.class);
   thrown.expectMessage("Multiple deletes/replaces on node");
   optimize(l, -1);

   l.clear();
   l.addDelete(2);
   l.addReplace(2, data("<newb/>"));
   thrown.expect(RuntimeException.class);
   thrown.expectMessage("Multiple deletes/replaces on node");
   optimize(l, -1);

   l.clear();
   l.addReplace(2, data("<newb/>"));
   l.addReplace(2, data("<newb/>"));
   thrown.expect(RuntimeException.class);
   thrown.expectMessage("Multiple deletes/replaces on node");
   optimize(l, -1);
 }

 /**
  * Checks Constraints.
  */
 @Test
 public void noMultipleRenamesOrUpdatesOnSameTarget() {
   final AtomicUpdateList l = atomics("<a><b/></a>");
   l.addRename(2, Data.ELEM, Token.token("foo"), Token.EMPTY);
   l.addRename(2, Data.ELEM, Token.token("foo2"), Token.EMPTY);
   thrown.expect(RuntimeException.class);
   thrown.expectMessage("Multiple renames on node");
   optimize(l, -1);

   l.clear();
   l.addUpdateValue(2, Data.ELEM, Token.token("foo"));
   l.addUpdateValue(2, Data.ELEM, Token.token("foo"));
   thrown.expect(RuntimeException.class);
   thrown.expectMessage("Multiple updates on node");
   optimize(l, -1);
 }

 /**
  * Checks Constraints.
  */
 @Test
 public void wrongOrderOfAtomicUpdates() {
   final AtomicUpdateList l = atomics("<a><b/></a>");
   l.addDelete(2);
   l.addDelete(3);
   thrown.expect(RuntimeException.class);
   thrown.expectMessage("Invalid order at location");
   optimize(l, -1);
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
   final AtomicUpdateList l = atomics(doc);

   l.addDelete(3);
   l.check();
   l.applyStructuralUpdates();
   assertEquals(2, l.calculatePreValue(2, false));
   assertEquals(3, l.calculatePreValue(3, false));
   assertEquals(3, l.calculatePreValue(4, false));
   assertEquals(4, l.calculatePreValue(5, false));
   assertEquals(2, l.calculatePreValue(2, true));
   assertEquals(4, l.calculatePreValue(3, true));
   assertEquals(5, l.calculatePreValue(4, true));
   assertEquals(6, l.calculatePreValue(5, true));

   l.clear();
   l.addDelete(7);
   l.addDelete(5);
   l.addDelete(3);
   l.check();
   l.applyStructuralUpdates();
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

   l.clear();
   l.addDelete(3);
   // mind that dummy insert data instance has size equal 2!
   l.addInsert(3, 1, data("<dummy4/>"), false);
   l.addInsert(3, 1, data("<dummy3/>"), false);
   l.check();
   l.applyStructuralUpdates();
   assertEquals(1, l.calculatePreValue(1, false));
   assertEquals(2, l.calculatePreValue(2, false));
   assertEquals(7, l.calculatePreValue(3, false));
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
   final String doc =
       "<n1>" +
           "<n2>T3</n2>T4<n5/>T6<n7/>" +
           "<n8><n9><n10><n11/><n12/></n10></n9></n8><n13/><n14/>" +
       "</n1>";
   final AtomicUpdateList l = atomics(doc);
   l.addDelete(13);
   l.addInsert(13, 10, insseq(l.data), false);
   l.addInsert(11, 10, insseq(l.data), false);
   l.addReplace(5, insseq(l.data));
   l.addDelete(3);
   l.execute(true);
   checkDistances(l.data, new int[][] {
       new int[] {17, 1},
       new int[] {16, 15},
       new int[] {15, 10},
       new int[] {14, 10},
       new int[] {13, 10},
       new int[] {12, 11},
       new int[] {11, 10},
       new int[] {10, 9},
       new int[] {9, 8},
       new int[] {8, 1},
       new int[] {7, 1},
       new int[] {6, 1},
       new int[] {5, 4},
       new int[] {4, 1},
       new int[] {3, 1},
       new int[] {2, 1},
       new int[] {1, 0},
   });
 }

 /**
  * Tests if the given child/parent PRE value pairs are still valid in the database.
 * @param d reference
 * @param pairs PRE value pairs
 */
private void checkDistances(final Data d, final int[][] pairs) {
   for(final int[] pair : pairs)
     if(d.parent(pair[0], d.kind(pair[0])) != pair[1])
       fail("Wrong parent for pre=" + pair[0]);
 }

/**
 * Tests if text node adjacency is correctly resolved.
 */
@Test
 public void textMerging() {
   final String doc =
       "<n1>" +
           "<n2>T3</n2>T4<n5/>T6<n7/>" +
       "</n1>";
   final AtomicUpdateList l = atomics(doc);
   l.addInsert(8, 1, data(l.data, "Tx3"), false);
   l.addInsert(8, 7, data(l.data, "Tx2"), false);
   l.addInsert(8, 7, data(l.data, "Tx1"), false);
   l.addReplace(6, data(l.data, "T6new"));
   l.addDelete(5);
   l.addInsert(4, 2, data(l.data, "Tx01"), false);
   l.addDelete(3);
   l.addInsert(3, 2, data(l.data, "Tx0"), false);
   l.execute(true);
   checkTextAdjacency(l.data, new byte[][] {
       token("Tx0Tx01"),
       token("T4T6new"),
       token("Tx1Tx2"),
       token("Tx3"),
   });
 }

 /**
  * Checks if text node adjacency has been correctly resolved for the given data instance.
 * @param data reference
 * @param texts in the expected order of occurrence
 */
private void checkTextAdjacency(final Data data, final byte[][] texts) {
   int i = 0;
   // find adjacent text nodes
   while(i + 1 < data.meta.size) {
     final int a = i++;
     final int b = i;
     final int aKind = data.kind(a);
     final int bKind = data.kind(b);
     if(aKind == Data.TEXT && bKind == Data.TEXT &&
         data.parent(a, aKind) == data.parent(b, bKind))
       fail("Adjacent text nodes at position " + i);
   }

   // check order of texts
   i = -1;
   int t = 0;
   while(++i < data.meta.size) {
     if(data.kind(i) == Data.TEXT && !eq(data.text(i, true), texts[t++]))
       fail("Invalid text node at position " + i);
   }
}

/**
  * Creates a {@link MemData} instance from the given string.
 * @param s XML fragment string
 * @return data instance or null in case of error
 */
 private Data data(final String s) {
   try {
     return CreateDB.mainMem(new IOContent(s), context);
    } catch(final IOException e) {
      fail(e.getMessage());
      return null;
    }
 }

 /**
  * Creates a Data instance that contains a text node with the given value.
 * @param d parent data instance
 * @param text for text node
 * @return data instance with text node
 */
private Data data(final Data d, final String text) {
   final MemData m = new MemData(d);
   m.text(0, 1, token(text), Data.TEXT);
   m.insert(0);
   return m;
 }

/**
 * Creates a small insertion sequence data containing 2 nodes.
 * @param d parent data instance
 * @return insertion sequence data instance
 */
private Data insseq(final Data d) {
  final MemData m = new MemData(d);
  m.elem(1, m.tagindex.index(token("dummy"), null, false), 1, 2, 0, false);
  m.insert(0);
  m.elem(1, m.tagindex.index(token("dummy"), null, false), 1, 1, 0, false);
  m.insert(1);
  return m;
}

 /**
  * Creates an {@link AtomicUpdateList} for the given XML fragment.
 * @param doc XML fragment string
 * @return atomic update list
 */
 private AtomicUpdateList atomics(final String doc) {
   return new AtomicUpdateList(data(doc));
 }

/**
 * Tests the given {@link AtomicUpdateList} for correct optimizations.
 * @param l list of atomic updates
 * @param expectedSize expected size after optimizing the list
 */
 private void optimize(final AtomicUpdateList l, final int expectedSize) {
  l.optimize();
  assertEquals(expectedSize, l.structuralUpdatesSize());
 }
}