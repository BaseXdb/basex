package org.basex.query.func.ft;

import static org.basex.util.ft.FTFlag.*;

import org.basex.query.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.util.ft.*;
import org.basex.util.list.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public class FtTokenize extends FtAccess {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return StrSeq.get(tokens(qc, false));
  }

  /**
   * Returns all tokens.
   * @param qc query context
   * @param all include separators
   * @return resulting tokens
   * @throws QueryException query exception
   */
  protected final TokenList tokens(final QueryContext qc, final boolean all) throws QueryException {
    final Item item = exprs[0].atomItem(qc, info);
    final FtTokenizeOptions opts = toOptions(1, new FtTokenizeOptions(), qc);

    final TokenList tl = new TokenList();
    if(item != Empty.VALUE) {
      final FTOpt opt = new FTOpt().assign(qc.ftOpt());
      final FTDiacritics dc = opts.get(FtTokenizeOptions.DIACRITICS);
      if(dc != null) opt.set(DC, dc == FTDiacritics.SENSITIVE);
      final Boolean st = opts.get(FtTokenizeOptions.STEMMING);
      if(st != null) opt.set(ST, st);
      final String ln = opts.get(FtTokenizeOptions.LANGUAGE);
      if(ln != null) opt.ln = Language.get(ln);
      final FTCase cs = opts.get(FtTokenizeOptions.CASE);
      if(cs != null) opt.cs = cs;

      final FTLexer lexer = new FTLexer(opt).init(toToken(item));
      if(all) lexer.all();
      while(lexer.hasNext()) tl.add(lexer.nextToken());
    }
    return tl;
  }
}
