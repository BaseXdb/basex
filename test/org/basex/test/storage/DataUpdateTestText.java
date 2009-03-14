package org.basex.test.storage;

import static org.basex.util.Token.*;
import static org.junit.Assert.*;
import java.io.IOException;
import org.basex.data.Data;
import org.junit.Test;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public final class DataUpdateTestText extends DataUpdateTest {
  /**
   * Test insert as last child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testInsertTextAsOnly1() throws Exception {
    final int nextid = data.meta.lastid;
    insertText(3, 0, token("junit"), Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.atom(4));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.atom(4));
  }

  /**
   * Test insert as last child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testInsertTextAsOnly2() throws Exception {
    final int nextid = data.meta.lastid;
    insertText(3, 1, token("junit"), Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.atom(4));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.atom(4));
  }

  /**
   * Test insert as last child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testInsertTextAsOnly3() throws Exception {
    final int nextid = data.meta.lastid;
    insertText(3, 2, token("junit"), Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.atom(4));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals((int) Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("junit"), data.atom(4));
  }

  /**
   * Test insert as last child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testInsertTextAfterAttsAsFirst() throws Exception {
    final int nextid = data.meta.lastid;
    insertText(6, 1, token("junit"), Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals((int) Data.TEXT, data.kind(9));
    assertEquals(6, data.parent(9, Data.TEXT));
    assertByteArraysEqual(token("junit"), data.atom(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals((int) Data.TEXT, data.kind(9));
    assertEquals(6, data.parent(9, Data.TEXT));
    assertByteArraysEqual(token("junit"), data.atom(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Test insert as last child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testInsertTextAfterAttsAsSecond() throws Exception {
    final int nextid = data.meta.lastid;
    insertText(6, 2, token("junit"), Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);

    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Test insert as last child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testInsertTextAfterAttsAsLast() throws Exception {
    final int nextid = data.meta.lastid;
    insertText(6, 0, token("junit"), Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals((int) Data.ELEM, data.kind(9));
    assertByteArraysEqual(token("junit"), data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Test insert text before text.
   * @throws Exception in case of problems.
   */
  @Test(expected = IOException.class)
  public void testInsertTextBeforeText() throws Exception {
    insertText(9, 1, token("foobar"), Data.TEXT);
  }

  /**
   * Test insert text before text.
   * @throws Exception in case of problems.
   */
  @Test(expected = IOException.class)
  public void testInsertTextAfterTextAsSecond() throws Exception {
    insertText(9, 2, token("foobar"), Data.TEXT);
  }

  /**
   * Test insert text before text.
   * @throws Exception in case of problems.
   */
  @Test(expected = IOException.class)
  public void testInsertTextAfterTextAsLast() throws Exception {
    insertText(9, 0, token("foobar"), Data.TEXT);
  }

  /**
   * Test updateText.
   * @throws Exception in case of problems
   */
  @Test
  public void testUpdateText() throws Exception {
    data.update(10, token("JUnit"));
    assertEquals((int) Data.TEXT, data.kind(10));
    assertByteArraysEqual(token("JUnit"), data.text(10));
    reload();
    assertEquals((int) Data.TEXT, data.kind(10));
    assertByteArraysEqual(token("JUnit"), data.text(10));
  }

  /**
   * Inserts a tag.
   * @param par parent node
   * @param pos inserting position
   * @param name tag name
   * @param kind node kind
   * @throws IOException I/O exception
   */
  private void insertText(final int par, final int pos, final byte[] name,
      final byte kind) throws IOException {

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

    data.insert(pre, par, name, kind);
  }

  /**
   * Don't remove.
   *
   */
  @Test
  public void foo() {
    return;
  }
}
