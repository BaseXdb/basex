package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CmpV;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.SeqType;
import org.basex.query.iter.Iter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.query.util.ItemSet;

/**
 * Sequence functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-10, ISC License
 * @author Christian Gruen
 */
final class FNSeq extends Fun {
  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case HEAD:     return head(ctx);
      default:       return super.atomic(ctx);
    }
  }

  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    switch(func) {
      case INDEXOF:  return indexOf(ctx);
      case DISTINCT: return distinct(ctx);
      case INSBEF:   return insbef(ctx);
      case REVERSE:  return reverse(ctx);
      case REMOVE:   return remove(ctx);
      case SUBSEQ:   return subseq(ctx);
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
    return expr[0].iter(ctx).next();
  }

  /**
   * Returns all but the first item in a sequence.
   * @param ctx query context
   * @return iterator
   * @throws QueryException query exception
   */
  private Iter tail(final QueryContext ctx) throws QueryException {
    // create iterator
    final Iter ir = expr[0].iter(ctx);
    // skip first item; if empty, return empty iterator
    if(ir.next() == null) return Iter.EMPTY;

    return new Iter() {
      @Override
      public Item next() throws QueryException {
        return ir.next();
      }
    };
  }

  /**
   * Looks for the index of an specified input item.
   * @param ctx query context
   * @return position(s) of item
   * @throws QueryException query exception
   */
  private Iter indexOf(final QueryContext ctx) throws QueryException {
    final Item it = expr[1].atomic(ctx);
    if(it == null) Err.empty(this);
    if(expr.length == 3) checkColl(expr[2], ctx);

    return new Iter() {
      final Iter iter = expr[0].iter(ctx);
      int c;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = iter.next();
          if(i == null) return null;
          c++;
          if(CmpV.valCheck(i, it) && CmpV.Comp.EQ.e(i, it)) return Itr.get(c);
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
  private Iter distinct(final QueryContext ctx) throws QueryException {
    if(expr.length == 2) checkColl(expr[1], ctx);

    return new Iter() {
      final Iter iter = expr[0].iter(ctx);
      final ItemSet map = new ItemSet();

      @Override
      public Item next() throws QueryException {
        while(true) {
          Item i = iter.next();
          if(i == null) return null;
          i = FNGen.atom(i);
          if(map.index(i)) return i;
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
  private Iter insbef(final QueryContext ctx) throws QueryException {
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
  private Iter subseq(final QueryContext ctx) throws QueryException {
    final long s = Math.round(checkDbl(expr[1], ctx));
    final long e = expr.length > 2 ? s + Math.round(checkDbl(expr[2], ctx)) :
      Long.MAX_VALUE;

    final Iter iter = ctx.iter(expr[0]);
    return iter.size() != -1 ? new Iter() {
      // directly access specified items
      long c = Math.max(1, s);

      @Override
      public Item next() {
        return c < e ? iter.get(c++ - 1) : null;
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

    // process reversable iterator...
    if(iter.reverse()) return iter;

    // process any other iterator...
    return new Iter() {
      final Iter si = iter.size() != -1 ? iter : SeqIter.get(iter);
      long c = si.size();

      @Override
      public int size() { return si.size(); }
      @Override
      public Item get(final long i) { return si.get(si.size() - i - 1); }
      @Override
      public Item next() { return --c < 0 ? null : si.get(c); }
    };
  }

  @Override
  public SeqType returned(final QueryContext ctx) {
    // index-of will create integers, insert-before might add new types
    return func == FunDef.INDEXOF || func == FunDef.INSBEF ? super.returned(ctx)
        : new SeqType(expr[0].returned(ctx).type, SeqType.Occ.ZM);
  }
}
