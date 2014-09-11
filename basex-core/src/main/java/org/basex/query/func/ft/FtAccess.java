package org.basex.query.func.ft;

import org.basex.query.expr.ft.*;
import org.basex.query.func.*;
import org.basex.query.value.item.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
abstract class FtAccess extends StandardFunc {
  /** Element: options. */
  static final QNm Q_OPTIONS = QNm.get("options");

  /**
   * Parses fulltext options.
   * @param ftexpr full-text expression
   * @param opts full-text options
   * @return expressions
   */
  final FTExpr options(final FTExpr ftexpr, final FTOptions opts) {
    FTExpr fte = ftexpr;
    if(opts != null) {
      if(opts.get(FTIndexOptions.ORDERED)) {
        fte = new FTOrder(info, fte);
      }
      if(opts.contains(FTIndexOptions.DISTANCE)) {
        final FTDistanceOptions fopts = opts.get(FTIndexOptions.DISTANCE);
        final Int min = Int.get(fopts.get(FTDistanceOptions.MIN));
        final Int max = Int.get(fopts.get(FTDistanceOptions.MAX));
        final FTUnit unit = fopts.get(FTDistanceOptions.UNIT);
        fte = new FTDistance(info, fte, min, max, unit);
      }
      if(opts.contains(FTIndexOptions.WINDOW)) {
        final FTWindowOptions fopts = opts.get(FTIndexOptions.WINDOW);
        final Int sz = Int.get(fopts.get(FTWindowOptions.SIZE));
        final FTUnit unit = fopts.get(FTWindowOptions.UNIT);
        fte = new FTWindow(info, fte, sz, unit);
      }
      if(opts.contains(FTIndexOptions.SCOPE)) {
        final FTScopeOptions fopts = opts.get(FTIndexOptions.SCOPE);
        final boolean same = fopts.get(FTScopeOptions.SAME);
        final FTUnit unit = fopts.get(FTScopeOptions.UNIT).unit();
        fte = new FTScope(info, fte, same, unit);
      }
      if(opts.contains(FTIndexOptions.CONTENT)) {
        final FTContents cont = opts.get(FTIndexOptions.CONTENT);
        fte = new FTContent(info, fte, cont);
      }
    }
    return fte;
  }
}
