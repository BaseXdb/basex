package org.basex.test.storage;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Tim Petrowsky
 */
public final class DataUpdateTestDeletes extends DataUpdateTest {
  /**
   * Test deletion of a simple node.
   * @throws Exception in case of problems.
   */
  @Test
  public void testSimpleNodeDelete() throws Exception {
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParSize = data.size(2, Data.ELEM);
    data.delete(3);
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(3));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
    reload();
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(3));
    assertEquals(oldDocSize - 1, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParSize - 1, data.size(2, Data.ELEM));
    assertEquals(1, data.parent(3, Data.ELEM));
  }

  /**
   * Test deletion of a node with a child.
   * @throws Exception in case of problems.
   */
  @Test
  public void testCascadingDelete() throws Exception {
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    data.delete(2);
    assertEquals(size - 2, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(2));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
    reload();
    assertEquals(size - 2, data.size);
    assertByteArraysEqual(token("parentnode"), data.tag(2));
    assertEquals(oldDocSize - 2, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 2, data.size(1, Data.ELEM));
    assertEquals(1, data.parent(2, Data.ELEM));
  }

  /**
   * Test deletion of a node with a child (with text) and attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testCascadingDelete2() throws Exception {
    final int oldDocSize = data.size(0, Data.DOC);
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(4, Data.ELEM);
    data.delete(6);
    assertEquals(size - 5, data.size);
    assertByteArraysEqual(token("b"),         data.tag(6));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
    reload();
    assertEquals(size - 5, data.size);
    assertByteArraysEqual(token("b"), data.tag(6));
    assertEquals(oldDocSize - 5, data.size(0, Data.DOC));
    assertEquals(oldRootSize - 5, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 5, data.size(4, Data.ELEM));
    assertEquals(2, data.parent(3, Data.ELEM));
  }

  /**
   * Test deletion of an attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testDeleteAttribute() throws Exception {
    final int oldRootSize = data.size(1, Data.ELEM);
    final int oldParentSize = data.size(6, Data.ELEM);
    data.delete(7);
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("contextnode"), data.tag(6));
    assertByteArraysEqual(token("id"), data.attName(7));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 1, data.size(6, Data.ELEM));
    assertEquals(6, data.parent(7, Data.ATTR));
    assertEquals(4, data.size(6, Data.ELEM));
    reload();
    assertEquals(size - 1, data.size);
    assertByteArraysEqual(token("contextnode"), data.tag(6));
    assertByteArraysEqual(token("id"), data.attName(7));
    assertEquals(oldRootSize - 1, data.size(1, Data.ELEM));
    assertEquals(oldParentSize - 1, data.size(6, Data.ELEM));
    assertEquals(6, data.parent(7, Data.ATTR));
    assertEquals(4, data.size(6, Data.ELEM));
  }

  /**
   * For the sake of completeness.
   * Test deletion of a text-node.
   * @throws Exception in case of problems.
   */
  @Test
  public void testDeleteText() throws Exception {
    data.delete(10);
    assertEquals(size - 1, data.size);
    reload();
    assertEquals(size - 1, data.size);
  }
}
