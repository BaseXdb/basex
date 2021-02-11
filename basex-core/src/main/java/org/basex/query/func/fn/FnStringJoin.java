package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnStringJoin extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter iter = exprs[0].atomIter(qc, info);
    final byte[] token = exprs.length == 2 ? toToken(exprs[1], qc) : Token.EMPTY;

    // no results: empty string
    Item item = iter.next();
    if(item == null) return Str.EMPTY;

    // single result
    final byte[] first = item.string(info);
    if((item = iter.next()) == null) return Str.get(first);

    // join multiple strings
    final TokenBuilder tb = new TokenBuilder().add(first);
    do {
      tb.add(token).add(item.string(info));
    } while((item = qc.next(iter)) != null);
    return Str.get(tb.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final SeqType st1 = exprs[0].seqType();
    final SeqType st2 = (exprs.length > 1 ? exprs[1] : Str.EMPTY).seqType();
    return (st1.zero() || st1.one() && st1.type.isStringOrUntyped()) &&
        st2.type.isStringOrUntyped() ? cc.function(Function.STRING, info, exprs[0]) : this;
  }
}
