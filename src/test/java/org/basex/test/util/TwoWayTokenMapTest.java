package org.basex.test.util;

import static org.junit.Assert.*;
import static org.basex.util.Token.*;

import org.basex.util.BitArray;
import org.basex.util.list.TokenList;
import org.basex.util.list.TwoWayTokenMap;
import org.junit.Before;
import org.junit.Test;


/**
 * Tests for {@link TwoWayTokenMapTest}.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Jens Erat
 */
public final class TwoWayTokenMapTest {
  /** Instance of {@link TwoWayTokenMap}. */
  private TwoWayTokenMap map;

  /** Create an instance of {@link BitArray}. */
  @Before
  public void setUp() {
    map = new TwoWayTokenMap();
  }

  /**
   * Test {@link TwoWayTokenMap}.
   *
   * Relies on correct behavior of {@link TokenList}.
   */
  @Test
  public void test() {
    // Inserts
    map.add("Test0");
    map.add("Test");
    map.add(token("Test2"));
    map.add(3);
    try {
      map.add("Test");
      // shouldn't be executed:
      fail("May not insert value twice, Exception should have been thrown!");
    } catch (IllegalArgumentException e) { }

    // Check Keys
    assertEquals(0, map.getKey("Test0"));
    assertEquals(1, map.getKey("Test"));
    assertEquals(2, map.getKey(token("Test2")));
    assertEquals(3, map.getKey(token(3)));
    assertEquals(-1, map.getKey("not-there"));

    // Check values
    assertArrayEquals(token("Test0"), map.get(0));
    assertArrayEquals(token("Test"), map.get(1));
    assertEquals("Test2", string(map.get(2)));
    assertEquals(3, toLong(map.get(3)));
    try {
      if (null != map.get(5)) {
        // shouldn't be executed:
        fail("No Key for value, Exception should have been thrown!");
      }
    } catch (IndexOutOfBoundsException e) { }

    // Update values
    map.set(0, token("Test-updated"));
    assertFalse(map.contains(token("Test0")));
    assertArrayEquals(token("Test-updated"), map.get(0));
    assertEquals(0, map.getKey(token("Test-updated")));
    assertEquals(-1, map.getKey(token("Test0")));

    // Contains
    assertTrue(map.contains(token("Test")));
    assertFalse(map.contains(token("notthere")));

    // Stack operations
    map.push(token("Stack1"));
    map.push(token("Stack2"));
    assertArrayEquals(map.peek(), token("Stack2"));
    assertArrayEquals(map.pop(), token("Stack2"));
    assertArrayEquals(map.peek(), token("Stack1"));
    try {
      map.push(token("Stack1"));
      // shouldn't be executed:
      fail("May not insert value twice, Exception should have been thrown!");
    } catch (IllegalArgumentException e) { }
  }

}
