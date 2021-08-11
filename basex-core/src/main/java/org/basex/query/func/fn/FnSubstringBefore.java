package org.basex.query.func.fn;

import static org.basex.util.Token.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.collation.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringBefore extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] string = toZeroToken(exprs[0], qc), sub = toZeroToken(exprs[1], qc);
    final Collation coll = toCollation(2, qc);
    if(string.length == 0 || sub.length == 0) return Str.EMPTY;

    if(coll == null) {
      final int pos = indexOf(string, sub);
      return pos == -1 ? Str.EMPTY : Str.get(substring(string, 0, pos));
    }
    return Str.get(coll.before(string, sub, info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr expr1 = exprs[0], expr2 = exprs[1];
    final SeqType st1 = expr1.seqType(), st2 = expr2.seqType();

    if((st1.zero() || st1.one() && st1.type.isStringOrUntyped()) &&
       (st2.zero() || st2.one() && st2.type.isStringOrUntyped()) && exprs.length < 3) {
      if(expr1 == Empty.VALUE || expr1 == Str.EMPTY || expr2 == Empty.VALUE || expr2 == Str.EMPTY ||
          expr1.equals(expr2)) return Str.EMPTY;
    }
    return this;
  }
}
