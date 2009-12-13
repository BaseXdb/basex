package org.basex.test.storage;

import static org.junit.Assert.*;
import java.io.IOException;
import org.basex.data.Data;
import org.basex.data.MemData;
import org.junit.Test;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public final class UpdateTestText extends UpdateTest {
  /**
   * Tests insert as last child.
   * @throws IOException I/O exception
   */
  @Test
  public void testInsertTextAsOnly1() throws IOException {
    final Data data = CONTEXT.data;
    final int nextid = data.meta.lastid;
    insertText(3, 0, JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.atom(4));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.atom(4));
  }

  /**
   * Tests insert as last child.
   * @throws IOException I/O exception
   */
  @Test
  public void testInsertTextAsOnly2() throws IOException {
    final Data data = CONTEXT.data;
    final int nextid = data.meta.lastid;
    insertText(3, 1, JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.atom(4));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.atom(4));
  }

  /**
   * Tests insert as last child.
   * @throws IOException I/O exception
   */
  @Test
  public void testInsertTextAsOnly3() throws IOException {
    final Data data = CONTEXT.data;
    final int nextid = data.meta.lastid;
    insertText(3, 2, JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.atom(4));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(JUNIT, data.atom(4));
  }

  /**
   * Tests insert as last child.
   * @throws IOException I/O exception
   */
  @Test
  public void testInsertTextAfterAttsAsFirst() throws IOException {
    final Data data = CONTEXT.data;
    final int nextid = data.meta.lastid;
    insertText(6, 1, JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.TEXT, data.kind(9));
    assertEquals(6, data.parent(9, Data.TEXT));
    assertByteArraysEqual(JUNIT, data.atom(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.TEXT, data.kind(9));
    assertEquals(6, data.parent(9, Data.TEXT));
    assertByteArraysEqual(JUNIT, data.atom(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert as last child.
   * @throws IOException I/O exception
   */
  @Test
  public void testInsertTextAfterAttsAsSecond() throws IOException {
    final Data data = CONTEXT.data;
    final int nextid = data.meta.lastid;
    insertText(6, 2, JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);

    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert as last child.
   * @throws IOException I/O exception
   */
  @Test
  public void testInsertTextAfterAttsAsLast() throws IOException {
    final Data data = CONTEXT.data;
    final int nextid = data.meta.lastid;
    insertText(6, 0, JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertByteArraysEqual(JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert text before text.
   * @throws IOException I/O exception
   */
  @Test(expected = IOException.class)
  public void testInsertTextBeforeText() throws IOException {
    insertText(9, 1, FOO, Data.TEXT);
  }

  /**
   * Tests insert text before text.
   * @throws IOException I/O exception
   */
  @Test(expected = IOException.class)
  public void testInsertTextAfterTextAsSecond() throws IOException {
    insertText(9, 2, FOO, Data.TEXT);
  }

  /**
   * Tests insert text before text.
   * @throws IOException I/O exception
   */
  @Test(expected = IOException.class)
  public void testInsertTextAfterTextAsLast() throws IOException {
    insertText(9, 0, FOO, Data.TEXT);
  }

  /**
   * Tests updateText.
   */
  @Test
  public void testUpdateText() {
    final Data data = CONTEXT.data;
    data.replace(10, Data.TEXT, JUNIT);
    assertEquals(Data.TEXT, data.kind(10));
    assertByteArraysEqual(JUNIT, data.text(10, true));
    reload();
    assertEquals(Data.TEXT, data.kind(10));
    assertByteArraysEqual(JUNIT, data.text(10, true));
  }

  /**
   * Inserts a value in the database.
   * @param par parent node
   * @param pos inserting position
   * @param val value to be inserted
   * @param kind node kind
   * @throws IOException I/O exception
   */
  private void insertText(final int par, final int pos, final byte[] val,
      final byte kind) throws IOException {

    final Data data = CONTEXT.data;
    int pre = par;
    int k = data.kind(pre);
    if(pos == 0) {
      pre += data.size(pre, k);
    } else {
      pre += data.attSize(pre, k);
      for(int p = 1; p < pos && data.parent(pre, k) == par; pre += data.size(
          pre, k), p++) {
        k = data.kind(pre);
      }
    }

    if(kind == Data.TEXT && (data.kind(pre) == Data.TEXT || data.parent(pre - 1,
        data.kind(pre - 1)) == par && data.kind(pre - 1) == Data.TEXT))
      throw new IOException("May not insert TEXT before/after TEXT!");

    final MemData md = new MemData(CONTEXT.data);
    md.text(0, pre - par, val, kind);
    md.insert(0);
    data.insert(pre, par, md);
  }

  /** Don't remove. */
  @Test
  public void foo() {
    return;
  }
}
