package org.basex.query.func.ft;

import static org.basex.query.QueryError.*;
import static org.basex.util.ft.FTFlag.*;

import org.basex.core.*;
import org.basex.query.*;
import org.basex.query.expr.ft.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
abstract class FtAccess extends StandardFunc {
  /**
   * Parses and returns full-text options.
   * @param opts options specified in the query
   * @param qc query context
   * @return options
   * @throws QueryException query exception
   */
  final FTOpt ftOpt(final FtIndexOptions opts, final QueryContext qc) throws QueryException {
    final FTOpt opt = new FTOpt();
    opt.set(FZ, opts.get(FtIndexOptions.FUZZY));
    opt.set(WC, opts.get(FtIndexOptions.WILDCARDS));
    if(opt.is(FZ) && opt.is(WC)) throw FT_OPTIONS.get(info, this);
    opt.errors = opts.contains(FtIndexOptions.ERRORS) ? opts.get(FtIndexOptions.ERRORS) :
      qc.context.options.get(MainOptions.LSERROR);
    return opt;
  }

  /**
   * Parses full-text options and returns a full-text expression.
   * @param expr full-text expression
   * @param opts options specified in the query
   * @return expression
   */
  final FTExpr ftExpr(final FTExpr expr, final FtIndexOptions opts) {
    FTExpr ex = expr;
    if(opts != null) {
      if(opts.get(FtIndexOptions.ORDERED)) {
        ex = new FTOrder(info, ex);
      }
      if(opts.contains(FtIndexOptions.DISTANCE)) {
        final FTDistanceOptions fopts = opts.get(FtIndexOptions.DISTANCE);
        final Int min = Int.get(fopts.get(FTDistanceOptions.MIN));
        final Int max = Int.get(fopts.get(FTDistanceOptions.MAX));
        final FTUnit unit = fopts.get(FTDistanceOptions.UNIT);
        ex = new FTDistance(info, ex, min, max, unit);
      }
      if(opts.contains(FtIndexOptions.WINDOW)) {
        final FTWindowOptions fopts = opts.get(FtIndexOptions.WINDOW);
        final Int size = Int.get(fopts.get(FTWindowOptions.SIZE));
        final FTUnit unit = fopts.get(FTWindowOptions.UNIT);
        ex = new FTWindow(info, ex, size, unit);
      }
      if(opts.contains(FtIndexOptions.SCOPE)) {
        final FTScopeOptions fopts = opts.get(FtIndexOptions.SCOPE);
        final boolean same = fopts.get(FTScopeOptions.SAME);
        final FTUnit unit = fopts.get(FTScopeOptions.UNIT).unit();
        ex = new FTScope(info, ex, same, unit);
      }
      if(opts.contains(FtIndexOptions.CONTENT)) {
        final FTContents content = opts.get(FtIndexOptions.CONTENT);
        ex = new FTContent(info, ex, content);
      }
    }
    return ex;
  }
}
