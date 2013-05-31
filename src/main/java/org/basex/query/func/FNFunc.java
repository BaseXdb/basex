package org.basex.query.func;

import static org.basex.query.func.Function.*;
import static org.basex.query.util.Err.*;

import org.basex.query.*;
import org.basex.query.expr.*;
import org.basex.query.iter.*;
import org.basex.query.util.*;
import org.basex.query.value.*;
import org.basex.query.value.item.*;
import org.basex.query.value.seq.*;
import org.basex.query.value.type.*;
import org.basex.util.*;

/**
 * Functions on functions.
 *
 * @author BaseX Team 2005-12, BSD License
 * @author Leo Woerteler
 */
public final class FNFunc extends StandardFunc {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNFunc(final InputInfo ii, final Function f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(sig) {
      case FOR_EACH:      return forEach(ctx);
      case FILTER:        return filter(ctx);
      case FOR_EACH_PAIR: return forEachPair(ctx);
      case FOLD_LEFT:     return foldLeft(ctx);
      case FOLD_RIGHT:    return foldRight(ctx);
      default:            return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii) throws QueryException {
    switch(sig) {
      case FUNCTION_ARITY:  return Int.get(checkFunc(expr[0], ctx).arity());
      case FUNCTION_NAME:   return checkFunc(expr[0], ctx).fName();
      case FUNCTION_LOOKUP: return lookup(ctx, ii);
      default:              return super.item(ctx, ii);
    }
  }

  /**
   * Looks up the specified function item.
   * @param ctx query context
   * @param ii input info
   * @return function item
   * @throws QueryException query exception
   */
  private Item lookup(final QueryContext ctx, final InputInfo ii) throws QueryException {
    final QNm name = checkQNm(expr[0].item(ctx, ii), ctx);
    final long arity = checkItr(expr[1], ctx);
    if(arity < 0 || arity > Integer.MAX_VALUE) FUNCUNKNOWN.thrw(ii, name);

    Expr lit = null;
    try {
      lit = Functions.getLiteral(name, (int) arity, ctx, ii);
    } catch(final QueryException e) {
      // function not found (in most cases: XPST0017)
      return null;
    }
    return lit == null ? null : lit.item(ctx, ii);
  }

  /**
   * Maps a function onto a sequence of items.
   * @param ctx query context
   * @return sequence of results
   * @throws QueryException exception
   */
  private Iter forEach(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(1, 1, ctx);
    final Iter xs = expr[0].iter(ctx);
    return new Iter() {
      /** Results. */
      Iter ys = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item it = ys.next();
          if(it != null) return it;
          final Item x = xs.next();
          if(x == null) return null;
          ys = f.invValue(ctx, info, x).iter();
        } while(true);
      }
    };
  }

  /**
   * Filters the given sequence with the given predicate.
   * @param ctx query context
   * @return filtered sequence
   * @throws QueryException query exception
   */
  private Iter filter(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(1, 1, ctx);
    final Iter xs = expr[0].iter(ctx);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        do {
          final Item it = xs.next();
          if(it == null) return null;
          if(checkBln(checkNoEmpty(f.invItem(ctx, info, it)), ctx)) return it;
        } while(true);
      }
    };
  }

  /**
   * Zips two sequences with the given zipper function.
   * @param ctx query context
   * @return sequence of results
   * @throws QueryException query exception
   */
  private Iter forEachPair(final QueryContext ctx) throws QueryException {
    final FItem zipper = withArity(2, 2, ctx);
    final Iter xs = expr[0].iter(ctx);
    final Iter ys = expr[1].iter(ctx);
    return new Iter() {
      /** Results. */
      Iter zs = Empty.ITER;

      @Override
      public Item next() throws QueryException {
        do {
          final Item it = zs.next();
          if(it != null) return it;
          final Item x = xs.next(), y = ys.next();
          if(x == null || y == null) return null;
          zs = zipper.invValue(ctx, info, x, y).iter();
        } while(true);
      }
    };
  }

  /**
   * Folds a sequence into a return value, starting from the left.
   * @param ctx query context
   * @return resulting sequence
   * @throws QueryException query exception
   */
  private Iter foldLeft(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(2, 2, ctx);
    final Iter xs = expr[0].iter(ctx);
    Item x = xs.next();

    // don't convert to a value if not necessary
    if(x == null) return expr[1].iter(ctx);

    Value sum = ctx.value(expr[1]);
    do sum = f.invValue(ctx, info, sum, x);
    while((x = xs.next()) != null);
    return sum.iter();
  }

  /**
   * Folds a sequence into a return value, starting from the left.
   * @param ctx query context
   * @return resulting sequence
   * @throws QueryException query exception
   */
  private Iter foldRight(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(2, 2, ctx);
    final Value xs = ctx.value(expr[0]);
    // evaluate start value lazily if it's passed straight through
    if(xs.isEmpty()) return expr[1].iter(ctx);

    Value res = ctx.value(expr[1]);
    for(long i = xs.size(); --i >= 0;) res = f.invValue(ctx, info, xs.itemAt(i), res);
    return res.iter();
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

    final Item it = checkItem(expr[p], ctx);
    if(it instanceof FItem) {
      final FItem fi = (FItem) it;
      if(fi.arity() == a) return fi;
    }
    throw Err.type(this, FuncType.arity(a), it);
  }

  @Override
  public boolean xquery3() {
    return true;
  }

  @Override
  public boolean uses(final Use u) {
    return (u == Use.CTX || u == Use.POS) && oneOf(sig, FUNCTION_LOOKUP) ||
        u == Use.X30 || super.uses(u);
  }
}
