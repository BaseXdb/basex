package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Bool;
import org.basex.util.IntList;

/**
 * FTUnion Expression. This expresses the union of two FTContains results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTUnion extends FTArrayExpr {
  /** Saving index of positive expressions. */
  private int[] pex;

  /**
   * Constructor.
   * @param e operands joined with the union operator
   */
  public FTUnion(final FTArrayExpr[] e) {
    exprs = e;
  }

  /**
   * Constructor.
   * @param e operands joined with the union operator
   * @param posex pointer on exprs with positiv values
   */
  public FTUnion(final FTArrayExpr[] e, final int[] posex) {
    exprs = e;
    pex = posex;
    mp = new boolean[pex.length];
    cp = new IntList(pex);
  }

  @Override
  public boolean more() {
    boolean b = false;
/*    if (cn.size > 0) {
      for (int i = 0; i < cn.size; i++) {
        mn[i] = exprs[nex[i]].more();
        if (!b) b = mn[i];
      }
      cn.reset(nex.length);
    }
  */  
    if (cp.size > 0) {
      for (int i = 0; i < cp.size; i++) {
        //mp[i] = exprs[pex[cp.get(i)]].more();
        mp[pex[cp.list[i]]] = exprs[pex[cp.list[i]]].more();
        if (!b) b = mp[i];
      }
      cp.reset();
    }
    if (!b) {
      for (boolean c : mp) if(c) return true;
    }
    return b;
  }
  
  /** Cache for one of the nodes. */
  private IntList cp;
  /** Flag is set, if ith expression has any result. */
  private boolean[] mp;
  /** Pointer on the positive expression with the lowest pre-values.*/
  private int minp = -1;
  
  @Override
  public FTNode next(final XPContext ctx) {
    if (minp == -1) {
      minp = 0;
      while(!mp[minp]) minp++;
      cp.set(minp, 0);
      for (int ip = minp + 1; ip < pex.length; ip++) {       
        if (mp[ip]) { 
          if (exprs[pex[ip]].next(ctx).getPre() < 
              exprs[pex[minp]].next(ctx).getPre()) {
            minp = ip;
            cp.set(ip, 0);
          } else if (exprs[pex[ip]].next(ctx).getPre() == 
              exprs[pex[minp]].next(ctx).getPre()) {
              cp.add(ip);
          }
        } 
      }
    }
    
   // if (nex.length == 0) {
      minp = -1;
      final FTNode m = exprs[pex[cp.list[0]]].next(ctx);
      for (int i = 1; i < cp.size; i++) {
        m.merge(exprs[pex[cp.list[i]]].next(ctx), 0);
      }
      return m;
 /*   } else {
      if (minn == -1) {
        minn = 0;
        for (int in = 1; in < nex.length; in++) {
          if (mn[minn] && mn[in] && exprs[nex[in]].next(ctx).getPre() < 
              exprs[nex[minn]].next(ctx).getPre()) {
            minn = in;
          }
        }
      }
      if (minp > -1 && minn > -1) {
        if (minp < minn) {
          FTNode n = exprs[pex[minp]].next(ctx);
          minp = -1;
          return n;
        } else if (minp > minn) {
          minn = -1;
          if (more())
          return next(ctx);
          else return new FTNode();
        } else {
          minn = -1;
          minp = -1;
          if (more()) return next(ctx);
          else return new FTNode();
        }
      } else if (minp > -1) {
        FTNode n = exprs[pex[minp]].next(ctx);
        minp = -1;
        return n;
      } else {
        FTNode n = exprs[nex[minn]].next(ctx);
        minn = -1;
        return n;
      }
      
    }*/
    
    /*
    if (c > -1) {
      FTNode cn = exprs[c].next(ctx);
      if ((c == 0) ? m1 : m0) {
        FTNode nn = exprs[(c == 0) ? 1 : 0].next(ctx);
        if (nn.getPre() < cn.getPre()) {
          return nn;
        } else if (nn.getPre() == cn.getPre()) {
          cn.merge(nn, 0);
          c = -1;
          return cn;
        } else {
          c = (c == 0) ? 1 : 0;
          return cn;
        }
      } else {
        c = -1;
        return cn;
      }
    } else {
      if (m0 && m1) {
        final FTNode c0 = exprs[0].next(ctx);
        final FTNode c1 = exprs[1].next(ctx);
        if (c0.getPre() == c1.getPre()) {
          c0.merge(c1, 0);
          return c0;
        } else if (c0.getPre() < c1.getPre()) {
          c = 1;
          return c0;
        } else {
          c = 0;
          return c1;
        }
      } else if(m0) {
        return exprs[0].next(ctx);
      } else {
        return exprs[1].next(ctx);
      }
    }*/
  }
  
  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    boolean b = false;
    // check each positive expression
    for (int i : pex) {
      final Bool it = (Bool) exprs[i].eval(ctx);
      if (!b) b = it.bool();
    }

/*    for (int i : nex) {
      exprs[i].eval(ctx);
    }
*/
    return Bool.get(b);
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(Expr e : exprs) e.plan(ser);
    ser.closeElement();
  }
}
 