package org.basex.query.func.fn;

import static org.basex.query.QueryContext.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.fn.FnLoadXQueryModule.LoadXQueryModuleOptions.*;

import java.io.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnLoadXQueryModule extends StandardFunc {
  /** The type of the value of the 'variables' and 'vendor-options' option. */
  private static final MapType QNAME_MAP_TYPE = MapType.get(AtomType.QNAME, Types.ITEM_ZM);
  /** Number of read modules (for statistics). */
  public static final AtomicInteger COMPILED_MODULES = new AtomicInteger();

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] modUri = toToken(arg(0).atomItem(qc, info));
    if(modUri.length == 0) throw MODULE_URI_EMPTY.get(info);
    final Item arg1 = arg(1).item(qc, ii);
    final XQMap options = arg1.isEmpty() ? XQMap.empty() : toMap(arg1, qc);

    // check for cached result
    QueryContext cacheContext = qc;
    while(cacheContext.parent != null) cacheContext = cacheContext.parent;
    final Map<CacheKey, XQMap> modCache = cacheContext.threads.compiledModuleCache().get();
    final CacheKey cacheKey = new CacheKey(modUri, options);
    final XQMap cached = modCache.get(cacheKey);
    if(cached != null) return cached;
    COMPILED_MODULES.incrementAndGet();

    final ConcurrentHashMap<String, String> srcCache = cacheContext.threads.sourceCache();
    final LoadXQueryModuleOptions opt = toOptions(options, new LoadXQueryModuleOptions(), qc);
    final List<IO> srcs = new ArrayList<>();
    final String cont = opt.get(CONTENT);
    if(cont != null) {
      srcs.add(new IOContent(cont));
    } else {
      final String[] locs;
      if(opt.contains(LOCATION_HINTS)) {
        locs = opt.get(LOCATION_HINTS);
      } else {
        final byte[] loc = qc.modDeclared.get(modUri);
        if(loc != null) {
          locs = new String[] { Token.string(loc) };
        } else {
          final String path = repoFilePath(modUri, qc.context);
          locs = path == null ? new String[0] : new String[] { path };
        }
      }
      if(locs.length == 0) throw MODULE_NOT_FOUND_X.get(info, modUri);
      for(final String loc : locs) srcs.add(ii.sc().resolve(loc, ii.path()));
    }

    final QNmMap<Value> bindings = new QNmMap<>();
    if(opt.contains(VARIABLES)) {
      final XQMap vars = ((XQMap) opt.get(VARIABLES)).coerceTo(QNAME_MAP_TYPE, qc, null, ii);
      vars.forEach((key, value) -> bindings.put((QNm) key, value));
    }

    if(opt.contains(VENDOR_OPTIONS)) {
      ((XQMap) opt.get(VENDOR_OPTIONS)).coerceTo(QNAME_MAP_TYPE, qc, null, ii);
    }

    if(opt.contains(XQUERY_VERSION)) {
      final String version = opt.get(XQUERY_VERSION).toString();
      if(!isSupported(version)) throw MODULE_XQUERY_VERSION_X.get(info, version);
    }

    final QueryContext mqc = new QueryContext(qc);
    for(final byte[] uri : qc.modDeclared) mqc.modDeclared.put(uri, qc.modDeclared.get(uri));
    int nParsed = 0;
    for(final IO src : srcs) {
      mqc.finalContext = false;
      try {
        final String path = src.path();
        String content;
        if(src instanceof IOContent) {
          content = src.readString();
        } else {
          content = srcCache.get(path);
          if(content == null) {
            final String fresh = src.readString();
            // first thread’s read wins, others take the published value
            final String prev = srcCache.putIfAbsent(path, fresh);
            content = prev != null ? prev : fresh;
          }
        }
        mqc.parse(content, path.isEmpty() ? Token.string(sc().baseURI().string()) : path);
      } catch(final IOException ex) {
        Util.debug(ex);
        throw WHICHMODFILE_X.get(info, src);
      } catch(final QueryException ex) {
        Util.debug(ex);
        throw MODULE_STATIC_ERROR_X_X.get(info, modUri, ex.getLocalizedMessage());
      }
      try {
        mqc.vars.bindExternal(mqc, bindings, false);
      } catch(final QueryException ex) {
        Util.debug(ex);
        throw ex.error() != INVCONVERT_X_X_X ? ex : MODULE_PARAMETER_TYPE_X_X.get(info, modUri,
            ex.getLocalizedMessage());
      }
      if(!mqc.finalContext && opt.contains(CONTEXT_ITEM)) {
        Value val = opt.get(CONTEXT_ITEM);
        try {
          if(mqc.contextType != null)  val = mqc.contextType.coerce(val, null, mqc, null, info);
        } catch(final QueryException ex) {
          Util.debug(ex);
          throw ex.error() != INVCONVERT_X_X_X ? ex : MODULE_CONTEXT_TYPE_X_X.get(info, modUri,
              ex.getLocalizedMessage());
        }
        mqc.contextValue = new ContextScope(val, mqc.contextType, new VarScope(), sc(), null,
            null);
        mqc.finalContext = true;
      }
      mqc.compile(true);
      if(mqc.main != null) throw MODULE_FOUND_MAIN_X.get(info, modUri);
      final Iterator<byte[]> it = mqc.modParsed.values().iterator();
      for(int i = 0; i < nParsed; ++i) it.next();
      final byte[] uri = it.next();
      if(!Token.eq(modUri, uri)) {
        final String path = src.path();
        throw path.isEmpty() ? MODULE_FOUND_OTHER_X.get(info, modUri)
                             : MODULE_FOUND_OTHER_X_X.get(info, modUri, path);
      }
      nParsed = mqc.modParsed.size();
    }

    final QNmMap<Map<Integer, Expr>> funcs = new QNmMap<>();
    for(final StaticFunc sf : mqc.functions.funcs()) {
      if(sf.updating()) mqc.updating();
      if(!sf.anns.contains(Annotation.PRIVATE) && Token.eq(sf.sc.module.uri(), modUri)) {
        for(int a = sf.minArity(); a <= sf.arity(); ++a) {
          final FuncBuilder fb = new FuncBuilder(info, a, true);
          final Expr item = Functions.item(sf, fb, mqc, true);
          funcs.computeIfAbsent(sf.name, HashMap::new).put(a, item);
        }
      }
    }

    final MapBuilder functions = new MapBuilder();
    for(final QNm qnm : funcs) {
      final MapBuilder arities = new MapBuilder();
      final Map<Integer, Expr> funcItems = funcs.get(qnm);
      for(final Map.Entry<Integer, Expr> entry : funcItems.entrySet()) {
        arities.put(Itr.get(entry.getKey()), entry.getValue().value(mqc));
      }
      functions.put(qnm, arities.map());
    }

    final MapBuilder variables = new MapBuilder();
    for(final StaticVar var : mqc.vars) {
      if(!var.anns.contains(Annotation.PRIVATE) && Token.eq(var.sc.module.uri(), modUri)) {
        try {
          variables.put(var.name, var.value(mqc));
        } catch(final QueryException ex) {
          throw ex;
        } catch(final Exception ex) {
          Util.debug(ex);
          throw VAREMPTY_X.get(info, var.name());
        }
      }
    }

    final MapBuilder result = new MapBuilder();
    result.put("variables", variables.map());
    result.put("functions", functions.map());
    final XQMap map = result.map();
    modCache.put(cacheKey, map);
    return map;
  }

  /**
   * Return the repository file path of the XQuery module with the given module URI, or
   * {@code null}, if there is no such module in the repository.
   * @param modUri module URI
   * @param context database context
   * @return the file path of the XQuery module in the repository (maybe {@code null})
   */
  private static String repoFilePath(final byte[] modUri, final Context context) {
    final String path = Strings.uri2path(Token.string(modUri));
    final String repoPath = context.soptions.get(StaticOptions.REPOPATH);
    for(final String suffix : IO.XQSUFFIXES) {
      final IOFile file = new IOFile(repoPath, path + suffix);
      if(file.exists()) return file.path();
    }
    return null;
  }

  /**
   * Options for fn:load-xquery-module.
   */
  public static final class LoadXQueryModuleOptions extends Options {
    /** load-xquery-module option xquery-version. */
    public static final ValueOption XQUERY_VERSION = new ValueOption("xquery-version",
        Types.DECIMAL_O, null);
    /** load-xquery-module option location-hints. */
    public static final StringsOption LOCATION_HINTS = new StringsOption("location-hints",
        (String[]) null);
    /** load-xquery-module option content. */
    public static final StringOption CONTENT = new StringOption("content");
    /** load-xquery-module option context-item. */
    public static final ValueOption CONTEXT_ITEM = new ValueOption("context-item", Types.ITEM_ZO,
        null);
    /** load-xquery-module option variable. */
    public static final ValueOption VARIABLES = new ValueOption("variables", Types.MAP_O, null);
    /** load-xquery-module option vendor-options. */
    public static final ValueOption VENDOR_OPTIONS = new ValueOption("vendor-options",
        Types.MAP_O, null);
  }

  /**
   * Cache key for loaded modules.
   */
  public static final class CacheKey {
    /** Module URI from function argument. */
    private final byte[] modUri;
    /** Options map from function argument. */
    private final XQMap opts;
    /** Pre-computed hash code. */
    private final int hashCode;

    /** Constructor.
     * @param modUri module URI
     * @param options options map
     * @throws QueryException query exception
     */
    private CacheKey(final byte[] modUri, final XQMap options) throws QueryException {
      this.modUri = modUri;
      this.opts = options;
      this.hashCode = 31 * Arrays.hashCode(modUri) + mapHashCode(options.toJava());
    }

    @Override public boolean equals(final Object o) {
      if(this == o) return true;
      if(!(o instanceof CacheKey k)) return false;
      try {
        return Token.eq(this.modUri, k.modUri) && new DeepEqual().equal(this.opts, k.opts);
      } catch(QueryException e) {
        throw Util.notExpected(e);
      }
    }

    @Override public int hashCode() {
      return hashCode;
    }

    /**
     * Computes a hash code for a map, recursively processing nested maps and Object arrays.
     * @param map map to process
     * @return hash code
     */
    private static int mapHashCode(final Map<?, ?> map) {
      int h = 0;
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        h += valueHashCode(entry.getKey()) ^ valueHashCode(entry.getValue());
      }
      return h;
    }

    /**
     * Computes a hash code for a value, recursively processing nested maps and Object arrays.
     * @param value value to process
     * @return hash code
     */
    private static int valueHashCode(final Object value) {
      if(value == null) return 0;
      if(value instanceof final Map<?, ?> map) return mapHashCode(map);
      if(value instanceof final Object[]  arr) return arrayHashCode(arr);
      if(value instanceof final int[]     arr) return Arrays.hashCode(arr);
      if(value instanceof final byte[]    arr) return Arrays.hashCode(arr);
      if(value instanceof final short[]   arr) return Arrays.hashCode(arr);
      if(value instanceof final long[]    arr) return Arrays.hashCode(arr);
      if(value instanceof final char[]    arr) return Arrays.hashCode(arr);
      if(value instanceof final float[]   arr) return Arrays.hashCode(arr);
      if(value instanceof final double[]  arr) return Arrays.hashCode(arr);
      if(value instanceof final boolean[] arr) return Arrays.hashCode(arr);
      return value.hashCode();
    }

    /**
     * Computes a hash code for an array, recursively processing nested maps and Object arrays.
     * @param arr array to process
     * @return hash code
     */
    private static int arrayHashCode(final Object[] arr) {
      int result = 1;
      for(final Object element : arr) result = 31 * result + valueHashCode(element);
      return result;
    }
  }
}
