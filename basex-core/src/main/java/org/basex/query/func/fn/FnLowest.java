package org.basex.query.func.fn;

import static org.basex.query.func.Function.*;

import java.util.function.*;

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
 * @author BaseX Team 2005-23, BSD License
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
    final Iter input = arg(0).iter(qc);
    final Collation coll = toCollation(arg(1), true, qc);
    final FItem key = defined(2) ? toFunction(arg(2), 1, qc) : null;

    final ItemList result = new ItemList();
    Value lowest = null;
    for(Item item; (item = input.next()) != null;) {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item it : (key == null ? item : key.invoke(qc, info, item)).atomValue(qc, info)) {
        vb.add(it.type.isUntyped() ? Dbl.get(toDouble(it)) : it);
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
    final Expr input = arg(0);
    final SeqType st = input.seqType();
    if(st.zero()) return input;

    if(!defined(1)) {
      final Predicate<Type> noCheck = type -> type.isSortable() && !type.isUntyped();
      if(st.zeroOrOne() && noCheck.test(st.type)) return input;

      // lowest(1 to 10)  ->  1 to 10
      if(input instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) input;
        return (seq.asc ? seq : seq.reverse(null)).itemAt(min ? 0 : seq.size() - 1);
      }
      // lowest(ITEM)  ->  ITEM
      if(noCheck.test(st.type) && (st.one() || input instanceof SingletonSeq &&
          ((SingletonSeq) input).singleItem())) return input;

      if(REPLICATE.is(input) && ((FnReplicate) input).singleEval(false)) {
        // lowest(replicate(5, 2))  ->  replicate(5, 2)
        final SeqType ast = input.arg(0).seqType();
        if(ast.zeroOrOne() && noCheck.test(ast.type)) return input;
      } else if(REVERSE.is(input) || SORT.is(input)) {
        // lowest(reverse(E))  ->  lowest(E)
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(min ? LOWEST : HIGHEST, info, args);
      }
    } else if(defined(2)) {
      arg(2, arg -> coerceFunc(arg, cc, SeqType.ANY_ATOMIC_TYPE_ZM, st.with(Occ.EXACTLY_ONE)));
    }
    return adoptType(input);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && defined(2) || super.has(flags);
  }
}
