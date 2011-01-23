package org.basex.test.data;

import org.basex.data.Data;
import org.basex.data.MemData;
import org.basex.util.Token;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This class tests the update features of the Data class.
 *
 * @author BaseX Team 2005-11, ISC License
 * @author Tim Petrowsky
 */
public final class UpdateTestAttributes extends UpdateTest {
  /**
   * Tests the update of an existing attribute.
   */
  @Test
  public void testUpdateAttribute() {
    final Data data = CONTEXT.data;
    data.rename(7, Data.ATTR, NAME, Token.EMPTY);
    data.replace(7, Data.ATTR, JUNIT);
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(NAME, data.name(7, Data.ATTR));
    assertByteArraysEqual(JUNIT, data.text(7, false));
    reload();
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(NAME, data.name(7, Data.ATTR));
    assertByteArraysEqual(JUNIT, data.text(7, false));
  }

  /**
   * Tests the update of an existing attribute.
   */
  @Test
  public void testUpdateAttribute2() {
    final Data data = CONTEXT.data;
    data.rename(8, Data.ATTR, NAME, Token.EMPTY);
    data.replace(8, Data.ATTR, JUNIT);
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(JUNIT, data.text(8, false));
    reload();
    assertEquals(size, data.meta.size);
    assertByteArraysEqual(JUNIT, data.text(8, false));
  }

  /**
   * Tests the insertion of a new attribute.
   */
  @Test
  public void testAddAttribute() {
    final Data data = CONTEXT.data;
    final long nextid = data.meta.lastid;

    final MemData md = new MemData(CONTEXT.data);
    md.attr(0, 1, data.atts.index(FOO, null, false), JUNIT, 0, false);
    md.insert(0);
    data.insertAttr(9, 6, md);
    assertEquals(size + 1, data.meta.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(FOO, data.name(9, Data.ATTR));
    assertByteArraysEqual(JUNIT, data.text(9, false));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertByteArraysEqual(FOO, data.name(9, Data.ATTR));
    assertByteArraysEqual(JUNIT, data.text(9, false));
  }
}
