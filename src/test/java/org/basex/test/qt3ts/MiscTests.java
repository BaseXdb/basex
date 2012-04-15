package org.basex.test.qt3ts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.test.qt3ts.misc.*;

/**
 * Test suite for the "misc" test group.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  MiscAnnexE.class,
  MiscAppendixA4.class,
  MiscCombinedErrorCodes.class,
  MiscErrorsAndOptimization.class,
  MiscHigherOrderFunctions.class,
  MiscSerialization.class,
  MiscStaticContext.class,
  MiscSurrogates.class,
  MiscXMLEdition.class
})
public class MiscTests { }
