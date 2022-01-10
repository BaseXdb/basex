package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

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
 * @author BaseX Team 2005-22, BSD License
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
    final Expr expr1 = exprs[0], expr2 = exprs.length > 1 ? exprs[1] : Str.EMPTY;

    // string-join(util:chars(A))  ->  string(A)
    if(_UTIL_CHARS.is(expr1) && expr2 == Str.EMPTY) return cc.function(STRING, info, expr1.args());

    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();
    return (st1.zero() || st1.one() && st1.type.isStringOrUntyped()) &&
        st2.type.isStringOrUntyped() ? cc.function(Function.STRING, info, expr1) : this;
  }
}
