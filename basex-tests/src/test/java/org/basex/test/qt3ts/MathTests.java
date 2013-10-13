package org.basex.test.qt3ts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.test.qt3ts.math.*;

/**
 * Test suite for the "math" test group.
 *
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  MathAcos.class,
  MathAsin.class,
  MathAtan.class,
  MathAtan2.class,
  MathCos.class,
  MathExp.class,
  MathExp10.class,
  MathLog.class,
  MathLog10.class,
  MathPi.class,
  MathPow.class,
  MathSin.class,
  MathSqrt.class,
  MathTan.class
})
public class MathTests { }
