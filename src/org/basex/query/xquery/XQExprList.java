package org.basex.query.xquery;

import org.basex.query.xquery.expr.Expr;
import org.basex.query.xquery.expr.FTAnd;
import org.basex.query.xquery.expr.FTContains;
import org.basex.query.xquery.expr.FTExpr;
import org.basex.query.xquery.expr.FTOr;
import org.basex.query.xquery.item.DNode;
import org.basex.query.xquery.path.Path;
import org.basex.util.Array;

/**
 * This is a simple container for XQuery expressions.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class XQExprList {
  /** Value array. */
  public Expr[] list;
  /** Sum up. */
  private boolean[] su;
  /** Current array size. */
  public int size;
  
  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  public XQExprList(final int is) {
    list = new Expr[is];
    su = new boolean[is];
  }
  
  /**
   * Adds next value.
   * @param v value to be added
   * @param a flag for and expression
   * @param ctx current context
   */
  public void add(final Expr v, final boolean a, final XQContext ctx) {
    boolean b = false;
    if (XQOptimizer.optAndOr && 
        v instanceof FTContains && ctx.item instanceof DNode) {
      final FTContains ftc = (FTContains) v;
      b = ftc.sumUp(((DNode) ctx.item).data); 
      if (b) {
        for (int i = 0; i < size; i++) {
          if (su[i]) {
            final FTContains ftcl = (FTContains) list[i];
            if (((Path) ftc.expr[0]).path().equals(
                ((Path) ftcl.expr[0]).path())) {
              if (a) {
                if (ftcl.expr[1] instanceof FTAnd) 
                  ((FTAnd) ftcl.expr[1]).add((FTExpr) ftc.expr[1]);
                else {
                  final FTAnd and = new FTAnd((FTExpr) ftc.expr[1]);
                  and.add((FTExpr) ((FTContains) list[i]).expr[1]);
                  ftcl.expr[1] = and;
                }
              } else {
                if (ftcl.expr[1] instanceof FTOr) 
                  ((FTOr) ftcl.expr[1]).add((FTExpr) ftc.expr[1]);
                else {
                  final FTOr or = new FTOr((FTExpr) ftc.expr[1]);
                  or.add((FTExpr) ((FTContains) list[i]).expr[1]);
                  ftcl.expr[1] = or;
                }
              }
              return;
            } 
          }
        }
      }
    }
    if(size == list.length) {
      list = Array.extend(list);
      su = Array.extend(su);
    }
    list[size] = v;
    su[size++] = b;
  }

  /**
   * Finishes the int array.
   * @return int array
   */
  public Expr[] finishXQ() {
    return size == list.length ? list : Array.finish(list, size);   
  }
  
  /**
   * Checks if there is only one expression contained.
   * @return result of check
   */
  public boolean single() {
    return size == 1;
  }
}
