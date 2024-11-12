package org.basex.query.func.fn;

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
  /** The type of the value of the 'variables' option. */
  public static final MapType VARIABLES_TYPE = MapType.get(AtomType.QNAME, SeqType.ITEM_ZM);

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
      final XQMap vars = ((XQMap) opt.get(VARIABLES)).coerceTo(VARIABLES_TYPE, qc, null, ii);
      for(final Item name : vars.keys()) bindings.put((QNm) name, vars.get(name));
    }

    final QueryContext mqc = new QueryContext(qc);
    final CompileContext cc = new CompileContext(mqc, true);
    for(final byte[] uri : qc.modDeclared) mqc.modDeclared.put(uri, qc.modDeclared.get(uri));
    for(final IO src : srcs) {
      try {
        mqc.parse(src.string(), src.path());
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
        throw MODULE_PARAMETER_TYPE_X_X.get(info, modUri, ex.getLocalizedMessage());
      }
      mqc.functions.compileAll(cc);
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
    public static final NumberOption XQUERY_VERSION = new NumberOption("xquery-version");
    /** load-xquery-module option location-hints. */
    public static final StringsOption LOCATION_HINTS = new StringsOption("location-hints",
        (String[]) null);
    /** load-xquery-module option content. */
    public static final StringOption CONTENT = new StringOption("content");
    /** load-xquery-module option context-item. */
    public static final ValueOption CONTEXT_ITEM = new ValueOption("context-item", SeqType.ITEM_ZO);
    /** load-xquery-module option variable. */
    public static final ValueOption VARIABLES = new ValueOption("variables", SeqType.MAP_O, null);
    /** load-xquery-module option vendor-options. */
    public static final ValueOption VENDOR_OPTIONS = new ValueOption("vendor-options",
        SeqType.MAP_O);
  }
}
