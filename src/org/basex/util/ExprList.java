package org.basex.util;

import org.basex.query.ExprInfo;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTAnd;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.FTOr;
import org.basex.query.xpath.path.LocPath;
import org.basex.query.xpath.path.Pred;
import org.basex.query.xpath.path.PredSimple;

/**
 * This is a simple container for native int values.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ExprList {
  /** Value array. */
  public ExprInfo[] list;
  /** Sum up. */
  public boolean[] su;
  /** Current array size. */
  public int size = 0;

  /**
   * Default constructor.
   */
  public ExprList() {
    this(8);
  }
  
  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  public ExprList(final int is) {
    list = new ExprInfo[is];
    su = new boolean[is];
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
    FTContains ftc0 = null;
 
    if (v instanceof PredSimple 
        && ((PredSimple) v).expr instanceof FTContains) {
      ftc0 = (FTContains) ((PredSimple) v).expr;
      s = ftc0.expr[0] instanceof LocPath ? 
          ((LocPath) ftc0.expr[0]).singlePath(ctx) : s;
      if (s) {
        for (int i = 0; i < size; i++) {
          if (su[i] && list[i] instanceof PredSimple 
              && ((PredSimple) list[i]).expr instanceof FTContains) {
            final FTContains ftc1 = (FTContains) ((PredSimple) list[i]).expr;
            if (ftc0.expr[0].sameAs(ftc1.expr[0])) {
                if (ftc1.expr[1] instanceof FTAnd) {
                  ((FTAnd) ftc1.expr[1]).add((FTArrayExpr) ftc0.expr[1]);
                } else {
                  final FTAnd fta = new FTAnd(new FTArrayExpr[]{
                      (FTArrayExpr) ftc1.expr[1], (FTArrayExpr) ftc0.expr[1]});
                  ftc1.expr[1] = fta;
                }
              return;
            } 
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
    FTContains ftc0 = null;
 
    if (v instanceof FTContains) {
      ftc0 = (FTContains) v;
      s = ftc0.expr[0] instanceof LocPath ? 
          ((LocPath) ftc0.expr[0]).singlePath(ctx) : s;
      if (s) {
        for (int i = 0; i < size; i++) {
          if (su[i] && list[i] instanceof FTContains) {
            final FTContains ftc1 = (FTContains) list[i];
            if (ftc0.expr[0].sameAs(ftc1.expr[0])) {
              if (and) {
                if (ftc1.expr[1] instanceof FTAnd) {
                  ((FTAnd) ftc1.expr[1]).add((FTArrayExpr) ftc0.expr[1]);
                } else {
                  final FTAnd fta = new FTAnd(new FTArrayExpr[]{
                      (FTArrayExpr) ftc1.expr[1], (FTArrayExpr) ftc0.expr[1]});
                  ftc1.expr[1] = fta;
                }
              } else {
                if (ftc1.expr[1] instanceof FTOr) {
                  ((FTOr) ftc1.expr[1]).add((FTArrayExpr) ftc0.expr[1]);
                } else {
                  final FTOr fta = new FTOr(new FTArrayExpr[]{
                      (FTArrayExpr) ftc1.expr[1], (FTArrayExpr) ftc0.expr[1]});
                  ftc1.expr[1] = fta;
                }
              }
              return;
            } 
          }
        }
      }
    }
    list[size] = v;
    su[size++] = s;
  }
  
  /**
   * Finishes the epxr info array.
   * @return pred array
   */
  public Pred[] finishPS() {
    Pred[] pr = new Pred[size];
    for (int i = 0; i < size; i++) pr[i] = (PredSimple) list[i];
    return pr; 
  }
  
  /**
   * Finishes the expr info array.
   * @return  array
   */
  public Expr[] finishE() {
    Expr[] e = new Expr[size];
    for (int i = 0; i < size; i++) e[i] = (Expr) list[i];
    return e; 
  }

}
