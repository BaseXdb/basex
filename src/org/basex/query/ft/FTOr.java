package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;

/**
 * FTOr expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public final class FTOr extends FTExpr {
  /**
   * Sequential constructor.
   * @param e expression list
   */
  public FTOr(final FTExpr[] e) {
    super(e);
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    final FTItem it = expr[0].atomic(ctx);
    for(int e = 1; e < expr.length; e++) {
      final FTItem i = expr[e].atomic(ctx);
      it.all.or(i.all);
      it.score(ctx.score.or(it.score(), i.score()));
    }
    return it;
  }

  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    final FTItem[] it = new FTItem[expr.length];
    for(int e = 0; e < expr.length; e++) {
      ir[e] = expr[e].iter(ctx);
      it[e] = ir[e].next();
    }
    
    return new FTIter() {
      @Override
      public FTItem next() throws QueryException { 
        // find item with smallest pre value
        int p = -1;
        for(int i = 0; i < it.length; i++) {
          if(it[i] != null && (p == -1 || it[p].pre > it[i].pre)) p = i;
        }
        // no items left - leave
        if(p == -1) return null;

        // merge all matches
        final FTItem item = it[p];
        for(int i = 0; i < it.length; i++) {
          if(it[i] != null && p != i && item.pre == it[i].pre) {
            item.all.or(it[i].all);
            it[i] = ir[i].next();
          }
        }
        it[p] = ir[p].next();
        return item;
      }
    };
  }
  
  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    // [CG] FT: negative variants currently ignored due to various syntax issues
    //   e.g. (ftnot 'a') ftor (ftnot 'b' with stemming)
    
    int sum = 0;
    for(int i = 0; i < expr.length; i++) {
      if(!expr[i].indexAccessible(ic) || ic.ftnot) return false;
      sum += ic.is;
      ic.ftnot = false;
    }
    ic.is = sum;
    return true;
  }

  @Override
  public String toString() {
    return toString(" " + FTOR + " ");
  }

  
  
  // [CG] FT: to be revised...
  
  /**
   * Index constructor.
   * @param posex pointer on expression
   * @param ftnot flag for ftnot expression
   * @param e expression list
  FTOr(final FTExpr[] e, final int[] posex, final boolean ftnot) {
    super(e);
    pex = posex;
    not = ftnot;
  }
   */

  /** Index of positive expressions.
  int[] pex;
  /** Index of negative (ftnot) expressions.
  int[] nex;
  /** Flag if one result was a ftnot.
  boolean not;
  
  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    for(int i = 0; i < expr.length; i++) ir[i] = expr[i].iter(ctx);
    
    return new FTIter() {
      /** Item array.
      final FTItem[] it = new FTItem[pex.length];
      /** Cache for one of the nodes.
      final IntList cp = new IntList(pex);
      /** Pointer on the positive expression with the lowest pre-values.
      int minp = -1;

      @Override
      public FTItem next() throws QueryException { 
        // [SG] is b needed?
        //boolean b = false;
        for(int i = 0; i < cp.size; i++) {
          final int p = pex[cp.list[i]];
          it[p] = ir[p].next();
          //if(!b) b = !mp[i].ftn.empty();
        }
        cp.reset();
        //if(!b) for(final FTNodeItem c : mp) if(!c.ftn.empty()) break;

        if(minp == -1) {
          minp = 0;
          while(minp < it.length && it[minp] == null) minp++;
          if(minp < it.length) cp.set(minp, 0);
          for(int ip = minp + 1; ip < pex.length; ip++) {
            if(it[ip] != null) {
              final FTItem n1 = it[pex[ip]];
              final FTItem n2 = it[pex[minp]];
              final int d = n1.pre - n2.pre;
              if(d < 0) {
                minp = ip;
                cp.set(ip, 0);
              } else if(d == 0) {
                cp.add(ip);
              }
            }
          }
        }

        minp = -1;
        final FTItem m = it[pex[cp.list[0]]];
        for(int i = 1; i < cp.size; i++) {
          m.union(ctx, it[pex[cp.list[i]]], 0);
          // in case of ftor !"a" ftor "b" "a b" is result
          // [CG] FT: check
          //m.fte.not = false;
        }

        // ftnot causes to set this flag (seq. index mode)
        // [CG] FT: check
        //if(m.empty()) m.fte.not = not;
        return m;
      }
    };
  }

  @Override
  public boolean indexAccessible(final IndexContext ic) throws QueryException {
    final IntList ip = new IntList();
    final IntList in = new IntList();
    final int min = ic.is;
    int sum = 0;

    for(int i = 0; i < expr.length; i++) {
      if(!expr[i].indexAccessible(ic)) return false;
      final boolean ftn = ic.ftnot;
      ic.ftnot = false;

      // [CG] FT: temporary
      if(ftn) return false;
      
      if(ftn) {
        if(ic.is == 0) return false;
        in.add(i);
      } else {
        ip.add(i);
        sum += ic.is;
      }
    }
    nex = in.finish();
    pex = ip.finish();

    // negative expressions found
    if(nex.length > 0) {
      ic.seq = true;
      ic.is = Integer.MAX_VALUE;
      return pex.length == 0;
    }
    
    ic.is = sum > min ? min : sum;
    return true;
  }
  
  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    if(nex.length > 0) {
      // !A FTOR !B = !(a ftand b)
      final FTExpr[] nexpr = new FTExpr[nex.length];
      for(int i = 0; i < nex.length; i++) nexpr[i] = expr[nex[i]].expr[0];
      return new FTNot(new FTAnd(nexpr, pex, nex));
    }
    return pex.length == 1 ? expr[0] : this;

    if(nex.length == 0) return pex.length == 1 ? expr[pex[0]] : this;
    
    not = true;
    pex = new int[expr.length];
    for(int i = 0; i < expr.length; i++) pex[i] = i;
    return this;
  }*/
}
