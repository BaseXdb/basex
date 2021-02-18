package org.basex.http.web;

import static org.basex.http.web.WebText.*;
import static org.basex.util.Token.*;

import java.io.*;
import java.util.*;

import javax.servlet.http.*;

import org.basex.core.*;
import org.basex.http.*;
import org.basex.http.restxq.*;
import org.basex.http.ws.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.http.*;

/**
 * This class caches RESTXQ modules found in the HTTP root directory.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class WebModules {
  /** Singleton instance. */
  private static volatile WebModules instance;

  /** RESTXQ path. */
  private final IOFile path;

  /** Module cache. */
  private HashMap<String, WebModule> modules = new HashMap<>();
  /** Indicates if modules have been cached. */
  private boolean parsed;
  /** Last access time. */
  private long access;

  /**
   * Private constructor.
   * @param ctx database context
   */
  private WebModules(final Context ctx) {
    final StaticOptions sopts = ctx.soptions;
    final String webpath = sopts.get(StaticOptions.WEBPATH);
    final String rxqpath = sopts.get(StaticOptions.RESTXQPATH);
    path = new IOFile(webpath).resolve(rxqpath);

    // RESTXQ parsing
    final int sec = sopts.get(StaticOptions.PARSERESTXQ);
    // < 0: process until cache is invalidated
    if(sec >= 0) {
      // speed up permission checks: keep cache for a minimum of time even if caching is disabled
      final int ms = sec == 0 ? 10 : sec * 1000;
      new Timer(true).scheduleAtFixedRate(new TimerTask() {
        @Override
        public void run() {
          synchronized(WebModules.this) {
            if(System.currentTimeMillis() - access >= ms) init(true);
          }
        }
      }, 0, 100);
    }
  }

  /**
   * Returns the singleton instance.
   * @param ctx database context
   * @return instance
   */
  public static WebModules get(final Context ctx) {
    if(instance == null) instance = new WebModules(ctx);
    return instance;
  }

  /**
   * Initializes the module cache.
   * @param update only update new modules
   */
  public synchronized void init(final boolean update) {
    if(!update) modules = new HashMap<>();
    parsed = false;
  }

  /**
   * Returns a WADL description for all available URIs.
   * @param request HTTP request
   * @return WADL description
   */
  public FElem wadl(final HttpServletRequest request) {
    return new RestXqWadl(request).create(modules);
  }

  /**
   * Returns a RESTXQ function that matches the current request or the specified error code best.
   * @param conn HTTP connection
   * @param error error code (assigned if error function is to be called)
   * @return function, or {@code null} if no function matches
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public RestXqFunction restxq(final HTTPConnection conn, final QNm error)
      throws QueryException, IOException {

    // collect all function candidates
    List<RestXqFunction> funcs = find(conn, error, false);
    if(funcs.isEmpty()) return null;

    // multiple functions: check specifity
    if(funcs.size() > 1) bestSpec(funcs);
    // multiple functions: check quality factors
    if(funcs.size() > 1) bestQf(funcs, conn);
    // multiple functions: check consume filter
    if(funcs.size() > 1) bestConsume(funcs, conn);

    final RestXqFunction first = funcs.get(0);
    if(funcs.size() == 1) return first;

    // show error if we are left with multiple function candidates
    throw first.path == null ?
      first.error(ERROR_CONFLICT_X_X, error, toString(funcs)) :
      first.error(PATH_CONFLICT_X_X, first.path, toString(funcs));
  }

  /**
   * Returns RESTXQ and permissions functions that match the current request.
   * @param conn HTTP connection
   * @param error error code (assigned if error function is to be called)
   * @param perm permission flag
   * @return list of matching functions, ordered by specifity
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private List<RestXqFunction> find(final HTTPConnection conn, final QNm error, final boolean perm)
      throws QueryException, IOException {

    // collect and sort all functions
    final ArrayList<RestXqFunction> list = new ArrayList<>();
    for(final WebModule mod : cache(conn.context).values()) {
      for(final RestXqFunction func : mod.functions()) {
        if(func.matches(conn, error, perm)) list.add(func);
      }
    }
    // sort by specifity
    Collections.sort(list);
    return list;
  }

  /**
   * Returns permission functions that match the current request.
   * @param conn HTTP connection
   * @return list of function, ordered by relevance
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public List<RestXqFunction> checks(final HTTPConnection conn) throws QueryException, IOException {
    return find(conn, null, true);
  }

  /**
   * Returns all implementations for the given WebSocket.
   * @param ws WebSocket
   * @param ann annotation (can be {@code null})
   * @return result of check
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public ArrayList<WsFunction> findWs(final WebSocket ws, final Annotation ann)
      throws QueryException, IOException {
    final ArrayList<WsFunction> funcs = new ArrayList<>();
    for(final WebModule mod : cache(ws.context).values()) {
      for(final WsFunction func : mod.wsFunctions()) {
        if(func.matches(ann, ws.path)) funcs.add(func);
      }
    }
    Collections.sort(funcs);
    return funcs;
  }

  /**
   * Returns the WebSocket function that matches the current request.
   * @param ws WebSocket
   * @param ann annotation
   * @return function, or {@code null} if no function matches
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  public WsFunction websocket(final WebSocket ws, final Annotation ann)
      throws QueryException, IOException {

    // collect and sort all function candidates
    final ArrayList<WsFunction> funcs = findWs(ws, ann);
    if(funcs.isEmpty()) return null;

    final WsFunction first = funcs.get(0);
    if(funcs.size() == 1) return first;

    // show error if we are left with multiple function candidates
    throw first.error(PATH_CONFLICT_X_X, first.path, toString(funcs));
  }

  /**
   * Returns a string representation of the specified functions.
   * @param funcs functions
   * @return string
   */
  private static String toString(final List<? extends WebFunction> funcs) {
    final TokenBuilder tb = new TokenBuilder();
    for(final WebFunction func : funcs) tb.add(Text.NL).add(Text.LI).add(func);
    return tb.toString();
  }

  /**
   * Filters functions by their consume filters.
   * @param funcs list of functions
   * @param conn HTTP connection
   */
  private static void bestConsume(final List<RestXqFunction> funcs, final HTTPConnection conn) {
    // retrieve most specific consume types from all functions
    final MediaType mt = conn.mediaType();
    final ArrayList<MediaType> types = new ArrayList<>(funcs.size());
    for(final RestXqFunction func : funcs) types.add(func.consumedType(mt));
    // find most specific type
    MediaType spec = null;
    for(final MediaType type : types) {
      if(spec == null || spec.compareTo(type) > 0) spec = type;
    }
    // drop functions with more generic types
    for(int f = funcs.size() - 1; f >= 0; f--) {
      if(!types.get(f).is(spec)) funcs.remove(f);
    }
  }

  /**
   * Filters functions by their specifity.
   * @param funcs list of functions
   */
  private static void bestSpec(final List<RestXqFunction> funcs) {
    for(int l = funcs.size() - 1; l > 0; l--) {
      if(funcs.get(0).compareTo(funcs.get(l)) != 0) funcs.remove(l);
    }
  }

  /**
   * Filters functions by their quality factors.
   * @param funcs list of functions
   * @param conn HTTP connection
   */
  private static void bestQf(final List<RestXqFunction> funcs, final HTTPConnection conn) {
    // find highest matching quality factors
    final ArrayList<MediaType> accepts = conn.accepts();
    double cQf = 0, sQf = 0;
    for(final RestXqFunction func : funcs) {
      for(final MediaType accept : accepts) {
        if(func.produces.isEmpty()) {
          cQf = Math.max(cQf, qf(accept, "q"));
          sQf = 1;
        } else {
          for(final MediaType produce : func.produces) {
            if(produce.matches(accept)) {
              cQf = Math.max(cQf, qf(accept, "q"));
              sQf = Math.max(sQf, qf(produce, "qs"));
            }
          }
        }
      }
    }
    bestQf(funcs, accepts, cQf, -1);
    if(funcs.size() > 1) bestQf(funcs, accepts, cQf, sQf);
  }

  /**
   * Filters functions by their quality factors.
   * @param funcs list of functions
   * @param accepts accept media types
   * @param clientQf client quality factor
   * @param serverQf server quality factor (ignore if {@code -1})
   */
  private static void bestQf(final List<RestXqFunction> funcs, final ArrayList<MediaType> accepts,
      final double clientQf, final double serverQf) {

    for(int fl = funcs.size() - 1; fl >= 0; fl--) {
      final RestXqFunction func = funcs.get(fl);
      final Checks<MediaType> check = accept -> {
        if(func.produces.isEmpty()) return qf(accept, "q") == clientQf;

        final Checks<MediaType> checkProduce = produce ->
          produce.matches(accept) && qf(accept, "q") == clientQf &&
          (serverQf == -1 || qf(produce, "qs") == serverQf);
        return checkProduce.any(func.produces);
      };
      if(!check.any(accepts)) funcs.remove(fl);
    }
  }

  /**
   * Returns the quality factor of the specified media type.
   * @param type media type
   * @param f quality factor string
   * @return quality factor
   */
  private static double qf(final MediaType type, final String f) {
    final String qf = type.parameters().get(f);
    return qf != null ? toDouble(token(qf)) : 1;
  }

  /**
   * Returns the module cache.
   * @param ctx database context
   * @return module cache
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private synchronized HashMap<String, WebModule> cache(final Context ctx)
      throws QueryException, IOException {

    final HashMap<String, WebModule> cache;
    if(parsed) {
      // module cache is still up-to-date
      cache = modules;
    } else {
      // module cache needs to be updated
      if(!path.exists()) throw HTTPCode.NO_RESTXQ_DIRECTORY.get();

      cache = new HashMap<>();
      parse(ctx, path, cache, modules);
      modules = cache;
      parsed = true;
    }

    // update last access time
    access = System.currentTimeMillis();
    return cache;
  }

  /**
   * Parses the specified path for modules with relevant annotations and caches new entries.
   * @param root root path
   * @param ctx database context
   * @param cache cached modules
   * @param old old cache
   * @throws QueryException query exception
   * @throws IOException I/O exception
   */
  private static void parse(final Context ctx, final IOFile root,
      final HashMap<String, WebModule> cache, final HashMap<String, WebModule> old)
      throws QueryException, IOException {

    // check if directory is to be skipped
    final IOFile[] files = root.children();
    for(final IOFile file : files) {
      if(file.name().equals(IO.IGNORESUFFIX)) return;
    }

    for(final IOFile file : files) {
      if(file.isDir()) {
        parse(ctx, file, cache, old);
      } else {
        final String path = file.path();
        if(file.hasSuffix(IO.XQSUFFIXES)) {
          // retrieve existing module or create new instance
          WebModule module = old.get(path);
          if(module == null) module = new WebModule(file);

          // parse updated module, add to cache
          module.parse(ctx);
          cache.put(path, module);
        }
      }
    }
  }
}
