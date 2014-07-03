package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.util.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.query.var.*;
import org.basex.util.*;

/**
 * Implementation-specific functions on functions.
 *
 * @author BaseX Team 2005-14, BSD License
 * @author Leo Woerteler
 */
public final class FNHof extends StandardFunc {
  /**
   * Constructor.
   * @param sc static context
   * @param info input info
   * @param func function definition
   * @param args arguments
   */
  public FNHof(final StaticContext sc, final InputInfo info, final Function func,
      final Expr... args) {
    super(sc, info, func, args);
  }

  @Override
  public Iter iter(final QueryContext qc) throws QueryException {
    switch(func) {
      case _HOF_SORT_WITH:  return sortWith(qc).iter();
      case _HOF_ID:
      case _HOF_CONST:      return qc.iter(exprs[0]);
      case _HOF_FOLD_LEFT1: return foldLeft1(qc).iter();
      case _HOF_UNTIL:      return until(qc).iter();
      case _HOF_TOP_K_BY:   return topKBy(qc).iter();
      case _HOF_TOP_K_WITH: return topKWith(qc).iter();
      default:              return super.iter(qc);
    }
  }

  @Override
  public Value value(final QueryContext qc) throws QueryException {
    switch(func) {
      case _HOF_SORT_WITH:  return sortWith(qc);
      case _HOF_FOLD_LEFT1: return foldLeft1(qc);
      case _HOF_UNTIL:      return until(qc);
      case _HOF_ID:
      case _HOF_CONST:      return qc.value(exprs[0]);
      case _HOF_TOP_K_BY:   return topKBy(qc);
      case _HOF_TOP_K_WITH: return topKWith(qc);
      default:              return super.value(qc);
    }
  }

