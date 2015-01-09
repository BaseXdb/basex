package org.basex.qt3ts;

import org.basex.qt3ts.math.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for the "math" test group.
 *
 * @author BaseX Team 2005-15, BSD License
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
