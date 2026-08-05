package org.basex.util.list;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;

/**
 * Tests for {@link BoolList}.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class BoolListTest {
  /** Test method for {@link BoolList#set(int, boolean)}: a gap must not expose released values. */
  @Test public void setAfterTruncation() {
    final BoolList list = new BoolList().add(true).add(true).add(true);
    list.size(1);
    list.set(2, true);
    assertArrayEquals(new boolean[] { true, false, true }, list.finish());
  }

  /** Test method for {@link BoolList#set(int, boolean)}: rejects a negative index. */
  @Test public void setNegative() {
    final BoolList list = new BoolList();
    final Throwable th = assertThrows(ArrayIndexOutOfBoundsException.class,
        () -> list.set(-1, true));
    assertEquals("Negative index: -1.", th.getMessage());
  }
}
