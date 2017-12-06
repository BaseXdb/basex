package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-17, BSD License
 * @author Christian Gruen
 */
public final class FnCompare extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Item item1 = exprs[0].atomItem(qc, info), item2 = exprs[1].atomItem(qc, info);
    final Collation coll = toCollation(2, qc);
    if(item1 == null || item2 == null) return null;
    final byte[] token1 = toToken(item1), token2 = toToken(item2);
    final long diff = coll == null ? diff(token1, token2) : coll.compare(token1, token2);
    return Int.get(diff < 0 ? -1 : diff > 0 ? 1 : 0);
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    if(st1.zero()) return expr1;
    if(st2.zero()) return expr2;
    if(st1.oneNoArray() && st2.oneNoArray()) exprType.assign(Occ.ONE);
    return this;
  }
}
