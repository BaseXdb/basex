package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
import org.basex.core.*;
import org.basex.util.*;
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
    query(_CACHE_CLEAR.args());
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
  @Test public void info() {
    final Function func = _CACHE_INFO;
    query(func.args() + "?entries", 0);
    query(func.args() + "?hits", 0);
    query(func.args() + "?misses", 0);
    query(func.args() + "?evictions", 0);
    query(func.args() + "?expirations", 0);
    query(func.args("unknown") + "?entries", 0);

    query(_CACHE_PUT.args("key", "INFO"));
    query(func.args() + "?entries", 1);
    query(_CACHE_GET.args("key"), "INFO");
    query(func.args() + "?hits", 1);
    query(_CACHE_GET.args("unknown"), "");
    query(func.args() + "?misses", 1);

    query(_CACHE_REMOVE.args("key"));
    query(func.args() + "?entries", 0);
    query(func.args() + "?hits", 1);

    query(_CACHE_PUT.args("key", "INFO", "cache"));
    query(func.args("cache") + "?entries", 1);
    query(func.args() + "?entries", 0);

    // statistics are discarded together with the cache
    query(_CACHE_DELETE.args("cache"));
    query(func.args("cache") + "?entries", 0);

    final int cachemax = context.soptions.get(StaticOptions.CACHEMAX);
    query("(0 to " + cachemax + ") ! " + _CACHE_PUT.args(" string()", " ."));
    query(func.args() + "?entries", cachemax);
    query(func.args() + "?evictions", 1);
  }

  /** Test method. */
  @Test public void init() {
    final Function func = _CACHE_INIT;
    query(func.args(), "");
    query(func.args(" {}"), "");
    query(func.args(" { 'max-entries': 2 }"), "");

    query(_CACHE_PUT.args("k1", "INIT"));
    query(_CACHE_PUT.args("k2", "INIT"));
    query(_CACHE_PUT.args("k3", "INIT"));
    query(_CACHE_SIZE.args(), 2);
    query(_CACHE_GET.args("k1"), "");
    query(_CACHE_INFO.args() + "?evictions", 1);

    // repeated initialization with the same options does not touch entries
    query(func.args(" { 'max-entries': 2 }"));
    query(_CACHE_SIZE.args(), 2);
    query(_CACHE_INFO.args() + "?evictions", 1);

    // a tightened bound is applied immediately
    query(func.args(" { 'max-entries': 1 }"));
    query(_CACHE_SIZE.args(), 1);
    query(_CACHE_INFO.args() + "?evictions", 2);

    // the configuration is preserved by cache:delete
    query(_CACHE_DELETE.args());
    query(_CACHE_PUT.args("k1", "INIT"));
    query(_CACHE_PUT.args("k2", "INIT"));
    query(_CACHE_SIZE.args(), 1);

    // custom caches are configured independently
    query(func.args(" { 'max-entries': 3 }", "cache"));
    query("(1 to 3) ! " + _CACHE_PUT.args(" string()", " .", "cache"));
    query(_CACHE_SIZE.args("cache"), 3);
    query(_CACHE_SIZE.args(), 1);

    // the configuration is discarded by cache:clear
    query(_CACHE_CLEAR.args());
    query("(1 to 3) ! " + _CACHE_PUT.args(" string()", " ."));
    query(_CACHE_SIZE.args(), 3);

    error(func.args(" { 'max-entries': 0 }"), BASEX_OPTIONS_X);
    error(func.args(" { 'ttl': -1 }"), BASEX_OPTIONS_X);
    error(func.args(" { 'unknown': 1 }"), INVALIDOPTION_X);
  }

  /** Test method. */
  @Test public void keys() {
    final Function func = _CACHE_KEYS;
    query(func.args(), "");
    query(func.args("unknown"), "");

    query(_CACHE_PUT.args("key", "KEYS"));
    query(func.args(), "key");
    query(_CACHE_PUT.args("key2", "KEYS"));
    query(func.args() + " => sort()", "key\nkey2");
    query(func.args("cache"), "");

    query(_CACHE_PUT.args("key3", "KEYS", "cache"));
    query(func.args("cache"), "key3");
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
    query(_CACHE_SIZE.args(), 0);
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
    query(_CACHE_GET.args(Integer.toString(cachemax)), cachemax);

    error(func.args("error", " true#0"), BASEX_FUNCTION_X);
    error(func.args("error", " [ function() { 123 } ]"), BASEX_FUNCTION_X);
    error(func.args("error", " { 0: concat(1, ?) }"), BASEX_FUNCTION_X);
    error(func.args("error", " Q{java.util.Random}new()"), BASEX_FUNCTION_X);
  }

  /** Test method. */
  @Test public void remove() {
    final Function func = _CACHE_REMOVE;
    query(func.args("key"), "");
    query(func.args("key", "unknown"), "");

    query(_CACHE_PUT.args("key", "REMOVE"));
    query(_CACHE_PUT.args("key2", "REMOVE"));
    query(func.args("key"), "");
    query(_CACHE_GET.args("key"), "");
    query(_CACHE_SIZE.args(), 1);

    query(_CACHE_PUT.args("key", "REMOVE", "cache"));
    query(func.args("key"), "");
    query(_CACHE_GET.args("key", "cache"), "REMOVE");
    query(func.args("key", "cache"), "");
    query(_CACHE_SIZE.args("cache"), 0);
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

  /** Test method. */
  @Test public void ttl() {
    query(_CACHE_INIT.args(" { 'ttl': 1 }"));
    query(_CACHE_PUT.args("key", "TTL"));
    query(_CACHE_GET.args("key"), "TTL");

    // unlimited lifetime; both caches share a single waiting period
    query(_CACHE_INIT.args(" { 'ttl': 0 }", "cache"));
    query(_CACHE_PUT.args("key", "TTL", "cache"));
    Performance.sleep(1100);

    query(_CACHE_GET.args("key"), "");
    query(_CACHE_SIZE.args(), 0);
    query(_CACHE_INFO.args() + "?expirations", 1);
    query(_CACHE_GET.args("key", "cache"), "TTL");
    query(_CACHE_INFO.args("cache") + "?expirations", 0);
  }
}
