package org.basex.test.qt3ts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.test.qt3ts.xs.*;

/**
 * Test suite for the "xs" test group.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  XsBase64Binary.class,
  XsDateTimeStamp.class,
  XsDouble.class,
  XsFloat.class,
  XsHexBinary.class,
  XsNormalizedString.class,
  XsToken.class
})
public class XsTests { }
