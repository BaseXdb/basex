package org.basex.query.func;

import static org.basex.query.util.Err.*;

import java.util.Arrays;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.AtomType;
import org.basex.query.item.Empty;
import org.basex.query.item.FunItem;
import org.basex.query.item.FunType;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Seq;
import org.basex.query.item.SeqType;
import static org.basex.query.item.SeqType.*;

import org.basex.query.iter.ItemIter;
import org.basex.query.iter.Iter;
import org.basex.util.InputInfo;

/**
 * Functions on functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Christian Gruen
 */
final class FNFunc extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNFunc(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case MAP:       return map(ctx);
      case FILTER:    return filter(ctx);
      case MAPPAIRS:  return zip(ctx);
      case FOLDLEFT:  return foldLeft(ctx);
      case FOLDRIGHT: return foldRight(ctx);
      default:
        return super.iter(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case FUNCARITY: return Itr.get(getFun(0, FunType.ANY, ctx).arity());
      case FUNCNAME:
      case PARTAPP:
        NOTIMPL.thrw(input, def.desc);
        return null;
      default:
        return super.item(ctx, ii);
    }
  }

  /**
   * Maps a function onto a sequence of items.
   * @param ctx context
   * @return sequence of results
   * @throws QueryException exception
   */
  private Iter map(final QueryContext ctx) throws QueryException {
    final FunItem f = withArity(0, 1, ctx);
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
    final FunItem f = withArity(0, 1, ctx);
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
    final FunItem zipper = withArity(0, 2, ctx);
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
    final FunItem f = withArity(0, 2, ctx);
    final Iter xs = expr[2].iter(ctx);

    ItemIter res = ItemIter.get(expr[1].iter(ctx));
    for(Item x; (x = xs.next()) != null;)
      res = f.invIter(ctx, input, Seq.get(res.item, res.item.length), x);

    return res;
  }

  /**
   * Folds a sequence into a return value, starting from the left.
   * @param ctx query context
   * @return resulting sequence
   * @throws QueryException query exception
   */
  private Iter foldRight(final QueryContext ctx) throws QueryException {
    final FunItem f = withArity(0, 2, ctx);
    ItemIter res = ItemIter.get(expr[1].iter(ctx));

    final Item[] xs = ItemIter.get(expr[2].iter(ctx)).item;
    for(int i = xs.length; i-- > 0;)
      res = f.invIter(ctx, input, xs[i], Seq.get(res.item, res.item.length));

    return res;
  }

  /**
   * Checks the type of the given function item.
   * @param p position
   * @param t type
   * @param ctx context
   * @return function item
   * @throws QueryException query exception
   */
  private FunItem getFun(final int p, final FunType t, final QueryContext ctx)
      throws QueryException {
    return (FunItem) checkType(checkItem(expr[p], ctx), t);
  }

  /**
   * Casts and checks the function item for its arity.
   * @param p position of the function
   * @param a arity
   * @param ctx query context
   * @return function item
   * @throws QueryException query exception
   */
  private FunItem withArity(final int p, final int a, final QueryContext ctx)
      throws QueryException {
    final SeqType[] args = new SeqType[a];
    Arrays.fill(args, ITEM_ZM);
    return getFun(p, FunType.get(args, ITEM_ZM), ctx);
  }

  @Override
  public boolean uses(final Use u) {
    return u == Use.X30 || super.uses(u);
  }
}
