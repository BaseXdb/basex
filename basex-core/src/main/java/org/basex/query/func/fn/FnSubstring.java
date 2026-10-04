package org.basex.query.func.fn;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;

/**
 * Function implementation.
 *
 * @author BaseX Team, BSD License
 * @author Christian Gruen
 */
public final class FnSubstring extends StandardFunc {
  @Override
  public AStr value(final QueryContext qc) throws QueryException {
    final AStr value = toZeroStr(arg(0), qc);
    final ANum start = start(qc), length = length(qc);

    final int size = value.length(info);
    final int s = index(start, size);
    final int e = length == null ? size : index(FnSubsequence.add(start, length, info), size);
    return s != -1 && s < e ? value.substring(info, s, e) : Str.EMPTY;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    // empty argument: return empty string
    final Expr value = arg(0), len = arg(2);
    if(value == Empty.VALUE || value == Str.EMPTY) return Str.EMPTY;

    if(arg(1) instanceof Value) {
      // invalid start offset: return empty string
      final double start = start(cc.qc).dbl();
      if(Double.isNaN(start)) return Str.EMPTY;

      // substring($string, $start, string-length($string)) → substring($string, $start)
      if(start >= 1 && Function.STRING_LENGTH.is(len) && len.args().length > 0 &&
          len.arg(0).equals(value) && !value.has(Flag.NDT)) {
        return cc.function(Function.SUBSTRING, info, value, arg(1));
      }
      // substring($string, 1) → string($string)
      if(start <= 1 && !defined(2) && value.seqType().type.isStringOrUntyped()) {
        return cc.function(Function.STRING, info, value);
      }
    }
    // zero, negative or invalid length: return empty string
    if(len instanceof Value) {
      final ANum length = length(cc.qc);
      if(length != null && !(length.dbl() > 0)) return Str.EMPTY;
    }
    return this;
  }

  /**
   * Evaluates and rounds the start argument.
   * @param qc query context
   * @return start position
   * @throws QueryException query exception
   */
  private ANum start(final QueryContext qc) throws QueryException {
    return round(toNumber(toAtomItem(arg(1), qc), arg(1)));
  }

  /**
   * Evaluates and rounds the length argument.
   * @param qc query context
   * @return length (can be {@code null})
   * @throws QueryException query exception
   */
  private ANum length(final QueryContext qc) throws QueryException {
    final Item length = arg(2).atomItem(qc, info);
    return length.isEmpty() ? null : round(toNumber(length, arg(2)));
  }

  /**
   * Rounds a number to an integer.
   * @param num number
   * @return rounded number
   */
  private static ANum round(final ANum num) {
    return num.round(0, FnRound.RoundMode.HALF_TO_CEILING);
  }

  /**
   * Converts a one-based position to a string index.
   * @param pos rounded position
   * @param size string length
   * @return index between {@code 0} and {@code size}, or {@code -1} for NaN
   * @throws QueryException query exception
   */
  private int index(final ANum pos, final int size) throws QueryException {
    final long index = FnSubsequence.index(pos, info);
    return index == -1 ? -1 : (int) Math.min(index, size);
  }
}
