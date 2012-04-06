package org.basex.test.data;

import static org.junit.Assert.*;

import org.basex.data.*;
import org.basex.util.*;
import org.junit.*;

/**
 * This class tests the update features of the {@link Data} class.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Tim Petrowsky
 */
public final class UpdateTestAttributes extends UpdateTest {
  /**
   * Tests the update of an existing attribute.
   */
  @Test
  public void updateAttribute() {
    final Data data = context.data();
    data.update(7, Data.ATTR, T_NAME, Token.EMPTY);
    data.update(7, Data.ATTR, T_JUNIT);
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_NAME, data.name(7, Data.ATTR));
    assertArraysEquals(T_JUNIT, data.text(7, false));
    reload();
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_NAME, data.name(7, Data.ATTR));
    assertArraysEquals(T_JUNIT, data.text(7, false));
  }

  /**
   * Tests the update of an existing attribute.
   */
  @Test
  public void updateAttribute2() {
    final Data data = context.data();
    data.update(8, Data.ATTR, T_NAME, Token.EMPTY);
    data.update(8, Data.ATTR, T_JUNIT);
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_JUNIT, data.text(8, false));
    reload();
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_JUNIT, data.text(8, false));
  }

  /**
   * Tests the insertion of a new attribute.
   */
  @Test
  public void addAttribute() {
    final Data data = context.data();
    final long nextid = data.meta.lastid;

    final MemData md = new MemData(context.data());
    md.attr(0, 1, data.atnindex.index(T_FOO, null, false), T_JUNIT, 0, false);
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
    assertArraysEquals(T_FOO, data.name(9, Data.ATTR));
    assertArraysEquals(T_JUNIT, data.text(9, false));
    reload();
    assertEquals(size + 1, data.meta.size);
    assertEquals(size + 1, data.size(0, Data.DOC));
    assertEquals(Data.ATTR, data.kind(9));
    assertEquals(6, data.parent(9, Data.ATTR));
    assertEquals(6, data.parent(8, Data.ATTR));
    assertEquals(6, data.parent(10, Data.ELEM));
    assertEquals(10, data.parent(11, Data.TEXT));
    assertEquals(nextid + 1, data.meta.lastid);
    assertArraysEquals(T_FOO, data.name(9, Data.ATTR));
    assertArraysEquals(T_JUNIT, data.text(9, false));
  }
}
