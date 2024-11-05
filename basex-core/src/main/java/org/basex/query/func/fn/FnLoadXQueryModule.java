package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.fn.FnLoadXQueryModule.LoadXQueryModuleOptions.*;

import java.io.*;
import java.util.*;
import java.util.List;

import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.parse.*;
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
  @Override
  public XQMap item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] modUri = toToken(arg(0).atomItem(qc, info));
    if(modUri.length == 0) throw MODULE_URI_EMPTY.get(info);
    final LoadXQueryModuleOptions opt = toOptions(arg(1), new LoadXQueryModuleOptions(), qc);
    final String cont = opt.get(CONTENT);
    final List<IO> srcs = new ArrayList<>();
    if(cont != null) {
      srcs.add(new IOContent(cont));
    } else {
      final String[] locs = opt.get(LOCATION_HINTS);
      if (locs.length == 0) throw MODULE_NOT_FOUND_X.get(info, modUri);
      for(final String loc : locs) srcs.add(IO.get(loc));
    }
    final QueryContext mqc = new QueryContext(qc);
    final CompileContext cc = new CompileContext(mqc, true);
      for (final IO src : srcs) {
        try {
          mqc.parse(src.string(), src.path());
        } catch(final IOException ex) {
          Util.debug(ex);
          throw WHICHMODFILE_X.get(info, src);
        } catch(final QueryException ex) {
          Util.debug(ex);
          throw MODULE_STATIC_ERROR_X_X.get(info, modUri, ex.getLocalizedMessage());
        }
        mqc.functions.compileAll(cc);
      }

    Map<QNm, Map<Integer, Expr>> funcs = new HashMap<>();
    for(final StaticFunc sf : mqc.functions.funcs()) {
      if(sf.updating()) mqc.updating();
      for(int a = sf.minArity(); a <= sf.arity(); ++a) {
        final int arity = a;
        final FuncBuilder fb = new FuncBuilder(info, a, true);
        final Expr item = Functions.item(sf, fb, mqc, true);
        funcs.compute(sf.name, (qnm, map) -> {
          final Map<Integer, Expr> m = map != null ? map : new HashMap<>();
          m.put(arity, item);
          return m;
        });
      }
    }

    final MapBuilder functions = new MapBuilder();
    for(final QNm qnm : funcs.keySet()) {
      final MapBuilder arities = new MapBuilder();
      Map<Integer, Expr> items = funcs.get(qnm);
      for(final Integer arity : items.keySet()) {
        arities.put(Int.get(arity), items.get(arity).value(mqc));
      }
      functions.put(qnm, arities.map());
    }

    final MapBuilder variables = new MapBuilder();
    for(final StaticVar var : mqc.vars) {
      variables.put(var.name, var.value(mqc));
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
    public static final StringsOption LOCATION_HINTS = new StringsOption("location-hints");
    /** load-xquery-module option content. */
    public static final StringOption CONTENT = new StringOption("content");
    /** load-xquery-module option context-item. */
    public static final ValueOption CONTEXT_ITEM = new ValueOption("context-item", SeqType.ITEM_ZO);
    /** load-xquery-module option variable. */
    public static final ValueOption VARIABLES = new ValueOption("variable", SeqType.MAP_O);
    /** load-xquery-module option vendor-options. */
    public static final ValueOption VENDOR_OPTIONS = new ValueOption("vendor-options",
        SeqType.MAP_O);
  }
}
