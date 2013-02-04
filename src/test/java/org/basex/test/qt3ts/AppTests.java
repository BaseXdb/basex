package org.basex.test.qt3ts;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import org.basex.test.qt3ts.app.*;

/**
 * Test suite for the "app" test group.
 *
 * @author BaseX Team 2005-12, BSD License
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
