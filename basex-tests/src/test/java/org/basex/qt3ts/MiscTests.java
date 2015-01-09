package org.basex.qt3ts;

import org.basex.qt3ts.misc.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for the "misc" test group.
 *
 * @author BaseX Team 2005-15, BSD License
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
