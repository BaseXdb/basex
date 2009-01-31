package org.basex.query.func;

import org.basex.BaseX;
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
import org.basex.query.util.ItemSet;
import org.basex.util.Token;

/**
 * Sequence functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNSeq extends Fun {
  @Override
  public Iter iter(final QueryContext ctx, final Iter[] arg)
      throws QueryException {
    switch(func) {
      case INDEXOF:  return indexOf(arg);
      case DISTINCT: return distinct(arg);
      case INSBEF:   return insbef(arg);
      case REVERSE:  return reverse(arg);
      case REMOVE:   return remove(arg);
      case SUBSEQ:   return subseq(arg);
      case DEEPEQ:   return Bln.get(deep(arg)).iter();
      default: BaseX.notexpected(func); return null;
    }
  }

  /**
   * Looks for the index of an specified input item.
   * @param arg arguments
   * @return position(s) of item
   * @throws QueryException evaluation exception
   */
  private Iter indexOf(final Iter[] arg) throws QueryException {
    final Item it = arg[1].atomic(this, false);
    if(arg.length == 3) checkColl(arg[2]);

    return new Iter() {
      int c = 0;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = arg[0].next();
          if(i == null) return null;
          c++;
          if(CmpV.valCheck(i, it) && CmpV.Comp.EQ.e(i, it)) return Itr.get(c);
        }
      }
    };
  }

  /**
   * Looks for distinct values in the sequence.
   * @param arg arguments
   * @return distinct iterator
   * @throws QueryException evaluation exception
   */
  private Iter distinct(final Iter[] arg) throws QueryException {
    if(arg.length == 2) checkColl(arg[1]);
    
    return new Iter() {
      final ItemSet map = new ItemSet();

      @Override
      public Item next() throws QueryException {
        Item i;
        while((i = arg[0].next()) != null) {
          i = FNGen.atom(i);
          if(map.index(i)) return i;
        }
        return null;
      }
    };
  }

  /**
   * Inserts items before the specified position.
   * @param arg arguments
   * @return iterator
   * @throws QueryException evaluation exception
   */
  private Iter insbef(final Iter[] arg) throws QueryException {
    final long pos = Math.max(1, checkItr(arg[1]));
    
    return new Iter() {
      long p = pos;
      boolean last;

      @Override
      public Item next() throws QueryException {
        if(last) return p > 0 ? arg[2].next() : null;
        final boolean sub = p == 0 || --p == 0;
        final Item i = arg[sub ? 2 : 0].next();
        if(i != null) return i;
        if(sub) --p;
        else last = true;
        return next();
      }
    };
  }

  /**
   * Removes an item at a specified position in a sequence.
   * @param arg arguments
   * @return iterator without Item
   * @throws QueryException evaluation exception
   */
  private Iter remove(final Iter[] arg) throws QueryException {
    final long pos = checkItr(arg[1]);

    return new Iter() {
      long c = 0;

      @Override
      public Item next() throws QueryException {
        Item i;
        while((i = arg[0].next()) != null) if(++c != pos) return i;
        return null;
      }
    };
  }

  /**
   * Creates a subsequence out of a sequence, starting with start and
   * ending with end.
   * @param arg arguments
   * @return subsequence
   * @throws QueryException evaluation exception
   */
  private Iter subseq(final Iter[] arg) throws QueryException {
    final long s = Math.round(checkDbl(arg[1]));
    final long e = arg.length > 2 ? s + Math.round(checkDbl(arg[2])) :
      Long.MAX_VALUE;
    
    return arg[0].size() != -1 ? new Iter() {
      // directly access specified items
      long c = Math.max(1, s);

      @Override
      public Item next() {
        return c < e ? arg[0].get(c++ - 1) : null;
      }
    } : new Iter() {
      // run through all items
      long c = 0;

      @Override
      public Item next() throws QueryException {
        while(true) {
          final Item i = arg[0].next();
          if(i == null || ++c >= e) return null;
          if(c >= s) return i;
        }
      }
    };
  }

  /**
   * Reverses a sequence.
   * @param arg arguments
   * @return iterator
   * @throws QueryException evaluation exception
   */
  private Iter reverse(final Iter[] arg) throws QueryException {
    final Iter iter = arg[0];
    
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
   * @param arg arguments
   * @return result of check
   * @throws QueryException evaluation exception
   */
  private boolean deep(final Iter[] arg) throws QueryException {
    if(arg.length == 3) checkColl(arg[2]);

    final Iter iter1 = arg[0];
    final Iter iter2 = arg[1];

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
      // explicit non-short-circuit..
      //while((n1 = niter1.next()) != null & (n2 = niter2.next()) != null) {
      while(true) {
        n1 = nextDeep(niter1);
        n2 = nextDeep(niter2);
        if(n1 == null && n2 == null || n1 == null ^ n2 == null) break;
        if(n1.type != n2.type) return false;
        
        if(n1.type == Type.ELM && !n1.qname().eq(n2.qname())) return false;

        if(n1.type == Type.ATT) {
          if(!n1.qname().eq(n2.qname()) || !Token.eq(n1.str(), n2.str()))
            return false;
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
  
  /**
   * Returns the next node for deep comparison.
   * @param iter iterator
   * @return node
   * @throws QueryException query exception
   */
  private Nod nextDeep(final NodeIter iter) throws QueryException {
    while(true) {
      final Nod n = iter.next();
      if(n == null || n.type != Type.COM && n.type != Type.PI) return n;
    }
  }
}
