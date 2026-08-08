package org.basex.query.func.ft;

import static org.basex.util.ft.FTFlag.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FtContains extends FtAccessFn {
  @Override
  protected Bln item(final QueryContext qc) throws QueryException {
    final Value input = arg(0).value(qc), terms = arg(1).value(qc);
    final FtContainsOptions options = toOptions(arg(2), new FtContainsOptions(), qc);

    final FTMode mode = options.get(FtIndexOptions.MODE);
    final FTOpt opt = ftOpt(options, qc.ftOpt(), qc);

    final FTDiacritics dc = options.get(FtContainsOptions.DIACRITICS);
    if(dc != null) opt.set(DC, dc == FTDiacritics.SENSITIVE);
    final Boolean st = options.get(FtContainsOptions.STEMMING);
    if(st != null) opt.set(ST, st);
    final String ln = options.get(FtContainsOptions.LANGUAGE);
    if(ln != null) opt.ln = Language.get(ln);
    final FTCase cs = options.get(FtContainsOptions.CASE);
    if(cs != null) opt.cs = cs;

    final FTTimesOptions times = options.get(FtContainsOptions.OCCURS);
    final Expr[] occ = times == null ? null : new Expr[] {
      Itr.get(times.get(FTTimesOptions.MIN)), Itr.get(times.get(FTTimesOptions.MAX)) };

    final FTWords ftw = new FTWords(info, terms, mode, occ).ftOpt(opt).optimize(qc);
    return new FTContains(input, ftExpr(ftw, options), info).item(qc);
  }
}
