package org.basex.qt3ts;

import org.basex.qt3ts.app.*;
import org.junit.runner.*;
import org.junit.runners.*;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test suite for the "app" test group.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
@RunWith(Suite.class)
@SuiteClasses({
  AppDemos.class,
  AppFunctxFn.class,
  AppFunctxFunctx.class,
  AppUseCaseNS.class,
  AppUseCasePARTS.class,
  AppUseCaseR.class,
  AppUseCaseSEQ.class,
  AppUseCaseSGML.class,
  AppUseCaseSTRING.class,
  AppUseCaseTREE.class,
  AppUseCaseXMP.class,
  AppXMark.class
})
public class AppTests { }
