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
public final class DataUpdateTestAttributes extends DataUpdateTest {
  /**
   * Test the update of an existing attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testUpdateAttribute() throws Exception {
    data.update(7, token("name"), token("junit"));
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("name"), data.attName(7));
    assertByteArraysEqual(token("junit"), data.attValue(7));
    reload();
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("name"), data.attName(7));
    assertByteArraysEqual(token("junit"), data.attValue(7));
  }

  /**
   * Test the update of an existing attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testUpdateAttribute2() throws Exception {
    data.update(8, token("id"), token("junit"));
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("junit"), data.attValue(8));
    reload();
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(token("junit"), data.attValue(8));
  }

  /**
   * Test the insertion of a new attribute.
   * @throws Exception in case of problems.
   */
  @Test
  public void testAddAttribute() throws Exception {
    final long nextid = data.meta.lastid;
    data.insert(9, 6, token("foo"), token("junit"));
    assertEquals(size + 1, data.meta.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("foo"), data.attName(9));
    assertByteArraysEqual(token("junit"), data.attValue(9));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals((int) Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(token("foo"), data.attName(9));
    assertByteArraysEqual(token("junit"), data.attValue(9));
  }

}
