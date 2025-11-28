package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.junit.jupiter.api.*;

/**
 * This class tests the functions of the Cache Module.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class CacheModuleTest extends SandboxTest {
  /** Initializes a test. */
  @BeforeEach public void initTest() {
    final Function clear = _CACHE_CLEAR;
    query(clear.args());
  }

  /** Test method. */
  @Test public void clear() {
    final Function func = _CACHE_CLEAR;
    query(_CACHE_PUT.args("key", "CLEAR"));
    query(_CACHE_PUT.args("key", "CLEAR", "cache"));
    query(_CACHE_PUT.args("key", "CLEAR", "cache2"));
    query(_CACHE_LIST.args(), "cache\ncache2");
    query(func.args());
    query(_CACHE_LIST.args(), "");
  }

  /** Test method. */
  @Test public void delete() {
    final Function func = _CACHE_DELETE;
    query(func.args(), "");
    query(_CACHE_LIST.args(), "");

    query(_CACHE_PUT.args("key", "DELETE"));
    query(_CACHE_SIZE.args(), 1);
    query(func.args(), "");
    query(_CACHE_SIZE.args(), 0);

    query(_CACHE_PUT.args("key", "DELETE"));
    query(func.args(""), "");
    query(_CACHE_SIZE.args(), 0);

    query(_CACHE_PUT.args("key", "DELETE", "cache"));
    query(_CACHE_LIST.args(), "cache");
    query(func.args("cache"), "");
    query(_CACHE_LIST.args(), "");
    query(_CACHE_SIZE.args("cache"), 0);
  }

  /** Test method. */
  @Test public void get() {
    final Function func = _CACHE_GET;
    query(func.args("key"), "");

    // test expiration of cache entries
    query(_CACHE_PUT.args("key", "GET"));
    query(func.args("key"), "GET");
    query(_CACHE_PUT.args("key", "specific", "cache"));
    query(func.args("key"), "GET");
    query(func.args("key", ""), "GET");
    query(func.args("key", "cache"), "specific");
  }

  /** Test method. */
  @Test public void getOrPut() {
    final Function func = _CACHE_GET_OR_PUT;
    query(_CACHE_GET.args("key"), "");
    query(func.args("key", " function() { 'GET-OR-PUT' }"), "GET-OR-PUT");
    query(_CACHE_GET.args("key"), "GET-OR-PUT");
    query(_CACHE_SIZE.args(), 1);
    query(func.args("key", " function() { 'NOT' + 'INVOKED' }"), "GET-OR-PUT");
    query(_CACHE_GET.args("key"), "GET-OR-PUT");
    query(_CACHE_SIZE.args(), 1);
  }

  /** Test method. */
  @Test public void list() {
    final Function func = _CACHE_LIST;
    query(func.args() + " => count()", 0);
    query(_CACHE_PUT.args("key", "NAMES"));
    query(func.args() + " => count()", 0);
    query(_CACHE_PUT.args("key", "NAMES", "cache"));
    query(func.args() + " => count()", 1);
    query(_CACHE_DELETE.args("cache"));
    query(func.args() + " => count()", 0);
  }

  /** Test method. */
  @Test public void put() {
    final Function func = _CACHE_PUT;
    query(func.args("key", "PUT"), "");
    query(_CACHE_GET.args("key"), "PUT");
    query(_CACHE_SIZE.args(), 1);
    query(func.args("key", " ()"), "");
    query(_CACHE_GET.args("key"), "");
    query(_CACHE_SIZE.args(), 1);
    query(func.args("key", " map:merge((1 to 100000) ! map:entry(., .))"), "");
    query(_CACHE_SIZE.args(), 1);
    query(_CACHE_GET.args("key") + " => map:size()", 100000);

    query(func.args("key", "PUT"));
    query(func.args("key", "PUT1", "cache1"));
    query(func.args("key", "PUT2", "cache2"));
    query(_CACHE_GET.args("key"), "PUT");
    query(_CACHE_GET.args("key", ""), "PUT");
    query(_CACHE_GET.args("key", "cache1"), "PUT1");
    query(_CACHE_GET.args("key", "cache2"), "PUT2");

    final int cachemax = context.soptions.get(StaticOptions.CACHEMAX);
    query("(0 to " + cachemax + ") ! " + func.args(" string()", " ."));
    query(_CACHE_GET.args("0"), "");
    query(_CACHE_GET.args("1"), 1);
    query(_CACHE_GET.args(String.valueOf(cachemax)), cachemax);

    error(func.args("error", " true#0"), BASEX_FUNCTION_X);
    error(func.args("error", " [ function() { 123 } ]"), BASEX_FUNCTION_X);
    error(func.args("error", " { 0: concat(1, ?) }"), BASEX_FUNCTION_X);
    error(func.args("error", " Q{java.util.Random}new()"), BASEX_FUNCTION_X);
  }

  /** Test method. */
  @Test public void size() {
    final Function func = _CACHE_SIZE;
    query(_CACHE_PUT.args("key", "SIZE"));
    query(_CACHE_PUT.args("key1", "SIZE1", "cache"));
    query(_CACHE_PUT.args("key2", "SIZE2", "cache"));

    query(func.args(), 1);
    query(func.args(""), 1);
    query(func.args("cache"), 2);
    query(func.args("unknown"));
  }
}
