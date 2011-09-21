package org.basex.test.data;

import org.junit.Before;
import org.junit.Test;

/**
 * Fixed ID -> PRE mapping tests.
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class IdPreMapBulkFixedTest extends IdPreMapBulkTestBase {
  /** Set-up method. */
  @Before
  @Override
  public void setUp() {
    baseid = 4;
    opcount = 10;
    super.setUp();
  }

  /** Insert test. */
  @Test
  public void testInsert1() {
    insert(5, 6, 2); insert(4, 8, 2); check();
  }

  /** Insert test. */
  @Test
  public void testInsert2() {
    insert(1, 6, 2); insert(2, 8, 2); insert(5, 10, 2); check();
  }

  /** Insert test. */
  @Test
  public void testInsert3() {
    insert(5, 6, 3); insert(4, 9, 2); insert(9, 11, 3); check();
  }

  /** Delete test. */ @Test
  public void testDelete1() { insertRecords(); delete(3, -10); check(); }
  /** Delete test. */ @Test
  public void testDelete2() { insertRecords(); delete(4, -8); check(); }
  /** Delete test. */ @Test
  public void testDelete3() { insertRecords(); delete(4, -7); check(); }
  /** Delete test. */ @Test
  public void testDelete4() { insertRecords(); delete(3, -9); check(); }
  /** Delete test. */ @Test
  public void testDelete5() { insertRecords(); delete(4, -2); check(); }
  /** Delete test. */ @Test
  public void testDelete6() { insertRecords(); delete(4, -3); check(); }
  /** Delete test. */ @Test
  public void testDelete7() { insertRecords(); delete(2, -4); check(); }
  /** Delete test. */ @Test
  public void testDelete8() { insertRecords(); delete(2, -5); check(); }
  /** Delete test. */ @Test
  public void testDelete9() { insertRecords(); delete(2, -2); check(); }
  /** Delete test. */ @Test
  public void testDelete10() { insertRecords(); delete(2, -3); check(); }
  /** Delete test. */ @Test
  public void testDelete11() { insertRecords(); delete(3, -2); check(); }
  /** Delete test. */ @Test
  public void testDelete12() { insertRecords(); delete(6, -2); check(); }
  /** Delete test. */ @Test
  public void testDelete13() { insertRecords(); delete(8, -2); check(); }
  /** Delete test. */ @Test
  public void testDelete14() { insertRecords(); delete(1, -7); check(); }
  /** Delete test. */ @Test
  public void testDelete15() { insertRecords(); delete(3, -1); check(); }
  /** Delete test. */ @Test
  public void testDelete16() { insertRecords(); delete(2, -1); check(); }
  /** Delete test. */ @Test
  public void testDelete17() { insertRecords(); delete(1, -2); check(); }
  /** Delete test. */ @Test
  public void testDelete18() { insertRecords(); delete(1, -11); check(); }
  /** Delete test. */ @Test
  public void testDelete19() { insertRecords(); delete(6, -8); check(); }

  /** Delete test. */ @Test
  public void testDelete20() { delete(1, -2); delete(2, -1); check(); }
  /** Delete test. */ @Test
  public void testDelete21() { delete(2, -2); delete(2, -1); check(); }
  /** Delete test. */ @Test
  public void testDelete22() { delete(0, -2); delete(0, -2); check(); }

  /** Insert some records. */
  private void insertRecords() {
    insert(4, 5, 4); insert(5, 9, 1); insert(2, 10, 2); insert(2, 12, 3);
  }
}
