package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.func.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.util.collation.*;
import org.basex.query.util.list.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;

/**
 * Function implementation.
 *
 * @author BaseX Team 2005-22, BSD License
 * @author Christian Gruen
 */
public class FnLowest extends StandardFunc {
  @Override
  public Value value(final QueryContext qc) throws QueryException {
    return value(true, qc);
  }

  /**
   * Returns the lowest or highest results.
   * @param min compute minimum or maximum
   * @param qc query context
   * @return result
   * @throws QueryException query exception
   */
  Value value(final boolean min, final QueryContext qc) throws QueryException {
    final Iter input = exprs[0].iter(qc);
    final Collation coll = toCollation(1, true, qc);
    final FItem key = exprs.length > 2 ? toFunction(exprs[2], 1, qc) : null;

    final ItemList result = new ItemList();
    Value lowest = null;
    for(Item item; (item = qc.next(input)) != null;) {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item it : (key == null ? item : key.invoke(qc, info, item)).atomValue(qc, info)) {
        vb.add(it.type == AtomType.DOUBLE || !it.instanceOf(AtomType.UNTYPED_ATOMIC) ? it :
          Dbl.get(toDouble(it)));
      }
      final Value low = vb.value();
      int diff = FnSort.compare(lowest != null ? lowest : low, low, coll, info);
      if(min) diff = -diff;
      if(diff > 0) continue;
      if(diff < 0) result.reset();
      result.add(item);
      lowest = low;
    }
    return result.value(this);
  }

  @Override
  protected Expr opt(final CompileContext cc) throws QueryException {
    return opt(true, cc);
  }

  /**
   * Optimizes the function.
   * @param min compute minimum or maximum
   * @param cc compilation context
   * @return optimized or original expression
   * @throws QueryException query exception
   */
  final Expr opt(final boolean min, final CompileContext cc) throws QueryException {
    // optimize sort on sequences
    final Expr input = exprs[0];
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    if(exprs.length < 2) {
      if(st.zeroOrOne() && st.type.isSortable()) return input;

      // range values
      if(input instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) input;
        return (seq.asc ? seq : seq.reverse(null)).itemAt(min ? 0 : seq.size() - 1);
      }
      // sortable single or singleton values
      if(st.type.isSortable() && (st.one() || input instanceof SingletonSeq &&
          ((SingletonSeq) input).singleItem())) return input;

      if(REPLICATE.is(input) && ((FnReplicate) input).singleEval(false)) {
        final SeqType ast = input.arg(0).seqType();
        if(ast.zeroOrOne() && ast.type.isSortable()) return input;
      }
      if(REVERSE.is(input) || SORT.is(input)) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(min ? LOWEST : HIGHEST, info, args);
      }
    } else if(exprs.length == 3) {
      exprs[2] = coerceFunc(exprs[2], cc, SeqType.ANY_ATOMIC_TYPE_ZM, st.with(Occ.EXACTLY_ONE));
    }
    return adoptType(input);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && exprs.length > 2 || super.has(flags);
  }
}
