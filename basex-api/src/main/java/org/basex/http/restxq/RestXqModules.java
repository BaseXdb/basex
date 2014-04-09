package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;

import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;

/**
 * This class caches RESTXQ modules found in the HTTP root directory.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class RestXqModules {
  /** Class instance. */
  private static final RestXqModules INSTANCE = new RestXqModules();

  /** Module cache. */
  private HashMap<String, RestXqModule> modules = new HashMap<>();
  /** RESTXQ path. */
  private IOFile restxq;
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
    cache(http);
    // collect all functions
    final ArrayList<RestXqFunction> list = new ArrayList<>();
    for(final RestXqModule mod : modules.values()) {
      for(final RestXqFunction rxf : mod.functions()) {
        if(rxf.matches(http, error)) list.add(rxf);
      }
    }
    // no path matches
    if(list.isEmpty()) return null;
    // choose most appropriate function
    RestXqFunction first = list.get(0);
    if(list.size() > 1) {
      // sort by specifity
      Collections.sort(list);
      first = list.get(0);
      // disallow more than one path with the same specifity
      if(first.compareTo(list.get(1)) == 0) {
        final TokenBuilder tb = new TokenBuilder();
        for(final RestXqFunction rxf : list) {
          if(first.compareTo(rxf) != 0) break;
          tb.add(Prop.NL).add(rxf.function.info.toString());
        }
        throw first.path == null ?
          first.error(ERROR_CONFLICT, first.error, tb) :
          first.error(PATH_CONFLICT, first.path, tb);
      }
    }
    // choose most specific function
    return first;
  }

  /**
   * Updates the module cache. Parses new modules and discards obsolete ones.
   * @param http http context
   * @throws Exception exception (including unexpected ones)
   */
  private synchronized void cache(final HTTPContext http) throws Exception {
    // initialize RESTXQ directory (may be relative against WEBPATH)
    if(restxq == null) {
      final GlobalOptions gopts = http.context().globalopts;
      restxq = new IOFile(gopts.get(GlobalOptions.WEBPATH)).resolve(
          gopts.get(GlobalOptions.RESTXQPATH));
    }
    // create new cache
    final HashMap<String, RestXqModule> cache = new HashMap<>();
    cache(http, restxq, cache);
    modules = cache;
  }

  /**
   * Parses the specified path for RESTXQ modules and caches new entries.
   * @param root root path
   * @param http http context
   * @param cache cached modules
   * @throws Exception exception (including unexpected ones)
   */
  private synchronized void cache(final HTTPContext http, final IOFile root,
      final HashMap<String, RestXqModule> cache) throws Exception {

    for(final IOFile file : root.children()) {
      if(file.isDir()) {
        cache(http, file, cache);
      } else {
        final String path = file.path();
        final boolean lib = path.endsWith(IO.XQMSUFFIX);
        if(lib || path.endsWith(IO.XQSUFFIX)) {
          RestXqModule module = modules.get(path);
          boolean parsed = false;
          if(module != null) {
            // check if module has been modified
            parsed = module.uptodate();
          } else {
            // create new module
            module = new RestXqModule(file, lib);
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
