package org.basex.util;

import static org.junit.Assert.*;

import org.basex.util.list.StringList;
import org.junit.Test;

/**
 * Tests for the {@link StringList} implementation.
 *
 * @author BaseX Team 2005-13, BSD License
 */
public class StringListTest {
  /** Tests {@code containsAll(l)} function. */
  @Test
  public void containsAllTest() {
    final StringList list1 = new StringList("A", "B", "C"), list2 = new StringList("A", "C");
    assertTrue("List1 does contain list2", list1.containsAll(list2));
    assertFalse("List2 does not contain list1.", list2.containsAll(list1));
  }

}
