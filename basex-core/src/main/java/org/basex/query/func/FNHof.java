package org.basex.query.func;

import static org.basex.query.func.Function.*;

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
 * @author BaseX Team 2005-13, BSD License
 * @author Leo Woerteler
 */
public final class FNHof extends StandardFunc {
  /**
   * Constructor.
   * @param sctx static context
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNHof(final StaticContext sctx, final InputInfo ii, final Function f,
      final Expr... e) {
    super(sctx, ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _HOF_SORT_WITH:  return sortWith(ctx).iter();
      case _HOF_ID:
      case _HOF_CONST:      return ctx.iter(expr[0]);
      case _HOF_FOLD_LEFT1: return foldLeft1(ctx).iter();
      case _HOF_UNTIL:      return until(ctx).iter();
      case _HOF_TOP_K_BY:   return topKBy(ctx).iter();
      case _HOF_TOP_K_WITH: return topKWith(ctx).iter();
      default:              return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case _HOF_SORT_WITH:  return sortWith(ctx);
      case _HOF_FOLD_LEFT1: return foldLeft1(ctx);
      case _HOF_UNTIL:      return until(ctx);
      case _HOF_ID:
      case _HOF_CONST:      return ctx.value(expr[0]);
      case _HOF_TOP_K_BY:   return topKBy(ctx);
      case _HOF_TOP_K_WITH: return topKWith(ctx);
      default:              return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case _HOF_ID:
      case _HOF_CONST: return expr[0].item(ctx, ii);
      default:         return super.item(ctx, ii);
    }
  }

  @Override
  protected Expr opt(final QueryContext ctx, final VarScope scp) {
    return oneOf(sig, _HOF_ID, _HOF_CONST) ? expr[0] : this;
  }

  /**
   * Folds a sequence into a return value, starting from the left and using the
   * leftmost item as start value.
   * @param ctx query context
   * @return resulting sequence
   * @throws QueryException query exception
   */
  private Value foldLeft1(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(1, 2, ctx);
    final Iter xs = expr[0].iter(ctx);

    Value sum = checkNoEmpty(xs.next());
    for(Item x; (x = xs.next()) != null;) sum = f.invokeValue(ctx, info, sum, x);
    return sum;
  }

  /**
   * Sorts the input sequence according to the given relation.
   * @param ctx query context
   * @return sorted sequence
   * @throws QueryException query exception
   */
  private Value sortWith(final QueryContext ctx) throws QueryException {
    final Value v = expr[0].value(ctx);
    final Comparator<Item> cmp = getComp(1, ctx);
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
   * @param ctx query context
   * @return accepted value
   * @throws QueryException exception
   */
  private Value until(final QueryContext ctx) throws QueryException {
    final FItem pred = withArity(0, 1, ctx);
    final FItem fun = withArity(1, 1, ctx);
    Value v = ctx.value(expr[2]);
    while(!checkBln(checkNoEmpty(pred.invokeItem(ctx, info, v)), ctx)) {
      v = fun.invokeValue(ctx, info, v);
    }
    return v;
  }

  /**
   * The best k elements of the input sequence according to a sort key.
   * @param ctx query context
   * @return best k elements
   * @throws QueryException query exception
   */
  private Value topKBy(final QueryContext ctx) throws QueryException {
    final FItem getKey = withArity(1, 1, ctx);
    final long k = checkItr(expr[2], ctx);
    if(k < 1 || k > Integer.MAX_VALUE / 2) return Empty.SEQ;

    final Iter iter = expr[0].iter(ctx);
    final MinHeap<Item, Item> heap = new MinHeap<Item, Item>((int) k,
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
        heap.insert(checkNoEmpty(getKey.invokeItem(ctx, info, it)), it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryRTException ex) { throw ex.getCause(); }

    final Item[] arr = new Item[heap.size()];
    for(int i = arr.length; --i >= 0;) arr[i] = heap.removeMin();
    return Seq.get(arr, arr.length);
  }

  /**
   * The best k elements of the input sequence according to a less-than predicate.
   * @param ctx query context
   * @return best k elements
   * @throws QueryException query exception
   */
  private Value topKWith(final QueryContext ctx) throws QueryException {
    final Comparator<Item> cmp = getComp(1, ctx);
    final long k = checkItr(expr[2], ctx);
    if(k < 1 || k > Integer.MAX_VALUE / 2) return Empty.SEQ;

    final Iter iter = expr[0].iter(ctx);
    final MinHeap<Item, Item> heap = new MinHeap<Item, Item>((int) k, cmp);

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
   * @param ctx query context
   * @return comparator
   * @throws QueryException exception
   */
  private Comparator<Item> getComp(final int pos, final QueryContext ctx)
      throws QueryException {
    final FItem lt = withArity(pos, 2, ctx);
    return new Comparator<Item>() {
      @Override
      public int compare(final Item a, final Item b) {
        try {
          return checkType(lt.invokeItem(ctx, info, a == null ? Empty.SEQ : a,
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
   * @param ctx query context
   * @return function item
   * @throws QueryException query exception
   */
  private FItem withArity(final int p, final int a, final QueryContext ctx) throws QueryException {
    final Item f = checkItem(expr[p], ctx);
    if(f instanceof FItem && ((XQFunction) f).arity() == a) return (FItem) f;
    throw Err.typeError(this, FuncType.arity(a), f);
  }
}
