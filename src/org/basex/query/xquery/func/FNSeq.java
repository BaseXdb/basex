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
import org.basex.query.xquery.iter.SeqIter;
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
    final Iter iter = arg[0];
    SeqIter seq = new SeqIter();
    Item i;

    switch(func) {
      case INDEXOF:
        Item it = arg[1].atomic(this, false);
        if(arg.length == 3) checkColl(arg[2]);

        //int p = 1;
        //while((i = iter.next()) != null) {
        //  if(CmpV.valCheck(i, it) && CmpV.COMP.EQ.e(i, it))
        //    seq.add(Itr.iter(p));
        //  p++;
        //}
        //return seq;
        return indexOf(iter, it);
      case DISTINCT:
        if(arg.length == 2) checkColl(arg[1]);
        //while((i = iter.next()) != null) distinct(seq, i);
        //return seq;
        return distinctPipelined(iter);
      case INSBEF:
        final Iter sub = arg[2];
        long r = Math.max(1, checkItr(arg[1]));

        while((i = iter.next()) != null) {
          if(--r == 0) seq.add(sub);
          seq.add(i.iter());
        }
        if(r > 0) seq.add(sub);
        return seq;
      case REVERSE:
        if(iter instanceof RangeIter) {
          ((RangeIter) iter).reverse();
          return iter;
        }
        while((i = iter.next()) != null) seq.insert(i, 0);
        return seq;
      case REMOVE:
        final long pos = checkItr(arg[1]);
        //long c = 0;
        //while((i = iter.next()) != null) if(++c != pos) seq.add(i);
        //return seq;
        return remove(iter, pos);
      case SUBSEQ:
        final long start = checkItr(arg[1]);
        final long end = arg.length > 2 ? start + checkItr(arg[2]) :
          Long.MAX_VALUE;
        //c = 0;
        //while((i = iter.next()) != null) {
        //  if(++c < start) continue;
        //  if(c >= end) break;
        //  seq.add(i);
        //}
        //return seq;
        return subseq(iter, start, end);
      case DEEPEQ:
        return Bln.get(deep(arg)).iter();
      default:
        throw new RuntimeException("Not defined: " + func);
    }
  }

  /**
   * Looks for the index of an specified input item (pipelined).
   * @param iter input iterator
   * @param it item to searched for in iterator
   * @return position(s) of item
   */
  private Iter indexOf(final Iter iter, final Item it) {
    return new Iter() {
      Item i;
      int index = 0;

      @Override
      public Item next() throws XQException {
        while ((i = iter.next()) != null) {
          index++;
          if(CmpV.valCheck(i, it) && CmpV.COMP.EQ.e(i, it)) {
            return Itr.get(index);
          }
        }
        return null;
      }
    };
  }
  
  /**
   * Looks for the specified item in the sequence.
   * @param sq sequence to be parsed
   * @param i item to be found
   * @throws XQException evaluation exception
   */
  private void distinct(final SeqIter sq, final Item i)
      throws XQException {
    
    final boolean nan = i.n() && i.dbl() != i.dbl();
    for(int r = 0; r < sq.size; r++) {
      final Item c = sq.item[r];
      if(nan && c.dbl() != c.dbl()) return;
      if(CmpV.valCheck(i, c) && CmpV.COMP.EQ.e(i, c)) return;
    }
    sq.add(FNGen.atom(i));
  }
  
  /**
   * Looks for the specified item in the sequence (pipelined).
   * @param iter input iterator
   * @return distinct iterator
   */
  private Iter distinctPipelined(final Iter iter) {
    return new Iter() {
      SeqIter sq = new SeqIter();
      Item i;

      @Override
      public Item next() throws XQException {
        loop1: while((i = iter.next()) != null) {
          
          final boolean nan = i.n() && i.dbl() != i.dbl();
          for(int r = 0; r < sq.size; r++) {
            final Item c = sq.item[r];
            if(nan && c.dbl() != c.dbl()) continue loop1;
            if(CmpV.valCheck(i, c) && CmpV.COMP.EQ.e(i, c)) continue loop1;
          }
          sq.add(FNGen.atom(i));
          return i;
        }
        return null;
      }
    };
  }
  
  /**
   * Removes an Item at a specified position in a sequence (pipelined).
   * @param iter input iterator
   * @param pos position of item to be removed
   * @return iterator without Item
   */
  private Iter remove(final Iter iter, final long pos) {
    return new Iter() {
      long c = 0;
      
      @Override
      public Item next() throws XQException {
        Item i;
        
        while((i = iter.next()) != null) if(++c != pos) return i;
        return null;
      }
    };
  }
  
  /**
   * Creates a subsequence out of a sequence, starting with start and 
   * ending with end (pipelined).
   * @param iter input iterator
   * @param start starting position
   * @param end ending position
   * @return subsequence
   */
  private Iter subseq(final Iter iter, final long start, final long end) {
    return new Iter() {

      long c = 0;
      
      @Override
      public Item next() throws XQException {
        Item i;
        
        while((i = iter.next()) != null) {
          if(++c < start) continue;
          if(c >= end) break;
          return i;
        }
        return null;
      }
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
    while((it1 = iter1.next()) != null & (it2 = iter2.next()) != null) {
      if(it1.n() && it2.n() && it1.dbl() != it1.dbl() && it2.dbl() != it2.dbl())
        continue;

      if(!CmpV.valCheck(it1, it2) || CmpV.COMP.NE.e(it1, it2)) return false;
      if(!it1.node() && !it2.node()) continue;

      // comparing nodes
      if(!(it1.node() && it2.node())) return false;
      final NodeIter niter1 = ((Node) it1).descOrSelf();
      final NodeIter niter2 = ((Node) it2).descOrSelf();

      Node n1 = null, n2 = null;
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
