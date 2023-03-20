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
 * @author BaseX Team 2005-23, BSD License
 * @author Christian Gruen
 */
public final class FnStringJoin extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Iter values = arg(0).atomIter(qc, info);
    final byte[] separator = defined(1) ? toToken(arg(1), qc) : Token.EMPTY;

    // no results: empty string
    Item item = values.next();
    if(item == null) return Str.EMPTY;

    // single result
    final byte[] first = item.string(info);
    if((item = values.next()) == null) return Str.get(first);

    // join multiple strings
    final TokenBuilder tb = new TokenBuilder().add(first);
    do {
      tb.add(separator).add(item.string(info));
    } while((item = qc.next(values)) != null);
    return Str.get(tb.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr values = arg(0), separator = defined(1) ? arg(1) : Str.EMPTY;

    // string-join(characters(A))  ->  string(A)
    if(CHARACTERS.is(values) && separator == Str.EMPTY)
      return cc.function(STRING, info, values.args());

    final SeqType st = values.seqType(), stSep = separator.seqType();
    return (st.zero() || st.one() && st.type.isStringOrUntyped()) &&
        stSep.type.isStringOrUntyped() ? cc.function(Function.STRING, info, values) : this;
  }
}
