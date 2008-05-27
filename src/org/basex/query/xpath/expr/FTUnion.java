package org.basex.query.xpath.expr;

import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;
import org.basex.util.Token;

/**
 * FTUnion Expression. This expresses the union of two FTContains results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTUnion extends FTArrayExpr {
  /** Flag to be set if pointer are needed. */
  public boolean po = false;
  
  /**
   * Constructor.
   * @param e operands joined with the union operator
   */
  public FTUnion(final Expr[] e) {
    exprs = e;
  }

  /**
   * Constructor.
   * @param e operands joined with the union operator
   * @param pointerNeeded to be set if pointer are needed
   */
  public FTUnion(final Expr[] e, final boolean pointerNeeded) {
    exprs = e;
    po = pointerNeeded;
  }

  
  @Override
  public NodeSet eval(final XPContext ctx) throws QueryException {
    int[][] res = null;
    int[][] tmp;
    int[] pntr;
    Object[] o;
      
    Item it = exprs[0].eval(ctx);
    if (it instanceof NodeSet) {
      res = ((NodeSet) it).ftidpos;
      pntr = ((NodeSet) it).ftpointer;

      for (int i = 1; i < exprs.length; i++) {
        it = exprs[i].eval(ctx);
        if (it instanceof NodeSet) {
          tmp = ((NodeSet) it).ftidpos;

          if (po) {
            o = calculateFTOr(res, tmp, pntr); 
            if (o.length == 0) {
              return null;
            }
            res = (int[][]) o[0];
            pntr = (int[]) o[1];
          } else {
            res = calculateFTOr(res, tmp);          
          }
        }
      }
      
      if (po)
        return new NodeSet(Array.extractIDsFromData(res), ctx, res, pntr);
      
      return new NodeSet(Array.extractIDsFromData(res), ctx, res);
    }
    return null;
  }

  @Override
  public Expr compile(final XPContext ctx) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e != el; e++) {
        exprs[e] = exprs[e].compile(ctx);
    }
    return this;
  }
  
  /**
   * Builds an or-conjunction of values1 and values2.
   * @param values1 input set
   * @param values2 input set
   * @return union set int[][]
   */
  public static int[][] calculateFTOr(final int[][] values1,
      final int[][] values2) {
    
    int[][] val1 = values1;
    int[][] val2 = values2;
    
    if(val1 == null || val1[0].length == 0) {
      return val2;
    } else if(val2 == null || val2[0].length == 0) {
      return val1;
    }

    int[][] maxResult = new int[2][val1[0].length + val2[0].length];

    // calculate maximum
    int max = Math.max(val1[0].length, val2[0].length);
    if(max == val1.length) {
      int[][] tmp = val1;
      val1 = val2;
      val2 = tmp;
    }

    // run variable for values1
    int i = 0;
    // run variable for values2
    int k = 0;
    // count inserted elements
    int counter = 0;

    int cmpResult;
    // process smaller set
    while(val1[0].length > i) {
      if(k >= val2[0].length) {
        break;
      }
      cmpResult = Array.compareIntArrayEntry(val1[0][i], 
          val1[1][i], val2[0][k], val2[1][k]);
      if(cmpResult == 1 || cmpResult == 2) {
        // same Id, pos0 < pos1 oder id0 < id1
        maxResult[0][counter] = val2[0][k];
        maxResult[1][counter] = val2[1][k];
        counter++;
        k++;
      } else if(cmpResult == -1 || cmpResult == -2) {
        // same Id, pos0 > pos1 oder id0 > id1
        maxResult[0][counter] = val1[0][i];
        maxResult[1][counter] = val1[1][i];
        counter++;
        i++;
        //k++;
      } else {
        // ids and pos identical
        maxResult[0][counter] = val1[0][i];
        maxResult[1][counter] = val1[1][i];
        counter++;
        i++;
        k++;
      }
    }

    if(counter == 0) return null;

    int[][] returnArray;

    // all elements form values2 are processed
    if(k == val2[0].length && i < val1[0].length) {
      //returnArray = new int[counter+values1.length-i][2];
      returnArray = new int[2][counter + val1[0].length - i];
      // copy left values (bigger than last element values2) from values1
      System.arraycopy(val1[0], i, returnArray[0], counter, 
          val1[0].length - i);
      System.arraycopy(val1[1], i, returnArray[1], counter, 
          val1[0].length - i);
    } else {
      // all elements form values1 are processed
      returnArray = new int[2][counter + val2[0].length - k];
      // copy left values (bigger than last element values1) from values2
      System.arraycopy(val2[0], k, returnArray[0], counter, 
          val2[0].length - k);
      System.arraycopy(val2[1], k, returnArray[1], counter, 
          val2[0].length - k);
    }

    System.arraycopy(maxResult[0], 0, returnArray[0], 0, counter);
    System.arraycopy(maxResult[1], 0, returnArray[1], 0, counter);
    return returnArray;
  }

  /**
   * Builds an or-conjunction of values1 and values2.
   * @param v1 input set
   * @param v2 input set
   * @param po pointer for input set v1
   * @return union set int[][]
   */
  public static Object[] calculateFTOr(final int[][] v1, 
      final int[][] v2, final int[] po) {
    
    int[][] val1 = v1;
    int[][] val2 = v2;
    int[] p = po;
    
    if(val1 == null || val1[0].length == 0) {
      return val2;
    } else if(val2 == null || val2[0].length == 0) {
      return val1;
    }

    int[][] mr = new int[2][val1[0].length + val2[0].length];

    // calculate maximum
    int max = Math.max(val1[0].length, val2[0].length);
    if(max == val1.length) {
      int[][] tmp = val1;
      val1 = val2;
      val2 = tmp;
    }

    // run variable for values1
    int i = 0;
    // run variable for values2
    int k = 0;
    // count inserted elements
    int c = 0;

    // space for new pointer
    int[] pn = new int[mr[0].length + 1];

    // first call FTUnion
    if (p == null) {
      p = new int[pn.length];
    }

    // first element in pointers shows the maximum level
    pn[0] = p[0] + 1;
    
    int cmpResult;
    // process smaller set
    while(val1[0].length > i) {
      if(k >= val2[0].length) {
        break;
      }
      cmpResult = Array.compareIntArrayEntry(val1[0][i], 
          val1[1][i], val2[0][k], val2[1][k]);
      if(cmpResult == 1 || cmpResult == 2) {
        // same Id, pos0 < pos1 or id0 < id1
        mr[0][c] = val2[0][k];
        mr[1][c] = val2[1][k];
        // add new element
        pn[c + 1] = pn[0];        
        c++;
        k++;
      } else if(cmpResult == -1 || cmpResult == -2) {
        // same Id, pos0 > pos1 or id0 > id1
        mr[0][c] = val1[0][i];
        mr[1][c] = val1[1][i];
        // copy old pointer
        pn[c + 1] = p[i + 1];
        c++;
        i++;
        //k++;
      } else {
        // ids and pos identical
        mr[0][c] = val1[0][i];
        mr[1][c] = val1[1][i];
        // copy old pointer
        pn[c + 1] = p[k + 1];
        c++;
        i++;
        k++;
      }
    }

    if(c == 0) return null;

    int[][] returnArray;

    // all elements from values2 are processed
    if(k == val2[0].length && i < val1[0].length) {
      returnArray = new int[2][c + val1[0].length - i];
      // copy left values (bigger than last element val2) from values1
      System.arraycopy(val1[0], i, returnArray[0], c, val1[0].length - i);
      System.arraycopy(val1[1], i, returnArray[1], c, val1[0].length - i);
      for (int z = 0; z < val1[0].length - i; z++)
        pn[c + z + 1] = p[i + 1 + z];

    } else {
      // all elements from val1 are processed
      returnArray = new int[2][c + val2[0].length - k];
      // copy left values (bigger than last element values1) from values2
      System.arraycopy(val2[0], k, returnArray[0], c, val2[0].length - k);
      System.arraycopy(val2[1], k, returnArray[1], c, val2[0].length - k);
      for (int z = 0; z < val2[0].length - k; z++)
        pn[c + z + 1] = pn[0];

    }

    System.arraycopy(mr[0], 0, returnArray[0], 0, c);
    System.arraycopy(mr[1], 0, returnArray[1], 0, c);
    return new Object[]{returnArray, pn};
  }
  
  /**
   * Testdriver for this class.
   * 
   * @param args not used
   */
  public static void main(final String[] args) {
    Object[] o = calculateFTOr(
        new int[][]{{5, 5, 5, 5, 5, 5, 5, 5, 5, 5},
                    {1, 3, 4, 6, 7, 8, 10, 11, 13, 15}}, 
        new int[][]{{5, 5, 5}, {2, 5, 9}}, 
        new int[]{1, 0, 1, 1, 0, 1, 0, 0, 1, 1, 1});
    String s1 = "  ";
    String s2 = "  ";
    String s3 = ((int[]) o[1])[0] + "|";
    for (int i = 0; i < ((int[][]) o[0])[1].length; i++) {
      s1 = s1 + (((int[][]) o[0]) [0][i] + ",");
      s2 = s2 + (((int[][]) o[0]) [1][i] + ",");
      s3 = s3 + ((int[]) o[1]) [i + 1] + ",";
    }
     System.out.println(s1);
     System.out.println(s2);
     System.out.println(s3);
     
  }
  
  @Override
  public void plan(final Serializer ser) throws Exception {
    ser.openElement(this,  Token.token("pointerNeeded"), Token.token(po));
    for (Expr e : exprs) e.plan(ser);
    ser.closeElement(this);
  }

}
 