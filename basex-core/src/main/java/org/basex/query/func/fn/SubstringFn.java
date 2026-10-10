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

/**
 * Substring function.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public abstract class SubstringFn extends StandardFunc {
  @Override
  public final Str value(final QueryContext qc) throws QueryException {
    final byte[] value = toZeroToken(arg(0), qc);
    final byte[] substring = toZeroToken(arg(1), qc);
    final Collation collation = toCollation(arg(2), qc);

    final boolean before = before(), last = last();
    if(value.length == 0) return Str.EMPTY;
    if(substring.length == 0) return before == last ? Str.get(value) : Str.EMPTY;

    if(collation == null) {
      final int pos = last ? lastIndexOf(value, substring) : indexOf(value, substring);
      return pos == -1 ? Str.EMPTY : Str.get(before ? substring(value, 0, pos) :
        substring(value, pos + substring.length));
    }
    return Str.get(collation.extract(value, substring, before, last, info));
  }

  @Override
  protected final Expr opt(final CompileContext cc) throws QueryException {
    final Expr value = arg(0), substring = arg(1);
    final SeqType st = value.seqType(), stSub = substring.seqType();

    if((st.zero() || st.one() && st.type.isStringOrUntyped()) &&
       (stSub.zero() || stSub.one() && stSub.type.isStringOrUntyped()) && !defined(2)) {
      // substring-before('', $b) → '', substring-before($a, $a) → ''
      if(value == Empty.VALUE || value == Str.EMPTY || value.equals(substring))
        return Str.EMPTY;
      // substring-after($a, '') → string($a), substring-before($a, '') → ''
      if(substring == Empty.VALUE || substring == Str.EMPTY)
        return before() == last() ? cc.function(STRING, info, value) : Str.EMPTY;
    }
    return this;
  }

  /**
   * Indicates if the substring before the match is returned.
   * @return result of check
   */
  abstract boolean before();

  /**
   * Indicates if the last match is searched for.
   * @return result of check
   */
  abstract boolean last();
}
