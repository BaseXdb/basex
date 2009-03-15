package org.basex.gui.view.map;

import org.basex.data.Data;
import org.basex.gui.GUIProp;
import org.basex.util.IntList;
import org.basex.util.Token;

/**
 * Stores an integer array of pre values and their corresponding weights.
 *
 * @author Workgroup DBIS, University of Konstanz 2005-09, ISC License
 * @author Joerg Hauser
 */
class MapList extends IntList {
  /** Weights array. */
  double[] weight;
  /** indicates if List has been sorted. */
  boolean sorted = false;
  
  /**
   * Constructor.
   */
  MapList() {
  }
  
  /**
   * Constructor, specifying an initial array.
   * @param v initial list values
   */
  MapList(final int[] v) {
    super(v);
  }
  
  @Override
  public void sort() {
    sort(weight, false);
    sorted = true;
  }
  
  /**
   * Initializes the weights of each list entry and stores it in an extra list.
   * @param parsize reference size
   * @param parchildren reference number of nodes
   * @param data reference
   * [JH] modify to take textnode sizes into account
   * [JH] some weight problems occur displaying folders without any files and 
   * children
   */
  void initWeights(final long parsize, final int parchildren, final Data data) {
    weight = new double[list.length];
    int[] nrchildren = new int[list.length];
    long[] sizes = new long[list.length];
    int sizeP = GUIProp.sizep;
    
    // use #children and size for weight
    if (0 < GUIProp.sizep && GUIProp.sizep < 100 && data.fs != null) {
      for(int i = 0; i < size - 1; i++) {
        sizes[i] = data.fs != null ? 
            Token.toLong(data.attValue(data.sizeID, list[i])) : 0;
        nrchildren[i] = list[i + 1] - list[i];
        weight[i] = sizeP / 100d * sizes[i] / parsize + 
            (1 - sizeP / 100d) * nrchildren[i] / parchildren;
      }
    // only sizes
    } else if (GUIProp.sizep == 100 && data.fs != null) {
      for(int i = 0; i < size - 1; i++) {
        sizes[i] = data.fs != null ? 
            Token.toLong(data.attValue(data.sizeID, list[i])) : 0;
        weight[i] = sizes[i] * 1d / parsize;
      }    
    //only #children
    } else if (GUIProp.sizep == 0 || data.fs == null) {
//      long len = 0;
//      for(int i = 0; i < size; i++) {
//        len += data.textLen(list[i]);
//      }
      for(int i = 0; i < size - 1; i++) {
        nrchildren[i] = list[i + 1] - list[i];
//        weight[i] = 1d * data.textLen(list[i]) / len + 
//            0d * nrchildren[i] / parchildren;
        // old
        weight[i] = nrchildren[i] * 1d / parchildren;
      }
    }
  }
  
  @Override
  public String toString() {
    if(weight == null) {
      StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "[");
      for(int i = 0; i < size; i++) {
        sb.append((i == 0 ? "" : ", ") + list[i]);
      }
      return sb.append("]").toString();
    } else {
      StringBuilder sb = new StringBuilder(getClass().getSimpleName() + "[");
      for(int i = 0; i < size; i++) {
        sb.append((i == 0 ? "" : ", ") + list[i] + "/" + weight[i]);
      }
      return sb.append("]").toString();
    }
  }
}