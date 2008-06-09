package org.basex.query.xquery.func;

import org.basex.query.xquery.XQContext;
import org.basex.query.xquery.XQException;
import org.basex.query.xquery.expr.CmpV;
import org.basex.query.xquery.item.Bln;
import org.basex.query.xquery.item.Item;
import org.basex.query.xquery.item.Itr;
import org.basex.query.xquery.item.Node;
import org.basex.query.xquery.item.Type;
import org.basex.query.xquery.iter.Iter;
import org.basex.query.xquery.iter.NodeIter;
import org.basex.query.xquery.iter.RangeIter;
import org.basex.query.xquery.util.ItemSet;
import org.basex.query.xquery.util.SeqBuilder;
import org.basex.util.Token;

/**
 * Sequence functions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
final class FNSeq extends Fun {
  @Override
  public Iter iter(final XQContext ctx, final Iter[] arg) throws XQException {
    switch(func) {
      case INDEXOF:  return indexOf(arg);
      case DISTINCT: return distinct(arg);
      case INSBEF:   return insbef(arg);
      case REVERSE:  return reverse(arg);
      case REMOVE:   return remove(arg);
      case SUBSEQ:   return subseq(arg);
      case DEEPEQ:   return Bln.get(deep(arg)).iter();
      default: throw new RuntimeException("Not defined: " + func);
    }
  }

  /**
   * Looks for the index of an specified input item.
   * @param arg arguments
   * @return position(s) of item
   * @throws XQException evaluation exception
   */
  private Iter indexOf(final Iter[] arg) throws XQException {
    final Item it = arg[1].atomic(this, false);
    if(arg.length == 3) checkColl(arg[2]);

    return new Iter() {
      int c = 0;

      @Override
      public Item next() throws XQException {
        while(true) {
          final Item i = arg[0].next();
          if(i == null) return null;
          c++;
          if(CmpV.valCheck(i, it) && CmpV.COMP.EQ.e(i, it)) return Itr.get(c);
        }
      }
    };
  }

  /**
   * Looks for distinct values in the sequence.
   * @param arg arguments
   * @return distinct iterator
   * @throws XQException evaluation exception
   */
  private Iter distinct(final Iter[] arg) throws XQException {
    if(arg.length == 2) checkColl(arg[1]);
    
    return new Iter() {
      ItemSet map = new ItemSet();

      @Override
      public Item next() throws XQException {
        Item i;
        while((i = arg[0].next()) != null) if(map.index(i)) return i;
        map = null;
        return null;
      }
    };
  }

  /**
   * Inserts items before the specified position.
   * @param arg arguments
   * @return iterator
   * @throws XQException evaluation exception
   */
  private Iter insbef(final Iter[] arg) throws XQException {
    final long pos = Math.max(1, checkItr(arg[1]));
    
    return new Iter() {
      long p = pos;
      boolean last;

      @Override
      public Item next() throws XQException {
        if(last) return p > 0 ? arg[2].next() : null;
        boolean sub = p == 0 || --p == 0;
        final Item i = arg[sub ? 2 : 0].next();
        if(i != null) return i;
        if(sub) --p;
        else last = true;
        return next();
      }
    };
  }

  /**
   * Removes an Item at a specified position in a sequence.
   * @param arg arguments
   * @return iterator without Item
   * @throws XQException evaluation exception
   */
  private Iter remove(final Iter[] arg) throws XQException {
    final long pos = checkItr(arg[1]);

    return new Iter() {
      long c = 0;

      @Override
      public Item next() throws XQException {
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
   * @throws XQException evaluation exception
   */
  private Iter subseq(final Iter[] arg) throws XQException {
    final long start = checkItr(arg[1]);
    final long end = arg.length > 2 ? start + checkItr(arg[2]) : Long.MAX_VALUE;
    
    return new Iter() {
      long c = 0;

      @Override
      public Item next() throws XQException {
        while(true) {
          final Item i = arg[0].next();
          if(i == null || ++c >= end) return null;
          if(c >= start) return i;
        }
      }
    };
  }

  /**
   * Reverses a sequence.
   * @param arg arguments
   * @return iterator
   * @throws XQException evaluation exception
   */
  private Iter reverse(final Iter[] arg) throws XQException {
    final Iter iter = arg[0];
    
    // process numeric iterator...
    if(iter instanceof RangeIter) {
      ((RangeIter) iter).reverse();
      return iter;
    }
    
    // process any other iterator...
    final SeqBuilder sb = new SeqBuilder();
    Item i;
    while((i = iter.next()) != null) sb.a(i);
    
    return new Iter() {
      int c = sb.size;

      @Override
      public long size() { return sb.size; }
      @Override
      public Item next() { return --c < 0 ? null : sb.item[c]; }
    };
  }


  /**
   * Checks items for deep equality.
   * @param arg arguments
   * @return result of check
   * @throws XQException evaluation exception
   */
  private boolean deep(final Iter[] arg) throws XQException {
    if(arg.length == 3) checkColl(arg[2]);

    final Iter iter1 = arg[0];
    final Iter iter2 = arg[1];

    Item it1 = null;
    Item it2 = null;
    // non-short-circuit logic (one & sign) to run both iterators..
    while((it1 = iter1.next()) != null & (it2 = iter2.next()) != null) {
      if(it1.n() && it2.n() && Double.isNaN(it1.dbl()) &&
          Double.isNaN(it2.dbl())) continue;

      if(!CmpV.valCheck(it1, it2) || CmpV.COMP.NE.e(it1, it2)) return false;
      if(!it1.node() && !it2.node()) continue;

      // comparing nodes
      if(!(it1.node() && it2.node())) return false;
      final NodeIter niter1 = ((Node) it1).descOrSelf();
      final NodeIter niter2 = ((Node) it2).descOrSelf();

      Node n1 = null, n2 = null;
      // non-short-circuit logic (one & sign) to run both iterators..
      while((n1 = niter1.next()) != null & (n2 = niter2.next()) != null) {
        if(n1.type != n2.type) return false;
        if((n1.type == Type.ELM || n1.type == Type.PI) &&
          !n1.qname().eq(n2.qname())) return false;

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

        Node a1 = null, a2 = null;
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
