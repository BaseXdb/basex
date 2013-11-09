package org.basex.data;

import static org.junit.Assert.*;

import org.junit.*;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Tim Petrowsky
 */
public final class UpdateTestDeletes extends UpdateTest {
  /**
   * Tests deletion of a simple node.
   */
  @Test
  public void simpleNodeDelete() {
    final Data data = context.data();
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParSize = data.size(2, Data.ELEM);
    data.startUpdate();
    data.delete(3);
    data.finishUpdate();
    assertEquals(size - 1, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(3, Data.ELEM));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
    reload();
    assertEquals(size - 1, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(3, Data.ELEM));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
  }

  /**
   * Tests deletion of a node with a child.
   */
  @Test
  public void cascadingDelete() {
    final Data data = context.data();
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    data.startUpdate();
    data.delete(2);
    data.finishUpdate();
    assertEquals(size - 2, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(2, Data.ELEM));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
    reload();
    assertEquals(size - 2, data.meta.size);
    assertArraysEquals(T_PARENTNODE, data.name(2, Data.ELEM));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
  }

  /**
   * Tests deletion of a node with a child (with text) and attribute.
   */
  @Test
  public void cascadingDelete2() {
    final Data data = context.data();
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(4, Data.ELEM);
    data.startUpdate();
    data.delete(6);
    data.finishUpdate();
    assertEquals(size - 5, data.meta.size);
    assertArraysEquals(T_B, data.name(6, Data.ELEM));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
    reload();
    assertEquals(size - 5, data.meta.size);
    assertArraysEquals(T_B, data.name(6, Data.ELEM));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
  }

  /**
   * Tests deletion of an attribute.
   */
  @Test
  public void deleteAttribute() {
    final Data data = context.data();
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(6, Data.ELEM);
    data.startUpdate();
    data.delete(7);
    data.finishUpdate();
    assertEquals(size - 1, data.meta.size);
    assertArraysEquals(T_CONTEXTNODE, data.name(6, Data.ELEM));
    assertArraysEquals(T_ID, data.name(7, Data.ATTR));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 1, data.size(6, Data.ELEM));
    assertEquals(6, data.parent(7, Data.ATTR));
    assertEquals(4, data.size(6, Data.ELEM));
    reload();
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
   */
  @Test
  public void deleteText() {
    final Data data = context.data();
    data.startUpdate();
    data.delete(10);
    data.finishUpdate();
    assertEquals(size - 1, data.meta.size);
    reload();
    assertEquals(size - 1, data.meta.size);
  }
}
