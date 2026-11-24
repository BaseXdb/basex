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
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class XQueryParse extends StandardFunc {
  /** QName. */
  public static final QNm Q_LIBRARY_MODULE = new QNm("LibraryModule");
  /** QName. */
  public static final QNm Q_MAIN_MODULE = new QNm("MainModule");
  /** QName. */
  private static final QNm Q_UPDATING = new QNm("updating");
  /** QName. */
  private static final QNm Q_PREFIX = new QNm("prefix");
  /** QName. */
  private static final QNm Q_URI = new QNm("uri");

  /** Options for parsing queries. */
  public static class XQueryParseOptions extends Options {
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
    final XQueryParseOptions options = toOptions(qc);

    // base-uri: choose URI specified in options, file path, or base-uri from parent query
    // optimization opens databases: share the resources and locks of the parent query
    final boolean optimize = options.get(XQueryParseOptions.OPTIMIZE);
    try(QueryContext qctx = optimize ? new QueryContext(qc, null) : new QueryContext(qc.context)) {
      // compilation may evaluate code: adopt the permissions and options of the calling code
      if(!optimize) qctx.optionsState(qc.optionsState());
      qctx.user = qc.user;
      qctx.maxPerm = perm(qc);
      qctx.modules.resolver = qc.modules.resolver;
      final AModule module = qctx.parse(query.readString(), null,
          toStaticContext(qctx, query.path(), options, XQueryParseOptions.BASE_URI));
      // library modules have no main expression to optimize
      if(optimize && module instanceof MainModule) qctx.optimize();
      else if(optimize || options.get(XQueryParseOptions.COMPILE)) qctx.compile();

      final FBuilder root;
      if(module instanceof MainModule) {
        root = FElem.build(Q_MAIN_MODULE).attr(Q_UPDATING, qctx.updating);
      } else {
        final QNm name = module.sc.module;
        root = FElem.build(Q_LIBRARY_MODULE).attr(Q_PREFIX, name.string()).attr(Q_URI, name.uri());
      }
      if(options.get(XQueryParseOptions.PLAN)) root.node(qctx.toXml(false));
      return root.finish();
    } catch(final QueryException ex) {
      if(!options.get(XQueryParseOptions.PASS)) ex.info(info);
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
    final Value value = map.value(Str.get(XQueryParseOptions.OPTIMIZE.name()));
    return value != null && !value.isEmpty() && value != Bln.FALSE;
  }
}
