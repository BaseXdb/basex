package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Lazy Module.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class LazyModuleTest extends SandboxTest {
  /** Test file. */
  private static final String FILE = "src/test/resources/corrupt.xml";

  /** Test method. */
  @Test public void cache() {
    final Function func = _LAZY_CACHE;
    query(_FILE_READ_TEXT.args(FILE), "<");
    query(func.args(_FILE_READ_BINARY.args(FILE)), "<");
    query(func.args(_FILE_READ_TEXT.args(FILE)), "<");
    query(func.args(_FILE_READ_BINARY.args(FILE), " true()"), "<");
    query(func.args(_FILE_READ_TEXT.args(FILE), " true()"), "<");
  }

  /** Test method. */
  @Test public void isCached() {
    final Function func = _LAZY_IS_CACHED;
    query(func.args(_FILE_READ_BINARY.args(FILE)), "false");
    query(func.args(_LAZY_CACHE.args(_FILE_READ_BINARY.args(FILE))), "true");
    query(func.args(_FILE_READ_TEXT.args(FILE)), "false");
    query(func.args(_LAZY_CACHE.args(_FILE_READ_TEXT.args(FILE))), "true");

    query("let $bin := " + _FILE_READ_BINARY.args(FILE) + " return (" +
        func.args(" $bin") + ',' + _PROF_VOID.args(" $bin") + ',' + func.args(" $bin") + ')',
        "false\ntrue");
  }

  /** Test method. */
  @Test public void isLazy() {
    final Function func = _LAZY_IS_LAZY;
    query(func.args(_FILE_READ_BINARY.args(FILE)), true);
    query(func.args(_FILE_READ_TEXT.args(FILE)), true);
    query(func.args("A"), false);
    query(func.args(_LAZY_CACHE.args(_FILE_READ_TEXT.args(FILE))), true);
    query(func.args(_LAZY_CACHE.args(_FILE_READ_BINARY.args(FILE))), true);
  }
}
