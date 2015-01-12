package org.basex.query.func.ft;

import static org.basex.util.ft.FTFlag.*;

import org.basex.query.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.util.ft.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-15, BSD License
 * @author Christian Gruen
 */
public final class FtTokenize extends FtAccess {
  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    final byte[] token = toToken(exprs[0], qc);
    final FtTokenizeOptions opts = toOptions(1, Q_OPTIONS, new FtTokenizeOptions(), qc);

    final FTOpt opt = new FTOpt().copy(qc.ftOpt());
    final FTDiacritics dc = opts.get(FtTokenizeOptions.DIACRITICS);
    if(dc != null) opt.set(DC, dc == FTDiacritics.SENSITIVE);
    final Boolean st = opts.get(FtTokenizeOptions.STEMMING);
    if(st != null) opt.set(ST, st);
    final String ln = opts.get(FtTokenizeOptions.LANGUAGE);
    if(ln != null) opt.ln = Language.get(ln);
    final FTCase cs = opts.get(FtTokenizeOptions.CASE);
    if(cs != null) opt.cs = cs;

    final FTLexer ftl = new FTLexer(opt).init(token);
    return new Iter() {
      @Override
      public Str next() {
        return ftl.hasNext() ? Str.get(ftl.nextToken()) : null;
      }
    };
  }
}
