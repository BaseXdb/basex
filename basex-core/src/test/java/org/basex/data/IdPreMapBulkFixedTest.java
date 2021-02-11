package org.basex.data;

import org.junit.jupiter.api.*;

/**
 * Fixed ID -> PRE mapping tests.
 * @author BaseX Team 2005-21, BSD License
 * @author Dimitar Popov
 */
public final class IdPreMapBulkFixedTest extends IdPreMapBulkTestBase {
  /** Set-up method. */
  @BeforeEach @Override
  public void setUp() {
    baseid = 4;
    opcount = 10;
    super.setUp();
  }

  /** Insert test. */
  @Test public void insert1() {
    insert(5, 6, 2); insert(4, 8, 2); check();
  }

  /** Insert test. */
  @Test public void insert2() {
    insert(1, 6, 2); insert(2, 8, 2); insert(5, 10, 2); check();
  }

  /** Insert test. */
  @Test public void insert3() {
    insert(5, 6, 3); insert(4, 9, 2); insert(9, 11, 3); check();
  }

  /** Delete test. */ @Test public void delete1() { insertRecords(); delete(3, -10); check(); }
  /** Delete test. */ @Test public void delete2() { insertRecords(); delete(4, -8); check(); }
  /** Delete test. */ @Test public void delete3() { insertRecords(); delete(4, -7); check(); }
  /** Delete test. */ @Test public void delete4() { insertRecords(); delete(3, -9); check(); }
  /** Delete test. */ @Test public void delete5() { insertRecords(); delete(4, -2); check(); }
  /** Delete test. */ @Test public void delete6() { insertRecords(); delete(4, -3); check(); }
  /** Delete test. */ @Test public void delete7() { insertRecords(); delete(2, -4); check(); }
  /** Delete test. */ @Test public void delete8() { insertRecords(); delete(2, -5); check(); }
  /** Delete test. */ @Test public void delete9() { insertRecords(); delete(2, -2); check(); }
  /** Delete test. */ @Test public void delete10() { insertRecords(); delete(2, -3); check(); }
  /** Delete test. */ @Test public void delete11() { insertRecords(); delete(3, -2); check(); }
  /** Delete test. */ @Test public void delete12() { insertRecords(); delete(6, -2); check(); }
  /** Delete test. */ @Test public void delete13() { insertRecords(); delete(8, -2); check(); }
  /** Delete test. */ @Test public void delete14() { insertRecords(); delete(1, -7); check(); }
  /** Delete test. */ @Test public void delete15() { insertRecords(); delete(3, -1); check(); }
  /** Delete test. */ @Test public void delete16() { insertRecords(); delete(2, -1); check(); }
  /** Delete test. */ @Test public void delete17() { insertRecords(); delete(1, -2); check(); }
  /** Delete test. */ @Test public void delete18() { insertRecords(); delete(1, -11); check(); }
  /** Delete test. */ @Test public void delete19() { insertRecords(); delete(6, -8); check(); }
  /** Delete test. */ @Test public void delete20() { insertRecords(); delete(12, -3); check(); }

  /** Delete test. */ @Test public void delete21() { delete(1, -2); delete(2, -1); check(); }
  /** Delete test. */ @Test public void delete22() { delete(2, -2); delete(2, -1); check(); }
  /** Delete test. */ @Test public void delete23() { delete(0, -2); delete(0, -2); check(); }
  /** Delete test. */ @Test public void delete24() { delete(1, -1); delete(2, -1); check(); }

  /** Insert some records. */
  private void insertRecords() {
    insert(4, 5, 4); insert(5, 9, 1); insert(2, 10, 2); insert(2, 12, 3);
  }
}
