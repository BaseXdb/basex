package org.basex.query.ft;

import static org.basex.query.QueryTokens.*;
import org.basex.query.IndexContext;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.item.FTItem;
import org.basex.query.iter.FTIter;
import org.basex.util.IntList;

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

  /**
   * Index constructor.
   * @param posex pointer on expression
   * @param ftnot flag for ftnot expression
   * @param e expression list
   */
  FTOr(final FTExpr[] e, final int[] posex, final boolean ftnot) {
    super(e);
    pex = posex;
    not = ftnot;
  }

  @Override
  public FTItem atomic(final QueryContext ctx) throws QueryException {
    FTItem it = null; 
    for(final FTExpr e : expr) {
      final FTItem i = e.atomic(ctx);
      if(it != null) {
        it.all.or(i.all);
        it.score(ctx.score.or(it.score(), i.score()));
      } else {
        it = i;
      }
    }
    return it;
  }

  @Override
  public String toString() {
    return toString(" " + FTOR + " ");
  }

  
  
  // [CG] FT: to be revised...
  
  /** Index of positive expressions. */
  int[] pex;
  /** Index of negative (ftnot) expressions. */
  int[] nex;
  /** Flag if one result was a ftnot. */
  boolean not;
  
  @Override
  public FTIter iter(final QueryContext ctx) throws QueryException {
    // initialize iterators
    final FTIter[] ir = new FTIter[expr.length];
    for(int i = 0; i < expr.length; i++) ir[i] = expr[i].iter(ctx);
    
    return new FTIter() {
      /** Item array. */
      final FTItem[] it = new FTItem[pex.length];
      /** Cache for one of the nodes. */
      final IntList cp = new IntList(pex);
      /** Pointer on the positive expression with the lowest pre-values.*/
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
    // [CG] FT: skip index access
    if(1 == 1) return false;

    final IntList ip = new IntList();
    final IntList in = new IntList();
    final int min = ic.is;
    int sum = 0;

    for(int i = 0; i < expr.length; i++) {
      final boolean ftnot = ic.ftnot;
      ic.ftnot = false;
      final boolean ia = expr[i].indexAccessible(ic);
      final boolean ftn = ic.ftnot;
      ic.ftnot = ftnot;
      if(!ia) return false;

      if(ftn) {
        if(ic.is > 0) in.add(i);
        else {
          ic.seq = true;
          ic.is = Integer.MAX_VALUE;
          return false;
        }
      } else if(ic.is > 0) {
        ip.add(i);
        sum += ic.is;
      }
    }
    nex = in.finish();
    pex = ip.finish();

    if(pex.length == 0 && nex.length > 0) {
      ic.seq = true;
      ic.is = Integer.MAX_VALUE;
    } else if(nex.length > 0 && pex.length > 0) {
      ic.seq = true;
      ic.is = Integer.MAX_VALUE;
      /* [SG] find solution here
       *
       * Will get complicated for arbitrarily mixed and nested pos./neg.
       * expressions..  A | !(B & (!C & D)) etc.
       *
       * Approach from the relational world (but not really worth the trouble):
       * Normalization to DNF/CNF.
       */
      return false;
    } else {
      ic.is = sum > min ? min : sum;
    } 
    return true;
  }
  
  @Override
  public FTExpr indexEquivalent(final IndexContext ic) throws QueryException {
    for(int i = 0; i < expr.length; i++) expr[i] = expr[i].indexEquivalent(ic);

    if(pex.length == 0) {
      // !A FTOR !B = !(a ftand b)
      FTExpr[] nexpr = new FTExpr[nex.length];
      for(int i = 0; i < nex.length; i++) nexpr[i] = expr[nex[i]].expr[0];
      return new FTNot(new FTAnd(nexpr, pex, nex));
    }

    if(nex.length == 0) return pex.length == 1 ? expr[pex[0]] : this;
    
    not = true;
    pex = new int[expr.length];
    for(int i = 0; i < expr.length; i++) pex[i] = i;
    return this;
  }
}
