package org.basex.query.xpath.expr;

import org.basex.core.Prop;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Item;
import org.basex.query.xpath.values.NodeSet;
import org.basex.util.Array;

/**
 * FTIntersection Expression. 
 * This expresses the intersection of two FTContains results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTIntersection extends FTArrayExpr {
  /** Flag for order preserving at intersection determination. */
  boolean pres;
  /** Query context. */
  public XPContext ctx;
  
  /**
   * Constructor.
   * @param e operands joined with the union operator
   * @param opres used to preserve order of query strings
   * @param context XPathContext
   * @Deprecated
   */
  public FTIntersection(final Expr[] e, final boolean opres, 
      final XPContext context) {
    exprs = e;
    pres = opres;
    ctx = context;
  }

  /**
   * Constructor.
   * @param e operands joined with the union operator
   * @param option FTOption with special features for the evaluation
   * @param context XPathContext
   */
  public FTIntersection(final Expr[] e, final FTOption option, 
      final XPContext context) {
    exprs = e;
    fto = option;
    ctx = context;
  }
  
  @Override
  public NodeSet eval(final XPContext context) throws QueryException {
    int[][] res = null;
    int[][] tmp;
    int[] pntr = null;
    Object[] o;

    Item it = exprs[0].eval(context);
    if(it instanceof NodeSet) {
      res = ((NodeSet) it).ftidpos;
      pntr = ((NodeSet) it).ftpointer;
      
      for (int i = 1; i < exprs.length; i++) {
        it = exprs[i].eval(context);
        if (it instanceof NodeSet) {
          tmp = ((NodeSet) it).ftidpos;
          if (pres) {
            o = calculateFTAndOrderPreserving(res, tmp, pntr); 
            if (o != null && o.length == 2 && o[0] == null && o[1] == null) {
              return new NodeSet(ctx);
            }
            res = (int[][]) o[0];
            pntr = (int[]) o[1];
          } else if(Prop.ftdetails) {
            o = calculateFTAnd(res, tmp, pntr); 
            if (o != null && o.length == 2 && o[0] == null && o[1] == null) {
              return new NodeSet(ctx);
            }
            res = (int[][]) o[0];
            pntr = (int[]) o[1];
          } else {
            res = calculateFTAnd(res, tmp);
          }
        }
      }
      if (pres || Prop.ftdetails) {
        return new NodeSet(Array.extractIDsFromData(res), context, res, pntr);
      }
      return new NodeSet(Array.extractIDsFromData(res), context, res);
    }
    return null;
  }

  @Override
  public Expr compile(final XPContext context) throws QueryException {
    final int el = exprs.length;
    for(int e = 0; e != el; e++) exprs[e] = exprs[e].compile(context);
    return this;
  }

  /**
   * Built join for value1 and value2; used key is id.
   * @param val1 input set int[][]
   * @param val2 input set  int[][]
   * @return result int[][]
   */
  public int[][] calculateFTAnd(final int[][] val1, final int[][] val2) {
    int lastId = -1;
    int[][] values1 = val1;
    int[][] values2 = val2;

    if(values1 == null || values1[0].length == 0 || values2 == null
        || values2[0].length == 0) {
      return new int[][]{};
    }

    // calculate minimum size
    int min = Math.min(values1[0].length, values2[0].length);
    // double space, because 2 values for each identical id
    int[][] maxResult = new int[2][values1[0].length + values2[0].length];

    //if (min == values2.length && min != values1.length) {
    if(min == values2[0].length && min != values1[0].length) {

      // change arrays
      int[][] tmp = values2;
      values2 = values1;
      values1 = tmp;
      //changedOrder = true;
    }

    // run variable for values1
    int i = 0;
    // run variable for values2
    int k = 0;
    // count added elements
    int counter = 0;
    int cmpResult;

    // each value from the smaller set are compared with the bigger set and
    // added to result
    while(i < values1[0].length && k < values2[0].length) {
      cmpResult = Array.compareIntArrayEntry(values1[0][i], values1[1][i],
          values2[0][k], values2[1][k]);
      if(cmpResult == -1) {
        // same Id, but pos1 < pos 2 values1[i] < values2[k]
        maxResult[0][counter] = values1[0][i];
        maxResult[1][counter] = values1[1][i];

        lastId = values1[0][i];
        counter++;
        i++;
      } else if(cmpResult == -2) {
        // id1 < i2
        if(lastId != values1[0][i]) {
          i++;
        } else {
          // same value and Id == lastId have to be copied
          while(i < values1[0].length && lastId == values1[0][i]) {

            maxResult[0][counter] = values1[0][i];
            maxResult[1][counter] = values1[1][i];

            counter++;
            i++;
          }
        }
      } else if(cmpResult == 2) {
        // id1 > i2
        if(lastId != values2[0][k]) {

          k++;
        } else {
          // all values with same Id == lastId have to be copied
          while(k < values2[0].length && lastId == values2[0][k]) {

            maxResult[0][counter] = values2[0][k];
            maxResult[1][counter] = values2[1][k];

            counter++;
            k++;
          }
        }
      } else if(cmpResult == 1) {
        // same ids, but pos1 > pos2 values1[i] > values2[k]
        maxResult[0][counter] = values2[0][k];
        maxResult[1][counter] = values2[1][k];

        lastId = values2[0][k];
        counter++;
        k++;
      } else {
        // entry identical
        maxResult[0][counter] = values2[0][k];
        maxResult[1][counter] = values2[1][k];
        counter++;
        i++;
        k++;
      }
    }

    // process left elements form values1, values2 done
    while(k > 0 && values1[0].length > i &&
        values1[0][i] == values2[0][k - 1]) {
      maxResult[0][counter] = values1[0][i];
      maxResult[1][counter] = values1[1][i];
      counter++;
      i++;
    }

    // process left elements form values2, values1 done
    while(i > 0 && values2[0].length > k &&
        values2[0][k] == values1[0][i - 1]) {
      //maxResult[counter] = values2[k];
      maxResult[0][counter] = values2[0][k];
      maxResult[1][counter] = values2[1][k];
      counter++;
      k++;
    }

    if(counter == 0) return new int[][]{};

    int[][] returnArray = new int[2][counter];
    System.arraycopy(maxResult[0], 0, returnArray[0], 0, counter);
    System.arraycopy(maxResult[1], 0, returnArray[1], 0, counter);

    return returnArray;
  }

  /**
   * Built join for value1 and value2. 
   * The resultset has the same
   * order, as the searchvalues are written in the query.
   * The variable pointer saves for each id the original position in the query;
   * it is updated each time
   *
   * @param val2 inputset int[][]
   * @param val1 inputset  int[][]
   * @param po pointer int[]
   * @return Object[] o[0]=int[][] results; o[1]=pointers
   */

  public Object[] calculateFTAndOrderPreserving(final int[][] val2, 
      final int[][]val1, final int[] po) {
    
    if (val1 == null || val1[0].length == 0 || val2 == null  
        || val2[0].length == 0) {
      return new Object[]{null, null};
    } 
    
    int[][] v1 = val1;
    int[][] v2 = val2;
    int[] p = po;
    
    int min = Math.min(v1[0].length, v2[0].length);
    // note changed order
    boolean changedOrder = false;

    if (min == v2[0].length && min != v1[0].length) {
      // change arrays
      int[][] tmp = v2;
      v2 = v1;
      v1 = tmp;
      changedOrder = true;
    }

    int[][] maxResult = new int[2][v1[0].length + v2[0].length];
    // space for new pointer
    int[] pointersnew = new int[maxResult[0].length + 1];

    // first call FTAND
    if (p == null) {
      p = new int[pointersnew.length];
    }

    // first element in pointers shows the maximum level
    pointersnew[0] = p[0] + 1;

    // run variable for values1
    int i = 0;
    // run variable for values2
    int k = 0;
    // number inserted elements
    int counter = 0;

    // each value from the smaller set are compared with the bigger set and
    // added to result
    while(v1[0].length > i) {
      if (k == v2[0].length) {
        break;
      }

      // same Ids
      if (v2[0][k] == v1[0][i]) {
        // changed order
        if (!changedOrder) {
          // copy values2
          while (k < v2[0].length 
              && v2[0][k] == v1[0][i]) {
            // same id
            //maxResult[counter] = values2[k];
            maxResult[0][counter] = v2[0][k];
            maxResult[1][counter] = v2[1][k];
            // copy old pointer
            pointersnew[counter + 1] = p[k + 1];
            counter++;
            k++;
          }
          // copy values1
          while (i < v1[0].length 
              && v2[0][k - 1] == v1[0][i]) {
            // same ids
            maxResult[0][counter] = v1[0][i];
            maxResult[1][counter] = v1[1][i];
            // add new element
            pointersnew[counter + 1] = pointersnew[0];
            counter++;
            i++;
          }
        } else {
          // copy values1
          while (i < v1[0].length 
              && v2[0][k] == v1[0][i]) {
            // same ids
            maxResult[0][counter] = v1[0][i];
            maxResult[1][counter] = v1[1][i];
            // add new element
            pointersnew[counter + 1] = p[i + 1]; //k
            counter++;
            i++;
          }
          // copy values2
          while (k < v2[0].length 
              && v2[0][k] == v1[0][i - 1]) {
            // same ids
            maxResult[0][counter] = v2[0][k];
            maxResult[1][counter] = v2[1][k];

            // copy old pointer
            pointersnew[counter + 1] = pointersnew[0];
            counter++;
            k++;
          }
        }
      } else if (v1[0][i] < v2[0][k]) {
        i++;
      } else {
        k++;
      }
    }

    // process left elements form values1, values2 done
    while(k > 0 && v1[0].length > i 
        && v1[0][i] == v2[0][k - 1]) {
      //maxResult[counter] = values1[i];
      maxResult[0][counter] = v1[0][i];
      maxResult[1][counter] = v1[1][i];
      // new element
      if (!changedOrder) {
        pointersnew[counter + 1] = pointersnew[0];
      } else {
        pointersnew[counter + 1] = p[k + 1];
      }

      counter++;
      i++;
    }


    // process left elements form values2, values1 done
    while(i > 0 && v2[0].length > k 
        && v2[0][k] == v1[0][i - 1]) {
      maxResult[0][counter] = v2[0][k];
      maxResult[1][counter] = v2[1][k];
      // copy old pointer
      if (!changedOrder) {
        pointersnew[counter + 1] = p[k + 1];
      } else {
        pointersnew[counter + 1] = pointersnew[0];
      }

      counter++;
      k++;
    }

    if (counter == 0) return new Object[]{null, null};

    int[][] rnArray = new int[2][counter];
    System.arraycopy(maxResult[0], 0, rnArray[0], 0, counter);
    System.arraycopy(maxResult[1], 0, rnArray[1], 0, counter);
    p = new int[counter + 1];
    System.arraycopy(pointersnew, 0, p, 0, counter + 1);

    Object[] o = new Object[2];
    o[0] = rnArray;
    o[1] = p;

    return o;
  }


  /**
   * Built join for value1 and value2; used key is id. 
   * Servers pointer on search strings for each id.
   * @param val1 input set int[][]
   * @param val2 input set  int[][]
   * @param p int[] pointer array, optional on val1
   * @return result int[][]
   */
  public Object[] calculateFTAnd(final int[][] val1, final int[][] val2, 
      final int[] p) {
    int lastId = -1;
    int[][] values1 = val1;
    int[][] values2 = val2;
    
    if(values1 == null || values1[0].length == 0 || values2 == null
        || values2[0].length == 0) {
      return new Object[]{null, null};
    }

    int[] pn = new int[val1[0].length + val2[0].length + 1];
    if(p != null) pn[0] = p[0] + 1;
    else pn[0] = 1;
    
    // calculate minimum size
    int min = Math.min(values1[0].length, values2[0].length);
    // double space, because 2 values for each identical id
    int[][] maxResult = new int[2][values1[0].length + values2[0].length];
    boolean co = false;
    
    if(min == values2[0].length && min != values1[0].length) {
      // change arrays
      int[][] tmp = values2;
      values2 = values1;
      values1 = tmp;
      co = true;
    }

    // run variable for values1
    int i = 0;
    // run variable for values2
    int k = 0;
    // count added elements
    int counter = 0;
    int cmpResult;

    // each value from the smaller set are compared with the bigger set and
    // added to result
    while(i < values1[0].length && k < values2[0].length) {
      cmpResult = Array.compareIntArrayEntry(values1[0][i], values1[1][i],
          values2[0][k], values2[1][k]);
      if(cmpResult == -1) {
        // same Id, but pos1 < pos 2 values1[i] < values2[k]
        maxResult[0][counter] = values1[0][i];
        maxResult[1][counter] = values1[1][i];

        // copy old pointer
        pn[counter + 1] = co ? pn[0] : p != null ? p[i + 1] : 0;

        lastId = values1[0][i];
        counter++;

        i++;
      } else if(cmpResult == -2) {
        // id1 < i2
        if(lastId != values1[0][i]) {
          i++;
        } else {
          // same value and Id == lastId have to be copied
          while(i < values1[0].length && lastId == values1[0][i]) {

            maxResult[0][counter] = values1[0][i];
            maxResult[1][counter] = values1[1][i];

            // copy old pointer
            pn[counter + 1] = co ? pn[0] : p != null ? p[i + 1] : 0;

            counter++;
            i++;
          }
        }
      } else if(cmpResult == 2) {
        // id1 > i2
        if(lastId != values2[0][k]) {

          k++;
        } else {
          // all values with same Id == lastId have to be copied
          while(k < values2[0].length && lastId == values2[0][k]) {

            maxResult[0][counter] = values2[0][k];
            maxResult[1][counter] = values2[1][k];

            // copy old pointer
            pn[counter + 1] = !co ? pn[0] : p != null ? p[k + 1] : 0;
            
            counter++;
            k++;
          }
        }
      } else if(cmpResult == 1) {
        // same ids, but pos1 > pos2 values1[i] > values2[k]
        maxResult[0][counter] = values2[0][k];
        maxResult[1][counter] = values2[1][k];

        // copy old pointer
        pn[counter + 1] = !co ? pn[0] : p != null ? p[k + 1] : 0;
        
        lastId = values2[0][k];
        counter++;
        k++;
      } else {
        // entry identical
        maxResult[0][counter] = values2[0][k];
        maxResult[1][counter] = values2[1][k];
        counter++;
        // copy old pointer
        pn[counter + 1] = !co ? pn[0] : p != null ? p[k + 1] : 0;

        i++;
        k++;
      }
    }

    // process left elements form values1, values2 done
    while(k > 0 && values1[0].length > i &&
        values1[0][i] == values2[0][k - 1]) {
      maxResult[0][counter] = values1[0][i];
      maxResult[1][counter] = values1[1][i];
      
      // copy old pointer
      pn[counter + 1] = co ? pn[0] : p != null ? p[i + 1] : 0;
      
      counter++;
      i++;
    }

    // process left elements form values2, values1 done
    while(i > 0 && values2[0].length > k &&
        values2[0][k] == values1[0][i - 1]) {
      //maxResult[counter] = values2[k];
      maxResult[0][counter] = values2[0][k];
      maxResult[1][counter] = values2[1][k];
      // copy old pointer
      pn[counter + 1] = !co ? pn[0] : p != null ? p[k + 1] : 0;

      counter++;
      k++;
    }

    if(counter == 0) return new int[][]{};

    int[][] returnArray = new int[2][counter];
    System.arraycopy(maxResult[0], 0, returnArray[0], 0, counter);
    System.arraycopy(maxResult[1], 0, returnArray[1], 0, counter);
    
    int[] poi = new int[counter + 1];
    System.arraycopy(pn, 0, poi, 0, counter + 1);
    
    return new Object[]{returnArray, poi};
  }

 
}
