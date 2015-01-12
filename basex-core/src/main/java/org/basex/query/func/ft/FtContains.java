package org.basex.query.func.ft;

import static org.basex.query.QueryError.*;
import static org.basex.util.ft.FTFlag.*;

import org.basex.query.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FtContains extends FtAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value input = qc.value(exprs[0]);
    final Value query = qc.value(exprs[1]);
    final FtContainsOptions opts = toOptions(2, Q_OPTIONS, new FtContainsOptions(), qc);

    final FTOpt tmp = qc.ftOpt();
    final FTOpt opt = new FTOpt().copy(tmp);
    final FTMode mode = opts.get(FtIndexOptions.MODE);
    opt.set(FZ, opts.get(FtIndexOptions.FUZZY));
    opt.set(WC, opts.get(FtIndexOptions.WILDCARDS));
    if(opt.is(FZ) && opt.is(WC)) throw BXFT_MATCH.get(info, this);

    final FTDiacritics dc = opts.get(FtContainsOptions.DIACRITICS);
    if(dc != null) opt.set(DC, dc == FTDiacritics.SENSITIVE);
    final Boolean st = opts.get(FtContainsOptions.STEMMING);
    if(st != null) opt.set(ST, st);
    final String ln = opts.get(FtContainsOptions.LANGUAGE);
    if(ln != null) opt.ln = Language.get(ln);
    final FTCase cs = opts.get(FtContainsOptions.CASE);
    if(cs != null) opt.cs = cs;

    qc.ftOpt(opt);
    final FTExpr fte;
    try {
      fte = new FTWords(info, query, mode, null).compile(qc, null);
    } finally {
      qc.ftOpt(tmp);
    }
    return new FTContains(input, options(fte, opts), info).item(qc, info);
  }
}
