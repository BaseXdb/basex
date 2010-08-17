package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CmpV;
import org.basex.query.expr.Expr;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.ItemIter;
import org.basex.query.util.ItemSet;
import org.basex.util.InputInfo;

/**
 * Sequence functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNSeq extends Fun {
  /**
   * Constructor.
   * @param ii input info
   * @param f function definition
   * @param e arguments
   */
  protected FNSeq(final InputInfo ii, final FunDef f, final Expr... e) {
    super(ii, f, e);
  }

  @Override
  public Item atomic(final QueryContext ctx, final InputInfo ii)
      throws QueryException {
    switch(def) {
      case HEAD: return head(ctx);
      default:   return super.atomic(ctx, ii);
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(def) {
      case INDEXOF:  return indexOf(ctx);
      case DISTINCT: return distinctValues(ctx);
      case INSBEF:   return insertBefore(ctx);
      case REVERSE:  return reverse(ctx);
      case REMOVE:   return remove(ctx);
      case SUBSEQ:   return subsequence(ctx);
      case TAIL:     return tail(ctx);
      default:       return super.iter(ctx);
    }
  }

  /**
   * Returns the first item in a sequence.
   * @param ctx query context
   * @return first item
   * @throws QueryException query exception
   */
  private Item head(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];
    return e.item() || e.type().zeroOrOne() ? e.atomic(ctx, input) :
      e.iter(ctx).next();
  }

  /**
   * Returns all but the first item in a sequence.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tail(final QueryContext ctx) throws QueryException {
    final Expr e = expr[0];
    if(e.item() || e.type().zeroOrOne()) return Iter.EMPTY;
    final Iter ir = e.iter(ctx);
    return ir.next() == null ? Iter.EMPTY : ir;
  }

  /**
   * Looks for the index of an specified input item.
   * @param ctx query context
   * @return position(s) of item
   * @throws QueryException query exception
   */
  private Iter indexOf(final QueryContext ctx) throws QueryException {
    final Item it = checkItem(expr[1], ctx);
    if(expr.length == 3) checkColl(expr[2], ctx);

    return new Iter() {
      final Iter ir = expr[0].iter(ctx);
      int c;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = ir.next();
          if(i == null) return null;
          c++;
          if(i.comparable(it) && CmpV.Op.EQ.e(input, i, it))
            return Itr.get(c);
        }
      }
    };
  }

  /**
   * Returns all distinct values of a sequence.
   * @param ctx query context
   * @return distinct iterator
   * @throws QueryException query exception
   */
  private Iter distinctValues(final QueryContext ctx) throws QueryException {
    if(expr.length == 2) checkColl(expr[1], ctx);

    return new Iter() {
      final ItemSet map = new ItemSet();
      final Iter ir = expr[0].iter(ctx);

      @Override
      public Item next() throws QueryException {
        while(true) {
          Item i = ir.next();
          if(i == null) return null;
          i = atom(i);
          if(map.index(input, i)) return i;
        }
      }
    };
  }

  /**
   * Inserts items before the specified position.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter insertBefore(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final long pos = Math.max(1, checkItr(expr[1], ctx));
      final Iter iter = expr[0].iter(ctx);
      final Iter ins = expr[2].iter(ctx);
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        if(last) return p > 0 ? ins.next() : null;
        final boolean sub = p == 0 || --p == 0;
        final Item i = (sub ? ins : iter).next();
        if(i != null) return i;
        if(sub) --p;
        else last = true;
        return next();
      }
    };
  }

  /**
   * Removes an item at a specified position in a sequence.
   * @param ctx query context
   * @return iterator without Item
   * @throws QueryException query exception
   */
  private Iter remove(final QueryContext ctx) throws QueryException {
    return new Iter() {
      final long pos = checkItr(expr[1], ctx);
      final Iter iter = expr[0].iter(ctx);
      long c;

      @Override
      public Item next() throws QueryException {
        return ++c != pos || iter.next() != null ? iter.next() : null;
      }
    };
  }

  /**
   * Creates a subsequence out of a sequence, starting with start and
   * ending with end.
   * @param ctx query context
   * @return subsequence
   * @throws QueryException query exception
   */
  private Iter subsequence(final QueryContext ctx) throws QueryException {
    final long s = Math.round(checkDbl(expr[1], ctx));
    final long e = expr.length > 2 ? s + Math.round(checkDbl(expr[2], ctx)) :
      Long.MAX_VALUE;

    final Iter iter = ctx.iter(expr[0]);
    final long max = iter.size();
    return max != -1 ? new Iter() {
      // directly access specified items
      long c = Math.max(1, s);

      @Override
      public Item next() throws QueryException {
        return c < e && c <= max ? iter.get(c++ - 1) : null;
      }
    } : new Iter() {
      // run through all items
      long c;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = iter.next();
          if(i == null || ++c >= e) return null;
          if(c >= s) return i;
        }
      }
    };
  }

  /**
   * Reverses a sequence.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter reverse(final QueryContext ctx) throws QueryException {
    final Iter iter = ctx.iter(expr[0]);
    // only one item found; no reversion necessary
    if(iter.size() == 1) return iter;
    // if possible, reverse and return the same iterator
    if(iter.reverse()) return iter;

    // process any other iterator...
    return new Iter() {
      final Iter ir = iter.size() != -1 ? iter : ItemIter.get(iter);
      final long s = ir.size();
      long c = s;

      @Override
      public long size() { return s; }
      @Override
      public Item get(final long i) throws QueryException {
        return ir.get(s - i - 1);
      }
      @Override
      public Item next() throws QueryException {
        if(--c >= 0) return ir.get(c);
        ir.reset();
        return null;
      }
    };
  }


  @Override
  public Expr cmp(final QueryContext ctx) {
    // evaluate return type

    // index-of will create integers, insert-before might add new types
    if(def == FunDef.INDEXOF || def == FunDef.INSBEF) return this;

    // head will return first item of argument, or nothing;
    // all other types will return existing types
    final Type t = expr[0].type().type;
    final SeqType.Occ o = def == FunDef.HEAD ? SeqType.Occ.ZO : SeqType.Occ.ZM;
    type = new SeqType(t, o);

    return this;
  }
}
