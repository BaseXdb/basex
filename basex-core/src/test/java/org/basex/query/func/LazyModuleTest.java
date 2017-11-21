package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.junit.*;

/**
 * This class tests the functions of the Lazy Module.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class LazyModuleTest extends AdvancedQueryTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/corrupt.xml";

  /** Test method. */
  @Test
  public void cache() {
    final Function func = _LAZY_CACHE;
    query(_FILE_READ_TEXT.args(FILE), "<");
    query(func.args(_FILE_READ_BINARY.args(FILE)), "<");
    query(func.args(_FILE_READ_TEXT.args(FILE)), "<");
  }

  /** Test method. */
  @Test
  public void isLazy() {
    final Function func = _LAZY_IS_LAZY;
    query(func.args(_FILE_READ_BINARY.args(FILE)), true);
    query(func.args("A"), false);
    query(func.args(_LAZY_CACHE.args(_FILE_READ_TEXT.args(FILE))), true);
  }

  /** Test method. */
  @Test
  public void isCached() {
    final Function func = _LAZY_IS_CACHED;
    query("let $bin := " + _FILE_READ_BINARY.args(FILE) + " return (" +
        func.args(" $bin") + ',' + _PROF_VOID.args(" $bin") + ',' + func.args(" $bin") + ')',
        "false\ntrue");
  }
}
