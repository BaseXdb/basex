package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Tim Petrowsky
 */
public final class UpdateTextTest extends DataUpdateTest {
  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAsOnly1(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int nextid = data.meta.lastid;
    insertText(3, 0, T_JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.atom(4));
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.atom(4));
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAsOnly2(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int nextid = data.meta.lastid;
    insertText(3, 1, T_JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.atom(4));
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.atom(4));
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAsOnly3(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int nextid = data.meta.lastid;
    insertText(3, 2, T_JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.atom(4));
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.TEXT));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.atom(4));
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAfterAttsAsFirst(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int nextid = data.meta.lastid;
    insertText(6, 1, T_JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.TEXT, data.kind(9));
    assertEquals(6, data.parent(9, Data.TEXT));
    assertArraysEquals(T_JUNIT, data.atom(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.TEXT, data.kind(9));
    assertEquals(6, data.parent(9, Data.TEXT));
    assertArraysEquals(T_JUNIT, data.atom(9));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAfterAttsAsSecond(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int nextid = data.meta.lastid;
    insertText(6, 2, T_JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);

    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAfterAttsAsLast(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int nextid = data.meta.lastid;
    insertText(6, 0, T_JUNIT, Data.TEXT);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.atom(11));
    assertEquals(6, data.parent(11, Data.TEXT));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests insert text before text.
   * @param mainmem main-memory flag
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextBeforeText(final boolean mainmem) {
    setUp(mainmem);
    assertThrows(IOException.class, () -> insertText(9, 1, T_FOO, Data.TEXT));
  }

  /**
   * Tests insert text before text.
   * @param mainmem main-memory flag
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAfterTextAsSecond(final boolean mainmem) {
    setUp(mainmem);
    assertThrows(IOException.class, () -> insertText(9, 2, T_FOO, Data.TEXT));
  }

  /**
   * Tests insert text before text.
   * @param mainmem main-memory flag
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertTextAfterTextAsLast(final boolean mainmem) {
    setUp(mainmem);
    assertThrows(IOException.class, () -> insertText(9, 0, T_FOO, Data.TEXT));
  }

  /**
   * Tests updateText.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void updateText(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    data.startUpdate(context.options);
    data.update(10, Data.TEXT, T_JUNIT);
    data.finishUpdate(context.options);
    assertEquals(Data.TEXT, data.kind(10));
    assertArraysEquals(T_JUNIT, data.text(10, true));
    reload(mainmem);
    assertEquals(Data.TEXT, data.kind(10));
    assertArraysEquals(T_JUNIT, data.text(10, true));
  }

  /**
   * Inserts a value in the database.
   * @param par parent node
   * @param pos inserting position
   * @param val value to be inserted
   * @param kind node kind
   * @throws IOException I/O exception
   */
  private static void insertText(final int par, final int pos, final byte[] val, final byte kind)
      throws IOException {

    final Data data = context.data();
    int pre = par;
    int k = data.kind(pre);
    if(pos == 0) {
      pre += data.size(pre, k);
    } else {
      pre += data.attSize(pre, k);
      for(int p = 1; p < pos && data.parent(pre, k) == par;
        pre += data.size(pre, k), ++p) k = data.kind(pre);
    }

    if(kind == Data.TEXT && (data.kind(pre) == Data.TEXT || data.parent(pre - 1,
        data.kind(pre - 1)) == par && data.kind(pre - 1) == Data.TEXT))
      throw new IOException("May not insert TEXT before/after TEXT!");

    final MemData md = new MemData(context.options);
    md.text(pre - par, val, kind);
    md.insert(0);
    data.startUpdate(context.options);
    data.insert(pre, par, new DataClip(md));
    data.finishUpdate(context.options);
  }
}
