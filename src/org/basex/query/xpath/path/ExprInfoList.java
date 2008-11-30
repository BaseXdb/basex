package org.basex.query.xpath.path;
/**
package org.basex.query.xpath.locpath;

import org.basex.query.ExprInfo;
import org.basex.query.FTPos;
import org.basex.query.xpath.expr.Expr;
import org.basex.query.xpath.expr.FTAnd;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.FTOr;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.expr.FTSelect;
import org.basex.util.Array;

 * This is a simple container for PredSimple expressions.
 * Could be used to sum up same FTContains Expr in different PredSimples.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
public final class ExprInfoList {
  /** Value array.
  public ExprInfo[] list;
  /** Current array size.
  public int size;
  
  /**
   * Default constructor.
  public ExprInfoList() {
    this(8);
  }
  
  /**
   * Constructor.
   * 
   * @param is initial size of the list
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
  private int add(final ExprInfo ei, final boolean and) {
    if (ei instanceof PredSimple) {
      final PredSimple p = (PredSimple) ei;
      if (p.expr instanceof FTContains) {
        final FTContains ftc1 = (FTContains) p.expr;
        for (int i = 0; i < size; i++) {
          final Expr ftc = sumUp(ftc1, 
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
        final FTContains ftc1 = (FTContains) ei;
        for (int i = 0; i < size; i++) {
          final Expr ftc = sumUp(ftc1, 
              (FTContains) list[i], and);
          if (ftc != null) {
            list[i] = ftc;
            return i; 
          }
        }
        if(size == list.length) list = Array.extend(list);
        list[size++] = ei;
        return size - 1;
    }
    return -1;
  }

  /**
   * Sum up two FTContains Expr to one.
   * If Summing up isn't possible, return null.
   * 
   * @param ftc1 FTContains 1
   * @param ftc2 FTContains 2
   * @param and Flag for FTAnd or FTOr
   * @return FTContains or null
  private static Expr sumUp(final FTContains ftc1, final FTContains ftc2, 
      final boolean and) {
    final LocPathRel l1 = (LocPathRel) ftc1.expr[0];
    final LocPathRel l2 = (LocPathRel) ftc2.expr[0];
    
    if ((l1.steps.get(0).test instanceof TestName ||
        l1.steps.get(0).test == TestNode.TEXT)
        && l1.steps.get(0).test.sameAs(l2.steps.get(0).test) 
        && l1.steps.size() == 1) {
      // sum 
      if (check(ftc1, ftc2)) {
        final FTSelect fts1 = (FTSelect) ftc1.expr[1];
        final FTSelect fts2 = (FTSelect) ftc2.expr[1];
        if (and) {
          if (fts2.getExpr() instanceof FTAnd) {
            FTAnd ftand = (FTAnd) fts2.getExpr();
            ftand.add(fts1.getExpr());
          } else {
            final FTAnd fta = new FTAnd(new FTArrayExpr[]{
              fts1.getExpr(), fts2.getExpr()});
            fts2.setExpr(fta);
          }
          return ftc2;
        } else {
          if (fts2.getExpr() instanceof FTOr) {
            FTOr ftor = (FTOr) fts2.getExpr();
            ftor.add(fts1.getExpr());
          } else {
            final FTOr ftor = new FTOr(new FTArrayExpr[]{
              fts1.getExpr(), fts2.getExpr()});
            fts2.setExpr(ftor);
          } 
        }
        //return ftc2;
      } else {
        FTSelect fts;
        if (and) {
          FTAnd fta = new FTAnd(new FTArrayExpr[]{
              (FTArrayExpr) ftc2.expr[1], (FTArrayExpr) ftc1.expr[1]});
          fts = new FTSelect(fta, new FTPositionFilter(new FTPos()));          
        } else {
          FTAnd fta = new FTAnd(new FTArrayExpr[]{
              (FTArrayExpr) ftc2.expr[1], (FTArrayExpr) ftc1.expr[1]});
          fts = new FTSelect(fta, new FTPositionFilter(new FTPos()));
        }
        ftc2.expr[1] = fts;
      }
      return ftc2;
    }
    return null;
  }
  
  /**
   * Check if two FTContains expressions could be summed up.
   * @param ftc1 FTContains expression1 
   * @param ftc2 FTContains expression2
   * @return boolean result
  private static boolean check(final FTContains ftc1, final FTContains ftc2) {
    if (ftc1.expr[1] instanceof FTSelect && ftc2.expr[1] instanceof FTSelect) {
      final FTSelect fts1 = (FTSelect) ftc1.expr[1];
      final FTSelect fts2 = (FTSelect) ftc2.expr[1];
      return fts1.checkSumUp(fts2.ftpos);
    }
    return false;
  }
  
  /**
   * Finishes the ExprInfo array to an Expr[].
   * @return Expr[] array
  public Expr[] finishE() {
    Expr[] e = new Expr[size];
    for (int i = 0; i < size; i++) e[i] = (Expr) list[i];
    return e;
  }

  /**
   * Finishes the ExprInfo array to an PredicateSimple[].
   * @return PredicateSimple
  public PredSimple[] finishPS() {
    PredSimple[] e = new PredSimple[size];
    for (int i = 0; i < size; i++) e[i] = (PredSimple) list[i];
    return e;
  }
}
*/
