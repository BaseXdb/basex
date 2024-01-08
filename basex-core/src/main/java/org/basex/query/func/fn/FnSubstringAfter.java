package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;
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
 * @author BaseX Team 2005-24, BSD License
 * @author Christian Gruen
 */
public final class FnSubstringAfter extends StandardFunc {
  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] substring = toZeroToken(arg(1), qc);
    final Collation collation = toCollation(arg(2), qc);

    if(value.length == 0) return Str.EMPTY;
    if(substring.length == 0) return Str.get(value);

    if(collation == null) {
      final int pos = indexOf(value, substring);
      return pos == -1 ? Str.EMPTY : Str.get(substring(value, pos + substring.length));
    }
    return Str.get(collation.after(value, substring, info));
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0), substring = arg(1);
    final SeqType st = value.seqType(), stSub = substring.seqType();

    if((st.zero() || st.one() && st.type.isStringOrUntyped()) &&
       (stSub.zero() || stSub.one() && stSub.type.isStringOrUntyped()) && !defined(2)) {
      if(value == Empty.VALUE || value == Str.EMPTY || value.equals(substring))
        return Str.EMPTY;
      if(substring == Empty.VALUE || substring == Str.EMPTY)
        return cc.function(STRING, info, value);
    }
    return this;
  }
}
