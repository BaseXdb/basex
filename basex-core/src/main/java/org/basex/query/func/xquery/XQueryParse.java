package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;
import static org.basex.util.Token.*;

import java.io.*;

import org.basex.io.*;
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
 * @author BaseX Team 2005-22, BSD License
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
    final IO query = toContent(0, qc);
    final XQueryOptions opts = toOptions(1, new XQueryOptions(), qc);

    // base-uri: choose uri specified in options, file path, or base-uri from parent query
    try(QueryContext qctx = new QueryContext(qc.context)) {
      final AModule module = qctx.parse(query.string(),
          toBaseUri(query.path(), opts, XQueryOptions.BASE_URI));
      if(opts.get(XQueryOptions.COMPILE)) qctx.compile();

      final FElem root;
      if(module instanceof MainModule) {
        root = new FElem(MAIN_MODULE).add(UPDATING, token(qctx.updating));
      } else {
        final QNm name = module.sc.module;
        root = new FElem(LIBRARY_MODULE).add(PREFIX, name.string()).add(URI, name.uri());
      }
      if(opts.get(XQueryOptions.PLAN)) root.add(qctx.toXml(false));
      return root;
    } catch(final QueryException ex) {
      if(!opts.get(XQueryOptions.PASS)) ex.info(info);
      throw ex;
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
