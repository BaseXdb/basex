package org.basex.query.func.fn;

import static org.basex.query.QueryError.*;

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
 * @author BaseX Team 2005-21, BSD License
 * @author Christian Gruen
 */
public final class FnCodepointsToString extends StandardFunc {
  /** Indicates that the input will always be a single integer. */
  private boolean singleInt;

  @Override
  public Str item(final QueryContext qc, final InputInfo ii) throws QueryException {
    // input is single integer
    if(singleInt) return toStr(exprs[0].item(qc, info).itr(info), info);

    // current input is single item
    final Iter iter = exprs[0].atomIter(qc, info);
    final long size = iter.size();
    if(size == 1) return toStr(toLong(iter.next()), info);

    // handle arbitrary input
    final TokenBuilder tb = new TokenBuilder(Seq.initialCapacity(size));
    for(Item item; (item = qc.next(iter)) != null;) {
      tb.add(checkCp(toLong(item), info));
    }
    return Str.get(tb.finish());
  }

  @Override
  protected Expr opt(final CompileContext cc) {
    singleInt = exprs[0].seqType().instanceOf(SeqType.INTEGER_O);
    return this;
  }

  /**
   * Converts a single codepoint to a string.
   * @param value value
   * @param ii input info
   * @return codepoint as string
   * @throws QueryException query exception
   */
  private static Str toStr(final long value, final InputInfo ii) throws QueryException {
    return Str.get(Token.cpToken(checkCp(value, ii)));
  }

  /**
   * Checks if the specified value is valid codepoint.
   * @param value codepoint
   * @param ii input info
   * @return codepoint as integer
   * @throws QueryException query exception
   */
  private static int checkCp(final long value, final InputInfo ii) throws QueryException {
    if(value >= 0 && value <= Integer.MAX_VALUE) {
      final int cp = (int) value;
      if(XMLToken.valid(cp)) return cp;
    }
    throw INVCODE_X.get(ii, Long.toHexString(value));
  }
}
