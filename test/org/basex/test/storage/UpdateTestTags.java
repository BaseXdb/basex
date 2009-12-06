package org.basex.test.storage;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.util.Token;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public final class UpdateTestTags extends UpdateTest {
  /**
   * Tests insert as last child.
   */
  @Test
  public void testInsertTagAsOnly1() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;
    insertTag(3, 0, JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.name(4, Data.ELEM));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.name(4, Data.ELEM));
  }

  /**
   * Tests insert as last child.
   */
  @Test
  public void testInsertTagAsOnly2() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;
    insertTag(3, 1, JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.name(4, Data.ELEM));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.name(4, Data.ELEM));
  }

  /**
   * Tests insert as last child.
   */
  @Test
  public void testInsertTagAsOnly3() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;
    insertTag(3, 2, JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.name(4, Data.ELEM));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.name(4, Data.ELEM));
  }

  /**
   * Tests insert as last child.
   */
  @Test
  public void testInsertTagAfterAttsAsFirst() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;
    insertTag(6, 1, JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertByteArraysEqual(JUNIT, data.name(9, Data.ELEM));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertByteArraysEqual(JUNIT, data.name(9, Data.ELEM));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert as last child.
   */
  @Test
  public void testInsertTagAfterAttsAsSecond() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;
    insertTag(6, 2, JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);

    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert as last child.
   */
  @Test
  public void testInsertTagAfterAttsAsLast() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;
    insertTag(6, 0, JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests updateTagName.
   */
  @Test
  public void testUpdateTagName() {
    final Data data = CONTEXT.data;
    data.rename(6, Data.ELEM, JUNIT, Token.EMPTY);
    assertEquals(Data.ELEM, data.kind(6));
    assertByteArraysEqual(JUNIT, data.name(6, Data.ELEM));
    reload();
    assertEquals(Data.ELEM, data.kind(6));
    assertByteArraysEqual(JUNIT, data.name(6, Data.ELEM));
  }

  /**
   * Don't remove.
   */
  @Test
  public void foo() {
    return;
  }

  /**
   * Inserts a tag.
   * @param par parent node
   * @param pos inserting position
   * @param name tag name
   * @param kind node kind
   */
  private void insertTag(final int par, final int pos,
      final byte[] name, final int kind) {
    int root = par;
    final Data data = CONTEXT.data;
    if(pos == 0) {
      root = par + data.size(par, kind);
    } else {
      int currPos = 1;
      root = par + data.attSize(par, kind);
      while(currPos < pos) {
        final int k = data.kind(root);
        if(data.parent(root, k) != par) break;
        root += data.size(root, k);
        currPos++;
      }
    }
    final MemData md = new MemData(CONTEXT.data);
    final int n = CONTEXT.data.tags.index(name, null, false);
    md.insertElem(0, 1, n, 1, 1, 0, false);
    data.insert(root, par, md);
  }
}
