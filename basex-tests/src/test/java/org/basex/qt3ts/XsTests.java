package org.basex.qt3ts;

import org.basex.qt3ts.xs.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for the "xs" test group.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  XsAnyURI.class,
  XsBase64Binary.class,
  XsDouble.class,
  XsFloat.class,
  XsHexBinary.class,
  XsNormalizedString.class,
  XsToken.class
})
public class XsTests { }
