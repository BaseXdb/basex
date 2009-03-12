package org.basex.gui.view.map;

import java.util.Arrays;

import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.util.IntList;
import org.basex.util.Token;
import org.basex.util.TokenList;

/**
 * stores an integer array of pre values and their corresponding weights, sizes 
 * and number of children.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-08, ISC License
 * @author Joerg Hauser
 */
public class MapList extends IntList {
  /** Weights array. */
  public double[] weights;

  /**
   * Constructor, specifying an initial list size.
   */
  public MapList() {
  }
  
  /**
   * Constructor, specifying an initial list size.
   * @param is initial size of the list
   */
  public MapList(final int is) {
    super(is);
  }

  /**
   * Constructor, specifying an initial array.
   * @param v initial list values
   */
  public MapList(final int[] v) {
    super(v);
  }
  
  @Override
  public void sort() {
    if(weights == null) return;
    
    final TokenList tl = new TokenList();
    for(int c = 0; c < size; c++) tl.add(Token.token(weights[c]));
    sort(tl.finish(), true, false);
    Arrays.sort(weights);
    for (int l = 0, r = weights.length - 1; l < r; l++, r--) {
      // exchange the first and last
      double temp = weights[l]; weights[l]  = weights[r]; weights[r] = temp;
    }
    
    // simple bubble sort, obsolete
//    boolean switched = true;
//    int n = size;
//    do {
//      switched = false;
//      for(int i = 0; i < size - 1; i++) {
//        if(weights[i] < weights[i + 1]) {
//          // switch entries
//          int tmpint = list[i];
//          list[i] = list[i + 1];
//          list[i + 1] = tmpint;
//          // switch weights
//          double wtmp = weights[i];
//          weights[i] = weights[i + 1];
//          weights[i + 1] = wtmp;
//          
//          switched = true;
//        }
//      }
//      n--;
//    } while (n >= 0 && switched);
  }
  
  /**
   * Initializes the weights of each list entry and stores it in an extra list.
   * @param parsize reference size
   * @param parchildren reference number of nodes
   * @param data reference
   * [JH] modify to take textnode sizes into account
   */
  public void initWeights(final long parsize, final int parchildren,
      final Data data) {
    weights = new double[list.length];
    int[] nrchildren = new int[list.length];
    long[] sizes = new long[list.length];
    int sizeP = GUIProp.sizep;
    
    // use #children and size for weight
    if (0 < GUIProp.sizep && GUIProp.sizep < 100 && data.fs != null) {
      for(int i = 0; i < size - 1; i++) {
        sizes[i] = data.fs != null ? 
            Token.toLong(data.attValue(data.sizeID, list[i])) : 0;
        nrchildren[i] = list[i + 1] - list[i];
        weights[i] = sizeP / 100d * sizes[i] / parsize + 
            (1 - sizeP / 100d) * nrchildren[i] / parchildren;
      }
    // only sizes
    } else if (GUIProp.sizep == 100 && data.fs != null) {
      for(int i = 0; i < size - 1; i++) {
        sizes[i] = data.fs != null ? 
            Token.toLong(data.attValue(data.sizeID, list[i])) : 0;
        weights[i] = sizes[i] * 1d / parsize;
      }    
    //only #children
    } else if (GUIProp.sizep == 0 || data.fs == null) {
      for(int i = 0; i < size - 1; i++) {
        nrchildren[i] = list[i + 1] - list[i];
        weights[i] = nrchildren[i] * 1d / parchildren;
      }
    }
    for (int i = 0; i < size; i++) if(Double.isNaN(weights[i])) weights[i] = 0;
  }
}