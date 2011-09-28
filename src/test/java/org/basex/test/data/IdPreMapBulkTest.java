package org.basex.test.data;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

/**
 * ID -> PRE mapping test.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Dimitar Popov
 */
public final class IdPreMapBulkTest extends IdPreMapBulkTestBase {
  /** Random number generator. */
  private static final Random RANDOM = new Random();
  /** Maximal number of bulk inserted/deleted records. */
  private final int bulkcount = 30;
  /** Number of times to repeat each test. */
  private final int iterations = 30;

  /** Set-up method. */
  @Before
  @Override
  public void setUp() {
    baseid = 200;
    opcount = 100;
    super.setUp();
  }

  /**
   * Bulk insert correctness: insert random number of values at random
   * positions.
   */
  @Test
  public void bulkInsertCorrectness() {
    for(int k = 0; k < iterations; ++k) {
      setUp();
      for(int c = 1, id = baseid + 1, i = 0; i < opcount; i += c) {
        c = RANDOM.nextInt(bulkcount) + 1;
        insert(RANDOM.nextInt(id), id, c);
        check();
        id += c;
      }
    }
  }

  /** Delete correctness: delete values at random positions. */
  @Test
  public void bulkDeleteCorrectness() {
    for(int k = 0; k < iterations; ++k) {
      setUp();
      for(int id = baseid; id > 0;) {
        final int deleteid = RANDOM.nextInt(id);
        // we don't want to delete more records than already exist:
        final int c = -Math.min(id - deleteid, RANDOM.nextInt(bulkcount) + 1);
        delete(deleteid, c);
        check();
        id += c;
      }
    }
  }

  /** Delete correctness: delete values at random positions. */
  @Test
  public void bulkDeleteCorrectness2() {
    for(int k = 0; k < iterations; ++k) {
      setUp();
      int n = baseid + 1;
      for(int i = 0; i < opcount; ++i) {
        final int c = RANDOM.nextInt(bulkcount) + 1;
        insert(RANDOM.nextInt(n), n, c);
        check();
        n += c;
      }
      check();

      for(; n > 0;) {
        final int pre = RANDOM.nextInt(n);
        // we don't want to delete more records than already exist:
        final int c = -Math.min(n - pre, RANDOM.nextInt(bulkcount) + 1);
        try {
          delete(pre, c);
          check();
        } catch(final ArrayIndexOutOfBoundsException ex) {
          dump();
          throw ex;
        }
        n += c;
      }
    }
  }

  /** Correctness: randomly insert/delete value at random positions. */
  @Test
  public void bulkInsertDeleteCorrectness() {
    for(int k = 0; k < iterations; ++k) {
      setUp();
      for(int i = 0, n = baseid, id = baseid + 1; i < opcount; ++i) {
        final int pre = RANDOM.nextInt(n + 1);
        final int c;
        // can't delete if all records have been deleted:
        if(RANDOM.nextBoolean() || n == 0) {
          c = RANDOM.nextInt(bulkcount) + 1;
          insert(pre, id, c);
          id += c;
        } else {
          c = -Math.min(n - pre, RANDOM.nextInt(bulkcount) + 1);
          delete(pre, c);
        }
        check();
        n += c;
      }
    }
  }
}
