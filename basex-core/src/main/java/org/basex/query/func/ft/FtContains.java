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
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FtContains extends FtAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value input = exprs[0].value(qc), query = exprs[1].value(qc);
    final FtContainsOptions opts = toOptions(2, new FtContainsOptions(), qc);

    final FTOpt opt = new FTOpt().assign(qc.ftOpt());
    final FTMode mode = opts.get(FtIndexOptions.MODE);
    opt.set(FZ, opts.get(FtIndexOptions.FUZZY));
    opt.set(WC, opts.get(FtIndexOptions.WILDCARDS));
    if(opt.is(FZ) && opt.is(WC)) throw FT_OPTIONS.get(info, this);

    final FTDiacritics dc = opts.get(FtContainsOptions.DIACRITICS);
    if(dc != null) opt.set(DC, dc == FTDiacritics.SENSITIVE);
    final Boolean st = opts.get(FtContainsOptions.STEMMING);
    if(st != null) opt.set(ST, st);
    final String ln = opts.get(FtContainsOptions.LANGUAGE);
    if(ln != null) opt.ln = Language.get(ln);
    final FTCase cs = opts.get(FtContainsOptions.CASE);
    if(cs != null) opt.cs = cs;

    final FTWords ftw = new FTWords(info, query, mode, null).init(qc, opt);
    return new FTContains(input, options(ftw, opts), info).item(qc, info);
  }
}
