package org.basex.query.xpath.locpath;

import org.basex.query.FTPos;
import org.basex.query.xpath.expr.FTAnd;
import org.basex.query.xpath.expr.FTArrayExpr;
import org.basex.query.xpath.expr.FTContains;
import org.basex.query.xpath.expr.FTPositionFilter;
import org.basex.query.xpath.expr.FTSelect;
import org.basex.util.Array;

/**
 * This is a simple container for PredSimple expressions.
 * Could be used to sum up same FTContains Expr in different PredSimples.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Christian Gruen
 */
public final class PredSimpleList {
  /** Value array. */
  public PredSimple[] list;
  /** Current array size. */
  public int size;
  
  /**
   * Default constructor.
   */
  public PredSimpleList() {
    this(8);
  }
  
  /**
   * Constructor.
   * 
   * @param is initial size of the list
   */
  public PredSimpleList(final int is) {
    list = new PredSimple[is];
  }
  
  /**
   * Adds next value.
   * Adds sums p with an equal pred or adds
   * p at the end of the list.
   * 
   * @param p PredSimple to be added
   * @return i index of p
   */
  public int add(final PredSimple p) {
    if (p.expr instanceof FTContains) {
      final FTContains ftc1 = (FTContains) p.expr;
      FTContains ftc2;
      
      for (int i = 0; i < size; i++) {
        ftc2 = (FTContains) list[i].expr;
        if (ftc1.expr1.sameAs(ftc2.expr1)) {
          // sum 
            if (check(ftc1, ftc2)) {
              final FTSelect fts1 = (FTSelect) ftc1.expr2;
              final FTSelect fts2 = (FTSelect) ftc2.expr2;
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
                  (FTArrayExpr) ftc2.expr2, (FTArrayExpr) ftc1.expr2});
              final FTSelect fts 
                = new FTSelect(fta, new FTPositionFilter(new FTPos()));
              
              ftc2.expr2 = fts;
          }
          return i;
        }
      }
      
      if(size == list.length) list = Array.extend(list);
      list[size++] = p;
      return size - 1;
    }
    
    return -1;
  }
  
  /**
   * Check if two FTContains expressions could be summed up.
   * @param ftc1 FTContains expression1 
   * @param ftc2 FTContains expression2
   * @return boolean result
   */
  private boolean check(final FTContains ftc1, final FTContains ftc2) {
    if (ftc1.expr2 instanceof FTSelect && ftc2.expr2 instanceof FTSelect) {
      final FTSelect fts1 = (FTSelect) ftc1.expr2;
      final FTSelect fts2 = (FTSelect) ftc2.expr2;
      return fts1.checkSumUp(fts2.ftpos);
    }
    return false;
  }
  
  /**
   * Finishes the int array.
   * @return int array
   */
  public PredSimple[] finish() {
    return size == list.length ? list : Array.finish(list, size);
  }
}
