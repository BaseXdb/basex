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
    final Collation coll = toCollation(1, false, qc);
    final FItem key = exprs.length > 2 ? toFunction(exprs[2], 1, qc) : null;

    final QueryFunction<Item, Value> modify = item -> {
      final ValueBuilder vb = new ValueBuilder(qc);
      for(final Item it : (key == null ? item : key.invoke(qc, info, item)).atomValue(qc, info)) {
        vb.add(it.type == AtomType.DOUBLE || !it.instanceOf(AtomType.UNTYPED_ATOMIC) ? it :
          Dbl.get(toDouble(it)));
      }
      return vb.value();
    };

    final ItemList result = new ItemList();
    final Iter iter = exprs[0].iter(qc);
    Item item = qc.next(iter);
    Value lowest;
    if(item != null) {
      result.add(item);
      lowest = modify.apply(item);
      while((item = qc.next(iter)) != null) {
        final Value low = modify.apply(item);
        int diff = FnSort.compare(lowest, low, coll, info);
        if(min) diff = -diff;
        if(diff > 0) continue;
        if(diff < 0) {
          result.reset();
          lowest = low;
        }
        result.add(item);
      }
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
    final Expr expr1 = exprs[0];
    final SeqType st1 = expr1.seqType();
    if(st1.zero()) return expr1;

    if(exprs.length < 2) {
      if(st1.zeroOrOne() && st1.type.isSortable()) return expr1;

      // range values
      if(expr1 instanceof RangeSeq) {
        final RangeSeq seq = (RangeSeq) expr1;
        return (seq.asc ? seq : seq.reverse(null)).itemAt(min ? 0 : seq.size() - 1);
      }
      // sortable single or singleton values
      final SeqType st = expr1.seqType();
      if(st.type.isSortable() && (st.one() || expr1 instanceof SingletonSeq &&
          ((SingletonSeq) expr1).singleItem())) return expr1;

      if(REPLICATE.is(expr1) && ((FnReplicate) expr1).singleEval(false)) {
        final SeqType ast = expr1.arg(0).seqType();
        if(ast.zeroOrOne() && ast.type.isSortable()) return expr1;
      }
      if(REVERSE.is(expr1) || SORT.is(expr1)) {
        final Expr[] args = exprs.clone();
        args[0] = args[0].arg(0);
        return cc.function(min ? LOWEST : HIGHEST, info, args);
      }
    } else if(exprs.length == 3) {
      exprs[2] = coerceFunc(exprs[2], cc, SeqType.ANY_ATOMIC_TYPE_ZM, st1.with(Occ.EXACTLY_ONE));
    }
    return adoptType(expr1);
  }

  @Override
  public boolean has(final Flag... flags) {
    return Flag.HOF.in(flags) && exprs.length > 2 || super.has(flags);
  }
}
