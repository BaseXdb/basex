package org.basex.query.xpath.locpath;

import org.basex.query.ExprInfo;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.util.Array;

/**
 * This is a simple container for PredSimple expressions.
 * Could be used to sum up same FTContains Expr in different PredSimples.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class ExprInfoList {
  /** Value array. */
  public ExprInfo[] list;
  /** Current array size. */
  public int size;
  
  /**
   * Default constructor.
   */
  public ExprInfoList() {
    this(8);
  }
  
  /**
   * Constructor.
   * 
   * @param is initial size of the list
   */
  public ExprInfoList(final int is) {
    list = new ExprInfo[is];
  }
  
  /**
   * Adds next value.
   * Adds sums p with an equal pred or adds
   * p at the end of the list.
   * 
   * @param ei ExprInfo to be added
   * @param and flag for And
   * @return i index of ei
   */
  public int add(final ExprInfo ei, final boolean and) {
    if (ei instanceof PredSimple) {
      final PredSimple p = (PredSimple) ei;
      if (p.expr instanceof FTContains) {
        final FTContains ftc1 = (FTContains) p.expr;
        for (int i = 0; i < size; i++) {
          final Expr ftc = FTContains.sumUp(ftc1, 
              (FTContains) ((PredSimple) list[i]).expr, and);
          if (ftc != null) {
            ((PredSimple) list[i]).expr = ftc;
            return i; 
          }
        }
        if(size == list.length) list = Array.extend(list);
        list[size++] = ei;
        return size - 1;
      }    
    } else if (ei instanceof FTContains) {
      //final Expr expr = (Expr) ei;
      //if (expr instanceof FTContains) {
        final FTContains ftc1 = (FTContains) ei;
        for (int i = 0; i < size; i++) {
          final Expr ftc = FTContains.sumUp(ftc1, 
              (FTContains) list[i], and);
          if (ftc != null) {
            list[i] = ftc;
            return i; 
          }
        }
        if(size == list.length) list = Array.extend(list);
        list[size++] = ei;
        return size - 1;
      //}    
    }
    
        
        /*  
        if (ftc1.expr[0].sameAs(ftc2.expr[0])) {
          // sum 
            if (check(ftc1, ftc2)) {
              final FTSelect fts1 = (FTSelect) ftc1.expr[1];
              final FTSelect fts2 = (FTSelect) ftc2.expr[1];
              if (fts2.getExpr() instanceof FTAnd) {
                FTAnd ftand = (FTAnd) fts2.getExpr();
                ftand.add(fts1.getExpr());
              } else {
                final FTAnd fta = new FTAnd(new FTArrayExpr[]{
                  fts1.getExpr(), fts2.getExpr()});
                fts2.setExpr(fta);
              }
            } else {
              FTAnd fta = new FTAnd(new FTArrayExpr[]{
                  (FTArrayExpr) ftc2.expr[1], (FTArrayExpr) ftc1.expr[1]});
              final FTSelect fts 
                = new FTSelect(fta, new FTPositionFilter(new FTPos()));
              
              ftc2.expr[1] = fts;
          }
          return i;
        }*/

    
    return -1;
  }
  
  /**
   * Finishes the ExprInfo array to an Expr[].
   * @return Expr[] array
   */
  public Expr[] finishE() {
    Expr[] e = new Expr[size];
    for (int i = 0; i < size; i++) e[i] = (Expr) list[i];
    return e;
  }

  /**
   * Finishes the ExprInfo array to an PredicateSimple[].
   * @return PredicateSimple
   */
  public PredSimple[] finishPS() {
    PredSimple[] e = new PredSimple[size];
    for (int i = 0; i < size; i++) e[i] = (PredSimple) list[i];
    return e;
  }

}
