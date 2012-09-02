package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;

import java.io.*;
import java.util.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.io.*;
import org.basex.query.*;

/**
 * This class caches RESXQ modules found in the HTTP root directory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqModules {
  /** Class instance. */
  private static final RestXqModules INSTANCE = new RestXqModules();

  /** Module cache. */
  private HashMap<String, RestXqModule> modules = new HashMap<String, RestXqModule>();
  /** RESTXQ path. */
  private IOFile restxq;
  /** Private constructor. */
  private RestXqModules() { }

  /**
   * Returns the singleton instance.
   * @return instance
   */
  static RestXqModules get() {
    return INSTANCE;
  }

  /**
   * Returns the module that matches the specified request, or {@code null}.
   * @param http HTTP context
   * @throws QueryException query exception
   * @return instance
   */
  RestXqFunction find(final HTTPContext http) throws QueryException {
    analyze(http);
    // collect all functions
    final ArrayList<RestXqFunction> list = new ArrayList<RestXqFunction>();
    for(final RestXqModule mod : modules.values()) mod.add(http, list);
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
        first.error(PATH_CONFLICT, first.path);
      }
    }
    // choose most specific function
    return first;
  }

  /**
   * Updates the module cache. Parses new modules and discards obsolete ones.
   * @param http http context
   * @throws QueryException query exception
   */
  private void analyze(final HTTPContext http) throws QueryException {
    // initialize RESTXQ directory (may be relative against WEBPATH)
    if(restxq == null) {
      final File fl = new File(http.context().mprop.get(MainProp.RESTXQPATH));
      restxq = fl.isAbsolute() ? new IOFile(fl) :
        new IOFile(http.context().mprop.get(MainProp.WEBPATH), fl.getPath());
    }

    // create new cache
    final HashMap<String, RestXqModule> tmp = new HashMap<String, RestXqModule>();
    for(final IOFile file : restxq.children()) {
      // only accept XQuery files with suffix ".xqm"
      if(!file.path().endsWith(IO.XQMSUFFIX)) continue;

      final String path = file.path();
      RestXqModule module = modules.get(path);

      boolean parsed = false;
      if(module != null) {
        // check if module has been modified
        parsed = module.uptodate();
      } else {
        // create new module
        module = new RestXqModule(file);
      }
      // add module if it has been parsed, and if it contains annotations
      if(parsed || module.analyze(http)) {
        module.touch();
        tmp.put(path, module);
      }
    }

    // replace cache with new one
    modules = tmp;
  }
}
