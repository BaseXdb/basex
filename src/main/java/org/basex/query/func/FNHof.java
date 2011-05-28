package org.basex.query.func;

import java.util.Arrays;
import java.util.Comparator;
import org.basex.query.QueryContext;
import org.basex.query.QueryError;
import org.basex.query.QueryException;
import org.basex.query.expr.Expr;
import org.basex.query.item.AtomType;
import org.basex.query.item.FItem;
import org.basex.query.item.FunType;
import org.basex.query.item.Item;
import org.basex.query.item.Value;
import org.basex.query.iter.ItemCache;
import org.basex.query.iter.Iter;
import org.basex.query.util.Err;
import org.basex.util.InputInfo;

/**
 * Implementation-specific functions on functions.
 *
 * @author BaseX Team 2005-11, BSD License
 * @author Leo Woerteler
 */
public final class FNHof extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  public FNHof(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case SORTWITH: return sortWith(ctx);
      case HOFID:    return expr[0].iter(ctx);
      case CONST:    return expr[0].iter(ctx);
      case UNTIL:    return until(ctx).iter();
      case ITERATE:  return iterate(ctx);
      default:       return super.iter(ctx);
    }
  }

  @Override
  public Value value(final QueryContext ctx) throws QueryException {
    switch(def) {
      case FOLDLEFT1: return foldLeft1(ctx);
      case UNTIL:     return until(ctx);
      case HOFID:     return expr[0].value(ctx);
      case CONST:     return expr[0].value(ctx);
      default:        return super.value(ctx);
    }
  }

  @Override
  public Item item(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case HOFID: return expr[0].item(ctx, ii);
      case CONST: return expr[0].item(ctx, ii);
      default:    return super.item(ctx, ii);
    }
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

    Value sum = checkEmpty(xs.next());
    for(Item x; (x = xs.next()) != null;) sum = f.invValue(ctx, input, sum, x);
    return sum;
  }

  /**
   * Sorts the input sequence according to the given relation.
   * @param ctx query context
   * @return sorted sequence
   * @throws QueryException query exception
   */
  private ItemCache sortWith(final QueryContext ctx) throws QueryException {
    final FItem lt = withArity(0, 2, ctx);
    final ItemCache ic = expr[1].value(ctx).cache();
    try {
      Arrays.sort(ic.item, 0, (int) ic.size(), new Comparator<Item>(){
        @Override
        public int compare(final Item it1, final Item it2) {
          try {
            return checkType(lt.invItem(ctx, input, it1, it2),
                AtomType.BLN).bool(input) ? -1 : 1;
          } catch(final QueryException qe) {
            throw new QueryError(qe);
          }
        }
      });
    } catch(final QueryError err) {
      throw err.wrapped();
    }
    return ic;
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
    Value v = expr[2].value(ctx);
    while(!checkType(pred.invItem(ctx, input, v), AtomType.BLN).bool(input)) {
      v = fun.invValue(ctx, input, v);
    }
    return v;
  }

  /**
   * Repeatedly applies a function to an argument, lazily returning all results.
   * @param ctx query context
   * @return result iterator
   * @throws QueryException query context
   */
  private Iter iterate(final QueryContext ctx) throws QueryException {
    final FItem f = withArity(0, 1, ctx);
    return new Iter() {
      /** Current value. */
      Value v = expr[1].value(ctx);
      long i, len = v.size();

      @Override
      public Item next() throws QueryException {
        while(i >= len) {
          v = f.invValue(ctx, input, v);
          i = 0;
          len = v.size();
        }
        return v.itemAt(i++);
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
    if(!f.func() || ((FItem) f).arity() != a)
      Err.type(this, FunType.arity(a), f);

    return (FItem) f;
  }

  @Override
  public boolean uses(final Use u) {
    return def == FunDef.PARTAPP && u == Use.CTX || u == Use.X30 ||
        super.uses(u);
  }
}
