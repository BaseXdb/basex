package org.basex.test.util;

import static org.junit.Assert.*;
import org.basex.query.item.Itr;
import org.basex.query.util.ItemList;
import org.junit.Test;

/**
 * Tests the ItemList for quickly storing a list of items.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Michael Seiferle
 */
public class ItemListTest {


  /** Length of the list.*/
  private static final int CAP = 1 << 4;
  /**
   * Tests the constructor.
   */
  @Test
  public final void testItemList() {
    final ItemList il = new ItemList();
    il.add(Itr.ZERO);
    assertEquals(1, il.size());
    assertEquals(Itr.ZERO, il.get(0));
  }

  /**
   * Tests adding elements.
   */
  @Test
  public final void testAdd() {
    final ItemList il = new ItemList();
    il.add(Itr.ZERO);

    for(int i = 0; i < CAP - 1; i++) {
      il.add(Itr.ZERO);
    }
  }
  /**
   * Tests the toArray implementation.
  @Test
  public final void testToArray() {
    ItemList il = new ItemList();
    il.add(Itr.ZERO);

    for(int i = 0; i < CAP - 1; i++) {
      il.add(Itr.ZERO);
    }
    assertEquals(CAP, il.toArray().length);
  }
   */
}
