package org.basex.query.xpath.expr;

import org.basex.data.Serializer;
import org.basex.query.QueryException;
import org.basex.query.xpath.XPContext;
import org.basex.query.xpath.values.Bool;
import org.basex.query.xpath.values.FTNode;
import org.basex.util.Array;
import org.basex.util.IntList;

/**
 * FTUnion Expression. This expresses the union of two FTContains results.
 * 
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Sebastian Gath
 */
public final class FTUnion extends FTArrayExpr {
  /** Saving index of positive expressions. */
  private int[] pex;

  /**
   * Constructor.
   * @param e operands joined with the union operator
   */
  public FTUnion(final FTArrayExpr[] e) {
    exprs = e;
  }

  /**
   * Constructor.
   * @param e operands joined with the union operator
   * @param posex pointer on exprs with positiv values
   */
  public FTUnion(final FTArrayExpr[] e, final int[] posex) {
    exprs = e;
    pex = posex;
    mp = new boolean[pex.length];
    cp = new IntList(pex);
  }

  @Override
  public boolean more() {
    boolean b = false;
/*    if (cn.size > 0) {
      for (int i = 0; i < cn.size; i++) {
        mn[i] = exprs[nex[i]].more();
        if (!b) b = mn[i];
      }
      cn.reset(nex.length);
    }
  */  
    if (cp.size > 0) {
      for (int i = 0; i < cp.size; i++) {
        //mp[i] = exprs[pex[cp.get(i)]].more();
        mp[pex[cp.get(i)]] = exprs[pex[cp.get(i)]].more();
        if (!b) b = mp[i];
      }
      cp.reset(pex.length);
    }
    if (!b) {
      for (boolean c : mp) if(c) return true;
    }
    return b;
  }
  
  /** Cache for one of the nodes. */
  private IntList cp;
  /** Flag is set, if ith expression has any result. */
  private boolean[] mp;
  /** Pointer on the positive expression with the lowest pre-values.*/
  private int minp = -1;
  
