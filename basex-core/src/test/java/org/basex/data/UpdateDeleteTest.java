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
public final class UpdateDeleteTest extends DataUpdateTest {
  /**
   * Tests deletion of a simple node.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void simpleNodeDelete(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParSize = data.size(2, Data.ELEM);
    data.startUpdate(context.options);
    data.delete(3);
    data.finishUpdate(context.options);
    assertEquals(size - 1, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(3, Data.ELEM));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
    reload(mainmem);
    assertEquals(size - 1, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(3, Data.ELEM));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
  }

  /**
   * Tests deletion of a node with a child.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void cascadingDelete(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    data.startUpdate(context.options);
    data.delete(2);
    data.finishUpdate(context.options);
    assertEquals(size - 2, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(2, Data.ELEM));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
    reload(mainmem);
    assertEquals(size - 2, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(2, Data.ELEM));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
  }

  /**
   * Tests deletion of a node with a child (with text) and attribute.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void cascadingDelete2(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(4, Data.ELEM);
    data.startUpdate(context.options);
    data.delete(6);
    data.finishUpdate(context.options);
    assertEquals(size - 5, data.meta.size);
    assertArraysEquals(T_B, data.name(6, Data.ELEM));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
    reload(mainmem);
    assertEquals(size - 5, data.meta.size);
    assertArraysEquals(T_B, data.name(6, Data.ELEM));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
  }

  /**
   * Tests deletion of an attribute.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void deleteAttribute(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(6, Data.ELEM);
    data.startUpdate(context.options);
    data.delete(7);
    data.finishUpdate(context.options);
    assertEquals(size - 1, data.meta.size);
    assertArraysEquals(T_CONTEXTNODE, data.name(6, Data.ELEM));
    assertArraysEquals(T_ID, data.name(7, Data.ATTR));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 1, data.size(6, Data.ELEM));
    assertEquals(6, data.parent(7, Data.ATTR));
    assertEquals(4, data.size(6, Data.ELEM));
    reload(mainmem);
    assertEquals(size - 1, data.meta.size);
    assertArraysEquals(T_CONTEXTNODE, data.name(6, Data.ELEM));
    assertArraysEquals(T_ID, data.name(7, Data.ATTR));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 1, data.size(6, Data.ELEM));
    assertEquals(6, data.parent(7, Data.ATTR));
    assertEquals(4, data.size(6, Data.ELEM));
  }

  /**
   * For the sake of completeness.
   * Tests deletion of a text-node.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void deleteText(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    data.startUpdate(context.options);
    data.delete(10);
    data.finishUpdate(context.options);
    assertEquals(size - 1, data.meta.size);
    reload(mainmem);
    assertEquals(size - 1, data.meta.size);
  }
}
