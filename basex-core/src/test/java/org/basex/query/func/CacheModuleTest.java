package org.basex.query.func;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.*;
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
    final Function clear = _CACHE_CLEAR;
    query(clear.args());
  }

  /** Test method. */
  @Test public void clear() {
    final Function func = _CACHE_CLEAR;
    query(_CACHE_PUT.args("key", "CLEAR"));
    query(_CACHE_KEYS.args(), "key");
    query(func.args(), "");
    query(_CACHE_KEYS.args(), "");
  }

  /** Test method. */
  @Test public void get() {
    final Function func = _CACHE_GET;
    query(func.args("key"), "");

    // test expiration of cache entries
    query(_CACHE_PUT.args("key", "expiring", "PT1S"));
    query(func.args("key"), "expiring");

    query("(1 to 10_000) ! " + _CACHE_PUT.args(" string()", " .", "PT1S"));
    Performance.sleep(2000);
    query(_CACHE_KEYS.args(), "");
  }

  /** Test method. */
  @Test public void getOrPut() {
    final Function func = _CACHE_GET_OR_PUT;
    query(_CACHE_GET.args("key"), "");
    query(func.args("key", " function() { 'GET-OR-PUT' }"), "GET-OR-PUT");
    query(_CACHE_GET.args("key"), "GET-OR-PUT");
    query(_CACHE_KEYS.args(), "key");
    query(func.args("key", " function() { 'NOT' + 'INVOKED' }"), "GET-OR-PUT");
    query(_CACHE_GET.args("key"), "GET-OR-PUT");
    query(_CACHE_KEYS.args(), "key");
  }

  /** Test method. */
  @Test public void keys() {
    final Function func = _CACHE_KEYS;
    for(int i = 0; i < 3; i++) query(_CACHE_PUT.args(Integer.toString(i), i));
    query(func.args() + " => sort()", "0\n1\n2");
    query(_CACHE_CLEAR.args());
    query(func.args(), "");
  }

  /** Test method. */
  @Test public void put() {
    final Function func = _CACHE_PUT;
    query(func.args("key", "PUT"), "");
    query(_CACHE_GET.args("key"), "PUT");
    query(_CACHE_KEYS.args(), "key");
    query(func.args("key", " ()"), "");
    query(_CACHE_GET.args("key"), "");
    query(_CACHE_KEYS.args(), "");
    query(func.args("key", " map:merge((1 to 100000) ! map:entry(., .))"), "");
    query(_CACHE_KEYS.args(), "key");
    query(_CACHE_GET.args("key") + " => map:size()", 100000);

    // expiration values
    query(func.args("duration", "expiring", " xs:dayTimeDuration('PT1M')"));
    query(_CACHE_GET.args("duration"), "expiring");
    query(func.args("duration", "expiring", "PT1M"));
    query(_CACHE_GET.args("duration"), "expiring");
    query(func.args("duration", "expiring", "PT0S"));
    query(_CACHE_GET.args("duration"), "");
    error(func.args("duration", "expiring", "PTS"), DATEFORMAT_X_X_X);

    query(func.args("dateTime", "expiring", " xs:dateTime('9999-01-01T01:01:01')"));
    query(_CACHE_GET.args("dateTime"), "expiring");
    query(func.args("dateTime", "expiring", "9999-01-01T01:01:01"));
    query(_CACHE_GET.args("dateTime"), "expiring");
    query(func.args("dateTime", "expiring", "2001-01-01T01:01:01"));
    query(_CACHE_GET.args("dateTime"), "");
    error(func.args("dateTime", "expiring", "9999-99-99T99:99:99"), DATEFORMAT_X_X_X);

    query(func.args("time", "expiring", " xs:time('12:12:12')"));
    query(func.args("time", "expiring", "12:12:12"));
    error(func.args("time", "expiring", "99:99:99"), DATEFORMAT_X_X_X);

    query(func.args("minutes", "expiring", 59));

    error(func.args("error", " true#0"), BASEX_FUNCTION_X);
    error(func.args("error", " [ function() { 123 } ]"), BASEX_FUNCTION_X);
    error(func.args("error", " { 0: concat(1, ?) }"), BASEX_FUNCTION_X);
    error(func.args("error", " Q{java.util.Random}new()"), BASEX_FUNCTION_X);
  }

  /** Test method. */
  @Test public void remove() {
    final Function func = _CACHE_REMOVE;
    query(_CACHE_PUT.args("key", "REMOVE"));
    query(_CACHE_KEYS.args(), "key");
    query(func.args("key"), "");
    query(_CACHE_KEYS.args(), "");
  }
}
