package org.basex.query.func.xquery;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.func.*;
import org.basex.query.scope.*;
import org.basex.query.value.node.*;
import org.basex.util.*;
import org.basex.util.options.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-16, BSD License
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

    boolean compile = false, plan = true, pass = false;
    if(exprs.length > 1) {
      final Options opts = toOptions(1, new XQueryOptions(), qc);
      compile = opts.get(XQueryOptions.COMPILE);
      plan = opts.get(XQueryOptions.PLAN);
      pass = opts.get(XQueryOptions.PASS);
    }

    try(QueryContext qctx = new QueryContext(qc.context)) {
      final Module mod = qctx.parse(string(query), path, null);
      final boolean library = mod instanceof LibraryModule;

      final FElem root;
      if(library) {
        root = new FElem(LIBRARY_MODULE);
        final LibraryModule lib = (LibraryModule) mod;
        root.add(PREFIX, lib.name.string());
        root.add(URI, lib.name.uri());
      } else {
        root = new FElem(MAIN_MODULE);
        root.add(UPDATING, token(qctx.updating));
      }

      if(compile) qctx.compile();
      if(plan) root.add(qctx.plan());
      return root;
    } catch(final QueryException ex) {
      if(!pass) ex.info(info);
      throw ex;
    }
  }
}
