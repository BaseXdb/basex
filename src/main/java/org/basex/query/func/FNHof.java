package org.basex.query.func;

import java.util.Arrays;
import java.util.Comparator;
import org.basex.query.QueryContext;
import org.basex.query.QueryError;
import org.basex.query.QueryException;
import org.basex.query.expr.*;
import org.basex.query.item.*;
import org.basex.query.iter.ValueBuilder;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.*;

/**
 * Implementation-specific functions on functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FNHof extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNHof(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
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
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(sig) {
      case _HOF_ID:
      case _HOF_CONST: return expr[0].item(ctx, ii);
      default:         return super.item(ctx, ii);
    }
  }

  @Override
  Expr cmp(final QueryContext ctx) throws QueryException {
    if(sig == Function._HOF_ID || sig == Function._HOF_CONST) return expr[0];
    return super.cmp(ctx);
  }

  /**
   * Folds a sequence into a return value, starting from the left and using the
   * leftmost item as start value.
   * @param ctx query context
   * @return resulting sequence
   * @throws QueryException query exception
   */
  private Value foldLeft1(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(0, 2, ctx);
    final Iter xs = expr[1].iter(ctx);

    Value sum = checkNoEmpty(xs.next());
    for(Item x; (x = xs.next()) != null;) sum = f.invValue(ctx, info, sum, x);
    return sum;
  }

  /**
   * Sorts the input sequence according to the given relation.
   * @param ctx query context
   * @return sorted sequence
   * @throws QueryException query exception
   */
  private Value sortWith(final QueryContext ctx) throws QueryException {
    final Value v = expr[1].value(ctx);
    final Comparator<Item> cmp = getComp(0, ctx);
    if(v.size() < 2) return v;
    final ValueBuilder vb = v.cache();
    try {
      Arrays.sort(vb.item, 0, (int) vb.size(), cmp);
    } catch(final QueryError err) {
      throw err.wrapped();
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
    while(!checkBln(checkNoEmpty(pred.invItem(ctx, info, v)), ctx)) {
      v = fun.invValue(ctx, info, v);
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
        new Comparator<Item>(){
      @Override
      public int compare(final Item it1, final Item it2) {
        try {
          return CmpV.OpV.LT.eval(info, it1, it2) ? -1 : 1;
        } catch(final QueryException qe) {
          throw new QueryError(qe);
        }
      }
    });

    try {
      for(Item it; (it = iter.next()) != null;) {
        heap.insert(checkNoEmpty(getKey.invItem(ctx, info, it)), it);
        if(heap.size() > k) heap.removeMin();
      }
    } catch(final QueryError e) { throw e.wrapped(); }

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
    } catch(final QueryError e) { throw e.wrapped(); }

    final Item[] arr = new Item[heap.size()];
    for(int i = arr.length; --i >= 0;) arr[i] = heap.removeMin();
    return Seq.get(arr, arr.length);
  }

  /**
   * Gets a comparator from a less-than predicate as function item.
   * The {@link Comparator#compare(Object, Object)} method throws a {@link QueryError}
   * if the comparison throws a {@link QueryException}.
   * @param pos argument position of the predicate
   * @param ctx query context
   * @return comparator
   * @throws QueryException exception
   */
  private Comparator<Item> getComp(final int pos, final QueryContext ctx)
      throws QueryException {
    final FItem lt = withArity(pos, 2, ctx);
    return new Comparator<Item>(){
      @Override
      public int compare(final Item a, final Item b) {
        try {
          return checkType(lt.invItem(ctx, info, a == null ? Empty.SEQ : a,
              b == null ? Empty.SEQ : b), AtomType.BLN).bool(info) ? -1 : 1;
        } catch(final QueryException qe) {
          throw new QueryError(qe);
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
  private FItem withArity(final int p, final int a, final QueryContext ctx)
      throws QueryException {
    final Item f = checkItem(expr[p], ctx);
    if(!f.type.isFunction() || ((FItem) f).arity() != a)
      Err.type(this, FuncType.arity(a), f);

    return (FItem) f;
  }

  @Override
  public boolean uses(final Use u) {
    return sig == Function.PARTIAL_APPLY && u == Use.CTX || u == Use.X30 ||
        super.uses(u);
  }
}
