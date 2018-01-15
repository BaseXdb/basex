package org.basex;

import org.basex.modules.*;
import org.expath.ns.*;
import org.exquery.ns.*;
import org.junit.*;

/**
 * Ensures the visibility of XQuery modules.
 *
 * @author BaseX Team 2005-18, BSD License
 * @author Christian Gruen
 */
public final class ModuleTest {
  /**
   * Ensures the the constructors of XQuery modules are visible.
   */
  @Test
  public void visibility() {
    new Restxq();
    new Request();
    new Response();
    new Session();
    new Sessions();
    new Geo();
  }
}
