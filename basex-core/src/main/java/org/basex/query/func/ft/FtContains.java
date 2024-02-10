package org.basex.query.func.ft;

import static org.basex.util.ft.FTFlag.*;

import org.basex.query.*;
import org.basex.query.CompileContext.*;
import org.basex.query.expr.ft.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.util.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FtContains extends FtAccess {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Value input = arg(0).value(qc), terms = arg(1).value(qc);
    final FtContainsOptions options = toOptions(arg(2), new FtContainsOptions(), true, qc);

    final FTMode mode = options.get(FtIndexOptions.MODE);
    final FTOpt opt = ftOpt(options, qc).assign(qc.ftOpt());

    final FTDiacritics dc = options.get(FtContainsOptions.DIACRITICS);
    if(dc != null) opt.set(DC, dc == FTDiacritics.SENSITIVE);
    final Boolean st = options.get(FtContainsOptions.STEMMING);
    if(st != null) opt.set(ST, st);
    final String ln = options.get(FtContainsOptions.LANGUAGE);
    if(ln != null) opt.ln = Language.get(ln);
    final FTCase cs = options.get(FtContainsOptions.CASE);
    if(cs != null) opt.cs = cs;

    final FTWords ftw = new FTWords(info, terms, mode, null).ftOpt(opt).optimize(qc);
    return new FTContains(input, ftExpr(ftw, options), info).item(qc, info);
  }

  @Override
  protected void simplifyArgs(final CompileContext cc) throws QueryException {
    arg(0, arg -> arg.simplifyFor(Simplify.STRING, cc));
    arg(1, arg -> arg.simplifyFor(Simplify.STRING, cc));
  }
}
