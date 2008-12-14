package org.basex.query.xpath.expr;

import java.io.IOException;
import org.basex.data.Serializer;
import org.basex.index.FTNode;
import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.item.Bln;
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
   * @param posex pointer on expressions with positive values
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
    if (cp.size > 0) {
      for (int i = 0; i < cp.size; i++) {
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
  public FTNode next(final QueryContext ctx) {
    if (minp == -1) {
      minp = 0;
      while(!mp[minp]) minp++;
      cp.set(minp, 0);
      for (int ip = minp + 1; ip < pex.length; ip++) {       
        if (mp[ip]) { 
          final FTNode n1 = exprs[pex[ip]].next(ctx);
          final FTNode n2 = exprs[pex[minp]].next(ctx);
          
          if (n1.getPre() < n2.getPre()) {
            minp = ip;
            cp.set(ip, 0);
          } else if (n1.getPre() == n2.getPre()) {
              cp.add(ip);
          }
        } 
      }
    }
    
    minp = -1;
    final FTNode m = exprs[pex[cp.list[0]]].next(ctx);
    for (int i = 1; i < cp.size; i++) {
      m.merge(exprs[pex[cp.list[i]]].next(ctx), 0);
    }
    return m;
  }
  
  @Override
  public Bln eval(final XPContext ctx) throws QueryException {
    boolean b = false;
    // check each positive expression
    for(final int i : pex) b |= ((Bln) exprs[i].eval(ctx)).bool();
    return Bln.get(b);
  }
  
  @Override
  public void plan(final Serializer ser) throws IOException {
    ser.openElement(this);
    for(Expr e : exprs) e.plan(ser);
    ser.closeElement();
  }
}
 