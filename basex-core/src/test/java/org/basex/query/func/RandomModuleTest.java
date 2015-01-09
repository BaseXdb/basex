package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.util.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Random Module.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Dirk Kirsten
 */
public final class RandomModuleTest extends AdvancedQueryTest {
  /** Test method. */
  @Test
  public void doubleTest() {
    final Double i1 = Double.valueOf(query(_RANDOM_DOUBLE.args()));
    final Double i2 = Double.valueOf(query(_RANDOM_DOUBLE.args()));
    assertTrue(!i1.equals(i2) && i1 >= 0.0 && i1 < 1.0);
  }

  /** Test method. */
  @Test
  public void seededDouble() {
    final int s = 12345;
    query(_RANDOM_SEEDED_DOUBLE.args(s, 1), new Random(s).nextDouble());
  }

  /** Test method. */
  @Test
  public void integer() {
    final Integer i = Integer.valueOf(query(_RANDOM_INTEGER.args(5)));
    assertTrue(i >= 0 && i < 5);
    error(_RANDOM_INTEGER.args(0), QueryError.BXRA_BOUNDS_X);
    error(_RANDOM_INTEGER.args(-1), QueryError.BXRA_BOUNDS_X);
    error(_RANDOM_INTEGER.args(8000000000L), QueryError.BXRA_BOUNDS_X);
  }

  /** Test method. */
  @Test
  public void seededInteger() {
    final int s = 12345;
    query(_RANDOM_SEEDED_INTEGER.args(s, 1), new Random(s).nextInt());
    query(_RANDOM_SEEDED_INTEGER.args(s, 1, 1000000), new Random(s).nextInt(1000000));
    error(_RANDOM_SEEDED_INTEGER.args(1, -1), QueryError.BXRA_NUM_X);
    error(_RANDOM_SEEDED_INTEGER.args(1, 1, -1), QueryError.BXRA_BOUNDS_X);
    error(_RANDOM_SEEDED_INTEGER.args(1, 1, 8000000000L), QueryError.BXRA_BOUNDS_X);
  }

  /** Test method. */
  @Test
  public void gaussian() {
    final int num = 50;
    query(_RANDOM_GAUSSIAN.args(num));
  }

  /** Test method. */
  @Test
  public void uuid() {
    final String s1 = query(_RANDOM_UUID.args());
    final String s2 = query(_RANDOM_UUID.args());
    assertFalse(s1.equals(s2));
  }
}
