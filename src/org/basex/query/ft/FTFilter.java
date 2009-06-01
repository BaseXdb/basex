package org.basex.query.ft;

import org.basex.query.QueryContext;
import org.basex.query.QueryException;
import org.basex.query.expr.Arr;
import org.basex.query.expr.Expr;
import org.basex.util.BoolList;
import org.basex.util.IntList;

/**
 * Abstract FTFilter expression.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Christian Gruen
 */
public abstract class FTFilter extends Arr {
  /** Units. */
  public enum FTUnit {
    /** Word unit. */      WORD,
    /** Sentence unit. */  SENTENCE,
    /** Paragraph unit. */ PARAGRAPH;
    /**
     * Returns a string representation.
     * @return string representation
     */
    @Override
    public String toString() { return name().toLowerCase(); }
  }
  /** Optional unit. */
  FTUnit unit;
  /** Select reference. */
  FTSelect sel;
  
  @Override
  @SuppressWarnings("unused")
  public Expr comp(final QueryContext ctx) throws QueryException {
    return super.comp(ctx);
  }

  /**
   * Evaluates the filter expression.
   * @param ctx query context
   * @return result of check
   * @throws QueryException query exception
   */
  abstract boolean filter(final QueryContext ctx) throws QueryException;
  
  /**
   * Checks if each token is reached by the ftdistance query.
   * @param mn minimum distance
   * @param mx maximum distance
   * @param dst flag for ftdistance
   * @return result of check
   */
  boolean checkDist(final long mn, final long mx, final boolean dst) {
    final IntList[] il = sortPositions();
    for(int z = 0; z < il[1].size; z++) {
      if(checkDist(z, il[0], il[1], mn, mx, new BoolList(sel.size), dst))
        return true;
    }
    return false;
  }

  /**
   * Checks if each token is reached by the ftdistance query.
   * @param x current position value
   * @param p pos list
   * @param pp pointer list
   * @param mn minimum number
   * @param mx maximum number
   * @param bl boolean list for each token
   * @param dst flag for ftdistance
   * @return boolean result
   */
  private boolean checkDist(final int x, final IntList p,  final IntList pp,
      final long mn, final long mx, final BoolList bl, final boolean dst) {

    if(bl.all(true)) return true;
    int i = x + 1;

    final int p1 = pos(p.list[x], unit);
    while(i < p.size) {
      final int p2 = pos(p.list[i], unit);

      if(dst) {
        // ftdistance
        final int d = p2 - p1 - 1;
        if(d >= mn && d <= mx && !bl.list[pp.list[i]]) {
          bl.list[pp.list[x]] = true;
          bl.list[pp.list[i]] = true;
          if(checkDist(i, p, pp, mn, mx, bl, dst)) return true;
        }
      } else {
        // ftwindow
        final int d = p2 - p1;
        if(mn + d <= mx && !bl.list[pp.list[i]]) {
          bl.list[pp.list[x]] = true;
          bl.list[pp.list[i]] = true;
          if(checkDist(i, p, pp, mn + d, mx, bl, dst)) return true;
        }
      }
      i++;
    }
    return false;
  }

  /**
   * Calculates a position value, dependent on the specified unit.
   * @param p word position
   * @param u unit
   * @return new position
   */
  int pos(final int p, final FTUnit u) {
    if(u == FTUnit.WORD) return p;
    sel.ft.init();
    while(sel.ft.more() && sel.ft.pos != p);
    return u == FTUnit.SENTENCE ? sel.ft.sent : sel.ft.para;
  }

  /**
   * Sorts the position values in numeric order.
   * IntList[0] = position values sorted
   * IntList[1] = pointer to the position values.
   * Each pos value has a pointer, showing which token
   * from the query cloud be found at that pos.
   * @return IntList[] position values and pointer
   */
  IntList[] sortPositions() {
    final IntList[] il = { new IntList(), new IntList() };
    final int[] k = new int[sel.size];
    int min = 0;

    while(true) {
      min = 0;
      boolean q = true;
      for(int j = 0; j < sel.size; j++) {
        if(k[j] > -1) {
          if(k[min] == -1) min = j;
          q = false;
          if(sel.pos[min].list[k[min]] > sel.pos[j].list[k[j]]) min = j;
        }
      }
      if(q) break;

      il[0].add(sel.pos[min].list[k[min]]);
      il[1].add(min);
      if(++k[min] == sel.pos[min].size) k[min] = -1;
    }
    return il;
  }
}
