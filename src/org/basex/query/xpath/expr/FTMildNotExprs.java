package org.basex.query.xpath.expr;

import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Array;

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
  public FTMildNotExprs(final Expr[] e) {
    exprs = e;
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    //int[] nodes = ctx.local.nodes;
    
    Item it = exprs[0].eval(ctx);
    if (it instanceof NodeSet) {
      int[][] tmp = ((NodeSet) it).ftidpos;
    
      if (tmp == null || tmp.length == 0 || exprs.length == 1) 
        return new NodeSet(new int[0], ctx);
      it = exprs[1].eval(ctx);
      if (it instanceof NodeSet) {
        tmp = determineNot(tmp, ((NodeSet) it).ftidpos);
        if (tmp == null) return new NodeSet(ctx);
        return new NodeSet(Array.extractIDsFromData(tmp), ctx, tmp);
      }
      
      /*for (int i = 1; i < exprs.length; i++) {
        it = exprs[i].eval(ctx);
        if (it instanceof NodeSet) {
            tmp = determineNot(tmp, ((NodeSet) it).ftidpos);
        }
      }*/
      
      
      //return new NodeSet(Array.extractIDsFromData(tmp), ctx, tmp);
    } 
    return null;  
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e != el; e++) exprs[e] = exprs[e].compile(ctx);
    return this;
  }
  
  /**
   * Each result wordA, is not allowed to be contained in result wordB.
   * each result for wordA, that is not contained in result set wordB,
   * is added to returned result set.
   *
   * @param resA result allowed to be contained
   * @param resB result not allowed to be contained
   * @return data []
   */
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
  
  /**
   * Each result wordA, is not allowed to be contained in result wordB.
   * each result for wordA, that is not contained in result set wordB,
   * is added to returned result set.
   *
   * @param resultWordA result allowed to be contained
   * @param resultWordB result not allowed to be contained
   * @return data []
   */
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

}
