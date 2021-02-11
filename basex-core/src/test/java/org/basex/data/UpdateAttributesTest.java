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
public final class UpdateAttributesTest extends DataUpdateTest {
  /**
   * Tests the update of an existing attribute.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void updateAttribute(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    data.startUpdate(context.options);
    data.update(7, Data.ATTR, T_NAME, Token.EMPTY);
    data.update(7, Data.ATTR, T_JUNIT);
    data.finishUpdate(context.options);
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_NAME, data.name(7, Data.ATTR));
    assertArraysEquals(T_JUNIT, data.text(7, false));
    reload(mainmem);
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_NAME, data.name(7, Data.ATTR));
    assertArraysEquals(T_JUNIT, data.text(7, false));
  }

  /**
   * Tests the update of an existing attribute.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void updateAttribute2(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    data.startUpdate(context.options);
    data.update(8, Data.ATTR, T_NAME, Token.EMPTY);
    data.update(8, Data.ATTR, T_JUNIT);
    data.finishUpdate(context.options);
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_JUNIT, data.text(8, false));
    reload(mainmem);
    assertEquals(size, data.meta.size);
    assertArraysEquals(T_JUNIT, data.text(8, false));
  }

  /**
   * Tests the insertion of a new attribute.
   * @param mainmem main-memory flag
   * @throws IOException I/O exception
   */
  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  public void addAttribute(final boolean mainmem) throws IOException {
    setUp(mainmem);
    final Data data = context.data();
    final long nextid = data.meta.lastid;

    final MemData md = new MemData(context.options);
    md.attr(1, md.attrNames.put(T_FOO), T_JUNIT, 0);
    md.insert(0);
    data.startUpdate(context.options);
    data.insertAttr(9, 6, new DataClip(md));
    data.finishUpdate(context.options);
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
    reload(mainmem);
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
