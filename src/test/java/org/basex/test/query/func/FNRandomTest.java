package org.basex.test.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.Assert.*;

import java.util.*;

import org.basex.test.query.*;
import org.junit.*;

/**
 * This class tests some XQuery random functions prefixed with "random".
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Dirk Kirsten
 */
public final class FNRandomTest extends AdvancedQueryTest {
  /**
   * Checks FNRAndom functions for correct argument handling.
   */
  @Test
  public void checkFunctionArguments() {
    check(_RANDOM_DOUBLE);
    check(_RANDOM_INTEGER);
    check(_RANDOM_SEEDED_DOUBLE);
    check(_RANDOM_SEEDED_INTEGER);
    check(_RANDOM_UUID);
  }

  /**
   * Test method for the random:random-double() function.
   */
  @Test
  public void randomDouble() {
    final Double i1 = Double.valueOf(query(_RANDOM_DOUBLE.args()));
    final Double i2 = Double.valueOf(query(_RANDOM_DOUBLE.args()));
    assertTrue(!i1.equals(i2) && i1 >= 0.0 && i1 < 1.0);
  }

  /**
   * Test method for the random:seeded-random-double() function.
   */
  @Test
  public void randomSeededDouble() {
    int num = 5;
    Random r = new Random();
    Integer seed = r.nextInt();
    query(_RANDOM_SEEDED_DOUBLE.args(seed, num));
  }

  /**
   * Test method for the random:random-int() function.
   */
  @Test
  public void randomInt() {
    final Integer i1 = Integer.valueOf(query(_RANDOM_INTEGER.args()));
    final Integer i2 = Integer.valueOf(query(_RANDOM_INTEGER.args(5)));
    assertTrue(!i1.equals(i2) && i2 >= 0 && i2 < 5);
  }

  /**
   * Test method for the random:seeded-random-int() function.
   */
  @Test
  public void randomSeededInt() {
    int num = 5;
    Random r = new Random();
    Integer seed = r.nextInt();
    query(_RANDOM_SEEDED_INTEGER.args(seed, num));
    query(_RANDOM_SEEDED_INTEGER.args(seed, num, 1000000));
  }

  /**
   * Test method for the random:random-gaussian() function.
   */
  @Test
  public void randomGaussian() {
    int num = 50;
    query(_RANDOM_GAUSSIAN.args(num));
  }

  /**
   * Test method for the random:uuid() function.
   */
  @Test
  public void randomUuid() {
    final String s1 = query(_RANDOM_UUID.args());
    final String s2 = query(_RANDOM_UUID.args());
    assertTrue(!s1.equals(s2));
  }
}
