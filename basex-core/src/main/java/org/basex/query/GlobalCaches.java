package org.basex.query;

import java.util.*;
import java.util.concurrent.*;

import org.basex.query.func.*;
import org.basex.query.util.format.*;
import org.basex.query.util.regex.*;
import org.basex.query.value.*;
import org.basex.query.value.map.*;
import org.basex.util.*;
import org.basex.util.hash.*;

/**
 * Caches of a root query, shared with all child contexts.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class GlobalCaches {
  /** Compiled regular expressions. */
  private final TokenObjectMap<RegExpr> regex = new TokenObjectMap<>();
  /** Parsed integer pictures. */
  private final TokenObjectMap<IntFormat> intFormats = new TokenObjectMap<>();
  /** Parsed date pictures. */
  private final TokenObjectMap<DatePicture> datePictures = new TokenObjectMap<>();
  /** Memoized function results. */
  private final Map<MemoKey, Value> memo = new ConcurrentHashMap<>();
  /** Functions returned by inspect:functions, indexed by module path. */
  private final Map<String, Value> functions = new ConcurrentHashMap<>();
  /** Per-thread results of fn:load-xquery-module (read by parallel tasks). */
  private final ThreadLocal<Map<String, XQMap>> loadedModules =
      ThreadLocal.withInitial(HashMap::new);

  /**
   * Returns a cached or new regular expression.
   * @param key key
   * @param create creates a new regular expression
   * @return regular expression
   * @throws QueryException query exception
   */
  public RegExpr regex(final byte[] key, final QuerySupplier<RegExpr> create)
      throws QueryException {
    return cache(regex, key, create);
  }

  /**
   * Returns a cached or new integer picture.
   * @param picture picture
   * @param info input info (can be {@code null})
   * @return integer picture
   * @throws QueryException query exception
   */
  public IntFormat intFormat(final byte[] picture, final InputInfo info) throws QueryException {
    return cache(intFormats, picture, () -> new IntFormat(picture, info));
  }

  /**
   * Returns a cached or new date picture.
   * @param picture picture
   * @param info input info (can be {@code null})
   * @return date picture
   * @throws QueryException query exception
   */
  public DatePicture datePicture(final byte[] picture, final InputInfo info)
      throws QueryException {
    return cache(datePictures, picture, () -> new DatePicture(picture, info));
  }

  /**
   * Returns a memoized function result.
   * @param key key
   * @return result (can be {@code null})
   */
  public Value memoized(final MemoKey key) {
    return memo.get(key);
  }

  /**
   * Memoizes a function result.
   * @param key key
   * @param value result
   */
  public void memoize(final MemoKey key, final Value value) {
    memo.put(key, value);
  }

  /**
   * Returns the cached functions of a module.
   * @param path module path
   * @return functions (can be {@code null})
   */
  public Value functions(final String path) {
    return functions.get(path);
  }

  /**
   * Caches the functions of a module.
   * @param path module path
   * @param funcs functions
   */
  public void functions(final String path, final Value funcs) {
    functions.put(path, funcs);
  }

  /**
   * Returns the results of fn:load-xquery-module for the current thread.
   * @return results, indexed by cache key
   */
  public Map<String, XQMap> loadedModules() {
    return loadedModules.get();
  }

  /**
   * Releases thread-bound data.
   */
  void close() {
    loadedModules.remove();
  }

  /**
   * Returns a cached or new value.
   * @param <V> value type
   * @param map map
   * @param key key
   * @param create creates a new value
   * @return value
   * @throws QueryException query exception
   */
  private static <V> V cache(final TokenObjectMap<V> map, final byte[] key,
      final QuerySupplier<V> create) throws QueryException {
    synchronized(map) {
      V value = map.get(key);
      if(value == null) {
        value = create.get();
        map.put(key, value);
      }
      return value;
    }
  }
}
