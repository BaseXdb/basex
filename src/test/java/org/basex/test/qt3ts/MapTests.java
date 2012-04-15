package org.basex.test.qt3ts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.test.qt3ts.map.*;

/**
 * Test suite for the "map" test group.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  MapCollation.class,
  MapContains.class,
  MapEntry.class,
  MapGet.class,
  MapKeys.class,
  MapNew.class,
  MapRemove.class,
  MapSize.class
})
public class MapTests { }
