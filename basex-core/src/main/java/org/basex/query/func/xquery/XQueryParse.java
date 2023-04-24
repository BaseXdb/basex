package org.basex.query.func.xquery;

import static org.basex.query.QueryError.*;

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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public class XQueryParse extends StandardFunc {
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
    /** Pass on error info. */
    public static final BooleanOption PASS = new BooleanOption("pass", false);
    /** Query base-uri. */
    public static final StringOption BASE_URI = new StringOption("base-uri");
  }

  @Override
  public FNode item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final IO query = toContent(arg(0), qc);
    final XQueryOptions options = toOptions(arg(1), new XQueryOptions(), true, qc);

    // base-uri: choose uri specified in options, file path, or base-uri from parent query
    try(QueryContext qctx = new QueryContext(qc.context)) {
      final AModule module = qctx.parse(query.string(),
          toBaseUri(query.path(), options, XQueryOptions.BASE_URI));
      if(options.get(XQueryOptions.COMPILE)) qctx.compile();

      final FBuilder root;
      if(module instanceof MainModule) {
        root = FElem.build(Q_MAIN_MODULE).add(Q_UPDATING, qctx.updating);
      } else {
        final QNm name = module.sc.module;
        root = FElem.build(Q_LIBRARY_MODULE).add(Q_PREFIX, name.string()).add(Q_URI, name.uri());
      }
      if(options.get(XQueryOptions.PLAN)) root.add(qctx.toXml(false));
      return root.finish();
    } catch(final QueryException ex) {
      if(!options.get(XQueryOptions.PASS)) ex.info(info);
      throw ex;
    } catch(final IOException ex) {
      throw IOERR_X.get(info, ex);
    }
  }
}