  @Override
  public Item item(final QueryContext qc, final InputInfo ii) throws QueryException {
    switch(func) {
      case _HOF_ID:
      case _HOF_CONST: return exprs[0].item(qc, ii);
      default:         return super.item(qc, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext qc, final VarScope scp) throws QueryException {
    switch(func) {
      case _HOF_ID:
      case _HOF_CONST:
        return exprs[0];
      case _HOF_FOLD_LEFT1:
        if(allAreValues() && exprs[0].size() < FNFunc.UNROLL_LIMIT) {
          qc.compInfo(QueryText.OPTUNROLL, this);
          final Value seq = (Value) exprs[0];
          if(seq.isEmpty()) throw INVEMPTY.get(info, description());
          final FItem f = withArity(1, 2, qc);
          Expr e = seq.itemAt(0);
          for(int i = 1, len = (int) seq.size(); i < len; i++)
            e = new DynFuncCall(info, sc, false, f, e, seq.itemAt(i)).optimize(qc, scp);
          return e;
        }
        return this;
      default:
        return this;
    }
  }

  /**
   * Folds a sequence into a return value, starting from the left and using the
   * leftmost item as start value.
   * @param qc query context
   * @return resulting sequence
   * @throws QueryException query exception
   */
  private Value foldLeft1(final QueryContext qc) throws QueryException {
    final FItem f = withArity(1, 2, qc);
    final Iter xs = exprs[0].iter(qc);

    Value sum = checkNoEmpty(xs.next());
    for(Item x; (x = xs.next()) != null;) sum = f.invokeValue(qc, info, sum, x);
    return sum;
  }

  /**
   * Sorts the input sequence according to the given relation.
   * @param qc query context
   * @return sorted sequence
   * @throws QueryException query exception
   */
  private Value sortWith(final QueryContext qc) throws QueryException {
    final Value v = exprs[0].value(qc);
    final Comparator<Item> cmp = getComp(1, qc);
    if(v.size() < 2) return v;
    final ValueBuilder vb = v.cache();
    try {
      Arrays.sort(vb.items(), 0, (int) vb.size(), cmp);
    } catch(final QueryRTException ex) {
      throw ex.getCause();
    }
    return vb.value();
  }

  /**
   * Applies a function to a start value until the given predicate holds.
   * @param qc query context
   * @return accepted value
   * @throws QueryException exception
   */
  private Value until(final QueryContext qc) throws QueryException {
    final FItem pred = withArity(0, 1, qc);
    final FItem fun = withArity(1, 1, qc);
    Value v = qc.value(exprs[2]);
    while(!checkBln(checkNoEmpty(pred.invokeItem(qc, info, v)), qc)) {
      v = fun.invokeValue(qc, info, v);
    }
    return v;
  }

  /**
   * The best k elements of the input sequence according to a sort key.
   * @param qc query context
   * @return best k elements
   * @throws QueryException query exception
   */
  private Value topKBy(final QueryContext qc) throws QueryException {
    final FItem getKey = withArity(1, 1, qc);
    final long k = checkItr(exprs[2], qc);
    if(k < 1 || k > Integer.MAX_VALUE / 2) return Empty.SEQ;

    final Iter iter = exprs[0].iter(qc);
    final MinHeap<Item, Item> heap = new MinHeap<>((int) k,
        new Comparator<Item>() {
      @Override
      public int compare(final Item it1, final Item it2) {
        try {
          return CmpV.OpV.LT.eval(it1, it2, sc.collation, info) ? -1 : 1;
        } catch(final QueryException qe) {
          throw new QueryRTException(qe);
        }
      }
    });

    try {
      for(Item it; (it = iter.next()) != null;) {
        heap.insert(checkNoEmpty(getKey.invokeItem(qc, info, it)), it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final Item[] arr = new Item[heap.size()];
    for(int i = arr.length; --i >= 0;) arr[i] = heap.removeMin();
    return Seq.get(arr, arr.length);
  }

  /**
   * The best k elements of the input sequence according to a less-than predicate.
   * @param qc query context
   * @return best k elements
   * @throws QueryException query exception
   */
  private Value topKWith(final QueryContext qc) throws QueryException {
    final Comparator<Item> cmp = getComp(1, qc);
    final long k = checkItr(exprs[2], qc);
    if(k < 1 || k > Integer.MAX_VALUE / 2) return Empty.SEQ;

    final Iter iter = exprs[0].iter(qc);
    final MinHeap<Item, Item> heap = new MinHeap<>((int) k, cmp);

    try {
      for(Item it; (it = iter.next()) != null;) {
        heap.insert(it, it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final Item[] arr = new Item[heap.size()];
    for(int i = arr.length; --i >= 0;) arr[i] = heap.removeMin();
    return Seq.get(arr, arr.length);
  }

  /**
   * Gets a comparator from a less-than predicate as function item.
   * The {@link Comparator#compare(Object, Object)} method throws a
   * {@link QueryRTException} if the comparison throws a {@link QueryException}.
   * @param pos argument position of the predicate
   * @param qc query context
   * @return comparator
   * @throws QueryException exception
   */
  private Comparator<Item> getComp(final int pos, final QueryContext qc) throws QueryException {
    final FItem lt = withArity(pos, 2, qc);
    return new Comparator<Item>() {
      @Override
      public int compare(final Item a, final Item b) {
        try {
          return checkType(lt.invokeItem(qc, info, a == null ? Empty.SEQ : a,
              b == null ? Empty.SEQ : b), AtomType.BLN).bool(info) ? -1 : 1;
        } catch(final QueryException qe) {
          throw new QueryRTException(qe);
        }
      }
    };
  }

  /**
   * Casts and checks the function item for its arity.
   * @param p position of the function
   * @param a arity
   * @param qc query context
   * @return function item
   * @throws QueryException query exception
   */
  private FItem withArity(final int p, final int a, final QueryContext qc) throws QueryException {
    final Item f = checkItem(exprs[p], qc);
    if(f instanceof FItem && ((XQFunction) f).arity() == a) return (FItem) f;
    throw Err.typeError(this, FuncType.arity(a), f);
  }
}
