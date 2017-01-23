package org.basex.http.restxq;

import static org.basex.http.restxq.RestXqText.*;

import java.util.*;
import java.util.concurrent.atomic.*;

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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class RestXqModules {
  /** Singleton instance. */
  private static RestXqModules instance;

  /** Parsing mutex. */
  private final AtomicBoolean parsed = new AtomicBoolean();
  /** RESTXQ path. */
  private final IOFile path;
  /** Indicates if modules should be parsed with every call. */
  private final boolean cached;

  /** Module cache. */
  private HashMap<String, RestXqModule> modules = new HashMap<>();
  /** Last access. */
  private long last;

  /**
   * Private constructor.
   * @param ctx database context
   */
  private RestXqModules(final Context ctx) {
    final StaticOptions sopts = ctx.soptions;
    final String webpath = sopts.get(StaticOptions.WEBPATH);
    final String rxqpath = sopts.get(StaticOptions.RESTXQPATH);
    path = new IOFile(webpath).resolve(rxqpath);

    // RESTXQ parsing
    final int ms = sopts.get(StaticOptions.PARSERESTXQ) * 1000;
    // = 0: parse every time
    cached = ms != 0;
    // >= 0: activate timer
    if(ms >= 0) {
      new Timer(true).scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          synchronized(parsed) {
            if(parsed.get() && System.currentTimeMillis() - last >= ms) parsed.set(false);
          }
        }
      }, 0, 500);
    }
  }

  /**
   * Returns the singleton instance.
   * @param ctx database context
   * @return instance
   */
  public static RestXqModules get(final Context ctx) {
    if(instance == null) instance = new RestXqModules(ctx);
    return instance;
  }

  /**
   * Initializes the module cache.
   */
  public void init() {
    parsed.set(false);
  }

  /**
   * Returns a WADL description for all available URIs.
   * @param conn HTTP connection
   * @return WADL description
   */
  public FElem wadl(final HTTPConnection conn) {
    return new RestXqWadl(conn).create(modules);
  }

  /**
   * Returns the function that matches the current request or the specified error code.
   * Returns {@code null} if no function matches.
   * @param conn HTTP connection
   * @param error error code (optional)
   * @return function
   * @throws Exception exception (including unexpected ones)
   */
  RestXqFunction find(final HTTPConnection conn, final QNm error) throws Exception {
    // collect all functions
    final ArrayList<RestXqFunction> list = new ArrayList<>();
    for(final RestXqModule mod : cache(conn.context).values()) {
      for(final RestXqFunction rxf : mod.functions()) {
        if(rxf.matches(conn, error)) list.add(rxf);
      }
    }
    // no path matches
    if(list.isEmpty()) return null;

    // sort by relevance
    Collections.sort(list);

    // return best matching function
    final RestXqFunction best = list.get(0);
    if(list.size() == 1 || best.compareTo(list.get(1)) != 0) return best;

    final RestXqFunction bestQf = bestQf(list, conn);
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
   * @param conn HTTP connection
   * @return best function, or {@code null} if more than one function exists
   */
  private static RestXqFunction bestQf(final ArrayList<RestXqFunction> list,
      final HTTPConnection conn) {

    // media types accepted by the client
    final MediaType[] accepts = conn.accepts();

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
   * @param ctx database context
   * @return module cache
   * @throws Exception exception (including unexpected ones)
   */
  private HashMap<String, RestXqModule> cache(final Context ctx) throws Exception {
    synchronized(parsed) {
      if(!parsed.get()) {
        if(!path.exists()) throw HTTPCode.NO_RESTXQ.get();

        final HashMap<String, RestXqModule> map = new HashMap<>();
        cache(ctx, path, map, modules);
        modules = map;
        parsed.set(cached);
      }
      last = System.currentTimeMillis();
      return modules;
    }
  }

  /**
   * Parses the specified path for RESTXQ modules and caches new entries.
   * @param root root path
   * @param ctx database context
   * @param cache cached modules
   * @param old old cache
   * @throws Exception exception (including unexpected ones)
   */
  private static void cache(final Context ctx, final IOFile root,
      final HashMap<String, RestXqModule> cache, final HashMap<String, RestXqModule> old)
      throws Exception {

    // check if directory is to be skipped
    final IOFile[] files = root.children();
    for(final IOFile file : files) if(file.name().equals(IO.IGNORESUFFIX)) return;

    for(final IOFile file : files) {
      if(file.isDir()) {
        cache(ctx, file, cache, old);
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
          if(parsed || module.parse(ctx)) {
            module.touch();
            cache.put(path, module);
          }
        }
      }
    }
  }
}
