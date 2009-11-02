package org.basex.query.func;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.CmpV;
import org.basex.query.item.Bln;
import org.basex.query.item.Item;
import org.basex.query.item.Itr;
import org.basex.query.item.Nod;
import org.basex.query.item.Type;
import org.basex.query.iter.Iter;
import org.basex.query.iter.NodeIter;
import org.basex.query.iter.SeqIter;
import org.basex.query.util.Err;
import org.basex.query.util.ItemSet;
import org.basex.util.Token;

/**
 * Sequence functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FNSeq extends Fun {
  @Override
  public Iter iter(final QueryContext ctx) throws QueryException {
    final Iter[] arg = new Iter[expr.length];
    for(int a = 0; a < expr.length; a++) arg[a] = ctx.iter(expr[a]);

    switch(func) {
      case INDEXOF:  return indexOf(ctx);
      case DISTINCT: return distinct(ctx);
      case INSBEF:   return insbef(ctx);
      case REVERSE:  return reverse(ctx);
      case REMOVE:   return remove(ctx);
      case SUBSEQ:   return subseq(ctx);
      default:       return super.iter(ctx);
    }
  }

  @Override
  public Item atomic(final QueryContext ctx) throws QueryException {
    switch(func) {
      case DEEPEQ:   return Bln.get(deep(ctx));
      default:       return super.atomic(ctx);
    }
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

  /**
   * Checks items for deep equality.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  private boolean deep(final QueryContext ctx) throws QueryException {
    if(expr.length == 3) checkColl(expr[2], ctx);
    return deep(ctx.iter(expr[0]), ctx.iter(expr[1]));
  }

  /**
   * Checks items for deep equality.
   * @param iter1 first iterator
   * @param iter2 second iterator
   * @return result of check
   * @throws QueryException query exception
   */
  public static boolean deep(final Iter iter1, final Iter iter2)
      throws QueryException {

    Item it1 = null;
    Item it2 = null;
    // explicit non-short-circuit..
    while((it1 = iter1.next()) != null & (it2 = iter2.next()) != null) {
      if(it1.n() && it2.n() && Double.isNaN(it1.dbl()) &&
          Double.isNaN(it2.dbl())) continue;

      if(!CmpV.valCheck(it1, it2) || CmpV.Comp.NE.e(it1, it2)) return false;
      if(!it1.node() && !it2.node()) continue;

      // comparing nodes
      if(!(it1.node() && it2.node())) return false;
      final NodeIter niter1 = ((Nod) it1).descOrSelf();
      final NodeIter niter2 = ((Nod) it2).descOrSelf();

      Nod n1 = null, n2 = null;
      while(true) {
        n1 = niter1.next();
        n2 = niter2.next();
        if(n1 == null && n2 == null || n1 == null ^ n2 == null) break;
        if(n1.type != n2.type) return false;

        final Item qn1 = n1.qname();
        if(qn1 != null && !qn1.eq(n2.qname())) return false;
        
        if(n1.type == Type.ATT || n1.type == Type.PI || n1.type == Type.COM) {
          if(!Token.eq(n1.str(), n2.str())) return false;
          continue;
        }
        
        NodeIter att1 = n1.attr();
        int s1 = 0;
        while(att1.next() != null) s1++;
        NodeIter att2 = n2.attr();
        int s2 = 0;
        while(att2.next() != null) s2++;
        if(s1 != s2) return false;

        Nod a1 = null, a2 = null;
        att1 = n1.attr();
        while((a1 = att1.next()) != null) {
          att2 = n2.attr();
          boolean found = false;
          while((a2 = att2.next()) != null) {
            if(a1.qname().eq(a2.qname())) {
              found = Token.eq(a1.str(), a2.str());
              break;
            }
          }
          if(!found) return false;
        }
      }
      if(n1 != n2) return false;
    }
    return it1 == it2;
  }
}
