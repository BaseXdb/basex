package org.basex.query.func.ft;

import static org.basex.query.util.Err.*;
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
 * @author BaseX Team 2005-14, BSD License
 * @author Christian Gruen
 */
public final class FtContains extends FtAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value input = qc.value(exprs[0]);
    final Value query = qc.value(exprs[1]);
    final FTOptions opts = toOptions(2, Q_OPTIONS, new FTOptions(), qc);

    final FTOpt opt = new FTOpt();
    final FTMode mode = opts.get(FTIndexOptions.MODE);
    opt.set(FZ, opts.get(FTIndexOptions.FUZZY));
    opt.set(WC, opts.get(FTIndexOptions.WILDCARDS));
    opt.set(DC, opts.get(FTOptions.DIACRITICS) == FTDiacritics.SENSITIVE);
    opt.set(ST, opts.get(FTOptions.STEMMING));
    opt.ln = Language.get(opts.get(FTOptions.LANGUAGE));
    opt.cs = opts.get(FTOptions.CASE);
    if(opt.is(FZ) && opt.is(WC)) throw BXFT_MATCH.get(info, this);

    final FTOpt tmp = qc.ftOpt();
    qc.ftOpt(opt);
    final FTExpr fte = new FTWords(info, query, mode, null).compile(qc, null);
    qc.ftOpt(tmp);
    return new FTContains(input, options(fte, opts), info).item(qc, info);
  }
}
