package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

import java.io.*;

import org.basex.core.*;
import org.basex.io.*;
import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.map.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryParse extends StandardFunc {
  /** QName. */
  private static final QNm Q_LIBRARY_MODULE = new QNm("LibraryModule");
  /** QName. */
  private static final QNm Q_MAIN_MODULE = new QNm("MainModule");
  /** QName. */
  private static final QNm Q_UPDATING = new QNm("updating");
  /** QName. */
  private static final QNm Q_PREFIX = new QNm("prefix");
  /** QName. */
  private static final QNm Q_URI = new QNm("uri");

  /** XQuery options. */
  public static class XQueryOptions extends Options {
    /** Return plan. */
    public static final BooleanOption PLAN = new BooleanOption("plan", true);
    /** Compile query. */
    public static final BooleanOption COMPILE = new BooleanOption("compile", false);
    /** Optimize query. */
    public static final BooleanOption OPTIMIZE = new BooleanOption("optimize", false);
    /** Pass on error info. */
    public static final BooleanOption PASS = new BooleanOption("pass", false);
    /** Query base-uri. */
    public static final StringOption BASE_URI = new StringOption(CommonOptions.BASE_URI);
  }

  @Override
  public FNode value(final QueryContext qc) throws QueryException {
    final IO query = toContent(arg(0), qc);
    final XQueryOptions options = toOptions(arg(1), new XQueryOptions(), qc);

    // base-uri: choose URI specified in options, file path, or base-uri from parent query
    // optimization opens databases: share the resources and locks of the parent query
    final boolean optimize = options.get(XQueryOptions.OPTIMIZE);
    try(QueryContext qctx = optimize ? new QueryContext(qc, null) : new QueryContext(qc.context)) {
      final AModule module = qctx.parse(query.readString(),
          toBaseUri(query.path(), options, XQueryOptions.BASE_URI));
      // library modules have no main expression to optimize
      if(optimize && module instanceof MainModule) qctx.optimize();
      else if(optimize || options.get(XQueryOptions.COMPILE)) qctx.compile();

      final FBuilder root;
      if(module instanceof MainModule) {
        root = FElem.build(Q_MAIN_MODULE).attr(Q_UPDATING, qctx.updating);
      } else {
        final QNm name = module.sc.module;
        root = FElem.build(Q_LIBRARY_MODULE).attr(Q_PREFIX, name.string()).attr(Q_URI, name.uri());
      }
      if(options.get(XQueryOptions.PLAN)) root.node(qctx.toXml(false));
      return root.finish();
    } catch(final QueryException ex) {
      if(!options.get(XQueryOptions.PASS)) ex.info(info);
      throw ex;
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }

  @Override
  public boolean accept(final ASTVisitor visitor) {
    // databases opened by an optimized query cannot be detected statically
    return (!optimize() || visitor.lock((String) null, false)) && super.accept(visitor);
  }

  /**
   * Checks if the query may be optimized.
   * @return result of check
   */
  private boolean optimize() {
    if(!defined(1)) return false;
    final Expr options = arg(1) instanceof final JNode jnode ? jnode.value : arg(1);
    if(!(options instanceof final XQMap map)) return true;
    try {
      final Value value = map.get(Str.get(XQueryOptions.OPTIMIZE.name()));
      return !value.isEmpty() && value != Bln.FALSE;
    } catch(final QueryException ex) {
      Util.debug(ex);
      return true;
    }
  }
}
