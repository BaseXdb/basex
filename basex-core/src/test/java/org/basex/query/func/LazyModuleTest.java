package org.basex.query.func;

import static org.basex.query.func.Function.*;

import org.basex.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Lazy Module.
 *
 * @author BaseX Team, BSD License
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

  /** Errors of lazy items are raised inside try clauses. */
  @Test public void tryCatch() {
    final String invalid = sandbox().path() + "invalid.txt";
    query(_FILE_WRITE_BINARY.args(invalid, " xs:hexBinary('00')"));
    final String read = "try { " + _FILE_READ_TEXT.args(invalid) + " } catch * { 'caught' }";
    query(read, "caught");
    query("boolean(" + read + ')', true);
    query("string-length(" + read + ')', 6);
    query("try { (" + _FILE_READ_TEXT.args(invalid) + ", 1) } catch * { 'caught' }", "caught");
    query(_LAZY_IS_CACHED.args(" try { " + _FILE_READ_TEXT.args(FILE) + " } catch * { }"), true);
  }

  /** Test method. */
  @Test public void isCached() {
    final Function func = _LAZY_IS_CACHED;
    query(func.args(_FILE_READ_BINARY.args(FILE)), false);
    query(func.args(_LAZY_CACHE.args(_FILE_READ_BINARY.args(FILE))), true);
    query(func.args(_FILE_READ_TEXT.args(FILE)), false);
    query(func.args(_LAZY_CACHE.args(_FILE_READ_TEXT.args(FILE))), true);
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
