package org.basex.query.xpath.expr;

import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.NodeSet;
import org.basex.query.xpath.values.Item;
import org.basex.util.Array;

/**
 * FTUnaryNotExprs. This expresses the mild combination of ftand and ftnot.
 * The selection A not in B matches a token sequence that matches a, but
 * not when it is part of b.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTUnaryNotExprs extends FTArrayExpr {
  /**
   * Constructor.
   * @param e operands joined with the mild not operator
   */
  public FTUnaryNotExprs(final FTArrayExpr[] e) {
    exprs = e;
  }

  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    //int[] nodes = ctx.local.nodes;
    
    Item it = exprs[0].eval(ctx);
    
    if (it instanceof NodeSet) {
      int[][] l = ((NodeSet) it).ftidpos;
      if (l == null || l[0] == null || l[0].length == 0) 
        return new NodeSet(ctx);

        it = exprs[1].eval(ctx);
        if (it instanceof NodeSet)          
          l = determineNot(l, ((NodeSet) it).eval(ctx).ftidpos);
       
        if (l == null) return new NodeSet(ctx);
        // pointer could always be set, because every id out of l
        return new NodeSet(Array.extractIDsFromData(l), ctx, l);
      }

    // should not happen
    return null;  
  }

  /**
   * Each result wordA, is not allowed to be contained in result wordB.
   *
   * @param resA result allowed to be contained
   * @param resB result not allowed to be contained
   * @return data []
   */
  public static int[][] determineNot(final int[][] resA, final int[][] resB) {
    if((resA == null && resB == null) || resA == null || 
       resA[0].length == 0 && resB[0].length == 0 || resA[0].length == 0) {
      return null;
    }

    // all resultWordA are hits
    if(resB == null || resB[0].length == 0) {
      return resA;
    }

    // pointer on resultWordA
    int i = 0;
    // pointer on resultWordB
    int k = 0;
    // counter for result set
    int count = 0;
    // array for result set
    int[][] maxResult = new int[2][resA[0].length];
    for(; i < resA[0].length; i++) {
      // ignore all minor values
      while(k < resB[0].length &&
          resA[0][i] > resB[0][k]) k++;

      // all done for resultWordB
      if(k == resB[0].length) break;

      // same elements -> duplicated elements are possible (in both arrays)
      if(resA[0][i] == resB[0][k]) {
        // ignore following same elements in resultWordA
        while(i < resA[0].length - 1 && resA[0][i] == resA[0][i + 1]) {
          i++;
        }

        // ignore following same elements in resultWordB
        while(k < resB[0].length - 1 && resB[0][k] == resB[0][k + 1]) 
          k++;

        // pointer on next element
        k++;
      } else {
        // apply result
        maxResult[0][count] = resA[0][i];
        maxResult[1][count] = resA[1][i];
        count++;
      }
    }

    int[][] result;
    // all done for resultWordA
    if(i == resA[0].length) {
      if(count == 0) return null;

      // copy only filled cells
      result = new int[2][count];
      System.arraycopy(maxResult[0], 0, result[0], 0, count);
      System.arraycopy(maxResult[1], 0, result[1], 0, count);
    } else {
      result = new int[2][count + resA[0].length - i];
      // copy only filled cells
      System.arraycopy(maxResult[0], 0, result[0], 0, count);
      System.arraycopy(maxResult[1], 0, result[1], 0, count);
      // copy not processed
      System.arraycopy(resA[0], i, result[0], count,
          resA[0].length - i);
      System.arraycopy(resA[1], i, result[1], count,
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
      // ignore all minor values
      while(k < resultWordB.length &&
          resultWordA[i] > resultWordB[k]) k++;

      // all done for resultWordB
      if(k == resultWordB.length) break;

      // same elements -> duplicated elements are possible (in both arrays)
      if(resultWordA[i] == resultWordB[k]) {
        // ignore following same elements in resultWordA
        while(i < resultWordA.length - 1 &&
            resultWordA[i] == resultWordA[i + 1]) i++;

        // ignore following same elements in resultWordA
        while(k < resultWordB.length - 1 &&
            resultWordB[k] == resultWordB[k + 1]) k++;

        // pointer on next element
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

   @Override
   public void plan(final Serializer ser) throws Exception {
     ser.openElement(this);
     for (Expr e : exprs) e.plan(ser);
     ser.closeElement(this);
   }

}
