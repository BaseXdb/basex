package org.basex.query.func.xquery;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.value.item.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class XQueryParse extends StandardFunc {
  /** Token. */
  private static final byte[] LIBRARY_MODULE = token("LibraryModule");
  /** Token. */
  private static final byte[] MAIN_MODULE = token("MainModule");
  /** Token. */
  private static final byte[] UPDATING = token("updating");
  /** Token. */
  private static final byte[] PREFIX = token("prefix");
  /** Token. */
  private static final byte[] URI = token("uri");

  /** XQuery options. */
  public static class XQueryOptions extends Options {
    /** Return plan. */
    public static final BooleanOption PLAN = new BooleanOption("plan", true);
    /** Compile query. */
    public static final BooleanOption COMPILE = new BooleanOption("compile", false);
    /** Pass on error info. */
    public static final BooleanOption PASS = new BooleanOption("pass", false);
    /** Query base-uri. */
    public static final StringOption BASE_URI = new StringOption("base-uri");
  }

  @Override
  public FElem item(final QueryContext qc, final InputInfo ii) throws QueryException {
    return parse(qc, toToken(exprs[0], qc), null);
  }

  /**
   * Parses the specified query and returns the resulting query plan.
   * @param qc query context
   * @param query query
   * @param path file path (may be {@code null})
   * @return query plan
   * @throws QueryException query exception
   */
  protected final FElem parse(final QueryContext qc, final byte[] query, final String path)
      throws QueryException {

    final XQueryOptions opts = toOptions(1, new XQueryOptions(), qc);

    // base-uri: choose uri specified in options, file path, or base-uri from parent query
    try(QueryContext qctx = new QueryContext(qc.context)) {
      final AModule mod = qctx.parse(string(query), toBaseUri(path, opts));
      final FElem root;
      if(mod instanceof LibraryModule) {
        final QNm module = mod.sc.module;
        root = new FElem(LIBRARY_MODULE);
        root.add(PREFIX, module.string());
        root.add(URI, module.uri());
      } else {
        root = new FElem(MAIN_MODULE);
        root.add(UPDATING, token(qctx.updating));
      }

      if(opts.get(XQueryOptions.COMPILE)) qctx.compile();
      if(opts.get(XQueryOptions.PLAN)) root.add(qctx.plan(false));
      return root;
    } catch(final QueryException ex) {
      if(!opts.get(XQueryOptions.PASS)) ex.info(info);
      throw ex;
    }
  }
}
