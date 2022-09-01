package org.basex.query.func.util;

import static org.basex.query.func.Function.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.expr.List;
import org.basex.query.func.*;
import org.basex.query.func.file.*;
import org.basex.query.iter.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public final class UtilItem extends StandardFunc {
  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    final Expr input = exprs[0];
    final long pos = pos(qc);

    // retrieve (possibly invalid) position
    if(pos < 0) return Empty.VALUE;
    // if possible, retrieve single item
    if(input.seqType().zeroOrOne()) return pos == 0 ? input.item(qc, info) : Empty.VALUE;

    // fast route if the size is known
    final Iter iter = input.iter(qc);
    final long size = iter.size();
    if(size >= 0) return pos < size ? iter.get(pos) : Empty.VALUE;

    // iterate until specified item is found
    long p = pos;
    for(Item item; (item = qc.next(iter)) != null;) {
      if(p-- == 0) return item;
    }
    return Empty.VALUE;
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    final Expr input = exprs[0], pos = exprs[1];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    Occ occ = Occ.ZERO_OR_ONE;
    if(pos instanceof Value) {
      final long ps = pos(cc.qc);
      // negative position
      if(ps < 0) return Empty.VALUE;
      // single expression with static position
      if(st.zeroOrOne()) return ps == 0 ? input : Empty.VALUE;
      final long size = input.size();
      if(size != -1) {
        // util:item(E, last)  ->  util:last(E)
        if(ps + 1 == size) return cc.function(_UTIL_LAST, info, input);
        // util:item(E, too-large)  ->  ()
        if(ps + 1 > size) return Empty.VALUE;
        // util:item(reverse(E), pos)  ->  util:item(E, size - pos)
        if(REVERSE.is(input))
          return cc.function(_UTIL_ITEM, info, input.arg(0), Int.get(size - ps));
        occ = Occ.EXACTLY_ONE;
      }
      if(ps == 0) return cc.function(HEAD, info, input);

      // util:item(tail(E), pos)  ->  util:item(E, pos + 1)
      if(TAIL.is(input))
        return cc.function(_UTIL_ITEM, info, input.arg(0), Int.get(ps + 2));
      // util:item(replicate(I, count), pos)  ->  I
      if(REPLICATE.is(input)) {
        // static integer will always be greater than 1
        final Expr[] args = input.args();
        if(args[0].size() == 1 && args[1] instanceof Int) {
          final long count = ((Int) args[1]).itr();
          return ps > count ? Empty.VALUE : args[0];
        }
      }
      // util:item(file:read-text-lines(E), pos)  ->  file:read-text-lines(E, pos, 1)
      if(_FILE_READ_TEXT_LINES.is(input))
        return FileReadTextLines.opt(this, ps, 1, cc);

      // util:item((I1, I2, I3), 2)  ->  I2
      // util:item((I, E), 2)  ->  head(E)
      // util:item((I, E1, E2), 3)  ->  util:item((E1, E2), 2)
      if(input instanceof List) {
        final Expr[] args = input.args();
        final int al = args.length;
        for(int a = 0; a < al; a++) {
          final boolean exact = a == ps, one = args[a].seqType().one();
          if(exact || !one && a > 0) {
            if(exact && one) return args[a];
            final Expr list = List.get(cc, info, Arrays.copyOfRange(args, a, al));
            return exact ? cc.function(HEAD, info, list) :
              cc.function(_UTIL_ITEM, info, list, Int.get(ps - a + 1));
          }
          if(!one) break;
        }
      }
    }

    // util:item(util:init(E), pos)  ->  util:item(E, pos)
    if(_UTIL_INIT.is(input))
      return cc.function(_UTIL_ITEM, info, input.arg(0), pos);

    final long diff = countInputDiff(1);
    if(diff != Long.MIN_VALUE) {
      // util:item(E, count(E))  ->  util:last(E)
      if(diff == 0) return cc.function(_UTIL_LAST, info, input);
      // util:item(E, count(E) + 1)  ->  ()
      if(diff > 0) return Empty.VALUE;
    }

    exprType.assign(st.with(occ)).data(input);
    return embed(cc, false);
  }

  /**
   * Returns the item position (starting with 0).
   * @param qc query context
   * @return position or {@code -1}
   * @throws QueryException query exception
   */
  private long pos(final QueryContext qc) throws QueryException {
    final double dp = toDouble(exprs[1], qc);
    final long pos = (long) dp;
    return dp != pos ? -1 : pos - 1;
  }
}
