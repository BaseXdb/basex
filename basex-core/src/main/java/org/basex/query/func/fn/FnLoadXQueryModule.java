package org.basex.query.func.fn;

import static org.basex.query.QueryContext.*;
import static org.basex.query.QueryError.*;
import static org.basex.query.func.fn.FnLoadXQueryModule.LoadXQueryModuleOptions.*;

import java.io.*;
import java.util.*;
import java.util.List;

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
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Gunther Rademacher
 */
public final class FnLoadXQueryModule extends Parse {
  /** The type of the value of the 'variables' and 'vendor-options' option. */
  public static final MapType QNAME_MAP_TYPE = MapType.get(AtomType.QNAME, SeqType.ITEM_ZM);

  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] modUri = toToken(arg(0).atomItem(qc, info));
    if(modUri.length == 0) throw MODULE_URI_EMPTY.get(info);
    final LoadXQueryModuleOptions opt = toOptions(arg(1), new LoadXQueryModuleOptions(), qc);

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
        locs = loc == null ? new String[0] : new String[] {Token.string(loc)};
      }
      if(locs.length == 0) throw MODULE_NOT_FOUND_X.get(info, modUri);
      for(final String loc : locs) srcs.add(ii.sc().resolve(loc, ii.path()));
    }

    final QNmMap<Value> bindings = new QNmMap<>();
    if(opt.contains(VARIABLES)) {
      final XQMap vars = ((XQMap) opt.get(VARIABLES)).coerceTo(QNAME_MAP_TYPE, qc, null, ii);
      for(final Item name : vars.keys()) bindings.put((QNm) name, vars.get(name));
    }

    if(opt.contains(VENDOR_OPTIONS)) {
      ((XQMap) opt.get(VENDOR_OPTIONS)).coerceTo(QNAME_MAP_TYPE, qc, null, ii);
    }

    if (opt.contains(XQUERY_VERSION)) {
      final String version = opt.get(XQUERY_VERSION).toString();
      if (!isSupportedXQueryVersion(version)) throw MODULE_XQUERY_VERSION_X.get(info, version);
    }

    final QueryContext mqc = new QueryContext(qc);
    for(final byte[] uri : qc.modDeclared) mqc.modDeclared.put(uri, qc.modDeclared.get(uri));
    int nParsed = 0;
    for(final IO src : srcs) {
      mqc.finalContext = false;
      try {
        final String path = src.path();
        mqc.parse(src.string(), path.isEmpty() ? Token.string(info.sc().baseURI().string()) : path);
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
        } catch (final QueryException ex) {
          Util.debug(ex);
          throw ex.error() != INVCONVERT_X_X_X ? ex : MODULE_CONTEXT_TYPE_X_X.get(info, modUri,
              ex.getLocalizedMessage());
        }
        mqc.contextValue = new ContextScope(val, mqc.contextType, new VarScope(), info.sc(), null,
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
          final int arity = a;
          final FuncBuilder fb = new FuncBuilder(info, a, true);
          final Expr item = Functions.item(sf, fb, mqc, true);
          Map<Integer, Expr> map = funcs.get(sf.name);
          if(map == null) {
            map = new HashMap<>();
            funcs.put(sf.name, map);
          }
          map.put(arity, item);
        }
      }
    }

    final MapBuilder functions = new MapBuilder();
    for(final QNm qnm : funcs) {
      final MapBuilder arities = new MapBuilder();
      final Map<Integer, Expr> funcItems = funcs.get(qnm);
      for(final Integer arity : funcItems.keySet()) {
        arities.put(Int.get(arity), funcItems.get(arity).value(mqc));
      }
      functions.put(qnm, arities.map());
    }

    final MapBuilder variables = new MapBuilder();
    for(final StaticVar var : mqc.vars) {
      if(!var.anns.contains(Annotation.PRIVATE) && Token.eq(var.sc.module.uri(), modUri)) {
        variables.put(var.name, var.value(mqc));
      }
    }

    final MapBuilder result = new MapBuilder();
    result.put("functions", functions.map());
    result.put("variables", variables.map());
    return result.map();
  }

  /**
   * Options for fn:load-xquery-module.
   */
  public static final class LoadXQueryModuleOptions extends Options {
    /** load-xquery-module option xquery-version. */
    public static final ValueOption XQUERY_VERSION = new ValueOption("xquery-version",
        SeqType.DECIMAL_O, null);
    /** load-xquery-module option location-hints. */
    public static final StringsOption LOCATION_HINTS = new StringsOption("location-hints",
        (String[]) null);
    /** load-xquery-module option content. */
    public static final StringOption CONTENT = new StringOption("content");
    /** load-xquery-module option context-item. */
    public static final ValueOption CONTEXT_ITEM = new ValueOption("context-item", SeqType.ITEM_ZO,
        null);
    /** load-xquery-module option variable. */
    public static final ValueOption VARIABLES = new ValueOption("variables", SeqType.MAP_O, null);
    /** load-xquery-module option vendor-options. */
    public static final ValueOption VENDOR_OPTIONS = new ValueOption("vendor-options",
        SeqType.MAP_O, null);
  }
}
