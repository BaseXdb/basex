package org.basex.query.xpath;

import org.basex.query.ExprInfo;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTAnd;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.FTOr;
import org.basex.query.xpath.path.LocPath;
import org.basex.query.xpath.path.Pred;
import org.basex.query.xpath.path.PredSimple;
import org.basex.util.Array;

/**
 * This is a simple container for XPath expressions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class ExprList {
  /** Value array. */
  public ExprInfo[] list;
  /** Sum up. */
  public boolean[] su;
  /** Current array size. */
  public int size;

  /**
   * Default constructor.
   */
  public ExprList() {
    list = new ExprInfo[1];
    su = new boolean[1];
  }
  
  /**
   * Adds next value.
   * @param v value to be added
   * @param ctx current context
   */
  public void addPS(final Pred v, final XPContext ctx) {
    if(size == list.length) {
      list = Array.extend(list);
      su = Array.extend(su);
    }
    
    boolean s = false;
    if(v instanceof PredSimple && ((PredSimple) v).expr instanceof FTContains) {
      final FTContains ft = (FTContains) ((PredSimple) v).expr;
      if(ft.cont instanceof LocPath) s = ((LocPath) ft.cont).singlePath(ctx);
      if(s) {
        for(int i = 0; i < size; i++) {
          if(!su[i]) continue;

          final FTContains ftc1 = (FTContains) ((PredSimple) list[i]).expr;
          if(ft.cont.sameAs(ftc1.cont)) {
            if(ftc1.query instanceof FTAnd) {
              ftc1.query.add(ft.query);
            } else {
              ftc1.query = new FTAnd(ftc1.query, ft.query);
            }
            return;
          }
        }
      }
    }
    list[size] = v;
    su[size++] = s;
  }

  /**
   * Adds next value.
   * @param v value to be added
   * @param ctx current context
   * @param and flag for and expression
   */
  public void add(final Expr v, final XPContext ctx, final boolean and) {
    if(size == list.length) {
      list = Array.extend(list);
      su = Array.extend(su);
    }
    
    boolean s = false;
    if(v instanceof FTContains) {
      final FTContains ft = (FTContains) v;
      if(ft.cont instanceof LocPath) s = ((LocPath) ft.cont).singlePath(ctx);
      if(s) {
        for(int i = 0; i < size; i++) {
          if(!su[i]) continue;

          final FTContains ftc1 = (FTContains) list[i];
          if(ft.cont.sameAs(ftc1.cont)) {
            if(and) {
              if(ftc1.query instanceof FTAnd) {
                ftc1.query.add(ft.query);
              } else {
                ftc1.query = new FTAnd(ftc1.query, ft.query);
              }
            } else {
              if(ftc1.query instanceof FTOr) {
                ftc1.query.add(ft.query);
              } else {
                ftc1.query = new FTOr(ftc1.query, ft.query);
              }
            }
            return;
          }
        }
      }
    }
    list[size] = v;
    su[size++] = s;
  }
  
  /**
   * Finishes the predicate array.
   * @return pred array
   */
  public Pred[] finishPS() {
    final Pred[] pr = new Pred[size];
    for(int i = 0; i < size; i++) pr[i] = (PredSimple) list[i];
    return pr; 
  }
  
  /**
   * Finishes the expression array.
   * @return array
   */
  public Expr[] finishE() {
    final Expr[] e = new Expr[size];
    for(int i = 0; i < size; i++) e[i] = (Expr) list[i];
    return e; 
  }
}
