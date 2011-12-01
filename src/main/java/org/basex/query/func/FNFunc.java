package org.basex.query.func;

import static org.basex.query.util.Err.*;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.DynFuncCall;
import org.basex.query.expr.Expr;
import org.basex.query.expr.PartFunApp;
import org.basex.query.expr.VarRef;
import org.basex.query.item.AtomType;
import org.basex.query.item.Empty;
import org.basex.query.item.FItem;
import org.basex.query.item.FuncType;
import org.basex.query.item.Item;
import org.basex.query.item.Int;
import org.basex.query.item.QNm;
import org.basex.query.item.Value;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.query.util.Var;
import org.basex.util.InputInfo;

/**
 * Functions on functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class FNFunc extends FuncCall {
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
    switch(def) {
      case MAP:        return map(ctx);
      case FILTER:     return filter(ctx);
      case MAP_PAIRS:  return zip(ctx);
      case FOLD_LEFT:  return foldLeft(ctx);
      case FOLD_RIGHT: return foldRight(ctx);
      default:         return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case FUNCTION_ARITY:
        return Int.get(getFun(0, FuncType.ANY_FUN, ctx).arity());
      case FUNCTION_NAME:
        return getFun(0, FuncType.ANY_FUN, ctx).fName();
      case PARTIAL_APPLY:
        return partApp(ctx, ii);
      case FUNCTION_LOOKUP:
        return lookup(ctx, ii);
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Looks up the specified function item.
   * @param ctx query context
   * @param ii input info
   * @return function item
   * @throws QueryException query exception
   */
  private Item lookup(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final QNm name = (QNm) checkType(expr[0].item(ctx, ii), AtomType.QNM);
    final long arity = checkItr(expr[1], ctx);
    try {
      return ctx.funcs.get(name, arity, true, ctx, ii);
    } catch(final QueryException e) {
      // function not found
      return null;
    }
  }

  /**
   * Partially applies the function to one argument.
   * @param ctx query context
   * @param ii input info
   * @return function item
   * @throws QueryException query exception
   */
  private Item partApp(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    final FItem f = getFun(0, FuncType.ANY_FUN, ctx);
    final long pos = expr.length == 2 ? 0 : checkItr(expr[2], ctx) - 1;

    final int arity = f.arity();
    if(pos < 0 || pos >= arity) INVPOS.thrw(ii, f.info(), pos + 1);

    final FuncType ft = (FuncType) f.type;
    final Var[] vars = new Var[arity - 1];
    final Expr[] vals = new Expr[arity];
    vals[(int) pos] = expr[1];
    for(int i = 0, j = 0; i < arity - 1; i++, j++) {
      if(i == pos) j++;
      vars[i] = ctx.uniqueVar(ii, ft.args[j]);
      vals[j] = new VarRef(ii, vars[i]);
    }

    return new PartFunApp(ii, new DynFuncCall(ii, f, vals),
        vars).comp(ctx).item(ctx, ii);
  }

  /**
   * Maps a function onto a sequence of items.
   * @param ctx query context
   * @return sequence of results
   * @throws QueryException exception
   */
  private Iter map(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(0, 1, ctx);
    final Iter xs = expr[1].iter(ctx);
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
          ys = f.invIter(ctx, input, x);
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
    final FItem f = withArity(0, 1, ctx);
    final Iter xs = expr[1].iter(ctx);
    return new Iter() {
      @Override
      public Item next() throws QueryException {
        do {
          final Item it = xs.next();
          if(it == null) return null;
          final Item b = f.invItem(ctx, input, it);
          if(checkType(b, AtomType.BLN).bool(input)) return it;
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
  private Iter zip(final QueryContext ctx) throws QueryException {
    final FItem zipper = withArity(0, 2, ctx);
    final Iter xs = expr[1].iter(ctx);
    final Iter ys = expr[2].iter(ctx);
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
          zs = zipper.invIter(ctx, input, x, y);
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
    final FItem f = withArity(0, 2, ctx);
    final Iter xs = expr[2].iter(ctx);
    Item x = xs.next();

    // don't convert to a value if not necessary
    if(x == null) return expr[1].iter(ctx);

    Value sum = expr[1].value(ctx);
    do sum = f.invValue(ctx, input, sum, x);
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
    final FItem f = withArity(0, 2, ctx);
    final Value xs = expr[2].value(ctx);
    // evaluate start value lazily if it's passed straight through
    if(xs.isEmpty()) return expr[1].iter(ctx);

    Value res = expr[1].value(ctx);
    for(long i = xs.size(); --i >= 0;)
      res = f.invValue(ctx, input, xs.itemAt(i), res);

    return res.iter();
  }

  /**
   * Checks the type of the given function item.
   * @param p position
   * @param t type
   * @param ctx query context
   * @return function item
   * @throws QueryException query exception
   */
  private FItem getFun(final int p, final FuncType t, final QueryContext ctx)
      throws QueryException {
    return (FItem) checkType(checkItem(expr[p], ctx), t);
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
    return (def == Function.PARTIAL_APPLY || def == Function.FUNCTION_LOOKUP)
        && u == Use.CTX || u == Use.X30 || super.uses(u);
  }
}