  @Override
  public FTNode next(final XPContext ctx) {
    if (minp == -1) {
      minp = 0;
      while(!mp[minp]) minp++;
      cp.set(minp, 0);
      for (int ip = minp + 1; ip < pex.length; ip++) {       
        if (mp[ip]) { 
          if (exprs[pex[ip]].next(ctx).getPre() < 
              exprs[pex[minp]].next(ctx).getPre()) {
            minp = ip;
            cp.set(ip, 0);
          } else if (exprs[pex[ip]].next(ctx).getPre() == 
              exprs[pex[minp]].next(ctx).getPre()) {
              cp.add(ip);
          }
        } 
      }
    }
    
   // if (nex.length == 0) {
      minp = -1;
      FTNode m = exprs[pex[cp.get(0)]].next(ctx);
      for (int i = 1; i < cp.size; i++) {
        m.merge(exprs[pex[cp.get(i)]].next(ctx), 0);
      }
      return m;
 /*   } else {
      if (minn == -1) {
        minn = 0;
        for (int in = 1; in < nex.length; in++) {
          if (mn[minn] && mn[in] && exprs[nex[in]].next(ctx).getPre() < 
              exprs[nex[minn]].next(ctx).getPre()) {
            minn = in;
          }
        }
      }
      if (minp > -1 && minn > -1) {
        if (minp < minn) {
          FTNode n = exprs[pex[minp]].next(ctx);
          minp = -1;
          return n;
        } else if (minp > minn) {
          minn = -1;
          if (more())
          return next(ctx);
          else return new FTNode();
        } else {
          minn = -1;
          minp = -1;
          if (more()) return next(ctx);
          else return new FTNode();
        }
      } else if (minp > -1) {
        FTNode n = exprs[pex[minp]].next(ctx);
        minp = -1;
        return n;
      } else {
        FTNode n = exprs[nex[minn]].next(ctx);
        minn = -1;
        return n;
      }
      
    }*/
    
    /*
    if (c > -1) {
      FTNode cn = exprs[c].next(ctx);
      if ((c == 0) ? m1 : m0) {
        FTNode nn = exprs[(c == 0) ? 1 : 0].next(ctx);
        if (nn.getPre() < cn.getPre()) {
          return nn;
        } else if (nn.getPre() == cn.getPre()) {
          cn.merge(nn, 0);
          c = -1;
          return cn;
        } else {
          c = (c == 0) ? 1 : 0;
          return cn;
        }
      } else {
        c = -1;
        return cn;
      }
    } else {
      if (m0 && m1) {
        final FTNode c0 = exprs[0].next(ctx);
        final FTNode c1 = exprs[1].next(ctx);
        if (c0.getPre() == c1.getPre()) {
          c0.merge(c1, 0);
          return c0;
        } else if (c0.getPre() < c1.getPre()) {
          c = 1;
          return c0;
        } else {
          c = 0;
          return c1;
        }
      } else if(m0) {
        return exprs[0].next(ctx);
      } else {
        return exprs[1].next(ctx);
      }
    }*/
  }

  
  @Override
  public Bool eval(final XPContext ctx) throws QueryException {
    boolean b = false;
    // check each positive expression
    for (int i : pex) {
      final Bool it = (Bool) exprs[i].eval(ctx);
      if (!b) b = it.bool();
    }

/*    for (int i : nex) {
      exprs[i].eval(ctx);
    }
*/
    return Bool.get(b);

    /*
    Item it = exprs[0].eval(ctx);
    if (it instanceof NodeSet) {
      int[][] res = ((NodeSet) it).ftidpos;
      int[] pntr = ((NodeSet) it).ftpointer;

      for (int i = 1; i < exprs.length; i++) {
        it = exprs[i].eval(ctx);
        if (it instanceof NodeSet) {
          int[][] tmp = ((NodeSet) it).ftidpos;

          if (po) {
            Object[] o = calculateFTOr(res, tmp, pntr);
            if(o.length == 0) return null;

            res = (int[][]) o[0];
            pntr = (int[]) o[1];
          } else {
            res = calculateFTOr(res, tmp);          
          }
        }
      }
      return new NodeSet(Array.extractIDsFromData(res), ctx, res,
          po ? pntr : null);
    }
    return null;*/
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
    
    if(val1 == null || val1[0].length == 0) return val2;
    if(val2 == null || val2[0].length == 0) return val1;

    int[][] maxResult = new int[2][val1[0].length + val2[0].length];

    // calculate maximum
    int max = Math.max(val1[0].length, val2[0].length);
    if(max == val1.length) {
      int[][] tmp = val1;
      val1 = val2;
      val2 = tmp;
    }

    // process smaller set
    int i = 0;
    int k = 0;
    int c = 0;
    while(val1[0].length > i) {
      if(k >= val2[0].length) break;

      final int cmp = Array.compareIntArrayEntry(val1[0][i], val1[1][i],
          val2[0][k], val2[1][k]);
      final boolean l = cmp > 0;
      maxResult[0][c] = l ? val2[0][k] : val1[0][i];
      maxResult[1][c] = l ? val2[1][k] : val1[1][i];

      if(cmp >= 0) k++;
      if(cmp <= 0) i++;
      c++;
    }
    if(c == 0) return null;

    final boolean l = k == val2[0].length && i < val1[0].length;
    final int[] left = l ? val1[0] : val2[0];
    final int v = left.length - (l ? i : k);

    final int[][] result = new int[2][c + v];
    // copy first values
    System.arraycopy(maxResult[0], 0, result[0], 0, c);
    System.arraycopy(maxResult[1], 0, result[1], 0, c);

    // copy left values
    System.arraycopy(left, l ? i : k, result[0], c, v);
    System.arraycopy(left, l ? i : k, result[1], c, v);

    return result;
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

    int i = 0;
    int k = 0;
    int c = 0;

    // space for new pointer
    int[] pn = new int[mr[0].length + 1];

    // first call FTUnion
    if (p == null) p = new int[pn.length];

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
    ser.openElement(this);
    for (Expr e : exprs) e.plan(ser);
    ser.closeElement(this);
  }

}
 