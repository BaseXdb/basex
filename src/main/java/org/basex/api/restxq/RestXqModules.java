package org.basex.api.restxq;

import java.util.*;

import org.basex.api.*;
import org.basex.core.*;
import org.basex.io.*;

/**
 * This class caches RESTful XQuery modules found in the HTTP root directory.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Christian Gruen
 */
final class RestXqModules {
  /** Class instance. */
  private static final RestXqModules INSTANCE = new RestXqModules();

  /** Module cache. */
  private HashMap<String, RestXqModule> modules = new HashMap<String, RestXqModule>();
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
   * @throws HTTPException HTTP exception
   * @return instance
   */
  RestXqModule find(final HTTPContext http) throws HTTPException {
    analyze();
    for(final RestXqModule mod : modules.values()) {
      if(mod.find(http) != null) return mod;
    }
    return null;
  }

  /**
   * Updates the module cache. Parses new modules and discards obsolete ones.
   * @throws HTTPException HTTP exception
   */
  private void analyze() throws HTTPException {
    final Context ctx = HTTPSession.context();
    // create new cache
    final HashMap<String, RestXqModule> tmp = new HashMap<String, RestXqModule>();

    // parse HTTP directory
    final IOFile root = new IOFile(ctx.mprop.get(MainProp.HTTPPATH));
    for(final IOFile file : root.children()) {
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
      if(parsed || module.analyze()) {
        module.touch();
        tmp.put(path, module);
      }
    }

    // replace cache with new one
    modules = tmp;
  }
}
