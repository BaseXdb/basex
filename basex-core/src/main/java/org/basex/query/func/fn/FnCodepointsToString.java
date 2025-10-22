package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;
import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnCodepointsToString extends StandardFunc {
  /** Indicates that the input will always be a single integer. */
  private boolean singleInt;

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr values = arg(0);

    // input is single integer
    if(singleInt) return Str.get(toCodepoint(values.item(qc, info).itr(info), info));

    // current input is single item
    final Iter iter = values.atomIter(qc, info);
    final long size = iter.size();
    if(size == 1) return Str.get(toCodepoint(toLong(iter.next()), info));

    // handle arbitrary input
    final TokenBuilder tb = new TokenBuilder(Seq.initialCapacity(size));
    for(Item item; (item = qc.next(iter)) != null;) {
      tb.add(toCodepoint(toLong(item), info));
    }
    return Str.get(tb.finish());
  }

  @Override
  public boolean test(final QueryContext qc, final InputInfo ii, final long pos)
      throws QueryException {
    if(!singleInt) {
      final Item item = arg(0).atomIter(qc, info).next();
      if(item == null) return false;
      toLong(item);
    }
    return true;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr values = arg(0);

    // codepoints-to-string(string-to-codepoints(A))  ->  string(A)
    if(STRING_TO_CODEPOINTS.is(values)) return cc.function(STRING, info, values.args());

    singleInt = values.seqType().instanceOf(Types.INTEGER_O);
    return this;
  }

  @Override
  protected boolean values(final boolean limit, final CompileContext cc) {
    return super.values(true, cc);
  }

  /**
   * Checks if the specified value is valid codepoint.
   * @param value codepoint
   * @param info input info (can be {@code null})
   * @return codepoint as integer
   * @throws QueryException query exception
   */
  private static int toCodepoint(final long value, final InputInfo info) throws QueryException {
    if(value >= 0 && value <= Integer.MAX_VALUE) {
      final int cp = (int) value;
      if(XMLToken.valid(cp)) return cp;
    }
    throw INVCODE_X.get(info, "&#x" + Long.toHexString(value) + ';');
  }
}
