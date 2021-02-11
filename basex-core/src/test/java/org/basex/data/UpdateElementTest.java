package org.basex.data;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;

import org.basex.util.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Tim Petrowsky
 */
public final class UpdateElementTest extends DataUpdateTest {
  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertElementAsOnly1(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final long nextid = data.meta.lastid;
    insertElement(3, 0, T_JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.name(4, Data.ELEM));
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.name(4, Data.ELEM));
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertElementAsOnly2(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final long nextid = data.meta.lastid;
    insertElement(3, 1, T_JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.name(4, Data.ELEM));
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.name(4, Data.ELEM));
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertElementAsOnly3(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final long nextid = data.meta.lastid;
    insertElement(3, 2, T_JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(Data.ELEM, data.kind(5));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.name(4, Data.ELEM));
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(3, data.parent(4, Data.ELEM));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(1, data.parent(5, Data.ELEM));
    assertEquals(5, data.parent(6, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_JUNIT, data.name(4, Data.ELEM));
  }

  /**
   * Tests insert as last child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void insertElementAfterAttsAsFirst(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final long nextid = data.meta.lastid;
    insertElement(6, 1, T_JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertArraysEquals(T_JUNIT, data.name(9, Data.ELEM));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertArraysEquals(T_JUNIT, data.name(9, Data.ELEM));
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
  public void insertElementAfterAttsAsSecond(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final long nextid = data.meta.lastid;
    insertElement(6, 2, T_JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
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
  public void insertElementAfterAttsAsLast(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final long nextid = data.meta.lastid;
    insertElement(6, 0, T_JUNIT, Data.ELEM);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
    reload(mainmem);
    assertEquals(size + 1, data.meta.size);
    assertEquals(Data.ELEM, data.kind(9));
    assertArraysEquals(T_JUNIT, data.name(11, Data.ELEM));
    assertEquals(6, data.parent(11, Data.ELEM));
    assertEquals(6, data.parent(9, Data.ELEM));
    assertEquals(4, data.parent(12, Data.ELEM));
    assertEquals(nextid + 1, data.meta.lastid);
  }

  /**
   * Tests updateTagName.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void updateElementName(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    data.startUpdate(context.options);
    data.update(6, Data.ELEM, T_JUNIT, Token.EMPTY);
    data.finishUpdate(context.options);
    assertEquals(Data.ELEM, data.kind(6));
    assertArraysEquals(T_JUNIT, data.name(6, Data.ELEM));
    reload(mainmem);
    assertEquals(Data.ELEM, data.kind(6));
    assertArraysEquals(T_JUNIT, data.name(6, Data.ELEM));
  }

  /**
   * Inserts a tag.
   * @param par parent node
   * @param pos inserting position
   * @param name tag name
   * @param kind node kind
   * @throws IOException I/O exception
   */
  private static void insertElement(final int par, final int pos, final byte[] name, final int kind)
      throws IOException {

    int root;
    final Data data = context.data();
    if(pos == 0) {
      root = par + data.size(par, kind);
    } else {
      root = par + data.attSize(par, kind);
      int currPos = 1;
      while(currPos < pos) {
        final int k = data.kind(root);
        if(data.parent(root, k) != par) break;
        root += data.size(root, k);
        ++currPos;
      }
    }

    final MemData md = new MemData(context.options);
    md.elem(1, md.elemNames.put(name), 1, 1, 0, false);
    md.insert(0);
    data.startUpdate(context.options);
    data.insert(root, par, new DataClip(md));
    data.finishUpdate(context.options);
  }
}
