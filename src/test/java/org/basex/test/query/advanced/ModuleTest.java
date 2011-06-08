package org.basex.test.query.advanced;

import org.basex.query.util.Err;
import org.junit.Test;

/**
 * Test cases for modules.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
public final class ModuleTest extends AdvancedQueryTest {
  /** Catches duplicate module import. */
  @Test
  public void duplImport() {
    error("import module namespace a='world' at 'etc/test/hello.xqm';" +
      "import module namespace a='world' at 'etc/test/hello.xqm'; 1",
      Err.DUPLMODULE);
  }

  /** Catches duplicate module import with different module uri. */
  @Test
  public void duplImportDiffUri() {
    error("import module namespace a='world' at 'etc/test/hello.xqm';" +
      "import module namespace a='galaxy' at 'etc/test/hello.xqm'; 1",
      Err.WRONGMODULE);
  }
}
