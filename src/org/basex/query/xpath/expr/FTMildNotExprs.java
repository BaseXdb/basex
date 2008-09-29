package org.basex.query.xpath.expr;

import org.basex.index.FTNode;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Bool;
import org.basex.util.IntList;

/**
 * FTMildNotExprs. This expresses the mild combination of ftand and ftnot.
 * The selection A not in B matches a token sequence that matches a, but
 * not when it is part of b.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTMildNotExprs extends FTArrayExpr {
  /**
   * Constructor.
   * @param e operands joined with the mild not operator
   */
  public FTMildNotExprs(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public boolean more() {
    return exprs[0].more();
  }

  /** Result node from expression 1. */
  FTNode n1 = null;
  
  @Override
  public FTNode next(final XPContext ctx) {
    final FTNode n0 = exprs[0].next(ctx);
    if (n1 == null) {
      if (exprs[1].more()) n1 = exprs[1].next(ctx);
      else return n0;
    } 
    
    final IntList pos = new IntList(n0.getPre());
    final IntList poi = new IntList(n0.getNumTokens());
    
    if (n0.getPre() < n1.getPre()) {
      return n0;
    } else if (n0.getPre() > n1.getPre()) {
      n1 = null;
      if (more()) return next(ctx);
      else return n0;
    } else {
      boolean mp0 = n0.morePos();
      boolean mp1 = n1.morePos();
      while(mp0 && mp1) {
        if (n0.nextPos() < n1.nextPos()) {
          pos.add(n0.nextPos());
          poi.add(n0.nextPoi());
          mp0 = n0.morePos();
        } else if (n0.nextPos() > n1.nextPos()) {
          mp1 = n1.morePos();
        } else {
          mp0 = n0.morePos();
          mp1 = n1.morePos();
        }
      }
    }
    if (pos.size > 1) {
      return new FTNode(pos.finish(), poi.finish());
    } else {
      n1 = null;
      if (more()) return next(ctx);
      else return new FTNode();
    }
  }
  
  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    ctx.ftpos.st = true;
    final Bool b0 = (Bool) exprs[0].eval(ctx);
    exprs[1].eval(ctx);
    return b0;
  }

  /**
   * Each result wordA, is not allowed to be contained in result wordB.
   * each result for wordA, that is not contained in result set wordB,
   * is added to returned result set.
   *
   * @param resA result allowed to be contained
   * @param resB result not allowed to be contained
   * @return data []
  public static int[][] determineNot(final int[][] resA,
      final int[][] resB) {
    if((resA == null && resB == null) || resA == null) {
      return null;
    }

    // all resultWordA are hits
    if(resB == null) {
      return resA;
    }

    // pointer on resultWordA
    int i = 0;
    // pointer on resultWordB
    int k = 0;
    // counter for result set
    int c = 0;
    // array for result set
    int[][] mr = new int[2][resA[0].length];
    for(; i < resA[0].length; i++) {
      // all done for resultWordB
      if(k == resB[0].length) break;

      // same elements -> further test
      if(resA[0][i] == resB[0][k] && resA[1][i] == resB[1][k]) {
        k++;
      } else {
        // apply result
        mr[0][c] = resA[0][i];
        mr[1][c] = resA[1][i];
        c++;
      }
    }

    int[][] result;
    // all done for resultWordA
    if(i == resA[0].length) {
      if(c == 0) return null;

      // copy only filled cells
      result = new int[2][c];
      System.arraycopy(mr[0], 0, result[0], 0, c);
      System.arraycopy(mr[1], 0, result[1], 0, c);
    } else {
      result = new int[2][c + resA[0].length - i];
      // copy only filled cells
      System.arraycopy(mr[0], 0, result[0], 0, c);
      System.arraycopy(mr[1], 0, result[1], 0, c);
      // copy not processed
      System.arraycopy(resA[0], i, result[0], c,
          resA[0].length - i);
      System.arraycopy(resA[1], i, result[1], c,
          resA[0].length - i);
    }
    return result;
  }
   */
  
  /**
   * Each result wordA, is not allowed to be contained in result wordB.
   * each result for wordA, that is not contained in result set wordB,
   * is added to returned result set.
   *
   * @param resultWordA result allowed to be contained
   * @param resultWordB result not allowed to be contained
   * @return data []
  public static int[] determineNot(final int[] resultWordA,
      final int[] resultWordB) {
    if((resultWordA == null && resultWordB == null) || resultWordA == null) {
      return new int[0];
    }

    // all resultWordA are hits
    if(resultWordB == null) {
      return resultWordA;
    }

    // pointer on resultWordA
    int i = 0;
    // pointer on resultWordB
    int k = 0;
    // counter for result set
    int count = 0;
    // array for result set
    int[] maxResult = new int[resultWordA.length];
    for(; i < resultWordA.length; i++) {
      // all done for resultWordB
      if(k == resultWordB.length) break;

      // same elements -> further test
      if(resultWordA[i] == resultWordB[k] && resultWordA[i] == resultWordB[k]) {
        k++;
      } else {
        // apply result
        maxResult[count] = resultWordA[i];
        count++;
      }
    }

    int[] result;
    // all done for resultWordA
    if(i == resultWordA.length) {
      if(count == 0) return new int[0];

      // copy only filled cells
      result = new int[count];
      System.arraycopy(maxResult, 0, result, 0, count);
    } else {
      result = new int[count + resultWordA.length - i];
      // copy only filled cells
      System.arraycopy(maxResult, 0, result, 0, count);
      // copy not processed
      System.arraycopy(resultWordA, i, result, count,
          resultWordA.length - i);
      }
    return result;
  }
   */
}
