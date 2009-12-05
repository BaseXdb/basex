package org.basex.test.storage;

import static org.basex.util.Token.*;
import org.basex.data.Data;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the update features of the Data class.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Tim Petrowsky
 */
public final class UpdateTestAttributes extends UpdateTest {
  /**
   * Tests the update of an existing attribute.
   */
  @Test
  public void testUpdateAttribute() {
    final Data data = CONTEXT.data;
    data.rename(7, Data.ATTR, token("name"));
    data.replace(7, Data.ATTR, token("junit"));
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("name"), data.name(7, false));
    assertByteArraysEqual(token("junit"), data.text(7, false));
    reload();
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("name"), data.name(7, false));
    assertByteArraysEqual(token("junit"), data.text(7, false));
  }

  /**
   * Tests the update of an existing attribute.
   */
  @Test
  public void testUpdateAttribute2() {
    final Data data = CONTEXT.data;
    data.rename(8, Data.ATTR, token("id"));
    data.replace(8, Data.ATTR, token("junit"));
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("junit"), data.text(8, false));
    reload();
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("junit"), data.text(8, false));
  }

  /**
   * Tests the insertion of a new attribute.
   */
  @Test
  public void testAddAttribute() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;
    data.insertAttr(9, 6, token("foo"), token("junit"), 0);
    assertEquals(size + 1, data.meta.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("foo"), data.name(9, false));
    assertByteArraysEqual(token("junit"), data.text(9, false));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("foo"), data.name(9, false));
    assertByteArraysEqual(token("junit"), data.text(9, false));
  }
}
