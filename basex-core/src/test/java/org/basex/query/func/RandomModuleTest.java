package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.basex.*;
import org.basex.query.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Random Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Dirk Kirsten
 */
public final class RandomModuleTest extends SandboxTest {
  /** Test method. */
  @Test public void doubleTest() {
    final Function func = _RANDOM_DOUBLE;
    // queries
    final Double i1 = Double.valueOf(query(func.args()));
    final Double i2 = Double.valueOf(query(func.args()));
    assertTrue(!i1.equals(i2) && i1 >= 0.0 && i1 < 1.0);
  }

  /** Test method. */
  @Test public void gaussian() {
    final Function func = _RANDOM_GAUSSIAN;
    // queries
    final int num = 50;
    query(func.args(num));
  }

  /** Test method. */
  @Test public void integer() {
    final Function func = _RANDOM_INTEGER;
    // queries
    final int i = Integer.parseInt(query(func.args(5)));
    assertTrue(i >= 0 && i < 5);
    error(func.args(0), QueryError.RANDOM_BOUNDS_X);
    error(func.args(-1), QueryError.RANDOM_BOUNDS_X);
    error(func.args(8000000000L), QueryError.RANDOM_BOUNDS_X);
  }

  /** Test method. */
  @Test public void seededDouble() {
    final Function func = _RANDOM_SEEDED_DOUBLE;
    // queries
    final int s = 12345;
    query(func.args(s, 1), new Random(s).nextDouble());
  }

  /** Test method. */
  @Test public void seededInteger() {
    final Function func = _RANDOM_SEEDED_INTEGER;
    // queries
    final int s = 12345;
    query(func.args(s, 1), new Random(s).nextInt());
    query(func.args(s, 1, 1000000), new Random(s).nextInt(1000000));
    error(func.args(1, -1), QueryError.RANGE_NEGATIVE_X);
    error(func.args(1, 1, -1), QueryError.RANDOM_BOUNDS_X);
    error(func.args(1, 1, 8000000000L), QueryError.RANDOM_BOUNDS_X);
  }

  /** Test method. */
  @Test public void uuid() {
    final Function func = _RANDOM_UUID;
    // queries
    final String s1 = query(func.args());
    final String s2 = query(func.args());
    assertNotEquals(s1, s2);
  }
}
