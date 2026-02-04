package org.basex.query.func.fn;

import static org.basex.query.QueryContext.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.fn.FnLoadXQueryModule.LoadXQueryModuleOptions.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.ann.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.hash.*;
import org.basex.query.util.parse.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;
import org.basex.util.list.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Gunther Rademacher
 */
public final class FnLoadXQueryModule extends StandardFunc {
  /** The type of the value of the 'variables' and 'vendor-options' option. */
  private static final MapType QNAME_MAP_TYPE = MapType.get(BasicType.QNAME, Types.ITEM_ZM);

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] modUri = toToken(arg(0), qc);
    if(modUri.length == 0) throw MODULE_URI_EMPTY.get(info);

    final XQMap options = toEmptyMap(arg(1), qc);
    final LoadXQueryModuleOptions opt = toOptions(options, new LoadXQueryModuleOptions(), qc);
    final String[] hints = opt.get(LOCATION_HINTS);

    // check for cached result
    Map<String, XQMap> modCache = null;
    String cacheKey = null;
    if(hints.length == 1 && options.structSize() == 1) {
      QueryContext qcAnc = qc;
      while(qcAnc.parent != null) qcAnc = qcAnc.parent;
      modCache = qcAnc.threads.moduleCache().get();
      cacheKey = new TokenBuilder(modUri).add('#').add(hints[0]).toString();
      if(modCache.containsKey(cacheKey)) return modCache.get(cacheKey);
    }

    final List<IO> srcs = new ArrayList<>();
    final String cont = opt.get(CONTENT);
    if(cont != null) {
      srcs.add(new IOContent(cont));
    } else {
      final StringList locs = new StringList().add(hints);
      if(locs.isEmpty()) {
        final TokenList files = qc.modDeclared.get(modUri);
        if(files != null) {
          for(final byte[] file : files) locs.add(Token.string(file));
        } else {
          final String path = repoFilePath(modUri, qc.context);
          if(path != null) locs.add(path);
        }
      }
      if(locs.isEmpty()) throw MODULE_NOT_FOUND_X.get(info, modUri);
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
      String version = opt.get(XQUERY_VERSION).toString();
      if(!version.contains(".")) version += ".0";
      if(!isSupported(version)) throw MODULE_XQUERY_VERSION_X.get(info, version);
    }

    final QueryContext mqc = new QueryContext(qc);
    for(final byte[] uri : qc.modDeclared) mqc.modDeclared.put(uri, qc.modDeclared.get(uri));
    int nParsed = 0;
    final Value ctx = opt.get(CONTEXT_ITEM);
    if(ctx != null) {
      mqc.contextValue = new ContextScope(ctx, mqc.contextType, new VarScope(), sc(), null, null);
      mqc.finalContext = true;
    }
    for(final IO src : srcs) {
      final AModule lib;
      try {
        final String path = src.path(), content = src.readString();
        final String uri = path.isEmpty() ? Token.string(sc().baseURI().string()) : path;
        lib = mqc.parse(content, uri);
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
        throw ex.error() != INVTYPE_X ? ex : MODULE_PARAMETER_TYPE_X_X.get(info, modUri,
            ex.getLocalizedMessage());
      }
      if(ctx != null && lib.sc.contextType != null && !lib.sc.contextType.instance(ctx)) {
        throw MODULE_CONTEXT_TYPE_X_X.get(info, modUri, ctx.seqType());
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
    for(final StaticFunc sf : mqc.functions) {
      if(sf.updating()) mqc.updating();
      if(!sf.anns.contains(Annotation.PRIVATE) && Token.eq(sf.name.uri(), modUri)) {
        for(int a = sf.minArity(); a <= sf.arity(); ++a) {
          final FuncBuilder fb = new FuncBuilder(sf.info, a, true);
          final Expr item = Functions.item(sf, fb, mqc);
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
      if(!var.anns.contains(Annotation.PRIVATE) && Token.eq(var.name.uri(), modUri)) {
        variables.put(var.name, var.value(mqc));
      }
    }

    final MapBuilder result = new MapBuilder();
    result.put("variables", variables.map());
    result.put("functions", functions.map());
    final XQMap map = result.map();
    if(modCache != null) modCache.put(cacheKey, map);
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
    public static final StringsOption LOCATION_HINTS = new StringsOption("location-hints");
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
}
