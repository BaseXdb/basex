package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * This class caches RESTXQ modules found in the HTTP root directory.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class RestXqModules {
  /** Class instance. */
  private static final RestXqModules INSTANCE = new RestXqModules();

  /** Module cache. */
  private HashMap<String, RestXqModule> modules = new HashMap<>();
  /** Private constructor. */
  private RestXqModules() { }

  /**
   * Returns the singleton instance.
   * @return instance
   */
  public static RestXqModules get() {
    return INSTANCE;
  }

  /**
   * Initializes the module cache.
   */
  public void init() {
    modules = new HashMap<>();
  }

  /**
   * Returns a WADL description for all available URIs.
   * @param http HTTP context
   * @return WADL description
   */
  public FElem wadl(final HTTPContext http) {
    return new RestXqWadl(http).create(modules);
  }

  /**
   * Returns the function that matches the current request or the specified error code.
   * Returns {@code null} if no function matches.
   * @param http HTTP context
   * @param error error code (optional)
   * @return function
   * @throws Exception exception (including unexpected ones)
   */
  RestXqFunction find(final HTTPContext http, final QNm error) throws Exception {
    // collect all functions
    final ArrayList<RestXqFunction> list = new ArrayList<>();
    for(final RestXqModule mod : cache(http).values()) {
      for(final RestXqFunction rxf : mod.functions()) {
        if(rxf.matches(http, error)) list.add(rxf);
      }
    }
    // no path matches
    if(list.isEmpty()) return null;

    // sort by relevance
    Collections.sort(list);

    // return best matching function
    final RestXqFunction best = list.get(0);
    if(list.size() == 1 || best.compareTo(list.get(1)) != 0) return best;

    final RestXqFunction bestQf = bestQf(list, http);
    if(bestQf != null) return bestQf;

    // show error if more than one path with the same specifity exists
    final TokenBuilder tb = new TokenBuilder();
    for(final RestXqFunction rxf : list) {
      if(best.compareTo(rxf) != 0) break;
      tb.add(Prop.NL).add(rxf.function.info.toString());
    }
    throw best.path == null ?
      best.error(ERROR_CONFLICT, error, tb) :
      best.error(PATH_CONFLICT, best.path, tb);
  }

  /**
   * Returns the function that has a media type whose quality factor matches the HTTP request best.
   * @param list list of functions
   * @param http http context
   * @return best function, or {@code null} if more than one function exists
   */
  private static RestXqFunction bestQf(final ArrayList<RestXqFunction> list,
      final HTTPContext http) {

    // media types accepted by the client
    final MediaType[] accepts = http.accepts();

    double bestQf = 0;
    RestXqFunction best = list.get(0);
    for(final RestXqFunction rxf : list) {
      // skip remaining functions with a weaker specifity
      if(best.compareTo(rxf) != 0) break;
      if(rxf.produces.isEmpty()) return null;

      for(final MediaType produce : rxf.produces) {
        for(final MediaType accept : accepts) {
          final String value = accept.parameters().get("q");
          final double qf = value == null ? 1 : Double.parseDouble(value);
          if(produce.matches(accept)) {
            // multiple functions with the same quality factor
            if(bestQf == qf) return null;
            if(bestQf < qf) {
              bestQf = qf;
              best = rxf;
            }
          }
        }
      }
    }
    return best;
  }

  /**
   * Updates the module cache. Parses new modules and discards obsolete ones.
   * @param http http context
   * @return module cache
   * @throws Exception exception (including unexpected ones)
   */
  private synchronized HashMap<String, RestXqModule> cache(final HTTPContext http)
      throws Exception {

    final StaticOptions sopts = http.context(false).soptions;
    HashMap<String, RestXqModule> cache = modules;

    // create new cache if it is empty, or if cache is to be recreated every time
    if(cache.isEmpty() || !sopts.get(StaticOptions.CACHERESTXQ)) {
      cache = new HashMap<>();
      final String webpath = sopts.get(StaticOptions.WEBPATH);
      final String rxqpath = sopts.get(StaticOptions.RESTXQPATH);
      final IOFile restxq = new IOFile(webpath).resolve(rxqpath);
      if(!restxq.exists()) throw HTTPCode.NO_RESTXQ.get();

      cache(http, restxq, cache, modules);
      modules = cache;
    }
    return cache;
  }

  /**
   * Parses the specified path for RESTXQ modules and caches new entries.
   * @param root root path
   * @param http http context
   * @param cache cached modules
   * @param old old cache
   * @throws Exception exception (including unexpected ones)
   */
  private static synchronized void cache(final HTTPContext http, final IOFile root,
      final HashMap<String, RestXqModule> cache, final HashMap<String, RestXqModule> old)
      throws Exception {

    // check if directory is to be skipped
    final IOFile[] files = root.children();
    for(final IOFile file : files) if(file.name().equals(IGNORE)) return;

    for(final IOFile file : files) {
      if(file.isDir()) {
        cache(http, file, cache, old);
      } else {
        final String path = file.path();
        if(file.hasSuffix(IO.XQSUFFIXES)) {
          RestXqModule module = old.get(path);
          boolean parsed = false;
          if(module != null) {
            // check if module has been modified
            parsed = module.uptodate();
          } else {
            // create new module
            module = new RestXqModule(file);
          }
          // add module if it has been parsed, and if it contains annotations
          if(parsed || module.parse(http)) {
            module.touch();
            cache.put(path, module);
          }
        }
      }
    }
  }
}
